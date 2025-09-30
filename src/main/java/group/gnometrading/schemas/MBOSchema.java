package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public class MBOSchema extends Schema {

    public final MBOEncoder encoder = new MBOEncoder();
    public final MBODecoder decoder = new MBODecoder();

    public MBOSchema() {
        super(SchemaType.MBO);
        this.wrap(this.buffer);
    }

    @Override
    public void wrap(MutableDirectBuffer buffer) {
        this.encoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buffer, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return MBOEncoder.BLOCK_LENGTH;
    }

    @Override
    public long getSequenceNumber() {
        return this.decoder.sequence();
    }
}
