package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class OHLCV1MSchema extends Schema {

    public final OHLCV1MEncoder encoder = new OHLCV1MEncoder();
    public final OHLCV1MDecoder decoder = new OHLCV1MDecoder();

    public OHLCV1MSchema() {
        super(SchemaType.OHLCV_1M);
        this.wrap(this.buffer);
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
    public long getSequenceNumber() {
        return 0;
    }
}
