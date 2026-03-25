package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class Ohlcv1sSchema extends Schema {

    public final Ohlcv1sEncoder encoder = new Ohlcv1sEncoder();
    public final Ohlcv1sDecoder decoder = new Ohlcv1sDecoder();

    public Ohlcv1sSchema() {
        super(SchemaType.OHLCV_1S);
        this.wrap(this.buffer);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Ohlcv1sEncoder.BLOCK_LENGTH;
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
