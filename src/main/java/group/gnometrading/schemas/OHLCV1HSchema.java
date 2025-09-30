package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class OHLCV1HSchema extends Schema {

    public final OHLCV1HEncoder encoder = new OHLCV1HEncoder();
    public final OHLCV1HDecoder decoder = new OHLCV1HDecoder();

    public OHLCV1HSchema() {
        super(SchemaType.OHLCV_1H);
        this.wrap(this.buffer);
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
    public long getSequenceNumber() {
        return 0;
    }
}
