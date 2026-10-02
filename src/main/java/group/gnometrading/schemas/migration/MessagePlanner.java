package group.gnometrading.schemas.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongUnaryOperator;
import org.agrona.collections.Long2LongHashMap;
import org.agrona.concurrent.UnsafeBuffer;
import uk.co.real_logic.sbe.PrimitiveType;

/**
 * Plans one message's conversion between two schema versions.
 *
 * <p>Defaults, all read from the schema: identical fields copy; integers widen when the target type
 * holds every source value (null maps to null); float widens to double; enum values and set choices
 * match by name; arrays may grow (zero padded); a new optional field is null; a new set is empty. Anything else (a new
 * required field, a removed field, narrowing, a sign change, a shrunk array, a changed composite, an
 * enum value with no counterpart) fails here, at class load, unless {@link MessageRules} says what to
 * do, so no conversion is ever guessed.
 */
final class MessagePlanner {

    private final MessageLayout source;
    private final MessageLayout target;
    private final MessageRules rules;
    private final String context;
    private final Set<String> consumed = new HashSet<>();

    private MessagePlanner(
            final MessageLayout source, final MessageLayout target, final MessageRules rules, final String context) {
        this.source = source;
        this.target = target;
        this.rules = rules;
        this.context = context;
    }

    static CompiledConverter plan(
            final MessageLayout source,
            final MessageLayout target,
            final MessageRules rules,
            final int schemaId,
            final int targetVersion) {
        final MessagePlanner planner =
                new MessagePlanner(source, target, rules, target.name() + " (to v" + targetVersion + ")");
        return new CompiledConverter(
                target.templateId(),
                schemaId,
                targetVersion,
                source.blockLength(),
                target.blockLength(),
                planner.ops());
    }

    private FieldOps.FieldOp[] ops() {
        checkRulesNameRealFields();
        final List<FieldOps.FieldOp> ops = new ArrayList<>();
        for (final FieldLayout field : this.target.fields().values()) {
            ops.add(planField(field));
        }
        for (final String name : this.source.fields().keySet()) {
            if (!this.consumed.contains(name) && !this.rules.drops.contains(name)) {
                throw fail(name + " is not in the target version; drop it explicitly if its data can go");
            }
        }
        ops.addAll(zeroUncovered(ops, this.target.blockLength()));
        return mergeCopies(ops);
    }

    /**
     * Zero-fills the target bytes no op writes (padding, the tail of a grown array), so a converter
     * never has to clear the whole block per message.
     */
    private static List<FieldOps.FieldOp> zeroUncovered(final List<FieldOps.FieldOp> ops, final int blockLength) {
        final boolean[] covered = new boolean[blockLength];
        for (final FieldOps.FieldOp op : ops) {
            Arrays.fill(covered, op.dstStart(), op.dstStart() + op.dstLength(), true);
        }
        final List<FieldOps.FieldOp> zeros = new ArrayList<>();
        int start = -1;
        for (int offset = 0; offset <= blockLength; offset++) {
            final boolean gap = offset < blockLength && !covered[offset];
            if (gap && start < 0) {
                start = offset;
            } else if (!gap && start >= 0) {
                zeros.add(new FieldOps.Constant(start, new byte[offset - start]));
                start = -1;
            }
        }
        return zeros;
    }

    private FieldOps.FieldOp planField(final FieldLayout to) {
        final String name = to.name();
        if (this.rules.computes.containsKey(name)) {
            requireInteger(to);
            return new FieldOps.Compute(to.offset(), to.primitive(), this.rules.computes.get(name), this.source);
        }
        final FieldLayout from = this.source.fields().get(this.rules.renames.getOrDefault(name, name));
        if (from == null) {
            return planNewField(to);
        }
        this.consumed.add(from.name());
        if (this.rules.transforms.containsKey(name)) {
            requireInteger(from);
            requireInteger(to);
            return integerConvert(from, to, this.rules.transforms.get(name));
        }
        if (from.kind() != to.kind()) {
            throw fail(name + " changed from " + from.kind() + " to " + to.kind() + "; transform or compute it");
        }
        return switch (to.kind()) {
            case SCALAR -> planScalar(from, to);
            case ARRAY -> planArray(from, to);
            case ENUM -> planEnum(from, to);
            case SET -> planSet(from, to);
            case COMPOSITE -> planComposite(from, to);
        };
    }

