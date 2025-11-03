package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class BBO1SSchema extends Schema {

    public final BBO1SEncoder encoder = new BBO1SEncoder();
    public final BBO1SDecoder decoder = new BBO1SDecoder();

    public BBO1SSchema() {
        super(SchemaType.BBO_1S);
        this.wrap(this.buffer);
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
    public long getSequenceNumber() {
        return this.decoder.sequence();
    }

    @Override
    public long getEventTimestamp() {
        return this.decoder.timestampEvent();
    }
}
