package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.OHLCV1MDecoder;
import group.gnometrading.schemas.OHLCV1MEncoder;
import group.gnometrading.schemas.OHLCV1MSchema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SamplingSchemaConverter;
import org.agrona.concurrent.EpochClock;
import org.agrona.concurrent.SystemEpochClock;

import java.util.concurrent.TimeUnit;

public class TradesToOHLCV1MConverter extends SamplingSchemaConverter<TradesSchema, OHLCV1MSchema> {

    private final OHLCV1MSchema state, output;

    public TradesToOHLCV1MConverter(EpochClock clock) {
        super(clock, TimeUnit.MINUTES.toMillis(1));
        this.state = new OHLCV1MSchema();
        this.output = new OHLCV1MSchema();
        reset();
    }

    public TradesToOHLCV1MConverter() {
        this(new SystemEpochClock());
    }

    private void reset() {
        state.encoder.open(OHLCV1MEncoder.openNullValue());
        state.encoder.high(OHLCV1MEncoder.highMinValue());
        state.encoder.low(OHLCV1MEncoder.lowMaxValue());
        state.encoder.close(OHLCV1MEncoder.closeNullValue());
        state.encoder.volume(0);
    }

    @Override
    protected OHLCV1MSchema sample() {
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

        if (currentState.open() == OHLCV1MDecoder.openNullValue()) {
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
