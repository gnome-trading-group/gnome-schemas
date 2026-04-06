package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class ModifyOrder extends SbeMessage {

    public final ModifyOrderEncoder encoder = new ModifyOrderEncoder();
    public final ModifyOrderDecoder decoder = new ModifyOrderDecoder();

    public ModifyOrder() {
        this.wrap(this.buffer);
    }

    @Override
    public void wrap(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return ModifyOrderEncoder.BLOCK_LENGTH;
    }
}
