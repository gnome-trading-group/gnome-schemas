package group.gnometrading.schemas;

import group.gnometrading.utils.ByteBufferUtils;
import group.gnometrading.utils.Copyable;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public abstract class Schema implements Copyable<Schema> {

    public final SchemaType schemaType;
    public final UnsafeBuffer buffer;
    public final MessageHeaderEncoder messageHeaderEncoder;
    public final MessageHeaderDecoder messageHeaderDecoder;

    public Schema(SchemaType schemaType) {
        this.schemaType = schemaType;
        this.messageHeaderDecoder = new MessageHeaderDecoder();
        this.messageHeaderEncoder = new MessageHeaderEncoder();

        this.buffer = ByteBufferUtils.createAlignedUnsafeBuffer(this.totalMessageSize());
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
    public void copyFrom(Schema other) {
        assert this.schemaType == other.schemaType : "Cannot copy from different schema types";
        this.buffer.putBytes(0, other.buffer, 0, this.totalMessageSize());
    }

}
