package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;
import group.gnometrading.schemas.SchemaType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WaterfallConverterTest {

    private static class DummySchema1 extends Schema<Integer, Boolean> {

        public DummySchema1() {
            super(SchemaType.MBO, 0, false);
        }

        @Override
        protected int getEncodedBlockLength() { return 0; }
        @Override
        protected void wrap() {}
    }

    private static class DummySchema2 extends Schema<Boolean, String> {

        public DummySchema2(boolean input) {
            super(SchemaType.MBO, input, "hey");
        }

        @Override
        protected int getEncodedBlockLength() { return 0; }
        @Override
        protected void wrap() {}
    }

    private static class DummySchema3 extends Schema<String, Long> {

        public DummySchema3(String input) {
            super(SchemaType.MBO, input, 1L);
        }

        @Override
        protected int getEncodedBlockLength() { return 0; }
        @Override
        protected void wrap() {}
    }

    private static class Converter1 implements SchemaConverter<DummySchema1, DummySchema2> {
        @Override
        public DummySchema2 convert(DummySchema1 source) {
            return new DummySchema2(source.decoder);
        }
    }

    private static class Converter2 implements SchemaConverter<DummySchema2, DummySchema3> {
        @Override
        public DummySchema3 convert(DummySchema2 source) {
            return new DummySchema3(source.decoder);
        }
    }

    @Test
    void testValidWaterfall() {
        var waterfall = new WaterfallConverter(
                new Converter1(),
                new Converter2()
        );
        var result = waterfall.convert(new DummySchema1());
        assertEquals("hey", result.encoder);
    }

    @Test
    void testInvalidWaterfall() {
        var waterfall = new WaterfallConverter(
                new Converter2(),
                new Converter1()
        );
        assertThrows(ClassCastException.class, () -> waterfall.convert(new DummySchema1()));
        assertThrows(ClassCastException.class, () -> waterfall.convert(new DummySchema2(false)));
    }

}