package group.gnometrading.schemas;

import group.gnometrading.schemas.book.Mbp1Decoder;
import group.gnometrading.schemas.book.Mbp1Encoder;
import org.agrona.MutableDirectBuffer;

public final class Mbp1Schema extends BookRecord {

    public final Mbp1Encoder encoder = new Mbp1Encoder();
    public final Mbp1Decoder decoder = new Mbp1Decoder();

    public Mbp1Schema() {
        super(RecordType.MBP_1);
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.headerEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.headerDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Mbp1Encoder.BLOCK_LENGTH;
    }

    @Override
    protected int getSbeVersion() {
        return Mbp1Encoder.SCHEMA_VERSION;
    }
}
