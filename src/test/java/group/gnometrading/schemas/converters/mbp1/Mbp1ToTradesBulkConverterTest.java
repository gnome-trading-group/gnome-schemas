package group.gnometrading.schemas.converters.mbp1;

import static org.junit.jupiter.api.Assertions.*;

import group.gnometrading.schemas.Action;
import group.gnometrading.schemas.Mbp1Schema;
import group.gnometrading.schemas.Side;
import group.gnometrading.schemas.TradesSchema;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class Mbp1ToTradesBulkConverterTest {

    private static final int EXCHANGE_ID_A = 11;
    private static final int SECURITY_ID_A = 22;
    private static final int EXCHANGE_ID_B = 33;
    private static final int SECURITY_ID_B = 44;
    private static final long TIMESTAMP_A = 1234;
    private static final long PRICE_A = 99;
    private static final long SIZE_A = 45;
    private static final long TIMESTAMP_1K = 1000;
    private static final long PRICE_1 = 100;
    private static final long SIZE_1 = 10;
    private static final long TIMESTAMP_2K = 2000;
    private static final long PRICE_2 = 200;
    private static final long SIZE_2 = 20;
    private static final long TIMESTAMP_3K = 3000;
    private static final long PRICE_3 = 300;
    private static final long SIZE_3 = 30;
    private static final long TIMESTAMP_4K = 4000;
    private static final long PRICE_4 = 400;
    private static final long SIZE_4 = 40;
    private static final long TIMESTAMP_5K = 5000;
    private static final long PRICE_5 = 500;
    private static final long SIZE_5 = 50;

    private final Mbp1ToTradesBulkConverter converter = new Mbp1ToTradesBulkConverter();

    @Test
    void testEmptyList() {
        List<Mbp1Schema> input = new ArrayList<>();
        List<TradesSchema> result = converter.convert(input);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSingleTradeElement() {
        List<Mbp1Schema> input =
                List.of(createMbp1Schema(EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_A, PRICE_A, SIZE_A, Action.Trade));
        List<TradesSchema> result = converter.convert(input);

        assertEquals(1, result.size());
        assertTradesSchema(result.get(0), EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_A, PRICE_A, SIZE_A);
    }

    @Test
    void testNonTradeElementsAreFiltered() {
        // Mbp1ToTradesConverter returns null for non-Trade actions
        List<Mbp1Schema> input = List.of(
                createMbp1Schema(EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_1K, PRICE_1, SIZE_1, Action.Add),
                createMbp1Schema(EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_2K, PRICE_2, SIZE_2, Action.Cancel),
                createMbp1Schema(EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_3K, PRICE_3, SIZE_3, Action.Modify));
        List<TradesSchema> result = converter.convert(input);

        // All non-Trade actions should be filtered out
        assertTrue(result.isEmpty());
    }

    @Test
    void testMixedTradeAndNonTradeElements() {
        List<Mbp1Schema> input = List.of(
                createMbp1Schema(EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_1K, PRICE_1, SIZE_1, Action.Trade),
                createMbp1Schema(EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_2K, PRICE_2, SIZE_2, Action.Add),
                createMbp1Schema(EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_3K, PRICE_3, SIZE_3, Action.Trade),
                createMbp1Schema(EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_4K, PRICE_4, SIZE_4, Action.Cancel),
                createMbp1Schema(EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_5K, PRICE_5, SIZE_5, Action.Trade));
        List<TradesSchema> result = converter.convert(input);

        // Only Trade actions should be in the result
        assertEquals(3, result.size());
        assertTradesSchema(result.get(0), EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_1K, PRICE_1, SIZE_1);
        assertTradesSchema(result.get(1), EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_3K, PRICE_3, SIZE_3);
        assertTradesSchema(result.get(2), EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_5K, PRICE_5, SIZE_5);
    }

    @Test
    void testOutputSchemasAreIndependent() {
        List<Mbp1Schema> input = List.of(
                createMbp1Schema(EXCHANGE_ID_A, SECURITY_ID_A, TIMESTAMP_1K, PRICE_1, SIZE_1, Action.Trade),
                createMbp1Schema(EXCHANGE_ID_B, SECURITY_ID_B, TIMESTAMP_2K, PRICE_2, SIZE_2, Action.Trade));
        List<TradesSchema> result = converter.convert(input);

        assertEquals(2, result.size());
        assertNotSame(result.get(0), result.get(1));
        assertNotEquals(
                result.get(0).decoder.exchangeId(), result.get(1).decoder.exchangeId());
    }

    private Mbp1Schema createMbp1Schema(
            int exchangeId, int securityId, long timestampEvent, long price, long size, Action action) {
        var schema = new Mbp1Schema();
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

    private void assertTradesSchema(
            TradesSchema schema, int exchangeId, int securityId, long timestampEvent, long price, long size) {
        assertEquals(exchangeId, schema.decoder.exchangeId());
        assertEquals(securityId, schema.decoder.securityId());
        assertEquals(timestampEvent, schema.decoder.timestampEvent());
        assertEquals(price, schema.decoder.price());
        assertEquals(size, schema.decoder.size());
    }
}
