package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.TradesSchema;

public final class TradesTestUtils {

    private TradesTestUtils() {}

    public static final int STREAM_ID = 99;

    public static TradesSchema genTrade(long price, long size, long eventTimestampNanos) {
        var schema = new TradesSchema();
        schema.encoder.streamId(STREAM_ID);
        schema.encoder.timestampEvent(eventTimestampNanos);
        schema.encoder.price(price);
        schema.encoder.size(size);
        return schema;
    }
}
