package group.gnometrading.schemas;

import group.gnometrading.utils.ByteBufferUtils;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

/**
 * A flyweight over one SBE message, in any of Gnome's schemas. Every schema uses the same eight-byte message
 * header, so the header is read here by offset rather than through one schema's generated header codec.
 */
public abstract class SbeMessage {

    public static final int HEADER_LENGTH = 8;

    private static final int TEMPLATE_ID_OFFSET = 2;
    private static final int SCHEMA_ID_OFFSET = 4;
    private static final int VERSION_OFFSET = 6;
    private static final int UINT16_MASK = 0xFFFF;

    public final UnsafeBuffer buffer;

    public SbeMessage() {
        this.buffer = ByteBufferUtils.createAlignedUnsafeBuffer(this.totalMessageSize());
    }

    protected abstract int getEncodedBlockLength();

    public final int totalMessageSize() {
        return HEADER_LENGTH + this.getEncodedBlockLength();
    }

    protected abstract int getSbeVersion();

    public final int templateId() {
        return templateId(this.buffer, 0);
    }

    public static int templateId(final DirectBuffer buffer, final int offset) {
        return buffer.getShort(offset + TEMPLATE_ID_OFFSET, Statics.BYTE_ORDER) & UINT16_MASK;
    }

    public final int schemaId() {
        return schemaId(this.buffer, 0);
    }

    public static int schemaId(final DirectBuffer buffer, final int offset) {
        return buffer.getShort(offset + SCHEMA_ID_OFFSET, Statics.BYTE_ORDER) & UINT16_MASK;
    }

    public static int blockLength(final DirectBuffer buffer, final int offset) {
        return buffer.getShort(offset, Statics.BYTE_ORDER) & UINT16_MASK;
    }

    public static int version(final DirectBuffer buffer, final int offset) {
        return buffer.getShort(offset + VERSION_OFFSET, Statics.BYTE_ORDER) & UINT16_MASK;
    }

    /**
     * Wraps {@link #buffer} around {@code buf} as a zero-copy view, then delegates to
     * {@link #wrapCodecs(MutableDirectBuffer)} to update the encoder and decoder.
     *
     * @param buf the buffer to wrap
     */
    public final void wrap(MutableDirectBuffer buf) {
        this.buffer.wrap(buf);
        this.wrapCodecs(buf);
    }

    /**
     * Wraps the subclass encoder and decoder around {@code buf}.
     *
     * <p>Called by {@link #wrap(MutableDirectBuffer)} after {@link #buffer} has been updated.
     * Do not call this directly.
     *
     * @param buf the buffer to wrap
     */
    protected abstract void wrapCodecs(MutableDirectBuffer buf);
}