    private FieldOps.FieldOp planNewField(final FieldLayout to) {
        final String name = to.name();
        if (this.rules.fills.containsKey(name)) {
            return constant(to, this.rules.fills.get(name));
        }
        if (this.rules.enumFills.containsKey(name)) {
            final Long value = to.namedValues().get(this.rules.enumFills.get(name));
            if (to.kind() != FieldKind.ENUM || value == null) {
                throw fail(name + " has no enum value " + this.rules.enumFills.get(name));
            }
            return constant(to, value);
        }
        // A new set means none of its flags applied to older data, so it starts empty.
        if (to.kind() == FieldKind.SET) {
            return new FieldOps.Constant(to.offset(), new byte[to.length()]);
        }
        if (!to.optional()) {
            throw fail(name + " is new and required; fill or compute it");
        }
        return nullConstant(to);
    }

    private FieldOps.FieldOp planScalar(final FieldLayout from, final FieldLayout to) {
        if (from.primitive() == to.primitive()) {
            return copy(from, to);
        }
        if (Scalars.isInteger(from.primitive())
                && Scalars.isInteger(to.primitive())
                && Scalars.canHold(to.primitive(), from.primitive())) {
            return integerConvert(from, to, null);
        }
        if (from.primitive() == PrimitiveType.FLOAT && to.primitive() == PrimitiveType.DOUBLE) {
            return new FieldOps.FloatToDouble(from.offset(), to.offset());
        }
        throw fail(to.name() + " changed from " + from.primitive() + " to " + to.primitive()
                + ", which loses values or changes sign; transform it");
    }

    private FieldOps.FieldOp planArray(final FieldLayout from, final FieldLayout to) {
        if (from.primitive() != to.primitive() || to.arrayLength() < from.arrayLength()) {
            throw fail(to.name() + " array changed from " + from.arrayLength() + "x" + from.primitive() + " to "
                    + to.arrayLength() + "x" + to.primitive() + "; only growing is automatic");
        }
        // The target block is zeroed first, so a grown array is zero padded.
        return new FieldOps.CopyRun(from.offset(), to.offset(), from.length());
    }

    private FieldOps.FieldOp planEnum(final FieldLayout from, final FieldLayout to) {
        final Map<String, String> renames = this.rules.enumRenames.getOrDefault(to.name(), Map.of());
        final Long2LongHashMap table = new Long2LongHashMap(Long.MIN_VALUE);
        boolean identity = from.primitive() == to.primitive();
        for (final Map.Entry<String, Long> value : from.namedValues().entrySet()) {
            final String targetName = renames.getOrDefault(value.getKey(), value.getKey());
            final Long targetValue = to.namedValues().get(targetName);
            if (targetValue == null) {
                throw fail(to.name() + " value " + value.getKey() + " has no counterpart; map it with mapEnum");
            }
            table.put(value.getValue(), targetValue);
            identity &= value.getValue().equals(targetValue);
        }
        // Every SBE enum has a null value, which data may hold even when the field is required.
        requireNullable(from, to);
        table.put(from.nullValue(), to.nullValue());
        identity &= from.nullValue() == to.nullValue();
        if (identity) {
            return copy(from, to);
        }
        return new FieldOps.EnumTranslate(
                to.name(), from.offset(), from.primitive(), to.offset(), to.primitive(), table);
    }

    private FieldOps.FieldOp planSet(final FieldLayout from, final FieldLayout to) {
        int width = 0;
        for (final long bit : from.namedValues().values()) {
            width = Math.max(width, (int) bit + 1);
        }
        final int[] bitMap = new int[width];
        boolean identity = from.primitive() == to.primitive();
        for (final Map.Entry<String, Long> choice : from.namedValues().entrySet()) {
            final Long targetBit = to.namedValues().get(choice.getKey());
            if (targetBit == null) {
                throw fail(to.name() + " choice " + choice.getKey() + " no longer exists; transform the set");
            }
            bitMap[choice.getValue().intValue()] = targetBit.intValue();
            identity &= choice.getValue().equals(targetBit);
        }
        if (identity) {
            return copy(from, to);
        }
        return new FieldOps.SetTranslate(from.offset(), from.primitive(), to.offset(), to.primitive(), bitMap);
    }

