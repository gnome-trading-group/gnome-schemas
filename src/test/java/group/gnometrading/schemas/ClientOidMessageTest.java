package group.gnometrading.schemas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClientOidMessageTest {

    @Test
    void testCounterRoundTrip() {
        Order order = new Order();
        order.encodeClientOid(12345L, 0);
        assertEquals(12345L, order.getClientOidCounter());
    }

    @Test
    void testStrategyIdRoundTrip() {
        Order order = new Order();
        order.encodeClientOid(1L, 42);
        assertEquals(42, order.getClientOidStrategyId());
    }

    @Test
    void testEncodingOverwritesPreviousValues() {
        Order order = new Order();
        order.encodeClientOid(999L, 7);
        order.encodeClientOid(1L, 1);
        assertEquals(1L, order.getClientOidCounter());
        assertEquals(1, order.getClientOidStrategyId());
    }

    @Test
    void testZeroValues() {
        Order order = new Order();
        order.encodeClientOid(0L, 0);
        assertEquals(0L, order.getClientOidCounter());
        assertEquals(0, order.getClientOidStrategyId());
    }

    @Test
    void testLargeCounter() {
        Order order = new Order();
        long counter = Long.MAX_VALUE;
        order.encodeClientOid(counter, 0);
        assertEquals(counter, order.getClientOidCounter());
    }

    @Test
    void testMaxStrategyId() {
        Order order = new Order();
        int strategyId = 0xFFFF;
        order.encodeClientOid(1L, strategyId);
        assertEquals(strategyId, order.getClientOidStrategyId());
    }

    @Test
    void testAllMessageTypesHaveSameOffset() {
        assertEquals(OrderDecoder.clientOidEncodingOffset(), CancelOrderDecoder.clientOidEncodingOffset());
        assertEquals(OrderDecoder.clientOidEncodingOffset(), ModifyOrderDecoder.clientOidEncodingOffset());
        assertEquals(OrderDecoder.clientOidEncodingOffset(), OrderExecutionReportDecoder.clientOidEncodingOffset());
    }

    @Test
    void testRoundTripOnAllMessageTypes() {
        long counter = 77L;
        int strategyId = 3;

        CancelOrder cancel = new CancelOrder();
        cancel.encodeClientOid(counter, strategyId);
        assertEquals(counter, cancel.getClientOidCounter());
        assertEquals(strategyId, cancel.getClientOidStrategyId());

        ModifyOrder modify = new ModifyOrder();
        modify.encodeClientOid(counter, strategyId);
        assertEquals(counter, modify.getClientOidCounter());
        assertEquals(strategyId, modify.getClientOidStrategyId());

        OrderExecutionReport report = new OrderExecutionReport();
        report.encodeClientOid(counter, strategyId);
        assertEquals(counter, report.getClientOidCounter());
        assertEquals(strategyId, report.getClientOidStrategyId());
    }
}
