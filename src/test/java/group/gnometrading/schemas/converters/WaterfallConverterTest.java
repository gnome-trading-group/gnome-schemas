package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Action;
import group.gnometrading.schemas.Mbp10Schema;
import group.gnometrading.schemas.Mbp1Schema;
import group.gnometrading.schemas.Ohlcv1sSchema;
import group.gnometrading.schemas.Side;
import group.gnometrading.schemas.TradesSchema;
import group.gnometrading.schemas.converters.mbp10.Mbp10ToMbp1Converter;
import group.gnometrading.schemas.converters.mbp1.Mbp1ToTradesConverter;
import group.gnometrading.schemas.converters.trades.TradesToOhlcv1sConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WaterfallConverterTest {

    @Test
    void testTwoStepConversion_MBP10ToMBP1ToTrades() {
        // Create a waterfall converter: MBP10 -> MBP1 -> Trades
        WaterfallConverter<Mbp10Schema, TradesSchema> converter = new WaterfallConverter<>(
                new Mbp10ToMbp1Converter(),
                new Mbp1ToTradesConverter()
        );

        // Create input MBP10 schema with Trade action
        Mbp10Schema input = new Mbp10Schema();
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
        WaterfallConverter<Mbp10Schema, TradesSchema> converter = new WaterfallConverter<>(
                new Mbp10ToMbp1Converter(),
                new Mbp1ToTradesConverter()
        );

        // Create input MBP10 schema with Add action (not Trade)
        // Mbp1ToTradesConverter returns null for non-Trade actions
        Mbp10Schema input = new Mbp10Schema();
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
    void testThreeStepConversion_MBP10ToMBP1ToTradesToOhlcv1s() {
        // Create a waterfall converter: MBP10 -> MBP1 -> Trades -> Ohlcv1s
        WaterfallConverter<Mbp10Schema, Ohlcv1sSchema> converter = new WaterfallConverter<>(
                new Mbp10ToMbp1Converter(),
                new Mbp1ToTradesConverter(),
                new TradesToOhlcv1sConverter()
        );

        // Create input MBP10 schema with Trade action
        Mbp10Schema input = new Mbp10Schema();
        input.encoder.exchangeId(99);
        input.encoder.securityId(1299);
        input.encoder.timestampEvent(0);
        input.encoder.timestampSent(1);
        input.encoder.timestampRecv(2);
        input.encoder.price(100);
        input.encoder.size(10);
        input.encoder.action(Action.Trade);
        input.encoder.side(Side.Bid);
        input.encoder.flags().marketByPrice(true);
        input.encoder.sequence(200);
        input.encoder.depth((short) 0);

        // First conversion - should return null (initializing)
        Ohlcv1sSchema result = converter.convert(input);
        assertNull(result);

        // Second conversion - should return OHLCV data (first interval)
        input.encoder.timestampEvent(1_000_000_000L);
        result = converter.convert(input);
        assertNotNull(result);
        assertEquals(99, result.decoder.exchangeId());
        assertEquals(1299, result.decoder.securityId());

        // Third conversion - should return null (still in same interval)
        input.encoder.timestampEvent(1_500_000_000L);
        result = converter.convert(input);
        assertNull(result);

        // Fourth conversion - should return OHLCV data
        input.encoder.timestampEvent(2_000_000_000L);
        result = converter.convert(input);
        assertNotNull(result);
        assertEquals(99, result.decoder.exchangeId());
        assertEquals(1299, result.decoder.securityId());
    }

    @Test
    void testClassCastException_WrongWaterfallOrder() {
        // Create a waterfall converter with wrong order: Trades -> MBP1 (impossible conversion)
        // This should cause a ClassCastException when we try to convert
        WaterfallConverter<TradesSchema, Mbp1Schema> converter = new WaterfallConverter<>(
                new Mbp1ToTradesConverter()  // This expects MBP1 as input, not Trades
        );

        // Create input TradesSchema
        TradesSchema input = new TradesSchema();
        input.encoder.exchangeId(11);
        input.encoder.securityId(22);
        input.encoder.price(99);
        input.encoder.size(45);
        input.encoder.action(Action.Trade);
        input.encoder.side(Side.Ask);

        // This should throw ClassCastException because Mbp1ToTradesConverter
        // expects Mbp1Schema but receives TradesSchema
        assertThrows(ClassCastException.class, () -> {
            converter.convert(input);
        });
    }

    @Test
    void testClassCastException_IncompatibleIntermediateConversion() {
        // Create a waterfall with incompatible converters in the middle
        // MBP10 -> MBP1 works, but then we try to use a converter that expects Trades
        WaterfallConverter<Mbp10Schema, Ohlcv1sSchema> converter = new WaterfallConverter<>(
                new Mbp10ToMbp1Converter(),
                new TradesToOhlcv1sConverter()  // This expects Trades, but gets MBP1
        );

        // Create input MBP10 schema
        Mbp10Schema input = new Mbp10Schema();
        input.encoder.exchangeId(11);
        input.encoder.securityId(22);
        input.encoder.action(Action.Trade);
        input.encoder.side(Side.Ask);

        // This should throw ClassCastException because TradesToOhlcv1sConverter
        // expects TradesSchema but receives Mbp1Schema
        assertThrows(ClassCastException.class, () -> {
            converter.convert(input);
        });
    }

    @Test
    void testSingleConverterInWaterfall() {
        // Test that waterfall works with just a single converter
        WaterfallConverter<Mbp10Schema, Mbp1Schema> converter = new WaterfallConverter<>(
                new Mbp10ToMbp1Converter()
        );

        // Create input MBP10 schema
        Mbp10Schema input = new Mbp10Schema();
        input.encoder.exchangeId(11);
        input.encoder.securityId(22);
        input.encoder.timestampEvent(1234);
        input.encoder.price(99);
        input.encoder.size(45);
        input.encoder.action(Action.Trade);
        input.encoder.side(Side.Ask);

        // Convert
        Mbp1Schema result = converter.convert(input);

        // Verify the result
        assertNotNull(result);
        assertEquals(11, result.decoder.exchangeId());
        assertEquals(22, result.decoder.securityId());
        assertEquals(1234, result.decoder.timestampEvent());
        assertEquals(99, result.decoder.price());
        assertEquals(45, result.decoder.size());
    }

}
