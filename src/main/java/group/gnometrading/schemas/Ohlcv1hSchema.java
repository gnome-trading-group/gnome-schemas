package group.gnometrading.schemas;

import group.gnometrading.schemas.book.Ohlcv1hDecoder;
import group.gnometrading.schemas.book.Ohlcv1hEncoder;
import org.agrona.MutableDirectBuffer;

public final class Ohlcv1hSchema extends BookRecord {

    public final Ohlcv1hEncoder encoder = new Ohlcv1hEncoder();
    public final Ohlcv1hDecoder decoder = new Ohlcv1hDecoder();

    public Ohlcv1hSchema() {
        super(RecordType.OHLCV_1H);
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.headerEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.headerDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Ohlcv1hEncoder.BLOCK_LENGTH;
    }

    @Override
    protected int getSbeVersion() {
        return Ohlcv1hEncoder.SCHEMA_VERSION;
    }
}
