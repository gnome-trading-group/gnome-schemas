package group.gnometrading.schemas;

import group.gnometrading.schemas.book.TradesDecoder;
import group.gnometrading.schemas.book.TradesEncoder;
import org.agrona.MutableDirectBuffer;

public final class TradesSchema extends BookRecord {

    public final TradesEncoder encoder = new TradesEncoder();
    public final TradesDecoder decoder = new TradesDecoder();

    public TradesSchema() {
        super(RecordType.TRADES);
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.headerEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.headerDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return TradesEncoder.BLOCK_LENGTH;
    }

    @Override
    protected int getSbeVersion() {
        return TradesEncoder.SCHEMA_VERSION;
    }
}
