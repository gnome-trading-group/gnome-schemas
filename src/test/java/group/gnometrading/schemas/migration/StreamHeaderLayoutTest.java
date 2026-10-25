package group.gnometrading.schemas.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import group.gnometrading.schemas.Family;
import group.gnometrading.schemas.StreamHeader;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StreamHeaderLayoutTest {

    private static final Map<String, Integer> HEADER_OFFSETS = Map.of(
            "streamId", StreamHeader.STREAM_ID_OFFSET,
            "timestampEvent", StreamHeader.TIMESTAMP_EVENT_OFFSET,
            "timestampRecv", StreamHeader.TIMESTAMP_RECV_OFFSET,
            "sequence", StreamHeader.SEQUENCE_OFFSET,
            "flags", StreamHeader.FLAGS_OFFSET);

    @Test
    void everyStreamRecordStartsWithTheStreamHeader() {
        for (final Family family : Family.values()) {
            final SchemaLayout layout = SchemaLayout.fromResource(MigrationStep.current(family.identifier()));
            assertEquals(family.schemaId(), layout.schemaId(), family + "'s schema id");
            for (final MessageLayout message : layout.messages().values()) {
                HEADER_OFFSETS.forEach((name, offset) -> {
                    final FieldLayout field = message.fields().get(name);
                    assertNotNull(field, message.name() + " lacks " + name);
                    assertEquals(offset, field.offset(), message.name() + "." + name);
                });
            }
        }
    }

    @Test
    void everyFamilysFlagsShareTheStreamBits() {
        for (final Family family : Family.values()) {
            final SchemaLayout layout = SchemaLayout.fromResource(MigrationStep.current(family.identifier()));
            for (final MessageLayout message : layout.messages().values()) {
                final Map<String, Long> bits = message.fields().get("flags").namedValues();
                assertEquals((long) StreamHeader.BAD_DATA_BIT, bits.get("badData"), message.name());
                assertEquals((long) StreamHeader.SNAPSHOT_BIT, bits.get("snapshot"), message.name());
                assertEquals((long) StreamHeader.LAST_MESSAGE_BIT, bits.get("lastMessage"), message.name());
                assertEquals((long) StreamHeader.BAD_TIMESTAMP_RECV_BIT, bits.get("badTimestampRecv"), message.name());
            }
        }
    }
}
