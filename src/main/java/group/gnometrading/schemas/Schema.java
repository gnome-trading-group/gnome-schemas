package group.gnometrading.schemas;

import group.gnometrading.utils.ByteBufferUtils;
import group.gnometrading.utils.Copyable;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public abstract class Schema<E, D> implements Copyable<Schema<E, D>> {

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

    protected abstract int getEncodedBlockLength();

    public abstract void wrap(MutableDirectBuffer buffer);

    /**
     * Retrieve the sequence number for the record. Note, the decoder *must*
     * be wrapped around a valid byte buffer for this to work.
     *
     * @return the sequence number
     */
    public abstract long getSequenceNumber();

    @Override
    public void copyFrom(Schema<E, D> other) {
        other.buffer.putBytes(0, this.buffer, 0, this.totalMessageSize());
    }

}
