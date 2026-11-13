package group.gnometrading.schemas.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MigrationGroupsTest {

    private static final SchemaLayout V0 = SchemaLayout.fromResource("migration/groups-v0.xml");
    private static final SchemaLayout V1 = SchemaLayout.fromResource("migration/groups-v1.xml");

    @Test
    void aMessagesRepeatingGroupsAreKnown() {
        assertEquals(List.of("entries"), V0.message(2).groups());
        assertEquals(List.of(), V0.message(1).groups());
    }

    @Test
    void aStepOverAMessageWithARepeatingGroupFailsNamingIt() {
        final IllegalStateException failure = assertThrows(
                IllegalStateException.class, () -> MigrationStep.between(V0, V1).compile());

        assertTrue(failure.getMessage().contains("Batch"), failure.getMessage());
        assertTrue(failure.getMessage().contains("entries"), failure.getMessage());
        assertTrue(failure.getMessage().contains("dropMessage"), failure.getMessage());
    }

    @Test
    void droppingTheMessageLetsTheRestOfTheStepCompile() {
        assertEquals(
                2, MigrationStep.between(V0, V1).dropMessage("Batch").compile().size());
    }
}
