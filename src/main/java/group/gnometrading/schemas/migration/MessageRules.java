package group.gnometrading.schemas.migration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.LongUnaryOperator;
import java.util.function.ToLongFunction;

/**
 * Explicit instructions for one message in one {@link MigrationStep}, for anything the default rules
 * refuse to guess. Field names are target-version names unless stated otherwise.
 */
public final class MessageRules {

    final Map<String, String> renames = new HashMap<>();
    final Map<String, LongUnaryOperator> transforms = new HashMap<>();
    final Map<String, Long> fills = new HashMap<>();
    final Map<String, String> enumFills = new HashMap<>();
    final Map<String, ToLongFunction<SourceFields>> computes = new HashMap<>();
    final Map<String, Map<String, String>> enumRenames = new HashMap<>();
    final Set<String> drops = new HashSet<>();

    /** The target field {@code to} takes its value from the source field {@code from}. */
    public MessageRules rename(final String from, final String to) {
        this.renames.put(to, from);
        return this;
    }

    /**
     * Applies {@code fn} to the source value. The result may change type, sign or range: it is checked
     * against the target type, so it fails rather than wraps. A null source value stays null.
     */
    public MessageRules transform(final String field, final LongUnaryOperator fn) {
        this.transforms.put(field, fn);
        return this;
    }

    /** Scales by {@code factor}, failing on overflow; e.g. a price moving to a finer scaling factor. */
    public MessageRules multiply(final String field, final long factor) {
        return transform(field, value -> Math.multiplyExact(value, factor));
    }

    /** Scales by {@code 1 / divisor}, failing if that would lose precision. */
    public MessageRules divide(final String field, final long divisor) {
        return transform(field, value -> {
            if (value % divisor != 0) {
                throw new IllegalStateException(value + " is not a multiple of " + divisor);
            }
            return value / divisor;
        });
    }

    /** A target field with no source gets this value in every converted message. */
    public MessageRules fill(final String field, final long value) {
        this.fills.put(field, value);
        return this;
    }

    /** A target enum field with no source gets this value, by name. */
    public MessageRules fillEnum(final String field, final String valueName) {
        this.enumFills.put(field, valueName);
        return this;
    }

    /** A target field derived from one or more source fields. */
    public MessageRules compute(final String field, final ToLongFunction<SourceFields> fn) {
        this.computes.put(field, fn);
        return this;
    }

    /** Enum values renamed between versions: source value name → target value name. */
    public MessageRules mapEnum(final String field, final Map<String, String> sourceToTarget) {
        this.enumRenames.put(field, Map.copyOf(sourceToTarget));
        return this;
    }

    /** Acknowledges that a source field no longer exists; its data is discarded. */
    public MessageRules drop(final String sourceField) {
        this.drops.add(sourceField);
        return this;
    }
}
