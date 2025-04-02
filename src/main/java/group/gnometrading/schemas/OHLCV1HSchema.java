package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class OHLCV1HSchema extends Schema<OHLCV1HEncoder, OHLCV1HDecoder> {

    public OHLCV1HSchema() {
        super(SchemaType.OHLCV_1H, new OHLCV1HEncoder(), new OHLCV1HDecoder());
    }

    @Override
    public void wrap(MutableDirectBuffer buffer) {
        this.encoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return OHLCV1HEncoder.BLOCK_LENGTH;
    }

    @Override
    public long getEventTimestamp() {
        return this.decoder.timestampEvent();
    }
}
