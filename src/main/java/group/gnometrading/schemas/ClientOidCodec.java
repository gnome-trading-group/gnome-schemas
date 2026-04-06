package group.gnometrading.schemas;

import org.agrona.DirectBuffer;

public final class ClientOidCodec {

    public static final int CLIENT_OID_LENGTH = 16;

    private static final int BYTE_MASK = 0xFF;
    private static final int SHIFT_8 = 8;
    private static final int SHIFT_16 = 16;
    private static final int SHIFT_24 = 24;
    private static final int SHIFT_32 = 32;
    private static final int SHIFT_40 = 40;
    private static final int SHIFT_48 = 48;
    private static final int SHIFT_56 = 56;
    private static final int STRATEGY_ID_OFFSET = 8;
    private static final int STRATEGY_ID_HIGH_OFFSET = 9;

    private ClientOidCodec() {}

    /**
     * Encodes a counter (bytes 0-7, little-endian) and strategyId (bytes 8-9, little-endian)
     * into a 16-byte clientOid array. Bytes 10-15 are zeroed.
     */
    public static void encode(byte[] dest, long counter, int strategyId) {
        dest[0] = (byte) (counter);
        dest[1] = (byte) (counter >>> SHIFT_8);
        dest[2] = (byte) (counter >>> SHIFT_16);
        dest[3] = (byte) (counter >>> SHIFT_24);
        dest[4] = (byte) (counter >>> SHIFT_32);
        dest[5] = (byte) (counter >>> SHIFT_40);
        dest[6] = (byte) (counter >>> SHIFT_48);
        dest[7] = (byte) (counter >>> SHIFT_56);
        dest[STRATEGY_ID_OFFSET] = (byte) (strategyId);
        dest[STRATEGY_ID_HIGH_OFFSET] = (byte) (strategyId >>> SHIFT_8);
        dest[10] = 0;
        dest[11] = 0;
        dest[12] = 0;
        dest[13] = 0;
        dest[14] = 0;
        dest[15] = 0;
    }

    /**
     * Decodes the counter from bytes 0-7 (little-endian) of a clientOid byte array.
     */
    public static long decodeCounter(byte[] src) {
        return ((long) (src[0] & BYTE_MASK))
                | ((long) (src[1] & BYTE_MASK) << SHIFT_8)
                | ((long) (src[2] & BYTE_MASK) << SHIFT_16)
                | ((long) (src[3] & BYTE_MASK) << SHIFT_24)
                | ((long) (src[4] & BYTE_MASK) << SHIFT_32)
                | ((long) (src[5] & BYTE_MASK) << SHIFT_40)
                | ((long) (src[6] & BYTE_MASK) << SHIFT_48)
                | ((long) (src[7] & BYTE_MASK) << SHIFT_56);
    }

    /**
     * Decodes the counter from bytes 0-7 (little-endian) of a clientOid in a DirectBuffer.
     */
    public static long decodeCounter(DirectBuffer buf, int offset) {
        return ((long) (buf.getByte(offset) & BYTE_MASK))
                | ((long) (buf.getByte(offset + 1) & BYTE_MASK) << SHIFT_8)
                | ((long) (buf.getByte(offset + 2) & BYTE_MASK) << SHIFT_16)
                | ((long) (buf.getByte(offset + 3) & BYTE_MASK) << SHIFT_24)
                | ((long) (buf.getByte(offset + 4) & BYTE_MASK) << SHIFT_32)
                | ((long) (buf.getByte(offset + 5) & BYTE_MASK) << SHIFT_40)
                | ((long) (buf.getByte(offset + 6) & BYTE_MASK) << SHIFT_48)
                | ((long) (buf.getByte(offset + 7) & BYTE_MASK) << SHIFT_56);
    }

    /**
     * Decodes the strategyId from bytes 8-9 (little-endian) of a clientOid byte array.
     */
    public static int decodeStrategyId(byte[] src) {
        return ((src[STRATEGY_ID_OFFSET] & BYTE_MASK)) | ((src[STRATEGY_ID_HIGH_OFFSET] & BYTE_MASK) << SHIFT_8);
    }

    /**
     * Decodes the strategyId from bytes 8-9 (little-endian) of a clientOid in a DirectBuffer.
     */
    public static int decodeStrategyId(DirectBuffer buf, int offset) {
        return ((buf.getByte(offset + STRATEGY_ID_OFFSET) & BYTE_MASK))
                | ((buf.getByte(offset + STRATEGY_ID_HIGH_OFFSET) & BYTE_MASK) << SHIFT_8);
    }
}
