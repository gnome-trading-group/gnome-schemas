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

    public abstract void wrap(MutableDirectBuffer buf);
}
