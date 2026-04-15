package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class CancelOrder extends ClientOidMessage {

    public final CancelOrderEncoder encoder = new CancelOrderEncoder();
    public final CancelOrderDecoder decoder = new CancelOrderDecoder();

    public CancelOrder() {
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int clientOidEncodingOffset() {
        return CancelOrderDecoder.clientOidEncodingOffset();
    }

    @Override
    protected int getEncodedBlockLength() {
        return CancelOrderEncoder.BLOCK_LENGTH;
    }
}
