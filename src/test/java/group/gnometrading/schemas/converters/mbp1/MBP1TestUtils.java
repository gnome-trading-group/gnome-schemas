package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.MBP1Schema;
import group.gnometrading.schemas.Side;

import java.util.Random;

public class MBP1TestUtils {

    public static final int EXCHANGE_ID = 24;
    public static final int SECURITY_ID = 50;

    public static MBP1Schema generate(long eventTimestampNanos) {
        var schema = new MBP1Schema();
        var encoder = schema.encoder;
        var random = new Random();

        encoder.exchangeId(EXCHANGE_ID);
        encoder.securityId(SECURITY_ID);

        encoder.timestampRecv(Math.abs(random.nextLong()));
        encoder.timestampSent(Math.abs(random.nextLong()));
        encoder.timestampEvent(eventTimestampNanos);

        encoder.price(Math.abs(random.nextLong()));
        encoder.size(Math.abs(random.nextLong()));
        encoder.side(random.nextBoolean() ? Side.Ask : Side.Bid);
        encoder.flags().marketByPrice(true);
        encoder.sequence(Math.abs(random.nextLong()));
        encoder.depth((short) Math.abs(random.nextInt()));

        encoder.bidPrice0(Math.abs(random.nextLong()));
        encoder.bidCount0(Math.abs(random.nextLong()));
        encoder.bidSize0(Math.abs(random.nextLong()));
        encoder.askPrice0(Math.abs(random.nextLong()));
        encoder.askCount0(Math.abs(random.nextLong()));
        encoder.askSize0(Math.abs(random.nextLong()));
        return schema;
    }
}
