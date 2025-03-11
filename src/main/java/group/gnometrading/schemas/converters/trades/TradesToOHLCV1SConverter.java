package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.OHLCV1SDecoder;
import group.gnometrading.schemas.OHLCV1SEncoder;
import group.gnometrading.schemas.OHLCV1SSchema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SamplingSchemaConverter;
import org.agrona.concurrent.EpochClock;
import org.agrona.concurrent.SystemEpochClock;

import java.util.concurrent.TimeUnit;

public class TradesToOHLCV1SConverter extends SamplingSchemaConverter<TradesSchema, OHLCV1SSchema> {

    private final OHLCV1SSchema state, output;

    public TradesToOHLCV1SConverter(EpochClock clock) {
        super(clock, TimeUnit.SECONDS.toMillis(1));
        this.state = new OHLCV1SSchema();
        this.output = new OHLCV1SSchema();
        reset();
    }

    public TradesToOHLCV1SConverter() {
        this(new SystemEpochClock());
    }

    private void reset() {
        state.encoder.open(OHLCV1SEncoder.openNullValue());
        state.encoder.high(OHLCV1SEncoder.highMinValue());
        state.encoder.low(OHLCV1SEncoder.lowMaxValue());
        state.encoder.close(OHLCV1SEncoder.closeNullValue());
        state.encoder.volume(0);
    }

    @Override
    protected OHLCV1SSchema sample() {
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

        if (currentState.open() == OHLCV1SDecoder.openNullValue()) {
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
