package group.gnometrading.schemas;

public abstract class ClientOidMessage extends SbeMessage {

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
    private static final int ZERO_PAD_START = 10;

    protected abstract int clientOidEncodingOffset();

    private int absoluteOffset() {
        return MessageHeaderDecoder.ENCODED_LENGTH + clientOidEncodingOffset();
    }

    public final long getClientOidCounter() {
        final int off = absoluteOffset();
        return ((long) (buffer.getByte(off) & BYTE_MASK))
                | ((long) (buffer.getByte(off + 1) & BYTE_MASK) << SHIFT_8)
                | ((long) (buffer.getByte(off + 2) & BYTE_MASK) << SHIFT_16)
                | ((long) (buffer.getByte(off + 3) & BYTE_MASK) << SHIFT_24)
                | ((long) (buffer.getByte(off + 4) & BYTE_MASK) << SHIFT_32)
                | ((long) (buffer.getByte(off + 5) & BYTE_MASK) << SHIFT_40)
                | ((long) (buffer.getByte(off + 6) & BYTE_MASK) << SHIFT_48)
                | ((long) (buffer.getByte(off + 7) & BYTE_MASK) << SHIFT_56);
    }

    public final int getClientOidStrategyId() {
        final int off = absoluteOffset();
        return (buffer.getByte(off + STRATEGY_ID_OFFSET) & BYTE_MASK)
                | ((buffer.getByte(off + STRATEGY_ID_HIGH_OFFSET) & BYTE_MASK) << SHIFT_8);
    }

    public final void encodeClientOid(final long counter, final int strategyId) {
        final int off = absoluteOffset();
        buffer.putByte(off, (byte) counter);
        buffer.putByte(off + 1, (byte) (counter >>> SHIFT_8));
        buffer.putByte(off + 2, (byte) (counter >>> SHIFT_16));
        buffer.putByte(off + 3, (byte) (counter >>> SHIFT_24));
        buffer.putByte(off + 4, (byte) (counter >>> SHIFT_32));
        buffer.putByte(off + 5, (byte) (counter >>> SHIFT_40));
        buffer.putByte(off + 6, (byte) (counter >>> SHIFT_48));
        buffer.putByte(off + 7, (byte) (counter >>> SHIFT_56));
        buffer.putByte(off + STRATEGY_ID_OFFSET, (byte) strategyId);
        buffer.putByte(off + STRATEGY_ID_HIGH_OFFSET, (byte) (strategyId >>> SHIFT_8));
        for (int i = ZERO_PAD_START; i < CLIENT_OID_LENGTH; i++) {
            buffer.putByte(off + i, (byte) 0);
        }
    }
}
