package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.Action;
import group.gnometrading.schemas.MBP1Schema;
import group.gnometrading.schemas.Side;
import group.gnometrading.schemas.TradesSchema;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MBP1ToTradesBulkConverterTest {

    private final MBP1ToTradesBulkConverter converter = new MBP1ToTradesBulkConverter();

    @Test
    void testEmptyList() {
        List<MBP1Schema> input = new ArrayList<>();
        List<TradesSchema> result = converter.convert(input);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSingleTradeElement() {
        List<MBP1Schema> input = List.of(createMBP1Schema(11, 22, 1234, 99, 45, Action.Trade));
        List<TradesSchema> result = converter.convert(input);

        assertEquals(1, result.size());
        assertTradesSchema(result.get(0), 11, 22, 1234, 99, 45);
    }

    @Test
    void testNonTradeElementsAreFiltered() {
        // MBP1ToTradesConverter returns null for non-Trade actions
        List<MBP1Schema> input = List.of(
                createMBP1Schema(11, 22, 1000, 100, 10, Action.Add),
                createMBP1Schema(11, 22, 2000, 200, 20, Action.Cancel),
                createMBP1Schema(11, 22, 3000, 300, 30, Action.Modify)
        );
        List<TradesSchema> result = converter.convert(input);

        // All non-Trade actions should be filtered out
        assertTrue(result.isEmpty());
    }

    @Test
    void testMixedTradeAndNonTradeElements() {
        List<MBP1Schema> input = List.of(
                createMBP1Schema(11, 22, 1000, 100, 10, Action.Trade),
                createMBP1Schema(11, 22, 2000, 200, 20, Action.Add),
                createMBP1Schema(11, 22, 3000, 300, 30, Action.Trade),
                createMBP1Schema(11, 22, 4000, 400, 40, Action.Cancel),
                createMBP1Schema(11, 22, 5000, 500, 50, Action.Trade)
        );
        List<TradesSchema> result = converter.convert(input);

        // Only Trade actions should be in the result
        assertEquals(3, result.size());
        assertTradesSchema(result.get(0), 11, 22, 1000, 100, 10);
        assertTradesSchema(result.get(1), 11, 22, 3000, 300, 30);
        assertTradesSchema(result.get(2), 11, 22, 5000, 500, 50);
    }

    @Test
    void testOutputSchemasAreIndependent() {
        List<MBP1Schema> input = List.of(
                createMBP1Schema(11, 22, 1000, 100, 10, Action.Trade),
                createMBP1Schema(33, 44, 2000, 200, 20, Action.Trade)
        );
        List<TradesSchema> result = converter.convert(input);

        assertEquals(2, result.size());
        assertNotSame(result.get(0), result.get(1));
        assertNotEquals(result.get(0).decoder.exchangeId(), result.get(1).decoder.exchangeId());
    }

    private MBP1Schema createMBP1Schema(int exchangeId, int securityId, long timestampEvent, long price, long size, Action action) {
        var schema = new MBP1Schema();
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
        schema.encoder.bidPrice0(9);
        schema.encoder.bidCount0(5);
        schema.encoder.bidSize0(4);
        schema.encoder.askPrice0(10);
        schema.encoder.askCount0(1);
        schema.encoder.askSize0(6);
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

