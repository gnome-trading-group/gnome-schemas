package group.gnometrading.schemas;

public class OHLCV1MSchema extends Schema<OHLCV1MEncoder, OHLCV1MDecoder> {

    public OHLCV1MSchema() {
        super(SchemaType.OHLCV_1M, new OHLCV1MEncoder(), new OHLCV1MDecoder());
    }

    @Override
    protected int getEncodedBlockLength() {
        return OHLCV1MEncoder.BLOCK_LENGTH;
    }

    @Override
    protected void wrap() {
        this.encoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderDecoder);
    }
}
