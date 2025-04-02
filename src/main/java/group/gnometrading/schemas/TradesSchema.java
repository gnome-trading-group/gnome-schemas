package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class TradesSchema extends Schema<TradesEncoder, TradesDecoder> {

    public TradesSchema() {
        super(SchemaType.TRADES, new TradesEncoder(), new TradesDecoder());
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
    public long getEventTimestamp() {
        return this.decoder.timestampEvent();
    }
}
