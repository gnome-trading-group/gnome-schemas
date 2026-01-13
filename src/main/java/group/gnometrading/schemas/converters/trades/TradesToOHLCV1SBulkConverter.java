package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.OHLCV1SSchema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SamplingSchemaBulkConverter;

public class TradesToOHLCV1SBulkConverter extends SamplingSchemaBulkConverter<TradesSchema, OHLCV1SSchema> {
    public TradesToOHLCV1SBulkConverter() {
        super(new TradesToOHLCV1SConverter());
    }

    @Override
    protected OHLCV1SSchema newOutputSchema() {
        return new OHLCV1SSchema();
    }
}
