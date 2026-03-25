package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class Mbp10Schema extends Schema {

    public final Mbp10Encoder encoder = new Mbp10Encoder();
    public final Mbp10Decoder decoder = new Mbp10Decoder();

    public Mbp10Schema() {
        super(SchemaType.MBP_10);
        this.wrap(this.buffer);
    }

    @Override
    public void wrap(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Mbp10Encoder.BLOCK_LENGTH;
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
