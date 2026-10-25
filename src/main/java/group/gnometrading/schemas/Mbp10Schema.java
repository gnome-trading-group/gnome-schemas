package group.gnometrading.schemas;

import group.gnometrading.schemas.book.Mbp10Decoder;
import group.gnometrading.schemas.book.Mbp10Encoder;
import org.agrona.MutableDirectBuffer;

public final class Mbp10Schema extends BookRecord {

    public final Mbp10Encoder encoder = new Mbp10Encoder();
    public final Mbp10Decoder decoder = new Mbp10Decoder();

    public Mbp10Schema() {
        super(RecordType.MBP_10);
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.headerEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.headerDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return Mbp10Encoder.BLOCK_LENGTH;
    }

    @Override
    protected int getSbeVersion() {
        return Mbp10Encoder.SCHEMA_VERSION;
    }
}
