package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class Intent extends SbeMessage {

    public final IntentEncoder encoder = new IntentEncoder();
    public final IntentDecoder decoder = new IntentDecoder();

    public Intent() {
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return IntentEncoder.BLOCK_LENGTH;
    }
}
