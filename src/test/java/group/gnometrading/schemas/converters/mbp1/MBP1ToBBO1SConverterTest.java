package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.BBO1SSchema;
import group.gnometrading.schemas.MBP1Schema;
import group.gnometrading.schemas.converters.DummyClock;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static group.gnometrading.schemas.converters.mbp1.MBP1TestUtils.generate;
import static org.junit.jupiter.api.Assertions.*;

class MBP1ToBBO1SConverterTest {

    @Test
    void testBasicConverter() {
        var clock = new DummyClock();
        var converter = new MBP1ToBBO1SConverter(clock);

        clock.time = 1000;
        var input = generate();
        var result = converter.convert(input);
        assertNull(result);

        clock.time = 2000;
        var input1 = generate();
        result = converter.convert(input1);
        assertNull(result); // Ignore first interval

        clock.time = 2001;
        converter.convert(generate());
        clock.time = 2005;
        input = generate();
        converter.convert(input);

        clock.time = 3001;
        input1 = generate();
        result = converter.convert(input1);
        assertSchema(result, input, 2000);

        clock.time = 5000;
        result = converter.convert(generate());
        assertSchema(result, input1, 3000);
    }

    private void assertSchema(BBO1SSchema to, MBP1Schema from, long time) {
        var fromDecoder = from.decoder;
        var toDecoder = to.decoder;
        assertEquals(fromDecoder.exchangeId(), toDecoder.exchangeId());
        assertEquals(fromDecoder.securityId(), toDecoder.securityId());
        assertEquals(TimeUnit.MILLISECONDS.toNanos(time), toDecoder.timestampEvent());;
        assertEquals(fromDecoder.timestampRecv(), toDecoder.timestampRecv());
        assertEquals(fromDecoder.price(), toDecoder.price());
        assertEquals(fromDecoder.size(), toDecoder.size());
        assertEquals(fromDecoder.side(), toDecoder.side());
        assertEquals(fromDecoder.sequence(), toDecoder.sequence());

        assertEquals(fromDecoder.flags().lastMessage(), toDecoder.flags().lastMessage());
        assertEquals(fromDecoder.flags().topOfBook(), toDecoder.flags().topOfBook());
        assertEquals(fromDecoder.flags().snapshot(), toDecoder.flags().snapshot());
        assertEquals(fromDecoder.flags().marketByPrice(), toDecoder.flags().marketByPrice());
        assertEquals(fromDecoder.flags().badTimestampRecv(), toDecoder.flags().badTimestampRecv());
        assertEquals(fromDecoder.flags().maybeBadBook(), toDecoder.flags().maybeBadBook());

        assertEquals(fromDecoder.askPrice0(), toDecoder.askPrice0());
        assertEquals(fromDecoder.askCount0(), toDecoder.askCount0());
        assertEquals(fromDecoder.askSize0(), toDecoder.askSize0());
        assertEquals(fromDecoder.bidPrice0(), toDecoder.bidPrice0());
        assertEquals(fromDecoder.bidCount0(), toDecoder.bidCount0());
        assertEquals(fromDecoder.bidSize0(), toDecoder.bidSize0());
    }
}