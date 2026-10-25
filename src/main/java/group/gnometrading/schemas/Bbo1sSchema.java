package group.gnometrading.schemas;

import group.gnometrading.schemas.book.Bbo1sDecoder;
import group.gnometrading.schemas.book.Bbo1sEncoder;
import org.agrona.MutableDirectBuffer;

public final class Bbo1sSchema extends BookRecord {

    public final Bbo1sEncoder encoder = new Bbo1sEncoder();
    public final Bbo1sDecoder decoder = new Bbo1sDecoder();

    public Bbo1sSchema() {
        super(RecordType.BBO_1S);
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.headerEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.headerDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Bbo1sEncoder.BLOCK_LENGTH;
    }

    @Override
    protected int getSbeVersion() {
        return Bbo1sEncoder.SCHEMA_VERSION;
    }
}
