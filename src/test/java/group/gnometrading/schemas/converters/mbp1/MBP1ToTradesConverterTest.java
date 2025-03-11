package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MBP1ToTradesConverterTest {

    private final MBP1ToTradesConverter converter = new MBP1ToTradesConverter();

    private static Stream<Arguments> generateConverterArgs() {
        return Stream.of(
                Arguments.of(
                        (Supplier<MBP1Schema>) () -> {
                            var schema = new MBP1Schema();

                            schema.encoder.exchangeId(11);
                            schema.encoder.securityId(22);
                            schema.encoder.timestampEvent(1234);
                            schema.encoder.timestampSent(1235);
                            schema.encoder.timestampRecv(1236);

                            schema.encoder.price(99);
                            schema.encoder.size(45);
                            schema.encoder.action(Action.Trade);
                            schema.encoder.side(Side.Ask);
                            schema.encoder.flags().marketByPrice(true);
                            schema.encoder.sequence(MBP10Encoder.sequenceNullValue());
                            schema.encoder.depth((short) 1);

                            schema.encoder.bidPrice0(9);
                            schema.encoder.bidCount0(5);
                            schema.encoder.bidSize0(4);
                            schema.encoder.askPrice0(10);
                            schema.encoder.askCount0(1);
                            schema.encoder.askSize0(6);

                            return schema;
                        },
                        (Supplier<TradesSchema>) () -> {
                            var schema = new TradesSchema();

                            schema.encoder.exchangeId(11);
                            schema.encoder.securityId(22);
                            schema.encoder.timestampEvent(1234);
                            schema.encoder.timestampSent(1235);
                            schema.encoder.timestampRecv(1236);

                            schema.encoder.price(99);
                            schema.encoder.size(45);
                            schema.encoder.action(Action.Trade);
                            schema.encoder.side(Side.Ask);
                            schema.encoder.flags().marketByPrice(true);
                            schema.encoder.sequence(MBP10Encoder.sequenceNullValue());
                            schema.encoder.depth((short) 1);

                            return schema;
                        },
                        true
                ),
                Arguments.of(
                        (Supplier<MBP1Schema>) () -> {
                            var schema = new MBP1Schema();

                            schema.encoder.exchangeId(11);
                            schema.encoder.securityId(22);
                            schema.encoder.timestampEvent(1234);
                            schema.encoder.timestampSent(1235);
                            schema.encoder.timestampRecv(1236);

                            schema.encoder.price(99);
                            schema.encoder.size(45);
                            schema.encoder.action(Action.Add);
                            schema.encoder.side(Side.Ask);
                            schema.encoder.flags().marketByPrice(true);
                            schema.encoder.sequence(MBP10Encoder.sequenceNullValue());
                            schema.encoder.depth((short) 1);

                            schema.encoder.bidPrice0(9);
                            schema.encoder.bidCount0(5);
                            schema.encoder.bidSize0(4);
                            schema.encoder.askPrice0(10);
                            schema.encoder.askCount0(1);
                            schema.encoder.askSize0(6);

                            return schema;
                        },
                        (Supplier<TradesSchema>) () -> {
                           return null;
                        },
                        true
                ),
                Arguments.of(
                        (Supplier<MBP1Schema>) () -> {
                            var schema = new MBP1Schema();

                            schema.encoder.exchangeId(11);
                            schema.encoder.securityId(22);
                            schema.encoder.timestampEvent(1234);
                            schema.encoder.timestampSent(1235);
                            schema.encoder.timestampRecv(1236);

                            schema.encoder.price(99);
                            schema.encoder.size(45);
                            schema.encoder.action(Action.Trade);
                            schema.encoder.side(Side.Ask);
                            schema.encoder.flags().marketByPrice(true);
                            schema.encoder.sequence(MBP10Encoder.sequenceNullValue());
                            schema.encoder.depth((short) 1);

                            schema.encoder.bidPrice0(9);
                            schema.encoder.bidCount0(5);
                            schema.encoder.bidSize0(4);
                            schema.encoder.askPrice0(10);
                            schema.encoder.askCount0(1);
                            schema.encoder.askSize0(6);

                            return schema;
                        },
                        (Supplier<TradesSchema>) () -> {
                            var schema = new TradesSchema();

                            schema.encoder.exchangeId(11);
                            schema.encoder.securityId(22);
                            schema.encoder.timestampEvent(1234);
                            schema.encoder.timestampSent(1235);
                            schema.encoder.timestampRecv(1236);

                            schema.encoder.price(99);
                            schema.encoder.size(45);
                            schema.encoder.action(Action.Trade);
                            schema.encoder.side(Side.Ask);
                            schema.encoder.flags().marketByPrice(false);
                            schema.encoder.sequence(MBP10Encoder.sequenceNullValue());
                            schema.encoder.depth((short) 1);

                            return schema;
                        },
                        false
                )
        );
    }


    @ParameterizedTest
    @MethodSource("generateConverterArgs")
    void testConverter(Supplier<MBP1Schema> from, Supplier<TradesSchema> to, boolean equals) {
        var result = converter.convert(from.get());

        if (result == null) {
            if (equals) {
                assertNull(to.get());
            } else {
                assertNotNull(to.get());
            }
        } else {
            if (equals) {
                assertEquals(to.get().decoder.toString(), result.decoder.toString());
            } else {
                assertNotEquals(to.get().decoder.toString(), result.decoder.toString());
            }
        }
    }

}