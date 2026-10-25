package group.gnometrading.schemas;

import org.agrona.DirectBuffer;

/**
 * The fields every stream record starts with, in every family, at these offsets from the start of its block.
 * Code that moves, stores or checks records reads them here without knowing the record's family; a test holds
 * every family's schema to this layout.
 */
public final class StreamHeader {

    public static final int STREAM_ID_OFFSET = 0;
    public static final int TIMESTAMP_EVENT_OFFSET = 4;
    public static final int TIMESTAMP_RECV_OFFSET = 12;
    public static final int SEQUENCE_OFFSET = 20;
    public static final int FLAGS_OFFSET = 28;
    public static final int LENGTH = 29;

    /** Flag bits with the same meaning in every family; bits 4-7 are each family's own. */
    public static final int BAD_DATA_BIT = 0;

    public static final int SNAPSHOT_BIT = 1;
    public static final int LAST_MESSAGE_BIT = 2;
    public static final int BAD_TIMESTAMP_RECV_BIT = 3;

    private static final int BYTE_MASK = 0xFF;

    private StreamHeader() {}

    /** The stream id of the record whose message (header included) starts at {@code messageOffset}. */
    public static int streamId(final DirectBuffer buffer, final int messageOffset) {
        return buffer.getInt(block(messageOffset) + STREAM_ID_OFFSET, Statics.BYTE_ORDER);
    }

    public static long timestampEvent(final DirectBuffer buffer, final int messageOffset) {
        return buffer.getLong(block(messageOffset) + TIMESTAMP_EVENT_OFFSET, Statics.BYTE_ORDER);
    }

    public static long timestampRecv(final DirectBuffer buffer, final int messageOffset) {
        return buffer.getLong(block(messageOffset) + TIMESTAMP_RECV_OFFSET, Statics.BYTE_ORDER);
    }

    public static long sequence(final DirectBuffer buffer, final int messageOffset) {
        return buffer.getLong(block(messageOffset) + SEQUENCE_OFFSET, Statics.BYTE_ORDER);
    }

    public static int flags(final DirectBuffer buffer, final int messageOffset) {
        return buffer.getByte(block(messageOffset) + FLAGS_OFFSET) & BYTE_MASK;
    }

    public static boolean isSet(final int flags, final int bit) {
        return (flags & (1 << bit)) != 0;
    }

    private static int block(final int messageOffset) {
        return messageOffset + SbeMessage.HEADER_LENGTH;
    }
}
