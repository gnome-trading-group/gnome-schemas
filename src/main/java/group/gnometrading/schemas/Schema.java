package group.gnometrading.schemas;

import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

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

        this.buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(this.totalMessageSize()));
        this.wrap();
    }

    public int totalMessageSize() {
        return MessageHeaderEncoder.ENCODED_LENGTH + this.getEncodedBlockLength();
    }

    protected abstract int getEncodedBlockLength();

    protected abstract void wrap();

}
