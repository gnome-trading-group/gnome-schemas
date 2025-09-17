package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class MBP1Schema extends Schema<MBP1Encoder, MBP1Decoder> {

    public MBP1Schema() {
        super(SchemaType.MBP_1, new MBP1Encoder(), new MBP1Decoder());
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
}
