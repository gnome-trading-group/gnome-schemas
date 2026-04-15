package group.gnometrading.schemas;

import group.gnometrading.utils.ByteBufferUtils;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public abstract class SbeMessage {

    public final UnsafeBuffer buffer;
    public final MessageHeaderEncoder messageHeaderEncoder;
    public final MessageHeaderDecoder messageHeaderDecoder;

    public SbeMessage() {
        this.messageHeaderDecoder = new MessageHeaderDecoder();
        this.messageHeaderEncoder = new MessageHeaderEncoder();
        this.buffer = ByteBufferUtils.createAlignedUnsafeBuffer(this.totalMessageSize());
    }

    protected abstract int getEncodedBlockLength();

    public final int totalMessageSize() {
        return MessageHeaderEncoder.ENCODED_LENGTH + this.getEncodedBlockLength();
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
