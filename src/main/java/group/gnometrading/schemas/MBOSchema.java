package group.gnometrading.schemas;

public class MBOSchema extends Schema<MBOEncoder, MBODecoder> {
    public MBOSchema() {
        super(SchemaType.MBO, new MBOEncoder(), new MBODecoder());
    }

    @Override
    public void wrap() {
        this.encoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return MBOEncoder.BLOCK_LENGTH;
    }
}