    private FieldOps.FieldOp planComposite(final FieldLayout from, final FieldLayout to) {
        if (!from.compositeSignature().equals(to.compositeSignature())) {
            throw fail(to.name() + " composite layout changed; compute its fields");
        }
        return copy(from, to);
    }

    private FieldOps.FieldOp integerConvert(final FieldLayout from, final FieldLayout to, final LongUnaryOperator fn) {
        if (from.optional()) {
            requireNullable(from, to);
        }
        return new FieldOps.IntegerConvert(
                from.offset(),
                from.primitive(),
                from.optional(),
                from.nullValue(),
                to.offset(),
                to.primitive(),
                to.nullValue(),
                fn);
    }

    private void requireNullable(final FieldLayout from, final FieldLayout to) {
        if (from.optional() && !to.optional()) {
            throw fail(to.name() + " became required but old data may be null; transform or fill it");
        }
    }

    private void requireInteger(final FieldLayout field) {
        if (!Scalars.isInteger(field.primitive())
                || field.kind() == FieldKind.ARRAY
                || field.kind() == FieldKind.COMPOSITE) {
            throw fail(field.name() + " is not a single integer, so it cannot be transformed or computed");
        }
    }

    private void checkRulesNameRealFields() {
        for (final String to : union(
                this.rules.transforms.keySet(),
                this.rules.fills.keySet(),
                this.rules.enumFills.keySet(),
                this.rules.computes.keySet(),
                this.rules.renames.keySet(),
                this.rules.enumRenames.keySet())) {
            if (!this.target.fields().containsKey(to)) {
                throw fail("rule names " + to + ", which is not a field of the target version");
            }
        }
        for (final String from : union(this.rules.drops, new HashSet<>(this.rules.renames.values()))) {
            if (!this.source.fields().containsKey(from)) {
                throw fail("rule names " + from + ", which is not a field of the source version");
            }
        }
    }

    @SafeVarargs
    private static Set<String> union(final Set<String>... sets) {
        final Set<String> all = new HashSet<>();
        for (final Set<String> set : sets) {
            all.addAll(set);
        }
        return all;
    }

    private static FieldOps.FieldOp copy(final FieldLayout from, final FieldLayout to) {
        return new FieldOps.CopyRun(from.offset(), to.offset(), to.length());
    }

    private FieldOps.FieldOp constant(final FieldLayout to, final long value) {
        requireInteger(to);
        final byte[] bytes = new byte[to.length()];
        Scalars.write(new UnsafeBuffer(bytes), 0, to.primitive(), value);
        return new FieldOps.Constant(to.offset(), bytes);
    }

    private FieldOps.FieldOp nullConstant(final FieldLayout to) {
        if (to.kind() == FieldKind.COMPOSITE) {
            throw fail(to.name() + " is a new composite; fill its fields by computing them");
        }
        final byte[] bytes = new byte[to.length()];
        final UnsafeBuffer scratch = new UnsafeBuffer(bytes);
        final int elementSize = to.primitive().size();
        for (int offset = 0; offset < to.length(); offset += elementSize) {
            if (to.primitive() == PrimitiveType.FLOAT) {
                scratch.putFloat(offset, Float.NaN, Scalars.ORDER);
            } else if (to.primitive() == PrimitiveType.DOUBLE) {
                scratch.putDouble(offset, Double.NaN, Scalars.ORDER);
            } else {
                Scalars.write(scratch, offset, to.primitive(), to.nullValue());
            }
        }
        return new FieldOps.Constant(to.offset(), bytes);
    }

    private static FieldOps.FieldOp[] mergeCopies(final List<FieldOps.FieldOp> ops) {
        final List<FieldOps.FieldOp> merged = new ArrayList<>();
        for (final FieldOps.FieldOp op : ops) {
            final int last = merged.size() - 1;
            if (op instanceof FieldOps.CopyRun next
                    && last >= 0
                    && merged.get(last) instanceof FieldOps.CopyRun previous
                    && previous.continuesWith(next)) {
                merged.set(last, previous.merge(next));
            } else {
                merged.add(op);
            }
        }
        return merged.toArray(new FieldOps.FieldOp[0]);
    }

    private IllegalStateException fail(final String reason) {
        return new IllegalStateException("Cannot migrate " + this.context + ": " + reason);
    }
}
