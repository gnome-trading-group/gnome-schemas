package group.gnometrading.schemas;

public class MBP10Schema extends Schema<MBP10Encoder, MBP10Decoder> {
    public MBP10Schema() {
        super(SchemaType.MBP_10, new MBP10Encoder(), new MBP10Decoder());
    }

    @Override
    public void wrap() {
        this.encoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return MBP10Encoder.BLOCK_LENGTH;
    }
}
