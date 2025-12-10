package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.OHLCV1HSchema;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static group.gnometrading.schemas.converters.trades.TradesTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TradesToOHLCV1HConverterTest {

    @Test
    void testBasicConverter() {
        var converter = new TradesToOHLCV1HConverter();

        var result = converter.convert(genTrade(100, 1, 0));
        assertNull(result);

        result = converter.convert(genTrade(101, 10, TimeUnit.HOURS.toNanos(1)));
        assertSchema(result, 0, 100, 100, 100, 100, 1);

        result = converter.convert(genTrade(105, 5, TimeUnit.HOURS.toNanos(1) + TimeUnit.MILLISECONDS.toNanos(1)));
        assertNull(result);

        result = converter.convert(genTrade(110, 3, TimeUnit.HOURS.toNanos(1) + TimeUnit.MILLISECONDS.toNanos(501)));
        assertNull(result);

        result = converter.convert(genTrade(10, 3, TimeUnit.HOURS.toNanos(2)));
        assertSchema(result, 1, 101, 110, 101, 110, 18);

        result = converter.convert(genTrade(10, 5, TimeUnit.HOURS.toNanos(3)));
        assertSchema(result, 2, 10, 10, 10, 10, 3);
    }

    private void assertSchema(OHLCV1HSchema schema, long time, long open, long high, long low, long close, long volume) {
        assertEquals(schema.decoder.timestampEvent(), TimeUnit.HOURS.toNanos(time));
        assertEquals(schema.decoder.open(), open);
        assertEquals(schema.decoder.high(), high);
        assertEquals(schema.decoder.low(), low);
        assertEquals(schema.decoder.close(), close);
        assertEquals(schema.decoder.volume(), volume);
        assertEquals(schema.decoder.exchangeId(), EXCHANGE_ID);
        assertEquals(schema.decoder.securityId(), SECURITY_ID);
    }
}