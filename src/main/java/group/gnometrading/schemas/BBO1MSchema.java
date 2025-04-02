package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class BBO1MSchema extends Schema<BBO1MEncoder, BBO1MDecoder> {

    public BBO1MSchema() {
        super(SchemaType.BBO_1M, new BBO1MEncoder(), new BBO1MDecoder());
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
    public long getEventTimestamp() {
        return this.decoder.timestampEvent();
    }
}
