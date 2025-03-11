package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.TradesSchema;

public class TradesTestUtils {

    public static final int EXCHANGE_ID = 99;
    public static final int SECURITY_ID = 1299;

    public static TradesSchema genTrade(long price, long size) {
        var schema = new TradesSchema();
        schema.encoder.exchangeId(EXCHANGE_ID);
        schema.encoder.securityId(SECURITY_ID);
        schema.encoder.price(price);
        schema.encoder.size(size);
        return schema;
    }
}
