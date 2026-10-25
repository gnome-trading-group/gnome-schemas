package group.gnometrading.schemas.converters;

import static org.junit.jupiter.api.Assertions.*;

import group.gnometrading.schemas.RecordType;
import group.gnometrading.schemas.SbeMessage;
import group.gnometrading.schemas.Statics;
import group.gnometrading.schemas.StreamHeader;
import group.gnometrading.schemas.StreamRecord;
import java.time.Duration;
import org.agrona.MutableDirectBuffer;
import org.junit.jupiter.api.Test;

class SamplingSchemaConverterTest {

    private static class DummySchema extends StreamRecord {

        int id;

        public DummySchema(int id, long eventTimestamp) {
            super(RecordType.MBO);
            this.id = id;
            this.buffer.putLong(
                    SbeMessage.HEADER_LENGTH + StreamHeader.TIMESTAMP_EVENT_OFFSET, eventTimestamp, Statics.BYTE_ORDER);
        }

        @Override
        protected int getEncodedBlockLength() {
            return StreamHeader.LENGTH;
        }

        @Override
        protected int getSbeVersion() {
            return 0;
        }

        @Override
        protected void wrapCodecs(MutableDirectBuffer buf) {}
    }

    @Test
    void testBasicSampling() {
        final int[] calls = {0, 0};
        final var sampler = new SamplingSchemaConverter<DummySchema, DummySchema>(Duration.ofSeconds(1)) {

            @Override
            protected DummySchema sample() {
                calls[1]++;
                return new DummySchema(calls[1], 0);
            }

            @Override
            protected void updateState(DummySchema source) {
                assertEquals(source.id, calls[0]);
                calls[0]++;
            }
        };

        var result = sampler.convert(new DummySchema(0, 0));
        assertEquals(1, calls[0]);
        assertEquals(0, calls[1]);
        assertNull(result);

        result = sampler.convert(new DummySchema(1, 500_000_000L));
        assertEquals(2, calls[0]);
        assertEquals(0, calls[1]);
        assertNull(result);

        result = sampler.convert(new DummySchema(2, 1_000_000_000L));
        assertEquals(3, calls[0]);
        assertEquals(1, calls[1]);
        assertEquals(1, result.id); // First interval now returns a result

        result = sampler.convert(new DummySchema(3, 1_001_000_000L));
        assertEquals(4, calls[0]);
        assertEquals(1, calls[1]);
        assertNull(result);

        result = sampler.convert(new DummySchema(4, 2_001_000_000L));
        assertEquals(5, calls[0]);
        assertEquals(2, calls[1]);
        assertEquals(2, result.id);
    }

    @Test
    void testFlushBeforeAnyBoundaryCrossed() {
        final var sampler = new SamplingSchemaConverter<DummySchema, DummySchema>(Duration.ofSeconds(1)) {
            @Override
            protected DummySchema sample() {
                return new DummySchema(99, getLastSampleTimeNanos());
            }

            @Override
            protected void updateState(DummySchema source) {}
        };

        // No events processed yet — flush returns null
        assertNull(sampler.flush());

        // Process one event at 500ms (no boundary crossed)
        assertNull(sampler.convert(new DummySchema(0, 500_000_000L)));
        assertEquals(-1, sampler.getLastSampleTimeNanos());

        // flush() must set lastSampleTimeNanos to bucket start (0), not leave it at -1
        DummySchema result = sampler.flush();
        assertNotNull(result);
        assertEquals(0L, result.getEventTimestamp());
        assertEquals(0L, sampler.getLastSampleTimeNanos());
    }

    @Test
    void testMissingSamples() {
        final int[] calls = {0, 0};
        final var sampler = new SamplingSchemaConverter<DummySchema, DummySchema>(Duration.ofMillis(500)) {

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

        var result = sampler.convert(new DummySchema(0, 500_000_000L));
        assertEquals(1, calls[0]);
        assertEquals(0, calls[1]);
        assertEquals(-1, sampler.getLastSampleTimeNanos());
        assertNull(result);

        result = sampler.convert(new DummySchema(1, 1_000_000_000L));
        assertEquals(2, calls[0]);
        assertEquals(1, calls[1]);
        assertEquals(500_000_000L, sampler.getLastSampleTimeNanos());
        assertEquals(0, result.id); // First interval now returns a result

        result = sampler.convert(new DummySchema(2, 1_499_000_000L));
        assertEquals(3, calls[0]);
        assertEquals(1, calls[1]);
        assertEquals(500_000_000L, sampler.getLastSampleTimeNanos());
        assertNull(result);

        result = sampler.convert(new DummySchema(3, 2_050_000_000L));
        assertEquals(4, calls[0]);
        assertEquals(2, calls[1]);
        assertEquals(1_000_000_000L, sampler.getLastSampleTimeNanos());
        assertEquals(2, result.id);

        result = sampler.convert(new DummySchema(4, 4_050_000_000L));
        assertEquals(5, calls[0]);
        assertEquals(3, calls[1]);
        assertEquals(2_000_000_000L, sampler.getLastSampleTimeNanos());
        assertEquals(3, result.id);
    }
}
