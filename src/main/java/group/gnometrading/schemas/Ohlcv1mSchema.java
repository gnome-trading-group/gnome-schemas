package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class Ohlcv1mSchema extends Schema {

    public final Ohlcv1mEncoder encoder = new Ohlcv1mEncoder();
    public final Ohlcv1mDecoder decoder = new Ohlcv1mDecoder();

    public Ohlcv1mSchema() {
        super(SchemaType.OHLCV_1M);
        this.wrap(this.buffer);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Ohlcv1mEncoder.BLOCK_LENGTH;
    }

    @Override
    public void wrap(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.messageHeaderDecoder);
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
