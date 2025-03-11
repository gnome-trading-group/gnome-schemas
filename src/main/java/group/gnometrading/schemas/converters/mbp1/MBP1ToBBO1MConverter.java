package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.BBO1MSchema;
import group.gnometrading.schemas.MBP1Schema;
import group.gnometrading.schemas.converters.SamplingSchemaConverter;
import group.gnometrading.schemas.converters.Utils;
import org.agrona.concurrent.EpochClock;
import org.agrona.concurrent.SystemEpochClock;

import java.util.concurrent.TimeUnit;

public class MBP1ToBBO1MConverter extends SamplingSchemaConverter<MBP1Schema, BBO1MSchema> {

    private final BBO1MSchema output;
    private final MBP1Schema state;

    public MBP1ToBBO1MConverter(EpochClock clock) {
        super(clock, TimeUnit.MINUTES.toMillis(1));
        this.output = new BBO1MSchema();
        this.state = new MBP1Schema();
    }

    public MBP1ToBBO1MConverter() {
        this(new SystemEpochClock());
    }

    protected BBO1MSchema sample() {
        final var decoder = state.decoder;
        final var encoder = output.encoder;

        encoder.exchangeId(decoder.exchangeId());
        encoder.securityId(decoder.securityId());
        encoder.timestampEvent(TimeUnit.MILLISECONDS.toNanos(this.getLastSampleTimeMillis()));
        encoder.timestampRecv(decoder.timestampRecv());

        encoder.price(decoder.price());
        encoder.size(decoder.size());
        encoder.side(decoder.side());
        Utils.copyFlags(decoder.flags(), encoder.flags());
        encoder.sequence(decoder.sequence());

        encoder.bidPrice0(decoder.bidPrice0());
        encoder.bidSize0(decoder.bidSize0());
        encoder.bidCount0(decoder.bidCount0());

        encoder.askPrice0(decoder.askPrice0());
        encoder.askSize0(decoder.askSize0());
        encoder.askCount0(decoder.askCount0());

        return output;
    }

    @Override
    protected void updateState(MBP1Schema source) {
        source.buffer.getBytes(0, this.state.buffer, 0, this.state.totalMessageSize());
    }
}
