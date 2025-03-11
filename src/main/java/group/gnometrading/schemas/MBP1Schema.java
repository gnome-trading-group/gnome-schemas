package group.gnometrading.schemas;

public class MBP1Schema extends Schema<MBP1Encoder, MBP1Decoder> {

    public MBP1Schema() {
        super(SchemaType.MBP_1, new MBP1Encoder(), new MBP1Decoder());
    }

    @Override
    protected int getEncodedBlockLength() {
        return MBP1Encoder.BLOCK_LENGTH;
    }

    @Override
    public void wrap() {
        this.encoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderDecoder);
    }
}
