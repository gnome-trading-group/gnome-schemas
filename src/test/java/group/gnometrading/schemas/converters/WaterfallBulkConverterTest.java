package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.*;
import group.gnometrading.schemas.converters.mbp1.MBP1ToTradesBulkConverter;
import group.gnometrading.schemas.converters.mbp10.MBP10ToMBP1BulkConverter;
import group.gnometrading.schemas.converters.trades.TradesToOHLCV1SBulkConverter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class WaterfallBulkConverterTest {

    @Test
    void testEmptyList() {
        WaterfallBulkConverter<MBP10Schema, TradesSchema> converter = new WaterfallBulkConverter<>(
                new MBP10ToMBP1BulkConverter(),
                new MBP1ToTradesBulkConverter()
        );

        List<MBP10Schema> input = new ArrayList<>();
        List<TradesSchema> result = converter.convert(input);
        assertTrue(result.isEmpty());
    }

    @Test
    void testTwoStepConversion_MBP10ToMBP1ToTrades() {
        WaterfallBulkConverter<MBP10Schema, TradesSchema> converter = new WaterfallBulkConverter<>(
                new MBP10ToMBP1BulkConverter(),
                new MBP1ToTradesBulkConverter()
        );

        List<MBP10Schema> input = List.of(
                createMBP10Schema(11, 22, 1000, 100, 10, Action.Trade),
                createMBP10Schema(11, 22, 2000, 200, 20, Action.Trade),
                createMBP10Schema(11, 22, 3000, 300, 30, Action.Trade)
        );

        List<TradesSchema> result = converter.convert(input);

        assertEquals(3, result.size());
        assertTradesSchema(result.get(0), 11, 22, 1000, 100, 10);
        assertTradesSchema(result.get(1), 11, 22, 2000, 200, 20);
        assertTradesSchema(result.get(2), 11, 22, 3000, 300, 30);
    }

    @Test
    void testTwoStepConversion_FiltersNullsFromIntermediateStep() {
        WaterfallBulkConverter<MBP10Schema, TradesSchema> converter = new WaterfallBulkConverter<>(
                new MBP10ToMBP1BulkConverter(),
                new MBP1ToTradesBulkConverter()
        );

        // Mix of Trade and non-Trade actions
        List<MBP10Schema> input = List.of(
                createMBP10Schema(11, 22, 1000, 100, 10, Action.Trade),
                createMBP10Schema(11, 22, 2000, 200, 20, Action.Add),    // Will be filtered
                createMBP10Schema(11, 22, 3000, 300, 30, Action.Trade),
                createMBP10Schema(11, 22, 4000, 400, 40, Action.Cancel)  // Will be filtered
        );

        List<TradesSchema> result = converter.convert(input);

        // Only Trade actions should pass through
        assertEquals(2, result.size());
        assertTradesSchema(result.get(0), 11, 22, 1000, 100, 10);
        assertTradesSchema(result.get(1), 11, 22, 3000, 300, 30);
    }

    @Test
    void testThreeStepConversion_MBP10ToMBP1ToTradesToOHLCV1S() {
        WaterfallBulkConverter<MBP10Schema, OHLCV1SSchema> converter = new WaterfallBulkConverter<>(
                new MBP10ToMBP1BulkConverter(),
                new MBP1ToTradesBulkConverter(),
                new TradesToOHLCV1SBulkConverter()
        );

        List<MBP10Schema> input = List.of(
                createMBP10Schema(99, 1299, TimeUnit.MILLISECONDS.toNanos(500), 100, 10, Action.Trade),
                createMBP10Schema(99, 1299, TimeUnit.MILLISECONDS.toNanos(1500), 110, 5, Action.Trade),
                createMBP10Schema(99, 1299, TimeUnit.MILLISECONDS.toNanos(2500), 120, 15, Action.Trade)
        );

        List<OHLCV1SSchema> result = converter.convert(input);

        // 2 samples during iteration + 1 final sample (last element started interval 2)
        assertEquals(3, result.size());
        assertEquals(99, result.get(0).decoder.exchangeId());
        assertEquals(1299, result.get(0).decoder.securityId());
    }

    @Test
    void testSingleConverterInWaterfall() {
        WaterfallBulkConverter<MBP10Schema, MBP1Schema> converter = new WaterfallBulkConverter<>(
                new MBP10ToMBP1BulkConverter()
        );

        List<MBP10Schema> input = List.of(
                createMBP10Schema(11, 22, 1000, 100, 10, Action.Trade),
                createMBP10Schema(33, 44, 2000, 200, 20, Action.Trade)
        );

        List<MBP1Schema> result = converter.convert(input);

        assertEquals(2, result.size());
        assertEquals(11, result.get(0).decoder.exchangeId());
        assertEquals(33, result.get(1).decoder.exchangeId());
    }

    @Test
    void testClassCastException_IncompatibleIntermediateConversion() {
        // Create a waterfall with incompatible converters
        WaterfallBulkConverter<MBP10Schema, OHLCV1SSchema> converter = new WaterfallBulkConverter<>(
                new MBP10ToMBP1BulkConverter(),
                new TradesToOHLCV1SBulkConverter()  // Expects Trades, but gets MBP1
        );

        List<MBP10Schema> input = List.of(createMBP10Schema(11, 22, 1000, 100, 10, Action.Trade));

        assertThrows(ClassCastException.class, () -> converter.convert(input));
    }

    private MBP10Schema createMBP10Schema(int exchangeId, int securityId, long timestampEvent, long price, long size, Action action) {
        var schema = new MBP10Schema();
        schema.encoder.exchangeId(exchangeId);
        schema.encoder.securityId(securityId);
        schema.encoder.timestampEvent(timestampEvent);
        schema.encoder.timestampSent(timestampEvent + 1);
        schema.encoder.timestampRecv(timestampEvent + 2);
        schema.encoder.price(price);
        schema.encoder.size(size);
        schema.encoder.action(action);
        schema.encoder.side(Side.Ask);
        schema.encoder.flags().marketByPrice(true);
        schema.encoder.sequence(100);
        schema.encoder.depth((short) 1);
        return schema;
    }

    private void assertTradesSchema(TradesSchema schema, int exchangeId, int securityId, long timestampEvent, long price, long size) {
        assertEquals(exchangeId, schema.decoder.exchangeId());
        assertEquals(securityId, schema.decoder.securityId());
        assertEquals(timestampEvent, schema.decoder.timestampEvent());
        assertEquals(price, schema.decoder.price());
        assertEquals(size, schema.decoder.size());
    }
}

