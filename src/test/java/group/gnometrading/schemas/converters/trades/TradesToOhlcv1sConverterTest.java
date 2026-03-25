package group.gnometrading.schemas.converters.trades;

import static group.gnometrading.schemas.converters.trades.TradesTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import group.gnometrading.schemas.Ohlcv1sSchema;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class TradesToOhlcv1sConverterTest {

    @Test
    void testBasicConverter() {
        var converter = new TradesToOhlcv1sConverter();

        var result = converter.convert(genTrade(100, 1, 0));
        assertNull(result);

        result = converter.convert(genTrade(101, 10, TimeUnit.MILLISECONDS.toNanos(1000)));
        assertSchema(result, 0, 100, 100, 100, 100, 1);

        result = converter.convert(genTrade(105, 5, TimeUnit.MILLISECONDS.toNanos(1001)));
        assertNull(result);

        result = converter.convert(genTrade(110, 3, TimeUnit.MILLISECONDS.toNanos(1500)));
        assertNull(result);

        result = converter.convert(genTrade(10, 3, TimeUnit.MILLISECONDS.toNanos(2000)));
        assertSchema(result, 1000, 101, 110, 101, 110, 18);

        result = converter.convert(genTrade(10, 5, TimeUnit.MILLISECONDS.toNanos(3000)));
        assertSchema(result, 2000, 10, 10, 10, 10, 3);
    }

    private void assertSchema(
            Ohlcv1sSchema schema, long time, long open, long high, long low, long close, long volume) {
        assertEquals(schema.decoder.timestampEvent(), TimeUnit.MILLISECONDS.toNanos(time));
        assertEquals(schema.decoder.open(), open);
        assertEquals(schema.decoder.high(), high);
        assertEquals(schema.decoder.low(), low);
        assertEquals(schema.decoder.close(), close);
        assertEquals(schema.decoder.volume(), volume);
        assertEquals(schema.decoder.exchangeId(), EXCHANGE_ID);
        assertEquals(schema.decoder.securityId(), SECURITY_ID);
    }
}
