package group.gnometrading.schemas.converters;

import static org.junit.jupiter.api.Assertions.*;

import group.gnometrading.schemas.RecordType;
import group.gnometrading.schemas.converters.mbp10.Mbp10ToMbp1Converter;
import org.junit.jupiter.api.Test;

class SchemaConversionRegistryTest {

    @Test
    void testValidConverter() {
        assertInstanceOf(
                Mbp10ToMbp1Converter.class, SchemaConversionRegistry.getConverter(RecordType.MBP_10, RecordType.MBP_1));
    }

    @Test
    void testInvalidConverter() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    SchemaConversionRegistry.getConverter(RecordType.MBP_10, RecordType.MBO);
                },
                "Conversion from MBP_10 to MBO does not exist");
    }
}
