package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.OHLCV1HDecoder;
import group.gnometrading.schemas.OHLCV1HEncoder;
import group.gnometrading.schemas.OHLCV1HSchema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SamplingSchemaConverter;
import org.agrona.concurrent.EpochClock;
import org.agrona.concurrent.SystemEpochClock;

import java.util.concurrent.TimeUnit;

public class TradesToOHLCV1HConverter extends SamplingSchemaConverter<TradesSchema, OHLCV1HSchema> {

    private final OHLCV1HSchema state, output;

    public TradesToOHLCV1HConverter(EpochClock clock) {
        super(clock, TimeUnit.MINUTES.toMillis(1));
        this.state = new OHLCV1HSchema();
        this.output = new OHLCV1HSchema();
        reset();
    }

    public TradesToOHLCV1HConverter() {
        this(new SystemEpochClock());
    }

    private void reset() {
        state.encoder.open(OHLCV1HEncoder.openNullValue());
        state.encoder.high(OHLCV1HEncoder.highMinValue());
        state.encoder.low(OHLCV1HEncoder.lowMaxValue());
        state.encoder.close(OHLCV1HEncoder.closeNullValue());
        state.encoder.volume(0);
    }

    @Override
    protected OHLCV1HSchema sample() {
        this.state.encoder.timestampEvent(TimeUnit.MILLISECONDS.toNanos(this.getLastSampleTimeMillis()));
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

        if (currentState.open() == OHLCV1HDecoder.openNullValue()) {
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
