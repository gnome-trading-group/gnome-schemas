package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class OHLCV1SSchema extends Schema<OHLCV1SEncoder, OHLCV1SDecoder> {

    public OHLCV1SSchema() {
        super(SchemaType.OHLCV_1S, new OHLCV1SEncoder(), new OHLCV1SDecoder());
    }

    @Override
    protected int getEncodedBlockLength() {
        return OHLCV1SEncoder.BLOCK_LENGTH;
    }

    @Override
    public void wrap(MutableDirectBuffer buffer) {
        this.encoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderDecoder);
    }

    @Override
    public long getSequenceNumber() {
        return 0;
    }
}
