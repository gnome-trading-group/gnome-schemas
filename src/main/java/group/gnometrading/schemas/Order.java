package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class Order extends ClientOidMessage {

    public final OrderEncoder encoder = new OrderEncoder();
    public final OrderDecoder decoder = new OrderDecoder();

    public Order() {
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.headerEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.headerDecoder);
    }

    @Override
    protected int clientOidEncodingOffset() {
        return OrderDecoder.clientOidEncodingOffset();
    }

    @Override
    protected int getEncodedBlockLength() {
        return OrderEncoder.BLOCK_LENGTH;
    }

    @Override
    protected int getSbeVersion() {
        return OrderEncoder.SCHEMA_VERSION;
    }
}
