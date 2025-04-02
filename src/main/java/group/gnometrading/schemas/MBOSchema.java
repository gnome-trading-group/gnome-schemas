package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class MBOSchema extends Schema<MBOEncoder, MBODecoder> {
    public MBOSchema() {
        super(SchemaType.MBO, new MBOEncoder(), new MBODecoder());
    }

    @Override
    public void wrap(MutableDirectBuffer buffer) {
        this.encoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return MBOEncoder.BLOCK_LENGTH;
    }

    @Override
    public long getEventTimestamp() {
        return this.decoder.timestampEvent();
    }
}
