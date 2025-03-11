package group.gnometrading.schemas;

public class BBO1MSchema extends Schema<BBO1MEncoder, BBO1MDecoder> {

    public BBO1MSchema() {
        super(SchemaType.BBO_1M, new BBO1MEncoder(), new BBO1MDecoder());
    }

    @Override
    protected int getEncodedBlockLength() {
        return BBO1MEncoder.BLOCK_LENGTH;
    }

    @Override
    public void wrap() {
        this.encoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderDecoder);
    }
}
