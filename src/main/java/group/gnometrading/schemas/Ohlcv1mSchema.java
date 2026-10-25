package group.gnometrading.schemas;

import group.gnometrading.schemas.book.Ohlcv1mDecoder;
import group.gnometrading.schemas.book.Ohlcv1mEncoder;
import org.agrona.MutableDirectBuffer;

public final class Ohlcv1mSchema extends BookRecord {

    public final Ohlcv1mEncoder encoder = new Ohlcv1mEncoder();
    public final Ohlcv1mDecoder decoder = new Ohlcv1mDecoder();

    public Ohlcv1mSchema() {
        super(RecordType.OHLCV_1M);
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.headerEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.headerDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Ohlcv1mEncoder.BLOCK_LENGTH;
    }

    @Override
    protected int getSbeVersion() {
        return Ohlcv1mEncoder.SCHEMA_VERSION;
    }
}
