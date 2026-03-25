package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.Mbp1Schema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SingleSchemaBulkConverter;

public final class Mbp1ToTradesBulkConverter extends SingleSchemaBulkConverter<Mbp1Schema, TradesSchema> {

    public Mbp1ToTradesBulkConverter() {
        super(new Mbp1ToTradesConverter());
    }

    @Override
    protected TradesSchema newOutputSchema() {
        return new TradesSchema();
    }
}
