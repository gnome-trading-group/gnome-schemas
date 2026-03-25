package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class Bbo1mSchema extends Schema {

    public final Bbo1mEncoder encoder = new Bbo1mEncoder();
    public final Bbo1mDecoder decoder = new Bbo1mDecoder();

    public Bbo1mSchema() {
        super(SchemaType.BBO_1M);
        this.wrap(this.buffer);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Bbo1mEncoder.BLOCK_LENGTH;
    }

    @Override
    public void wrap(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.messageHeaderDecoder);
    }

    @Override
    public long getSequenceNumber() {
        return this.decoder.sequence();
    }

    @Override
    public long getEventTimestamp() {
        return this.decoder.timestampEvent();
    }
}
