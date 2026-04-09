package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class Order extends ClientOidMessage {

    public final OrderEncoder encoder = new OrderEncoder();
    public final OrderDecoder decoder = new OrderDecoder();

    public Order() {
        this.wrap(this.buffer);
    }

    @Override
    public void wrap(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int clientOidEncodingOffset() {
        return OrderDecoder.clientOidEncodingOffset();
    }

    @Override
    protected int getEncodedBlockLength() {
        return OrderEncoder.BLOCK_LENGTH;
    }
}
