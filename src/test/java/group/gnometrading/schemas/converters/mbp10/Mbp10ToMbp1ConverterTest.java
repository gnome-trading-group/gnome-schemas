package group.gnometrading.schemas.converters.mbp10;

import static org.junit.jupiter.api.Assertions.*;

import group.gnometrading.schemas.Action;
import group.gnometrading.schemas.Mbp10Encoder;
import group.gnometrading.schemas.Mbp10Schema;
import group.gnometrading.schemas.Mbp1Schema;
import group.gnometrading.schemas.Side;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class Mbp10ToMbp1ConverterTest {

    private final Mbp10ToMbp1Converter converter = new Mbp10ToMbp1Converter();

    private static Stream<Arguments> generateConverterArgs() {
        return Stream.of(
                Arguments.of(
                        (Supplier<Mbp10Schema>) () -> {
                            var schema = new Mbp10Schema();

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

                            schema.encoder.bidPrice1(1);
                            schema.encoder.bidCount1(1);
                            schema.encoder.bidSize1(1);
                            schema.encoder.askPrice1(1);
                            schema.encoder.askCount1(1);
                            schema.encoder.askSize1(1);

                            return schema;
                        },
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
                        true),
                Arguments.of(
                        (Supplier<Mbp10Schema>) () -> {
                            var schema = new Mbp10Schema();

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

                            schema.encoder.bidPrice1(1);
                            schema.encoder.bidCount1(1);
                            schema.encoder.bidSize1(1);
                            schema.encoder.askPrice1(1);
                            schema.encoder.askCount1(1);
                            schema.encoder.askSize1(1);

                            return schema;
                        },
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
                            schema.encoder.flags().marketByPrice(false);
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
                        false));
    }

    @ParameterizedTest
    @MethodSource("generateConverterArgs")
    void testConverter(Supplier<Mbp10Schema> from, Supplier<Mbp1Schema> to, boolean equals) {
        var result = converter.convert(from.get());

        if (equals) assertEquals(to.get().decoder.toString(), result.decoder.toString());
        else assertNotEquals(to.get().decoder.toString(), result.decoder.toString());
    }
}
