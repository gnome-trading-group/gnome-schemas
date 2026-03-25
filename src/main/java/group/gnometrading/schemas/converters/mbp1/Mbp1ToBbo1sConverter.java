package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.Bbo1sSchema;
import group.gnometrading.schemas.Mbp1Schema;
import group.gnometrading.schemas.converters.SamplingSchemaConverter;
import group.gnometrading.schemas.converters.Utils;
import java.time.Duration;

public final class Mbp1ToBbo1sConverter extends SamplingSchemaConverter<Mbp1Schema, Bbo1sSchema> {

    private final Bbo1sSchema output;
    private final Mbp1Schema state;

    public Mbp1ToBbo1sConverter() {
        super(Duration.ofSeconds(1));
        this.output = new Bbo1sSchema();
        this.state = new Mbp1Schema();
    }

    @Override
    protected Bbo1sSchema sample() {
        final var decoder = state.decoder;
        final var encoder = output.encoder;

        encoder.exchangeId(decoder.exchangeId());
        encoder.securityId(decoder.securityId());
        encoder.timestampEvent(this.getLastSampleTimeNanos());
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
    protected void updateState(Mbp1Schema source) {
        source.buffer.getBytes(0, this.state.buffer, 0, this.state.totalMessageSize());
    }
}
