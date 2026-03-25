package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.Ohlcv1mDecoder;
import group.gnometrading.schemas.Ohlcv1mEncoder;
import group.gnometrading.schemas.Ohlcv1mSchema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SamplingSchemaConverter;
import java.time.Duration;

public final class TradesToOhlcv1mConverter extends SamplingSchemaConverter<TradesSchema, Ohlcv1mSchema> {

    private final Ohlcv1mSchema state;
    private final Ohlcv1mSchema output;

    public TradesToOhlcv1mConverter() {
        super(Duration.ofMinutes(1));
        this.state = new Ohlcv1mSchema();
        this.output = new Ohlcv1mSchema();
        reset();
    }

    private void reset() {
        state.encoder.open(Ohlcv1mEncoder.openNullValue());
        state.encoder.high(Ohlcv1mEncoder.highMinValue());
        state.encoder.low(Ohlcv1mEncoder.lowMaxValue());
        state.encoder.close(Ohlcv1mEncoder.closeNullValue());
        state.encoder.volume(0);
    }

    @Override
    protected Ohlcv1mSchema sample() {
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

        if (currentState.open() == Ohlcv1mDecoder.openNullValue()) {
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
