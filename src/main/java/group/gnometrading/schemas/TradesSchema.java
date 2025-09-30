package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class TradesSchema extends Schema {

    public final TradesEncoder encoder = new TradesEncoder();
    public final TradesDecoder decoder = new TradesDecoder();

    public TradesSchema() {
        super(SchemaType.TRADES);
        this.wrap(this.buffer);
    }

    @Override
    public void wrap(MutableDirectBuffer buffer) {
        this.encoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return TradesEncoder.BLOCK_LENGTH;
    }

    @Override
    public long getSequenceNumber() {
        return this.decoder.sequence();
    }
}
