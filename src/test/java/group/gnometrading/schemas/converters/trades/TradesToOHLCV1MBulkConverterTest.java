package group.gnometrading.schemas.converters.trades;

import group.gnometrading.schemas.OHLCV1MSchema;
import group.gnometrading.schemas.TradesSchema;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static group.gnometrading.schemas.converters.trades.TradesTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class TradesToOHLCV1MBulkConverterTest {

    @Test
    void testEmptyList() {
        var converter = new TradesToOHLCV1MBulkConverter();
        List<TradesSchema> input = new ArrayList<>();
        List<OHLCV1MSchema> result = converter.convert(input);
        // SamplingSchemaBulkConverter always calls sample() at the end,
        // which returns a result even for empty input (with uninitialized data)
        assertEquals(1, result.size());
    }

    @Test
    void testSingleElementNoSample() {
        var converter = new TradesToOHLCV1MBulkConverter();
        List<TradesSchema> input = List.of(genTrade(100, 10, TimeUnit.SECONDS.toNanos(30)));
        List<OHLCV1MSchema> result = converter.convert(input);

        // The final sample() should produce one result
        assertEquals(1, result.size());
        assertOHLCVSchema(result.get(0), 100, 100, 100, 100, 10);
    }

    @Test
    void testMultipleElementsWithinSameInterval() {
        var converter = new TradesToOHLCV1MBulkConverter();
        List<TradesSchema> input = List.of(
                genTrade(100, 10, TimeUnit.SECONDS.toNanos(10)),
                genTrade(110, 5, TimeUnit.SECONDS.toNanos(20)),
                genTrade(90, 15, TimeUnit.SECONDS.toNanos(30))
        );
        List<OHLCV1MSchema> result = converter.convert(input);

        // All within the same 1-minute interval, so only final sample() produces output
        assertEquals(1, result.size());
        // OHLCV: open=100, high=110, low=90, close=90, volume=30
        assertOHLCVSchema(result.get(0), 100, 110, 90, 90, 30);
    }

    @Test
    void testMultipleElementsAcrossIntervals() {
        var converter = new TradesToOHLCV1MBulkConverter();
        List<TradesSchema> input = List.of(
                genTrade(100, 10, TimeUnit.SECONDS.toNanos(30)),   // interval 0
                genTrade(110, 5, TimeUnit.MINUTES.toNanos(1)),     // triggers sample for interval 0
                genTrade(120, 15, TimeUnit.SECONDS.toNanos(90)),   // still in interval 1
                genTrade(130, 20, TimeUnit.MINUTES.toNanos(2) + TimeUnit.SECONDS.toNanos(30))  // triggers sample for interval 1
        );
        List<OHLCV1MSchema> result = converter.convert(input);

        // 2 samples during iteration + 1 final sample = 3 total
        assertEquals(3, result.size());

        // First interval: only one trade (100, 10)
        assertOHLCVSchema(result.get(0), 100, 100, 100, 100, 10);

        // Second interval: trades at 110 and 120
        assertOHLCVSchema(result.get(1), 110, 120, 110, 120, 20);

        // Third interval (final sample): trade at 130
        assertOHLCVSchema(result.get(2), 130, 130, 130, 130, 20);
    }

    @Test
    void testFinalSampleIncludesLastInterval() {
        var converter = new TradesToOHLCV1MBulkConverter();
        List<TradesSchema> input = List.of(
                genTrade(100, 10, TimeUnit.SECONDS.toNanos(30)),
                genTrade(110, 5, TimeUnit.MINUTES.toNanos(1)),  // triggers sample for interval 0
                genTrade(120, 15, TimeUnit.SECONDS.toNanos(90))
        );
        List<OHLCV1MSchema> result = converter.convert(input);

        // 1 sample during iteration + 1 final sample = 2 total
        assertEquals(2, result.size());

        // Verify the last result contains data from the last interval
        OHLCV1MSchema lastResult = result.get(result.size() - 1);
        assertEquals(EXCHANGE_ID, lastResult.decoder.exchangeId());
        assertEquals(SECURITY_ID, lastResult.decoder.securityId());
    }

    @Test
    void testOutputSchemasAreIndependent() {
        var converter = new TradesToOHLCV1MBulkConverter();
        List<TradesSchema> input = List.of(
                genTrade(100, 10, TimeUnit.SECONDS.toNanos(30)),
                genTrade(200, 20, TimeUnit.MINUTES.toNanos(1) + TimeUnit.SECONDS.toNanos(30)),
                genTrade(300, 30, TimeUnit.MINUTES.toNanos(2) + TimeUnit.SECONDS.toNanos(30))
        );
        List<OHLCV1MSchema> result = converter.convert(input);

        // Verify all results are different instances
        for (int i = 0; i < result.size(); i++) {
            for (int j = i + 1; j < result.size(); j++) {
                assertNotSame(result.get(i), result.get(j));
            }
        }
    }

    private void assertOHLCVSchema(OHLCV1MSchema schema, long open, long high, long low, long close, long volume) {
        assertEquals(open, schema.decoder.open());
        assertEquals(high, schema.decoder.high());
        assertEquals(low, schema.decoder.low());
        assertEquals(close, schema.decoder.close());
        assertEquals(volume, schema.decoder.volume());
        assertEquals(EXCHANGE_ID, schema.decoder.exchangeId());
        assertEquals(SECURITY_ID, schema.decoder.securityId());
    }
}

