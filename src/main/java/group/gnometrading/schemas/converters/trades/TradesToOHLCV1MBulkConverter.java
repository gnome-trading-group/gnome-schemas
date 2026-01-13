package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.OHLCV1MSchema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SamplingSchemaBulkConverter;

public class TradesToOHLCV1MBulkConverter extends SamplingSchemaBulkConverter<TradesSchema, OHLCV1MSchema> {
    public TradesToOHLCV1MBulkConverter() {
        super(new TradesToOHLCV1MConverter());
    }

    @Override
    protected OHLCV1MSchema newOutputSchema() {
        return new OHLCV1MSchema();
    }
}
