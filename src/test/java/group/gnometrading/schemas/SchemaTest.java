package group.gnometrading.schemas;

import static org.junit.jupiter.api.Assertions.*;

import group.gnometrading.schemas.converters.SchemaVersionConversionRegistry;
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
    void testMigrateIfNeededNoConverterThrows() {
        Mbp1Schema schema = new Mbp1Schema();
        byte[] data = new byte[schema.totalMessageSize()];
        schema.buffer.getBytes(0, data, 0, data.length);
        data[6] = (byte) 99;
        data[7] = 0;
        assertThrows(UnsupportedOperationException.class, () -> schema.migrateIfNeeded(data));
    }

    @Test
    void testMigrateIfNeededConverterInvoked() {
        Mbp1Schema schema = new Mbp1Schema();
        byte[] original = new byte[schema.totalMessageSize()];
        schema.buffer.getBytes(0, original, 0, original.length);

        byte[] converted = new byte[schema.totalMessageSize()];
        schema.buffer.getBytes(0, converted, 0, converted.length);

        original[6] = (byte) 88;
        original[7] = 0;
        SchemaVersionConversionRegistry.register(SchemaType.MBP_1, 88, data -> converted);

        assertSame(converted, schema.migrateIfNeeded(original));
    }

    @Test
    void testMigrateIfNeededNullData() {
        Mbp1Schema schema = new Mbp1Schema();
        assertNull(schema.migrateIfNeeded(null));
    }

    @Test
    void testMigrateIfNeededShortData() {
        Mbp1Schema schema = new Mbp1Schema();
        byte[] data = new byte[4];
        assertSame(data, schema.migrateIfNeeded(data));
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
