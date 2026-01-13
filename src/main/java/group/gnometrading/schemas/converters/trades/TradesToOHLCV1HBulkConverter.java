package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.OHLCV1HSchema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SamplingSchemaBulkConverter;

public class TradesToOHLCV1HBulkConverter extends SamplingSchemaBulkConverter<TradesSchema, OHLCV1HSchema> {
    public TradesToOHLCV1HBulkConverter() {
        super(new TradesToOHLCV1HConverter());
    }

    @Override
    protected OHLCV1HSchema newOutputSchema() {
        return new OHLCV1HSchema();
    }
}
