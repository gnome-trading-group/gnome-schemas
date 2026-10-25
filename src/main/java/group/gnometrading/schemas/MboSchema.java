package group.gnometrading.schemas;

import group.gnometrading.schemas.book.MboDecoder;
import group.gnometrading.schemas.book.MboEncoder;
import org.agrona.MutableDirectBuffer;

public final class MboSchema extends BookRecord {

    public final MboEncoder encoder = new MboEncoder();
    public final MboDecoder decoder = new MboDecoder();

    public MboSchema() {
        super(RecordType.MBO);
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.headerEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.headerDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return MboEncoder.BLOCK_LENGTH;
    }

    @Override
    protected int getSbeVersion() {
        return MboEncoder.SCHEMA_VERSION;
    }
}
