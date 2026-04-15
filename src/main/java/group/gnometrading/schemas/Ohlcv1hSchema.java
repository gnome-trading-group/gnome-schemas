package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class Ohlcv1hSchema extends Schema {

    public final Ohlcv1hEncoder encoder = new Ohlcv1hEncoder();
    public final Ohlcv1hDecoder decoder = new Ohlcv1hDecoder();

    public Ohlcv1hSchema() {
        super(SchemaType.OHLCV_1H);
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Ohlcv1hEncoder.BLOCK_LENGTH;
    }

    @Override
    public long getSequenceNumber() {
        return this.decoder.timestampEvent();
    }

    @Override
    public long getEventTimestamp() {
        return this.decoder.timestampEvent();
    }
}
