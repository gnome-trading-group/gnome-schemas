package group.gnometrading.schemas.migration;

import java.util.function.LongUnaryOperator;
import java.util.function.ToLongFunction;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.Long2LongHashMap;
import uk.co.real_logic.sbe.PrimitiveType;

/** The precompiled steps a converter runs per message. Offsets are relative to the message body. */
final class FieldOps {

    private FieldOps() {}

    interface FieldOp {
        void apply(DirectBuffer src, int srcBody, MutableDirectBuffer dst, int dstBody);

        /** First target byte this op writes, relative to the body. */
        int dstStart();

        /** Number of target bytes this op writes. */
        int dstLength();
    }

    /** Copies bytes unchanged. Adjacent copies are merged into one, so most messages are a few of these. */
    record CopyRun(int srcOffset, int dstOffset, int length) implements FieldOp {
        @Override
        public void apply(final DirectBuffer src, final int srcBody, final MutableDirectBuffer dst, final int dstBody) {
            dst.putBytes(dstBody + this.dstOffset, src, srcBody + this.srcOffset, this.length);
        }

        @Override
        public int dstStart() {
            return this.dstOffset;
        }

        @Override
        public int dstLength() {
            return this.length;
        }

        boolean continuesWith(final CopyRun next) {
            return this.srcOffset + this.length == next.srcOffset && this.dstOffset + this.length == next.dstOffset;
        }

        CopyRun merge(final CopyRun next) {
            return new CopyRun(this.srcOffset, this.dstOffset, this.length + next.length);
        }
    }

    /** Writes the same bytes into every message: a fill or a new field's null value. */
    record Constant(int dstOffset, byte[] bytes) implements FieldOp {
        @Override
        public void apply(final DirectBuffer src, final int srcBody, final MutableDirectBuffer dst, final int dstBody) {
            dst.putBytes(dstBody + this.dstOffset, this.bytes);
        }

        @Override
        public int dstStart() {
            return this.dstOffset;
        }

        @Override
        public int dstLength() {
            return this.bytes.length;
        }
    }

    /** Integer to integer, widening or (through {@code fn}) anything range-checked; null maps to null. */
    record IntegerConvert(
            int srcOffset,
            PrimitiveType srcType,
            boolean srcOptional,
            long srcNull,
            int dstOffset,
            PrimitiveType dstType,
            long dstNull,
            LongUnaryOperator fn)
            implements FieldOp {
        @Override
        public void apply(final DirectBuffer src, final int srcBody, final MutableDirectBuffer dst, final int dstBody) {
            final long value = Scalars.read(src, srcBody + this.srcOffset, this.srcType);
            if (this.srcOptional && value == this.srcNull) {
                Scalars.writeUnchecked(dst, dstBody + this.dstOffset, this.dstType, this.dstNull);
            } else if (this.fn == null) {
                // Only planned when the target type holds every source value, so it cannot overflow.
                Scalars.writeUnchecked(dst, dstBody + this.dstOffset, this.dstType, value);
            } else {
                Scalars.write(dst, dstBody + this.dstOffset, this.dstType, this.fn.applyAsLong(value));
            }
        }

        @Override
        public int dstStart() {
            return this.dstOffset;
        }

        @Override
        public int dstLength() {
            return this.dstType.size();
        }
    }

    record FloatToDouble(int srcOffset, int dstOffset) implements FieldOp {
        @Override
        public void apply(final DirectBuffer src, final int srcBody, final MutableDirectBuffer dst, final int dstBody) {
            dst.putDouble(
                    dstBody + this.dstOffset, src.getFloat(srcBody + this.srcOffset, Scalars.ORDER), Scalars.ORDER);
        }

        @Override
        public int dstStart() {
            return this.dstOffset;
        }

        @Override
        public int dstLength() {
            return Double.BYTES;
        }
    }

    /** Enum values matched by name across versions, so a renumbered enum still means the same thing. */
    record EnumTranslate(
            String field,
            int srcOffset,
            PrimitiveType srcType,
            int dstOffset,
            PrimitiveType dstType,
            Long2LongHashMap table)
            implements FieldOp {
        @Override
        public void apply(final DirectBuffer src, final int srcBody, final MutableDirectBuffer dst, final int dstBody) {
            final long value = Scalars.read(src, srcBody + this.srcOffset, this.srcType);
            final long translated = this.table.get(value);
            if (translated == this.table.missingValue()) {
                throw new IllegalStateException(this.field + " holds " + value + ", which is not a value of its enum");
            }
            Scalars.write(dst, dstBody + this.dstOffset, this.dstType, translated);
        }

        @Override
        public int dstStart() {
            return this.dstOffset;
        }

        @Override
        public int dstLength() {
            return this.dstType.size();
        }
    }

    /** Set choices matched by name; {@code bitMap[sourceBit]} is the target bit. */
    record SetTranslate(int srcOffset, PrimitiveType srcType, int dstOffset, PrimitiveType dstType, int[] bitMap)
            implements FieldOp {
        @Override
        public void apply(final DirectBuffer src, final int srcBody, final MutableDirectBuffer dst, final int dstBody) {
            final long bits = Scalars.read(src, srcBody + this.srcOffset, this.srcType);
            long mapped = 0;
            for (int bit = 0; bit < this.bitMap.length; bit++) {
                if ((bits & (1L << bit)) != 0) {
                    mapped |= 1L << this.bitMap[bit];
                }
            }
            Scalars.write(dst, dstBody + this.dstOffset, this.dstType, mapped);
        }

        @Override
        public int dstStart() {
            return this.dstOffset;
        }

        @Override
        public int dstLength() {
            return this.dstType.size();
        }
    }

    /** A target field derived from source fields. Rare, so the source view is made per message. */
    record Compute(int dstOffset, PrimitiveType dstType, ToLongFunction<SourceFields> fn, MessageLayout source)
            implements FieldOp {
        @Override
        public void apply(final DirectBuffer src, final int srcBody, final MutableDirectBuffer dst, final int dstBody) {
            final long value = this.fn.applyAsLong(new SourceFields(this.source.fields()).wrap(src, srcBody));
            Scalars.write(dst, dstBody + this.dstOffset, this.dstType, value);
        }

        @Override
        public int dstStart() {
            return this.dstOffset;
        }

        @Override
        public int dstLength() {
            return this.dstType.size();
        }
    }
}
