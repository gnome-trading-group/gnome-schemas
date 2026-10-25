package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class Intent extends CoreMessage {

    public final IntentEncoder encoder = new IntentEncoder();
    public final IntentDecoder decoder = new IntentDecoder();

    public Intent() {
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.headerEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.headerDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return IntentEncoder.BLOCK_LENGTH;
    }

    @Override
    protected int getSbeVersion() {
        return IntentEncoder.SCHEMA_VERSION;
    }
}
