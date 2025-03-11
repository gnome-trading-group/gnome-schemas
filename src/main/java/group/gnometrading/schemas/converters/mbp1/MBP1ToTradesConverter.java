package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.Action;
import group.gnometrading.schemas.MBP1Schema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SchemaConverter;
import group.gnometrading.schemas.converters.Utils;

public class MBP1ToTradesConverter implements SchemaConverter<MBP1Schema, TradesSchema> {

    private final TradesSchema output;

    public MBP1ToTradesConverter() {
        this.output = new TradesSchema();
    }

    @Override
    public TradesSchema convert(MBP1Schema source) {
        final var decoder = source.decoder;
        if (decoder.action() != Action.Trade) {
            return null;
        }

        final var encoder = output.encoder;

        encoder.exchangeId(decoder.exchangeId());
        encoder.securityId(decoder.securityId());
        encoder.timestampEvent(decoder.timestampEvent());
        encoder.timestampSent(decoder.timestampSent());
        encoder.timestampRecv(decoder.timestampRecv());

        encoder.price(decoder.price());
        encoder.size(decoder.size());
        encoder.action(decoder.action());
        encoder.side(decoder.side());
        Utils.copyFlags(decoder.flags(), encoder.flags());
        encoder.sequence(decoder.sequence());
        encoder.depth(decoder.depth());

        return this.output;
    }
}
