package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.Ohlcv1hSchema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SamplingSchemaBulkConverter;

public final class TradesToOhlcv1hBulkConverter extends SamplingSchemaBulkConverter<TradesSchema, Ohlcv1hSchema> {

    public TradesToOhlcv1hBulkConverter() {
        super(new TradesToOhlcv1hConverter());
    }

    @Override
    protected Ohlcv1hSchema newOutputSchema() {
        return new Ohlcv1hSchema();
    }
}
