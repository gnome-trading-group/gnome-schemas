package group.gnometrading.schemas.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import group.gnometrading.schemas.SbeMessage;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

class GatewayMessageLimitsTest {

    private static final int HEADER = SbeMessage.HEADER_LENGTH;
    private static final int GROUP_HEADER = GroupSizeEncodingEncoder.ENCODED_LENGTH;

    @Test
    void everyMessageFitsASlotWithItsGroupsFull() {
        assertFits(
                "SessionHello",
                SessionHelloEncoder.BLOCK_LENGTH
                        + GROUP_HEADER
                        + GatewayMessageLimits.MAX_HELLO_LISTINGS
                                * SessionHelloEncoder.ListingsEncoder.sbeBlockLength());
        assertFits(
                "SessionHelloOrders",
                SessionHelloOrdersEncoder.BLOCK_LENGTH
                        + GROUP_HEADER
                        + GatewayMessageLimits.HELLO_ORDERS_PER_PAGE
                                * SessionHelloOrdersEncoder.OrdersEncoder.sbeBlockLength());
        assertFits("SessionWelcome", SessionWelcomeEncoder.BLOCK_LENGTH);
        assertFits("SessionHeartbeat", SessionHeartbeatEncoder.BLOCK_LENGTH);
        assertFits(
                "GatewayHeartbeat",
                GatewayHeartbeatEncoder.BLOCK_LENGTH
                        + GROUP_HEADER
                        + GatewayMessageLimits.MAX_HEARTBEAT_ACCOUNTS
                                * GatewayHeartbeatEncoder.AccountsEncoder.sbeBlockLength());
        assertFits("SessionGoodbye", SessionGoodbyeEncoder.BLOCK_LENGTH);
        assertFits("ReHelloRequired", ReHelloRequiredEncoder.BLOCK_LENGTH);
        assertFits("OrderStatus", OrderStatusEncoder.BLOCK_LENGTH);
        assertFits("VenueQuery", VenueQueryEncoder.BLOCK_LENGTH);
        assertFits(
                "VenueQueryResult",
                VenueQueryResultEncoder.BLOCK_LENGTH
                        + GROUP_HEADER
                        + GatewayMessageLimits.QUERY_ORDERS_PER_PAGE
                                * VenueQueryResultEncoder.OrdersEncoder.sbeBlockLength());
        assertFits("AdmissionRecord", AdmissionRecordEncoder.BLOCK_LENGTH);
        assertFits("CancelEnded", CancelEndedEncoder.BLOCK_LENGTH);
        assertFits("OrderTiming", OrderTimingEncoder.BLOCK_LENGTH);
    }

    @Test
    void pagesAreAsLargeAsASlotAllows() {
        assertTrue(HEADER
                        + SessionHelloOrdersEncoder.BLOCK_LENGTH
                        + GROUP_HEADER
                        + (GatewayMessageLimits.HELLO_ORDERS_PER_PAGE + 1)
                                * SessionHelloOrdersEncoder.OrdersEncoder.sbeBlockLength()
                > GatewayMessageLimits.MAX_MESSAGE_SIZE);
        assertTrue(HEADER
                        + VenueQueryResultEncoder.BLOCK_LENGTH
                        + GROUP_HEADER
                        + (GatewayMessageLimits.QUERY_ORDERS_PER_PAGE + 1)
                                * VenueQueryResultEncoder.OrdersEncoder.sbeBlockLength()
                > GatewayMessageLimits.MAX_MESSAGE_SIZE);
    }

    @Test
    void aFullOrdersPageRoundTrips() {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[GatewayMessageLimits.MAX_MESSAGE_SIZE]);
        final SessionHelloOrdersEncoder encoder = new SessionHelloOrdersEncoder()
                .wrapAndApplyHeader(buffer, 0, new MessageHeaderEncoder())
                .sessionSeq(41)
                .page(2)
                .last((short) 1);
        final SessionHelloOrdersEncoder.OrdersEncoder orders =
                encoder.ordersCount(GatewayMessageLimits.HELLO_ORDERS_PER_PAGE);
        for (int i = 0; i < GatewayMessageLimits.HELLO_ORDERS_PER_PAGE; i++) {
            orders.next()
                    .clientOidCounter(1000 + i)
                    .exchangeId(3)
                    .securityId(77)
                    .side(Side.Bid)
                    .state(OrderState.NEW)
                    .price(45_000_000L)
                    .size(10)
                    .exchangeOrderId("0x" + "ab".repeat(32))
                    .venueOrderId("01a1a34a-f100-7e4c-bcdf-741e9c016262")
                    .cumulativeQty(i)
                    .notional(0)
                    .fees(0);
        }
        assertTrue(HEADER + encoder.encodedLength() <= GatewayMessageLimits.MAX_MESSAGE_SIZE);

        final SessionHelloOrdersDecoder decoder =
                new SessionHelloOrdersDecoder().wrapAndApplyHeader(buffer, 0, new MessageHeaderDecoder());
        assertEquals(41, decoder.sessionSeq());
        assertEquals(1, decoder.last());
        int seen = 0;
        for (final SessionHelloOrdersDecoder.OrdersDecoder order : decoder.orders()) {
            assertEquals(1000 + seen, order.clientOidCounter());
            assertEquals("0x" + "ab".repeat(32), order.exchangeOrderId());
            assertEquals("01a1a34a-f100-7e4c-bcdf-741e9c016262", order.venueOrderId());
            assertEquals(OrderState.NEW, order.state());
            seen++;
        }
        assertEquals(GatewayMessageLimits.HELLO_ORDERS_PER_PAGE, seen);
    }

    @Test
    void aFullHeartbeatRoundTrips() {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[GatewayMessageLimits.MAX_MESSAGE_SIZE]);
        final GatewayHeartbeatEncoder encoder = new GatewayHeartbeatEncoder()
                .wrapAndApplyHeader(buffer, 0, new MessageHeaderEncoder())
                .gatewayEpoch(7)
                .heartbeatSeq(9)
                .sentNanos(11)
                .state(GatewayState.DEGRADED);
        final GatewayHeartbeatEncoder.AccountsEncoder accounts =
                encoder.accountsCount(GatewayMessageLimits.MAX_HEARTBEAT_ACCOUNTS);
        for (int i = 0; i < GatewayMessageLimits.MAX_HEARTBEAT_ACCOUNTS; i++) {
            accounts.next()
                    .venueAccountId(i)
                    .state(AccountState.CATCHING_UP)
                    .budgetUseBps(9000)
                    .queued(i);
        }

        final GatewayHeartbeatDecoder decoder =
                new GatewayHeartbeatDecoder().wrapAndApplyHeader(buffer, 0, new MessageHeaderDecoder());
        assertEquals(GatewayState.DEGRADED, decoder.state());
        int seen = 0;
        for (final GatewayHeartbeatDecoder.AccountsDecoder account : decoder.accounts()) {
            assertEquals(seen, account.venueAccountId());
            assertEquals(AccountState.CATCHING_UP, account.state());
            seen++;
        }
        assertEquals(GatewayMessageLimits.MAX_HEARTBEAT_ACCOUNTS, seen);
    }

    private static void assertFits(final String message, final int body) {
        assertTrue(
                HEADER + body <= GatewayMessageLimits.MAX_MESSAGE_SIZE,
                message + " is " + (HEADER + body) + " bytes full, over " + GatewayMessageLimits.MAX_MESSAGE_SIZE);
    }
}
