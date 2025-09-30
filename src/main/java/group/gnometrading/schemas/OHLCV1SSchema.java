package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class OHLCV1SSchema extends Schema {

    public final OHLCV1SEncoder encoder = new OHLCV1SEncoder();
    public final OHLCV1SDecoder decoder = new OHLCV1SDecoder();

    public OHLCV1SSchema() {
        super(SchemaType.OHLCV_1S);
        this.wrap(this.buffer);
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
