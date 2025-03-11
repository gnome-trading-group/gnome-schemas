package group.gnometrading.schemas;

public class OHLCV1HSchema extends Schema<OHLCV1HEncoder, OHLCV1HDecoder> {

    public OHLCV1HSchema() {
        super(SchemaType.OHLCV_1H, new OHLCV1HEncoder(), new OHLCV1HDecoder());
    }

    @Override
    protected void wrap() {
        this.encoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return OHLCV1HEncoder.BLOCK_LENGTH;
    }
}
