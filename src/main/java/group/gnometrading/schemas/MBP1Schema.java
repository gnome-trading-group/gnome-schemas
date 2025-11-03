package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class MBP1Schema extends Schema {

    public final MBP1Encoder encoder = new MBP1Encoder();
    public final MBP1Decoder decoder = new MBP1Decoder();

    public MBP1Schema() {
        super(SchemaType.MBP_1);
        this.wrap(this.buffer);
    }

    @Override
    protected int getEncodedBlockLength() {
        return MBP1Encoder.BLOCK_LENGTH;
    }

    @Override
    public void wrap(MutableDirectBuffer buffer) {
        this.encoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderDecoder);
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
