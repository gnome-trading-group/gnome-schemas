package group.gnometrading.schemas.migration;

import java.nio.ByteOrder;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import uk.co.real_logic.sbe.PrimitiveType;

/** Reads and writes SBE integer primitives as longs, honouring signedness. */
final class Scalars {

    static final ByteOrder ORDER = ByteOrder.LITTLE_ENDIAN;

    private static final long UINT8_MASK = 0xFFL;
    private static final long UINT16_MASK = 0xFFFFL;
    private static final long UINT32_MASK = 0xFFFF_FFFFL;

    private Scalars() {}

    static boolean isInteger(final PrimitiveType type) {
        return type != null && type != PrimitiveType.FLOAT && type != PrimitiveType.DOUBLE;
    }

    static boolean isUnsigned(final PrimitiveType type) {
        return switch (type) {
            case CHAR, UINT8, UINT16, UINT32, UINT64 -> true;
            default -> false;
        };
    }

    /** Whether every value of {@code from} can be held by {@code to}. */
    static boolean canHold(final PrimitiveType to, final PrimitiveType from) {
        if (isUnsigned(from)) {
            return isUnsigned(to) ? to.size() >= from.size() : to.size() > from.size();
        }
        return !isUnsigned(to) && to.size() >= from.size();
    }

    static long read(final DirectBuffer buffer, final int offset, final PrimitiveType type) {
        return switch (type) {
            case CHAR, UINT8 -> buffer.getByte(offset) & UINT8_MASK;
            case INT8 -> buffer.getByte(offset);
            case UINT16 -> buffer.getShort(offset, ORDER) & UINT16_MASK;
            case INT16 -> buffer.getShort(offset, ORDER);
            case UINT32 -> buffer.getInt(offset, ORDER) & UINT32_MASK;
            case INT32 -> buffer.getInt(offset, ORDER);
            case INT64, UINT64 -> buffer.getLong(offset, ORDER);
            default -> throw new IllegalArgumentException("Not an integer type: " + type);
        };
    }

    /**
     * Writes {@code value} as {@code type}.
     *
     * @throws IllegalStateException if the value does not fit, so a transform can never wrap silently
     */
    static void write(final MutableDirectBuffer buffer, final int offset, final PrimitiveType type, final long value) {
        if (!fits(type, value)) {
            throw new IllegalStateException(value + " does not fit in " + type);
        }
        writeUnchecked(buffer, offset, type, value);
    }

    /** Writes {@code value} as {@code type}, for values already known to fit. */
    static void writeUnchecked(
            final MutableDirectBuffer buffer, final int offset, final PrimitiveType type, final long value) {
        switch (type) {
            case CHAR, UINT8, INT8 -> buffer.putByte(offset, (byte) value);
            case UINT16, INT16 -> buffer.putShort(offset, (short) value, ORDER);
            case UINT32, INT32 -> buffer.putInt(offset, (int) value, ORDER);
            case INT64, UINT64 -> buffer.putLong(offset, value, ORDER);
            default -> throw new IllegalArgumentException("Not an integer type: " + type);
        }
    }

    private static boolean fits(final PrimitiveType type, final long value) {
        return switch (type) {
            case INT64, UINT64 -> true;
            case CHAR, UINT8 -> value >= 0 && value <= UINT8_MASK;
            case UINT16 -> value >= 0 && value <= UINT16_MASK;
            case UINT32 -> value >= 0 && value <= UINT32_MASK;
            case INT8 -> value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE;
            case INT16 -> value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
            case INT32 -> value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE;
            default -> false;
        };
    }
}
