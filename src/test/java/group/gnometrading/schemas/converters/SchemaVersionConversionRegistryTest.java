package group.gnometrading.schemas.converters;

import static org.junit.jupiter.api.Assertions.*;

import group.gnometrading.schemas.SchemaType;
import org.junit.jupiter.api.Test;

class SchemaVersionConversionRegistryTest {

    @Test
    void testFindReturnsEmptyWhenNothingRegistered() {
        assertTrue(SchemaVersionConversionRegistry.find(SchemaType.MBO, 99).isEmpty());
    }

    @Test
    void testFindReturnsEmptyForUnregisteredVersion() {
        SchemaVersionConversionRegistry.register(SchemaType.MBP_10, 5, data -> data);
        assertTrue(SchemaVersionConversionRegistry.find(SchemaType.MBP_10, 99).isEmpty());
    }

    @Test
    void testFindReturnsRegisteredConverter() {
        byte[] sentinel = new byte[] {1, 2, 3};
        SchemaVersionConverter converter = data -> sentinel;
        SchemaVersionConversionRegistry.register(SchemaType.TRADES, 3, converter);
        var found = SchemaVersionConversionRegistry.find(SchemaType.TRADES, 3);
        assertTrue(found.isPresent());
        assertSame(sentinel, found.get().convert(new byte[0]));
    }
}
