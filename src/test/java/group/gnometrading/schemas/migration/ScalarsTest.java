package group.gnometrading.schemas.migration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.real_logic.sbe.PrimitiveType;

class ScalarsTest {

    @ParameterizedTest
    @CsvSource({
        "INT8, -128, 127, -129, 128",
        "INT16, -32768, 32767, -32769, 32768",
        "INT32, -2147483648, 2147483647, -2147483649, 2147483648",
        "UINT8, 0, 255, -1, 256",
        "CHAR, 0, 255, -1, 256",
        "UINT16, 0, 65535, -1, 65536",
        "UINT32, 0, 4294967295, -1, 4294967296",
    })
    void roundTripsItsRangeAndRefusesJustOutside(
            final PrimitiveType type, final long min, final long max, final long below, final long above) {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[8]);

        Scalars.write(buffer, 0, type, min);
        assertEquals(min, Scalars.read(buffer, 0, type));
        Scalars.write(buffer, 0, type, max);
        assertEquals(max, Scalars.read(buffer, 0, type), "unsigned values read back unsigned");
        assertThrows(IllegalStateException.class, () -> Scalars.write(buffer, 0, type, below));
        assertThrows(IllegalStateException.class, () -> Scalars.write(buffer, 0, type, above));
    }

    @ParameterizedTest
    @CsvSource({"INT64", "UINT64"})
    void sixtyFourBitTypesTakeAnyLong(final PrimitiveType type) {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[8]);

        assertDoesNotThrow(() -> Scalars.write(buffer, 0, type, Long.MIN_VALUE));
        assertEquals(Long.MIN_VALUE, Scalars.read(buffer, 0, type));
    }

    @ParameterizedTest
    @CsvSource({"FLOAT", "DOUBLE"})
    void floatingTypesAreNotIntegers(final PrimitiveType type) {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[8]);

        assertFalse(Scalars.isInteger(type));
        assertThrows(IllegalArgumentException.class, () -> Scalars.read(buffer, 0, type));
        assertThrows(IllegalArgumentException.class, () -> Scalars.writeUnchecked(buffer, 0, type, 1));
        assertThrows(IllegalStateException.class, () -> Scalars.write(buffer, 0, type, 1));
    }

    @ParameterizedTest
    @CsvSource({
        // to, from, holds
        "INT64, UINT32, true",
        "UINT64, UINT32, true",
        "UINT16, UINT8, true",
        "INT16, INT8, true",
        "INT64, INT32, true",
        "INT32, UINT32, false",
        "UINT64, INT32, false",
        "INT16, INT32, false",
        "UINT32, UINT64, false",
        "INT16, UINT8, true",
        "INT8, UINT8, false",
    })
    void canHoldKnowsSignednessAndWidth(final PrimitiveType to, final PrimitiveType from, final boolean holds) {
        assertEquals(holds, Scalars.canHold(to, from));
        assertTrue(Scalars.isInteger(to));
    }
}
