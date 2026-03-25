package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.Ohlcv1mSchema;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.SamplingSchemaBulkConverter;

public final class TradesToOhlcv1mBulkConverter extends SamplingSchemaBulkConverter<TradesSchema, Ohlcv1mSchema> {

    public TradesToOhlcv1mBulkConverter() {
        super(new TradesToOhlcv1mConverter());
    }

    @Override
    protected Ohlcv1mSchema newOutputSchema() {
        return new Ohlcv1mSchema();
    }
}
