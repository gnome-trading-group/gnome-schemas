package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class Bbo1sSchema extends Schema {

    public final Bbo1sEncoder encoder = new Bbo1sEncoder();
    public final Bbo1sDecoder decoder = new Bbo1sDecoder();

    public Bbo1sSchema() {
        super(SchemaType.BBO_1S);
        this.wrap(this.buffer);
    }

    @Override
    public void wrap(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Bbo1sEncoder.BLOCK_LENGTH;
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
