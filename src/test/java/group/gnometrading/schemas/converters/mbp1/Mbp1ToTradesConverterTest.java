package group.gnometrading.schemas.converters.mbp1;

import static org.junit.jupiter.api.Assertions.*;

import group.gnometrading.schemas.Action;
import group.gnometrading.schemas.Mbp10Encoder;
import group.gnometrading.schemas.Mbp1Schema;
import group.gnometrading.schemas.Side;
import group.gnometrading.schemas.TradesSchema;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class Mbp1ToTradesConverterTest {

    private final Mbp1ToTradesConverter converter = new Mbp1ToTradesConverter();

    private static Stream<Arguments> generateConverterArgs() {
        return Stream.of(
                Arguments.of(
                        (Supplier<Mbp1Schema>) () -> {
                            var schema = new Mbp1Schema();

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
                            schema.encoder.sequence(Mbp10Encoder.sequenceNullValue());
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
                            schema.encoder.sequence(Mbp10Encoder.sequenceNullValue());
                            schema.encoder.depth((short) 1);

                            return schema;
                        },
                        true),
                Arguments.of(
                        (Supplier<Mbp1Schema>) () -> {
                            var schema = new Mbp1Schema();

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
                            schema.encoder.sequence(Mbp10Encoder.sequenceNullValue());
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
                        true),
                Arguments.of(
                        (Supplier<Mbp1Schema>) () -> {
                            var schema = new Mbp1Schema();

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
                            schema.encoder.sequence(Mbp10Encoder.sequenceNullValue());
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
                            schema.encoder.sequence(Mbp10Encoder.sequenceNullValue());
                            schema.encoder.depth((short) 1);

                            return schema;
                        },
                        false));
    }

    @ParameterizedTest
    @MethodSource("generateConverterArgs")
    void testConverter(Supplier<Mbp1Schema> from, Supplier<TradesSchema> to, boolean equals) {
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
