package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class BBO1SSchema extends Schema<BBO1SEncoder, BBO1SDecoder> {

    public BBO1SSchema() {
        super(SchemaType.BBO_1S, new BBO1SEncoder(), new BBO1SDecoder());
    }

    @Override
    public void wrap(MutableDirectBuffer buffer) {
        this.encoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return BBO1SEncoder.BLOCK_LENGTH;
    }

    @Override
    public long getEventTimestamp() {
        return this.decoder.timestampEvent();
    }
}
