package group.gnometrading.schemas;

import group.gnometrading.utils.ByteBufferUtils;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public abstract class Schema<E, D> {

    public final SchemaType schemaType;
    public final E encoder;
    public final D decoder;
    public final UnsafeBuffer buffer;
    public final MessageHeaderEncoder messageHeaderEncoder;
    public final MessageHeaderDecoder messageHeaderDecoder;

    public Schema(SchemaType schemaType, E encoder, D decoder) {
        this.schemaType = schemaType;
        this.encoder = encoder;
        this.decoder = decoder;
        this.messageHeaderDecoder = new MessageHeaderDecoder();
        this.messageHeaderEncoder = new MessageHeaderEncoder();

        this.buffer = ByteBufferUtils.createAlignedUnsafeBuffer(this.totalMessageSize());
        this.wrap(this.buffer);
    }

    public int totalMessageSize() {
        return MessageHeaderEncoder.ENCODED_LENGTH + this.getEncodedBlockLength();
    }

    public void copyTo(final Schema<E, D> other) {
        this.buffer.putBytes(0, other.buffer, 0, this.totalMessageSize());
    }

    protected abstract int getEncodedBlockLength();

    public abstract void wrap(MutableDirectBuffer buffer);

    /**
     * Retrieve the sequence number for the record. Note, the decoder *must*
     * be wrapped around a valid byte buffer for this to work.
     *
     * @return the sequence number
     */
    public abstract long getSequenceNumber();

}
