package group.gnometrading.schemas;

import group.gnometrading.schemas.book.Bbo1mDecoder;
import group.gnometrading.schemas.book.Bbo1mEncoder;
import org.agrona.MutableDirectBuffer;

public final class Bbo1mSchema extends BookRecord {

    public final Bbo1mEncoder encoder = new Bbo1mEncoder();
    public final Bbo1mDecoder decoder = new Bbo1mDecoder();

    public Bbo1mSchema() {
        super(RecordType.BBO_1M);
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.headerEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.headerDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Bbo1mEncoder.BLOCK_LENGTH;
    }

    @Override
    protected int getSbeVersion() {
        return Bbo1mEncoder.SCHEMA_VERSION;
    }
}
