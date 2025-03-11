package group.gnometrading.schemas;

public class TradesSchema extends Schema<TradesEncoder, TradesDecoder> {

    public TradesSchema() {
        super(SchemaType.TRADES, new TradesEncoder(), new TradesDecoder());
    }

    @Override
    public void wrap() {
        this.encoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(this.buffer, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return TradesEncoder.BLOCK_LENGTH;
    }
}
