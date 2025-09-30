package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class BBO1MSchema extends Schema {

    public final BBO1MEncoder encoder = new BBO1MEncoder();
    public final BBO1MDecoder decoder = new BBO1MDecoder();

    public BBO1MSchema() {
        super(SchemaType.BBO_1M);
        this.wrap(this.buffer);
    }

    @Override
    protected int getEncodedBlockLength() {
        return BBO1MEncoder.BLOCK_LENGTH;
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
