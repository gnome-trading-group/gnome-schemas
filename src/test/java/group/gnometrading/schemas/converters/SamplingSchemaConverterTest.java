package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;
import group.gnometrading.schemas.SchemaType;
import org.agrona.MutableDirectBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SamplingSchemaConverterTest {

    private static class DummySchema extends Schema {

        int id;

        public DummySchema(int id) {
            super(SchemaType.MBO);
            this.id = id;
        }

        @Override
        protected int getEncodedBlockLength() {
            return 0;
        }

        @Override
        public void wrap(MutableDirectBuffer buffer) {}
        @Override
        public long getSequenceNumber() { return 0; }
    }

    @Test
    void testBasicSampling() {
        final int[] calls = {0, 0};
        final var clock = new DummyClock();
        final var sampler = new SamplingSchemaConverter<DummySchema, DummySchema>(clock, 1_000) {

            @Override
            protected DummySchema sample() {
                calls[1]++;
                return new DummySchema(calls[1]);
            }

            @Override
            protected void updateState(DummySchema source) {
                assertEquals(source.id, calls[0]);
                calls[0]++;
            }
        };

        clock.time = 0;
        var result = sampler.convert(new DummySchema(0));
        assertEquals(1, calls[0]);
        assertEquals(0, calls[1]);
        assertNull(result);

        clock.time = 500;
        result = sampler.convert(new DummySchema(1));
        assertEquals(2, calls[0]);
        assertEquals(0, calls[1]);
        assertNull(result);

        clock.time = 1000;
        result = sampler.convert(new DummySchema(2));
        assertEquals(3, calls[0]);
        assertEquals(1, calls[1]);
        assertNull(result); // Ignore the first, incomplete interval

        clock.time = 1001;
        result = sampler.convert(new DummySchema(3));
        assertEquals(4, calls[0]);
        assertEquals(1, calls[1]);
        assertNull(result);

        clock.time = 2001;
        result = sampler.convert(new DummySchema(4));
        assertEquals(5, calls[0]);
        assertEquals(2, calls[1]);
        assertEquals(2, result.id);
    }

    @Test
    void testMissingSamples() {
        final int[] calls = {0, 0};
        final var clock = new DummyClock();
        final var sampler = new SamplingSchemaConverter<DummySchema, DummySchema>(clock, 500) {

            DummySchema state = null;

            @Override
            protected DummySchema sample() {
                calls[1]++;
                return state;
            }

            @Override
            protected void updateState(DummySchema source) {
                assertEquals(source.id, calls[0]);
                calls[0]++;
                this.state = source;
            }
        };

        clock.time = 500;
        var result = sampler.convert(new DummySchema(0));
        assertEquals(1, calls[0]);
        assertEquals(0, calls[1]);
        assertEquals(-1, sampler.getLastSampleTimeMillis());
        assertNull(result);

        clock.time = 1000;
        result = sampler.convert(new DummySchema(1));
        assertEquals(2, calls[0]);
        assertEquals(1, calls[1]);
        assertEquals(500, sampler.getLastSampleTimeMillis());
        assertNull(result); // Ignore the first interval

        clock.time = 1499;
        result = sampler.convert(new DummySchema(2));
        assertEquals(3, calls[0]);
        assertEquals(1, calls[1]);
        assertEquals(500, sampler.getLastSampleTimeMillis());
        assertNull(result);

        clock.time = 2050;
        result = sampler.convert(new DummySchema(3));
        assertEquals(4, calls[0]);
        assertEquals(2, calls[1]);
        assertEquals(1000, sampler.getLastSampleTimeMillis());
        assertEquals(2, result.id);

        clock.time = 4050;
        result = sampler.convert(new DummySchema(4));
        assertEquals(5, calls[0]);
        assertEquals(3, calls[1]);
        assertEquals(2000, sampler.getLastSampleTimeMillis());
        assertEquals(3, result.id);
    }
}
