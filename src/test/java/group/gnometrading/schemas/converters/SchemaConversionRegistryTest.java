package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.SchemaType;
import group.gnometrading.schemas.converters.mbp10.MBP10ToMBP1Converter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchemaConversionRegistryTest {

    @Test
    void testValidConverter() {
        assertInstanceOf(MBP10ToMBP1Converter.class, SchemaConversionRegistry.getConverter(SchemaType.MBP_10, SchemaType.MBP_1));
    }

    @Test
    void testInvalidConverter() {
        assertThrows(IllegalArgumentException.class, () -> {
            SchemaConversionRegistry.getConverter(SchemaType.MBP_10, SchemaType.MBO);
        }, "Conversion from MBP_10 to MBO does not exist");
    }

}