package group.gnometrading.schemas;

public class OHLCV1SSchema extends Schema<OHLCV1SEncoder, OHLCV1SDecoder> {

    public OHLCV1SSchema() {
        super(SchemaType.OHLCV_1S, new OHLCV1SEncoder(), new OHLCV1SDecoder());
    }

    @Override
    protected int getEncodedBlockLength() {
        return OHLCV1SEncoder.BLOCK_LENGTH;
    }

    @Override
    protected void wrap() {
        this.encoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderDecoder);
    }
}
