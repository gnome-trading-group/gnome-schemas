package group.gnometrading.schemas;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SchemaTest {

    @Test
    void testMigrateIfNeededVersionMatch() {
        Mbp1Schema schema = new Mbp1Schema();
        byte[] data = new byte[schema.totalMessageSize()];
        schema.buffer.getBytes(0, data, 0, data.length);
        assertSame(data, schema.migrateIfNeeded(data));
    }

    @Test
    void testMigrateIfNeededNullData() {
        Mbp1Schema schema = new Mbp1Schema();
        assertNull(schema.migrateIfNeeded(null));
    }

    @Test
    void testMigrateIfNeededTruncatedDataIsRejected() {
        Mbp1Schema schema = new Mbp1Schema();
        byte[] data = new byte[4];
        assertThrows(IllegalStateException.class, () -> schema.migrateIfNeeded(data));
    }

    @Test
    void testBasicCopy() {
        Ohlcv1sSchema schema = new Ohlcv1sSchema();

        schema.encoder.open(100);
        schema.encoder.high(20);
        schema.encoder.low(40);
        schema.encoder.close(60);
        schema.encoder.volume(50);

        Ohlcv1sSchema other = new Ohlcv1sSchema();
        other.copyFrom(schema);

        assertEquals(100, other.decoder.open());
        assertEquals(20, other.decoder.high());
        assertEquals(40, other.decoder.low());
        assertEquals(60, other.decoder.close());
        assertEquals(50, other.decoder.volume());
    }
}
