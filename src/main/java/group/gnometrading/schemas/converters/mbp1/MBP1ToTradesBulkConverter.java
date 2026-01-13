package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.MBP1Schema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SingleSchemaBulkConverter;

public class MBP1ToTradesBulkConverter extends SingleSchemaBulkConverter<MBP1Schema, TradesSchema> {

    public MBP1ToTradesBulkConverter() {
        super(new MBP1ToTradesConverter());
    }

    @Override
    protected TradesSchema newOutputSchema() {
        return new TradesSchema();
    }
}
