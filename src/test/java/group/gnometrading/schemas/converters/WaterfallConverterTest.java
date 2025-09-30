package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.*;
import group.gnometrading.schemas.converters.mbp10.MBP10ToMBP1Converter;
import group.gnometrading.schemas.converters.mbp1.MBP1ToTradesConverter;
import group.gnometrading.schemas.converters.trades.TradesToOHLCV1SConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WaterfallConverterTest {

    @Test
    void testTwoStepConversion_MBP10ToMBP1ToTrades() {
        // Create a waterfall converter: MBP10 -> MBP1 -> Trades
        WaterfallConverter<MBP10Schema, TradesSchema> converter = new WaterfallConverter<>(
                new MBP10ToMBP1Converter(),
                new MBP1ToTradesConverter()
        );

        // Create input MBP10 schema with Trade action
        MBP10Schema input = new MBP10Schema();
        input.encoder.exchangeId(11);
        input.encoder.securityId(22);
        input.encoder.timestampEvent(1234);
        input.encoder.timestampSent(1235);
        input.encoder.timestampRecv(1236);
        input.encoder.price(99);
        input.encoder.size(45);
        input.encoder.action(Action.Trade);
        input.encoder.side(Side.Ask);
        input.encoder.flags().marketByPrice(true);
        input.encoder.sequence(100);
        input.encoder.depth((short) 1);
        input.encoder.bidPrice0(9);
        input.encoder.bidCount0(5);
        input.encoder.bidSize0(4);
        input.encoder.askPrice0(10);
        input.encoder.askCount0(1);
        input.encoder.askSize0(6);

        // Convert through the waterfall
        TradesSchema result = converter.convert(input);

        // Verify the result
        assertNotNull(result);
        assertEquals(11, result.decoder.exchangeId());
        assertEquals(22, result.decoder.securityId());
        assertEquals(1234, result.decoder.timestampEvent());
        assertEquals(1235, result.decoder.timestampSent());
        assertEquals(1236, result.decoder.timestampRecv());
        assertEquals(99, result.decoder.price());
        assertEquals(45, result.decoder.size());
        assertEquals(Action.Trade, result.decoder.action());
        assertEquals(Side.Ask, result.decoder.side());
        assertEquals(100, result.decoder.sequence());
        assertEquals((short) 1, result.decoder.depth());
    }

    @Test
    void testTwoStepConversion_ReturnsNullWhenIntermediateConverterReturnsNull() {
        // Create a waterfall converter: MBP10 -> MBP1 -> Trades
        WaterfallConverter<MBP10Schema, TradesSchema> converter = new WaterfallConverter<>(
                new MBP10ToMBP1Converter(),
                new MBP1ToTradesConverter()
        );

        // Create input MBP10 schema with Add action (not Trade)
        // MBP1ToTradesConverter returns null for non-Trade actions
        MBP10Schema input = new MBP10Schema();
        input.encoder.exchangeId(11);
        input.encoder.securityId(22);
        input.encoder.timestampEvent(1234);
        input.encoder.timestampSent(1235);
        input.encoder.timestampRecv(1236);
        input.encoder.price(99);
        input.encoder.size(45);
        input.encoder.action(Action.Add);  // Not a Trade action
        input.encoder.side(Side.Ask);
        input.encoder.flags().marketByPrice(true);
        input.encoder.sequence(100);
        input.encoder.depth((short) 1);

        // Convert through the waterfall
        TradesSchema result = converter.convert(input);

        // Verify the result is null
        assertNull(result);
    }

    @Test
    void testThreeStepConversion_MBP10ToMBP1ToTradesToOHLCV1S() {
        // Create a waterfall converter: MBP10 -> MBP1 -> Trades -> OHLCV1S
        DummyClock clock = new DummyClock();
        WaterfallConverter<MBP10Schema, OHLCV1SSchema> converter = new WaterfallConverter<>(
                new MBP10ToMBP1Converter(),
                new MBP1ToTradesConverter(),
                new TradesToOHLCV1SConverter(clock)
        );

        // Create input MBP10 schema with Trade action
        MBP10Schema input = new MBP10Schema();
        input.encoder.exchangeId(99);
        input.encoder.securityId(1299);
        input.encoder.timestampEvent(1000);
        input.encoder.timestampSent(1001);
        input.encoder.timestampRecv(1002);
        input.encoder.price(100);
        input.encoder.size(10);
        input.encoder.action(Action.Trade);
        input.encoder.side(Side.Bid);
        input.encoder.flags().marketByPrice(true);
        input.encoder.sequence(200);
        input.encoder.depth((short) 0);

        // First conversion - should return null (initializing)
        clock.time = 0;
        OHLCV1SSchema result = converter.convert(input);
        assertNull(result);

        // Second conversion - should return null (first interval ignored)
        clock.time = 1000;
        result = converter.convert(input);
        assertNull(result);

        // Third conversion - should return null (still in same interval)
        clock.time = 1500;
        result = converter.convert(input);
        assertNull(result);

        // Fourth conversion - should return OHLCV data
        clock.time = 2000;
        result = converter.convert(input);
        assertNotNull(result);
        assertEquals(99, result.decoder.exchangeId());
        assertEquals(1299, result.decoder.securityId());
    }

    @Test
    void testClassCastException_WrongWaterfallOrder() {
        // Create a waterfall converter with wrong order: Trades -> MBP1 (impossible conversion)
        // This should cause a ClassCastException when we try to convert
        WaterfallConverter<TradesSchema, MBP1Schema> converter = new WaterfallConverter<>(
                new MBP1ToTradesConverter()  // This expects MBP1 as input, not Trades
        );

        // Create input TradesSchema
        TradesSchema input = new TradesSchema();
        input.encoder.exchangeId(11);
        input.encoder.securityId(22);
        input.encoder.price(99);
        input.encoder.size(45);
        input.encoder.action(Action.Trade);
        input.encoder.side(Side.Ask);

        // This should throw ClassCastException because MBP1ToTradesConverter
        // expects MBP1Schema but receives TradesSchema
        assertThrows(ClassCastException.class, () -> {
            converter.convert(input);
        });
    }

    @Test
    void testClassCastException_IncompatibleIntermediateConversion() {
        // Create a waterfall with incompatible converters in the middle
        // MBP10 -> MBP1 works, but then we try to use a converter that expects Trades
        WaterfallConverter<MBP10Schema, OHLCV1SSchema> converter = new WaterfallConverter<>(
                new MBP10ToMBP1Converter(),
                new TradesToOHLCV1SConverter()  // This expects Trades, but gets MBP1
        );

        // Create input MBP10 schema
        MBP10Schema input = new MBP10Schema();
        input.encoder.exchangeId(11);
        input.encoder.securityId(22);
        input.encoder.action(Action.Trade);
        input.encoder.side(Side.Ask);

        // This should throw ClassCastException because TradesToOHLCV1SConverter
        // expects TradesSchema but receives MBP1Schema
        assertThrows(ClassCastException.class, () -> {
            converter.convert(input);
        });
    }

    @Test
    void testSingleConverterInWaterfall() {
        // Test that waterfall works with just a single converter
        WaterfallConverter<MBP10Schema, MBP1Schema> converter = new WaterfallConverter<>(
                new MBP10ToMBP1Converter()
        );

        // Create input MBP10 schema
        MBP10Schema input = new MBP10Schema();
        input.encoder.exchangeId(11);
        input.encoder.securityId(22);
        input.encoder.timestampEvent(1234);
        input.encoder.price(99);
        input.encoder.size(45);
        input.encoder.action(Action.Trade);
        input.encoder.side(Side.Ask);

        // Convert
        MBP1Schema result = converter.convert(input);

        // Verify the result
        assertNotNull(result);
        assertEquals(11, result.decoder.exchangeId());
        assertEquals(22, result.decoder.securityId());
        assertEquals(1234, result.decoder.timestampEvent());
        assertEquals(99, result.decoder.price());
        assertEquals(45, result.decoder.size());
    }

}