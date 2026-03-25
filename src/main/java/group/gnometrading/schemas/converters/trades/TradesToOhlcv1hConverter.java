package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.Ohlcv1hDecoder;
import group.gnometrading.schemas.Ohlcv1hEncoder;
import group.gnometrading.schemas.Ohlcv1hSchema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SamplingSchemaConverter;
import java.time.Duration;

public final class TradesToOhlcv1hConverter extends SamplingSchemaConverter<TradesSchema, Ohlcv1hSchema> {

    private final Ohlcv1hSchema state;
    private final Ohlcv1hSchema output;

    public TradesToOhlcv1hConverter() {
        super(Duration.ofHours(1));
        this.state = new Ohlcv1hSchema();
        this.output = new Ohlcv1hSchema();
        reset();
    }

    private void reset() {
        state.encoder.open(Ohlcv1hEncoder.openNullValue());
        state.encoder.high(Ohlcv1hEncoder.highMinValue());
        state.encoder.low(Ohlcv1hEncoder.lowMaxValue());
        state.encoder.close(Ohlcv1hEncoder.closeNullValue());
        state.encoder.volume(0);
    }

    @Override
    protected Ohlcv1hSchema sample() {
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

        if (currentState.open() == Ohlcv1hDecoder.openNullValue()) {
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
