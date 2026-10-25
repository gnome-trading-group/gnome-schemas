package group.gnometrading.schemas;

import group.gnometrading.schemas.book.Ohlcv1sDecoder;
import group.gnometrading.schemas.book.Ohlcv1sEncoder;
import org.agrona.MutableDirectBuffer;

public final class Ohlcv1sSchema extends BookRecord {

    public final Ohlcv1sEncoder encoder = new Ohlcv1sEncoder();
    public final Ohlcv1sDecoder decoder = new Ohlcv1sDecoder();

    public Ohlcv1sSchema() {
        super(RecordType.OHLCV_1S);
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.headerEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.headerDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Ohlcv1sEncoder.BLOCK_LENGTH;
    }

    @Override
    protected int getSbeVersion() {
        return Ohlcv1sEncoder.SCHEMA_VERSION;
    }
}
