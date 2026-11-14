package group.gnometrading.schemas.gateway;

import group.gnometrading.schemas.SbeMessage;
import org.agrona.MutableDirectBuffer;

/** A flyweight over one gateway {@code OrderTiming}: when one request went through the gateway and its venue. */
public final class OrderTiming extends SbeMessage {

    public final OrderTimingEncoder encoder = new OrderTimingEncoder();
    public final OrderTimingDecoder decoder = new OrderTimingDecoder();
    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();

    public OrderTiming() {
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(final MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.headerEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.headerDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return OrderTimingEncoder.BLOCK_LENGTH;
    }

    @Override
    protected int getSbeVersion() {
        return OrderTimingEncoder.SCHEMA_VERSION;
    }
}
