package group.gnometrading.schemas;

import org.agrona.MutableDirectBuffer;

public final class OrderExecutionReport extends ClientOidMessage {

    public final OrderExecutionReportEncoder encoder = new OrderExecutionReportEncoder();
    public final OrderExecutionReportDecoder decoder = new OrderExecutionReportDecoder();

    public OrderExecutionReport() {
        this.wrap(this.buffer);
    }

    @Override
    public void wrap(MutableDirectBuffer buf) {
        this.encoder.wrapAndApplyHeader(buf, 0, this.messageHeaderEncoder);
        this.decoder.wrapAndApplyHeader(buf, 0, this.messageHeaderDecoder);
    }

    @Override
    protected int clientOidEncodingOffset() {
        return OrderExecutionReportDecoder.clientOidEncodingOffset();
    }

    @Override
    protected int getEncodedBlockLength() {
        return OrderExecutionReportEncoder.BLOCK_LENGTH;
    }
}
