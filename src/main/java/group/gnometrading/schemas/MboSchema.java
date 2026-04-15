package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class MboSchema extends Schema {

    public final MboEncoder encoder = new MboEncoder();
    public final MboDecoder decoder = new MboDecoder();

    public MboSchema() {
        super(SchemaType.MBO);
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return MboEncoder.BLOCK_LENGTH;
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
