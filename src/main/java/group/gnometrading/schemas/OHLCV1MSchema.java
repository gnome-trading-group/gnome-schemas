package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class OHLCV1MSchema extends Schema<OHLCV1MEncoder, OHLCV1MDecoder> {

    public OHLCV1MSchema() {
        super(SchemaType.OHLCV_1M, new OHLCV1MEncoder(), new OHLCV1MDecoder());
    }

    @Override
    protected int getEncodedBlockLength() {
        return OHLCV1MEncoder.BLOCK_LENGTH;
    }

    @Override
    public void wrap(MutableDirectBuffer buffer) {
        this.encoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderDecoder);
    }

    @Override
    public long getEventTimestamp() {
        return this.decoder.timestampEvent();
    }
}
