package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.Ohlcv1sSchema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SamplingSchemaBulkConverter;

public final class TradesToOhlcv1sBulkConverter extends SamplingSchemaBulkConverter<TradesSchema, Ohlcv1sSchema> {

    public TradesToOhlcv1sBulkConverter() {
        super(new TradesToOhlcv1sConverter());
    }

    @Override
    protected Ohlcv1sSchema newOutputSchema() {
        return new Ohlcv1sSchema();
    }
}
