package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.Ohlcv1sDecoder;
import group.gnometrading.schemas.Ohlcv1sEncoder;
import group.gnometrading.schemas.Ohlcv1sSchema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SamplingSchemaConverter;
import java.time.Duration;

public final class TradesToOhlcv1sConverter extends SamplingSchemaConverter<TradesSchema, Ohlcv1sSchema> {

    private final Ohlcv1sSchema state;
    private final Ohlcv1sSchema output;

    public TradesToOhlcv1sConverter() {
        super(Duration.ofSeconds(1));
        this.state = new Ohlcv1sSchema();
        this.output = new Ohlcv1sSchema();
        reset();
    }

    private void reset() {
        state.encoder.open(Ohlcv1sEncoder.openNullValue());
        state.encoder.high(Ohlcv1sEncoder.highMinValue());
        state.encoder.low(Ohlcv1sEncoder.lowMaxValue());
        state.encoder.close(Ohlcv1sEncoder.closeNullValue());
        state.encoder.volume(0);
    }

    @Override
    protected Ohlcv1sSchema sample() {
        this.state.encoder.timestampEvent(this.getLastSampleTimeNanos());
        this.state.buffer.getBytes(0, this.output.buffer, 0, this.state.totalMessageSize());
        reset();
        return this.output;
    }

    @Override
    protected void updateState(TradesSchema source) {
        final var decoder = source.decoder;
        final var encoder = this.state.encoder;
        final var currentState = this.state.decoder;

        encoder.exchangeId(decoder.exchangeId());
        encoder.securityId(decoder.securityId());

        if (currentState.open() == Ohlcv1sDecoder.openNullValue()) {
            encoder.open(decoder.price());
        }
        if (currentState.high() < decoder.price()) {
            encoder.high(decoder.price());
        }
        if (currentState.low() > decoder.price()) {
            encoder.low(decoder.price());
        }
        encoder.close(decoder.price());
        encoder.volume(currentState.volume() + decoder.size());
    }
}
