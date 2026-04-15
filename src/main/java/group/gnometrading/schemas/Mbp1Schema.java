package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class Mbp1Schema extends Schema {

    public final Mbp1Encoder encoder = new Mbp1Encoder();
    public final Mbp1Decoder decoder = new Mbp1Decoder();

    public Mbp1Schema() {
        super(SchemaType.MBP_1);
        this.wrap(this.buffer);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Mbp1Encoder.BLOCK_LENGTH;
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
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
