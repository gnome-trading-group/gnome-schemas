package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class CancelOrder extends SbeMessage {

    public final CancelOrderEncoder encoder = new CancelOrderEncoder();
    public final CancelOrderDecoder decoder = new CancelOrderDecoder();

    public CancelOrder() {
        this.wrap(this.buffer);
    }

    @Override
    public void wrap(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return CancelOrderEncoder.BLOCK_LENGTH;
    }
}
