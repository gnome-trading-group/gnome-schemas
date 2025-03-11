package group.gnometrading.schemas;

public class BBO1SSchema extends Schema<BBO1SEncoder, BBO1SDecoder> {

    public BBO1SSchema() {
        super(SchemaType.BBO_1S, new BBO1SEncoder(), new BBO1SDecoder());
    }

    @Override
    public void wrap() {
        this.encoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return BBO1SEncoder.BLOCK_LENGTH;
    }
}
