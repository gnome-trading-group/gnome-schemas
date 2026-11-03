package group.gnometrading.schemas.gateway;

/**
 * Caps on the repeating groups of the gateway protocol. Every gateway message, its groups full to these caps, fits a
 * 1 KB ring slot (gnome-sequencer's {@code SequencedEvent.MAX_MESSAGE_SIZE}), which is also under one Aeron frame, so
 * none is ever fragmented or refused by a ring. Senders split anything longer into pages.
 */
public final class GatewayMessageLimits {

    /** A ring slot's capacity, header included. */
    public static final int MAX_MESSAGE_SIZE = 1024;

    /** Open orders per {@code SessionHelloOrders} page. */
    public static final int HELLO_ORDERS_PER_PAGE = 6;

    /** Orders per {@code VenueQueryResult} page. */
    public static final int QUERY_ORDERS_PER_PAGE = 6;

    /** Listings a {@code SessionHello} can name. */
    public static final int MAX_HELLO_LISTINGS = 128;

    /** Accounts a {@code GatewayHeartbeat} can report: one gateway's accounts on its venue. */
    public static final int MAX_HEARTBEAT_ACCOUNTS = 64;

    private GatewayMessageLimits() {}
}
