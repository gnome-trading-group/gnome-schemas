package group.gnometrading.schemas.converters.mbp10;

import group.gnometrading.schemas.*;
import group.gnometrading.schemas.converters.SchemaConverter;
import group.gnometrading.schemas.converters.Utils;

public class MBP10ToMBP1Converter implements SchemaConverter<MBP10Schema, MBP1Schema> {

    private final MBP1Schema output;

    public MBP10ToMBP1Converter() {
        this.output = new MBP1Schema();
    }

    @Override
    public MBP1Schema convert(MBP10Schema source) {
        final var encoder = output.encoder;
        encoder.exchangeId(source.decoder.exchangeId());
        encoder.securityId(source.decoder.securityId());
        encoder.timestampEvent(source.decoder.timestampEvent());
        encoder.timestampSent(source.decoder.timestampSent());
        encoder.timestampRecv(source.decoder.timestampRecv());

        encoder.price(source.decoder.price());
        encoder.size(source.decoder.size());
        encoder.action(source.decoder.action());
        encoder.side(source.decoder.side());
        Utils.copyFlags(source.decoder.flags(), encoder.flags());
        encoder.sequence(source.decoder.sequence());
        encoder.depth(source.decoder.depth());

        encoder.bidPrice0(source.decoder.bidPrice0());
        encoder.bidSize0(source.decoder.bidSize0());
        encoder.bidCount0(source.decoder.bidCount0());

        encoder.askPrice0(source.decoder.askPrice0());
        encoder.askSize0(source.decoder.askSize0());
        encoder.askCount0(source.decoder.askCount0());

        return this.output;
    }
}
