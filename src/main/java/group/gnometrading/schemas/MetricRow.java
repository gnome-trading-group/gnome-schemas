package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

/**
 * The flyweight a metric ring buffer is typed by. A row's values are var data, which {@link #totalMessageSize()}
 * doesn't count, so rows are encoded into a buffer of their own and published raw rather than claimed.
 */
public final class MetricRow extends CoreMessage {

    public final MetricRowEncoder encoder = new MetricRowEncoder();
    public final MetricRowDecoder decoder = new MetricRowDecoder();

    public MetricRow() {
        this.wrap(this.buffer);
    }

    @Override
    protected void wrapCodecs(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.headerEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.headerDecoder);
    }

    @Override
    protected int getEncodedBlockLength() {
        return MetricRowEncoder.BLOCK_LENGTH;
    }

    @Override
    protected int getSbeVersion() {
        return MetricRowEncoder.SCHEMA_VERSION;
    }
}
