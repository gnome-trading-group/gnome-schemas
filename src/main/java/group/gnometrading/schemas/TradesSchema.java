package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class TradesSchema extends Schema {

    public final TradesEncoder encoder = new TradesEncoder();
    public final TradesDecoder decoder = new TradesDecoder();

    public TradesSchema() {
        super(SchemaType.TRADES);
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return TradesEncoder.BLOCK_LENGTH;
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
