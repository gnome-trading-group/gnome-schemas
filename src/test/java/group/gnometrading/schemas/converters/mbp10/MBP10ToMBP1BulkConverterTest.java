package group.gnometrading.schemas.converters.mbp10;

import group.gnometrading.schemas.Action;
import group.gnometrading.schemas.MBP10Schema;
import group.gnometrading.schemas.MBP1Schema;
import group.gnometrading.schemas.Side;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MBP10ToMBP1BulkConverterTest {

    private final MBP10ToMBP1BulkConverter converter = new MBP10ToMBP1BulkConverter();

    @Test
    void testEmptyList() {
        List<MBP10Schema> input = new ArrayList<>();
        List<MBP1Schema> result = converter.convert(input);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSingleElement() {
        List<MBP10Schema> input = List.of(createMBP10Schema(11, 22, 1234, 99, 45));
        List<MBP1Schema> result = converter.convert(input);

        assertEquals(1, result.size());
        assertMBP1Schema(result.get(0), 11, 22, 1234, 99, 45);
    }

    @Test
    void testMultipleElements() {
        List<MBP10Schema> input = List.of(
                createMBP10Schema(11, 22, 1000, 100, 10),
                createMBP10Schema(11, 22, 2000, 200, 20),
                createMBP10Schema(11, 22, 3000, 300, 30)
        );
        List<MBP1Schema> result = converter.convert(input);

        assertEquals(3, result.size());
        assertMBP1Schema(result.get(0), 11, 22, 1000, 100, 10);
        assertMBP1Schema(result.get(1), 11, 22, 2000, 200, 20);
        assertMBP1Schema(result.get(2), 11, 22, 3000, 300, 30);
    }

    @Test
    void testOutputSchemasAreIndependent() {
        // Verify that each output schema is a separate instance
        List<MBP10Schema> input = List.of(
                createMBP10Schema(11, 22, 1000, 100, 10),
                createMBP10Schema(33, 44, 2000, 200, 20)
        );
        List<MBP1Schema> result = converter.convert(input);

        assertEquals(2, result.size());
        // Verify they are different instances with different data
        assertNotSame(result.get(0), result.get(1));
        assertNotEquals(result.get(0).decoder.exchangeId(), result.get(1).decoder.exchangeId());
    }

    private MBP10Schema createMBP10Schema(int exchangeId, int securityId, long timestampEvent, long price, long size) {
        var schema = new MBP10Schema();
        schema.encoder.exchangeId(exchangeId);
        schema.encoder.securityId(securityId);
        schema.encoder.timestampEvent(timestampEvent);
        schema.encoder.timestampSent(timestampEvent + 1);
        schema.encoder.timestampRecv(timestampEvent + 2);
        schema.encoder.price(price);
        schema.encoder.size(size);
        schema.encoder.action(Action.Trade);
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

    private void assertMBP1Schema(MBP1Schema schema, int exchangeId, int securityId, long timestampEvent, long price, long size) {
        assertEquals(exchangeId, schema.decoder.exchangeId());
        assertEquals(securityId, schema.decoder.securityId());
        assertEquals(timestampEvent, schema.decoder.timestampEvent());
        assertEquals(price, schema.decoder.price());
        assertEquals(size, schema.decoder.size());
    }
}

