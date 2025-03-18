package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.OHLCV1HSchema;
import group.gnometrading.schemas.converters.DummyClock;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static group.gnometrading.schemas.converters.trades.TradesTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TradesToOHLCV1HConverterTest {

    @Test
    void testBasicConverter() {
        var clock = new DummyClock();
        var converter = new TradesToOHLCV1HConverter(clock);

        clock.time = 0;
        var result = converter.convert(genTrade(100, 1));
        assertNull(result);

        clock.time = TimeUnit.HOURS.toMillis(1);
        result = converter.convert(genTrade(101, 10));
        assertNull(result); // Ignore first interval

        clock.time += 1;
        result = converter.convert(genTrade(105, 5));
        assertNull(result);

        clock.time += 500;
        result = converter.convert(genTrade(110, 3));
        assertNull(result);

        clock.time = TimeUnit.HOURS.toMillis(2);
        result = converter.convert(genTrade(10, 3));
        assertSchema(result, 1, 101, 110, 101, 110, 18);

        clock.time = TimeUnit.HOURS.toMillis(3);;
        result = converter.convert(genTrade(10, 5));
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