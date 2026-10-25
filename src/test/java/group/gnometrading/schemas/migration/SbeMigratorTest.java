package group.gnometrading.schemas.migration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import group.gnometrading.schemas.CancelOrderDecoder;
import group.gnometrading.schemas.ExecType;
import group.gnometrading.schemas.IntentDecoder;
import group.gnometrading.schemas.IntentEncoder;
import group.gnometrading.schemas.MessageHeaderDecoder;
import group.gnometrading.schemas.MessageHeaderEncoder;
import group.gnometrading.schemas.MetricRowDecoder;
import group.gnometrading.schemas.OrderExecutionReportDecoder;
import group.gnometrading.schemas.SbeMessage;
import group.gnometrading.schemas.book.Mbp10Decoder;
import group.gnometrading.schemas.book.TradesDecoder;
import group.gnometrading.schemas.book.TradesEncoder;
import java.util.Arrays;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

class SbeMigratorTest {

    // Above 2^31: an unsigned uint32 read as a signed int would come out negative.
    private static final long LARGE_SIZE = 4_000_000_000L;
    private static final int HEADER = SbeMessage.HEADER_LENGTH;
    private static final int CORE = MessageHeaderDecoder.SCHEMA_ID;
    private static final int BOOK = group.gnometrading.schemas.book.MessageHeaderDecoder.SCHEMA_ID;

    @Test
    void everyTemplateHasAStepFromEveryArchivedVersionOfItsSchema() {
        for (final String schema : SbeMigrator.SCHEMAS) {
            final SchemaLayout current = SchemaLayout.fromResource(MigrationStep.current(schema));
            for (int version = 0; version < current.version(); version++) {
                final SchemaLayout archived = SchemaLayout.fromResource(MigrationStep.archived(schema, version));
                for (final int templateId : archived.messages().keySet()) {
                    assertTrue(
                            SbeMigrator.hasStep(current.schemaId(), templateId, version),
                            schema + " template " + templateId + " v" + version);
                }
            }
        }
    }

    @Test
    void eachSchemaMatchesItsArchivedVersion() {
        for (final String schema : SbeMigrator.SCHEMAS) {
            final SchemaLayout current = SchemaLayout.fromResource(MigrationStep.current(schema));
            final SchemaLayout archived = SchemaLayout.fromResource(MigrationStep.archived(schema, current.version()));
            assertEquals(
                    archived.messages(),
                    current.messages(),
                    schema + ".xml changed layout without a version bump. Bump its version to "
                            + (current.version() + 1) + ", archive it as "
                            + MigrationStep.archived(schema, current.version() + 1)
                            + ", and register MigrationStep.from(\"" + schema + "\", " + current.version() + ")");
            assertEquals(archived.schemaId(), current.schemaId());
        }
    }

    @Test
    void eachFamilyKeepsItsTemplatesInItsOwnHundred() {
        final SchemaLayout core = SchemaLayout.fromResource(MigrationStep.current(SbeMigrator.CORE));
        final SchemaLayout book = SchemaLayout.fromResource(MigrationStep.current(SbeMigrator.BOOK));
        core.messages().keySet().forEach(id -> assertTrue(id > 0 && id < 100, "core template " + id));
        book.messages().keySet().forEach(id -> assertTrue(id >= 100 && id < 200, "book template " + id));
    }

    @Test
    void storedV0AndV1BookRecordsAreDropped() {
        final byte[] v0 = new byte[HEADER + group.gnometrading.schemas.v0.Mbp10Encoder.BLOCK_LENGTH];
        new group.gnometrading.schemas.v0.Mbp10Encoder()
                .wrapAndApplyHeader(new UnsafeBuffer(v0), 0, new group.gnometrading.schemas.v0.MessageHeaderEncoder())
                .exchangeId(7)
                .securityId(42);

        assertEquals(0, SbeMigrator.migrateStream(v0).length);
        assertEquals(0, SbeMigrator.migrateStream(v1Trade(1)).length);
    }

    @Test
    void orderManagementMessagesMigrateFromV0() {
        final IntentDecoder intent = new IntentDecoder()
                .wrapAndApplyHeader(
                        new UnsafeBuffer(SbeMigrator.migrateStream(v0Intent(LARGE_SIZE))),
                        0,
                        new MessageHeaderDecoder());
        final OrderExecutionReportDecoder report = new OrderExecutionReportDecoder()
                .wrapAndApplyHeader(
                        new UnsafeBuffer(SbeMigrator.migrateStream(v0Report())), 0, new MessageHeaderDecoder());

        assertEquals(SbeMigrator.currentVersion(CORE), intent.sbeSchemaVersion());
        assertEquals(LARGE_SIZE, intent.bidSize());
        assertEquals(IntentDecoder.askSizeNullValue(), intent.askSize());
        assertEquals(123_000_000L, intent.bidPrice());
        assertEquals(5_000_000L, report.filledQty());
        assertEquals(LARGE_SIZE, report.cumulativeQty());
        assertEquals(OrderExecutionReportDecoder.leavesQtyNullValue(), report.leavesQty());
        assertEquals(ExecType.PARTIAL_FILL, report.execType());
        assertEquals("", report.exchangeOrderId(), "v0 reports never carried the venue's order ID");
    }

    @Test
    void unchangedLayoutsOnlyGetANewVersion() {
        final byte[] v0 = new byte[HEADER + group.gnometrading.schemas.v0.CancelOrderEncoder.BLOCK_LENGTH];
        new group.gnometrading.schemas.v0.CancelOrderEncoder()
                .wrapAndApplyHeader(new UnsafeBuffer(v0), 0, new group.gnometrading.schemas.v0.MessageHeaderEncoder())
                .exchangeId(3)
                .securityId(9);

        final byte[] migrated = SbeMigrator.migrateStream(v0);

        assertEquals(v0.length, migrated.length);
        assertEquals(SbeMigrator.currentVersion(CORE), migrated[6]);
        assertArrayEquals(
                Arrays.copyOfRange(v0, HEADER, v0.length), Arrays.copyOfRange(migrated, HEADER, migrated.length));
        assertEquals(
                9,
                new CancelOrderDecoder()
                        .wrapAndApplyHeader(new UnsafeBuffer(migrated), 0, new MessageHeaderDecoder())
                        .securityId());
    }

    @Test
    void metricRowsKeepTheirValuesAcrossVersions() {
        final byte[] values = {1, 2, 3, 4, 5};
        final byte[] blob = concat(v1MetricRow(4, values), v1MetricRow(5, new byte[0]), currentIntent(9));

        final byte[] migrated = SbeMigrator.migrateStream(blob);

        final UnsafeBuffer buffer = new UnsafeBuffer(migrated);
        final MetricRowDecoder row = new MetricRowDecoder().wrapAndApplyHeader(buffer, 0, new MessageHeaderDecoder());
        assertEquals(SbeMigrator.currentVersion(CORE), row.sbeSchemaVersion());
        assertEquals(4, row.streamId());
        final byte[] read = new byte[row.valuesLength()];
        row.getValues(read, 0, read.length);
        assertArrayEquals(values, read);
        final int second = HEADER + MetricRowDecoder.BLOCK_LENGTH + 2 + values.length;
        assertEquals(
                5,
                new MetricRowDecoder()
                        .wrapAndApplyHeader(buffer, second, new MessageHeaderDecoder())
                        .streamId());
        final int third = second + HEADER + MetricRowDecoder.BLOCK_LENGTH + 2;
        assertEquals(
                9,
                new IntentDecoder()
                        .wrapAndApplyHeader(buffer, third, new MessageHeaderDecoder())
                        .strategyId());
        assertEquals(third + HEADER + IntentDecoder.BLOCK_LENGTH, migrated.length);
    }

    @Test
    void currentDataOfEverySchemaIsReturnedWithoutCopying() {
        final byte[] mixed = concat(currentTrade(5, 1_000_000L), currentIntent(1), currentTrade(6, 2_000_000L));

        assertSame(mixed, SbeMigrator.migrateStream(mixed));
    }

    @Test
    void mixedVersionsConvertOnlyTheOldMessagesAndKeepOrder() {
        final byte[] blob = concat(currentIntent(1), v0Intent(LARGE_SIZE), currentTrade(3, 3_000_000L));

        final byte[] migrated = SbeMigrator.migrateStream(blob);

        final UnsafeBuffer buffer = new UnsafeBuffer(migrated);
        final int intentLength = HEADER + IntentDecoder.BLOCK_LENGTH;
        assertEquals(2 * intentLength + HEADER + TradesDecoder.BLOCK_LENGTH, migrated.length);
        assertEquals(
                1,
                new IntentDecoder()
                        .wrapAndApplyHeader(buffer, 0, new MessageHeaderDecoder())
                        .strategyId());
        assertEquals(
                LARGE_SIZE,
                new IntentDecoder()
                        .wrapAndApplyHeader(buffer, intentLength, new MessageHeaderDecoder())
                        .bidSize());
        assertEquals(
                3,
                new TradesDecoder()
                        .wrapAndApplyHeader(
                                buffer, 2 * intentLength, new group.gnometrading.schemas.book.MessageHeaderDecoder())
                        .sequence());
    }

    @Test
    void singleMessageMigrationForJournals() {
        final UnsafeBuffer src = new UnsafeBuffer(v0Intent(7_000_000L));
        final ExpandableArrayBuffer dst = new ExpandableArrayBuffer();

        assertTrue(SbeMigrator.needsMigration(src, 0));
        final int length = SbeMigrator.migrateMessage(src, 0, dst, 0);

        assertEquals(HEADER + IntentDecoder.BLOCK_LENGTH, length);
        assertEquals(
                7_000_000L,
                new IntentDecoder()
                        .wrapAndApplyHeader(dst, 0, new MessageHeaderDecoder())
                        .bidSize());
    }

    @Test
    void malformedDataIsRejected() {
        final byte[] trade = currentTrade(1, 1L);
        assertThrows(
                IllegalStateException.class, () -> SbeMigrator.migrateStream(Arrays.copyOf(trade, trade.length - 1)));
        assertThrows(IllegalStateException.class, () -> SbeMigrator.migrateStream(Arrays.copyOf(trade, 5)));

        final byte[] otherSchema = trade.clone();
        otherSchema[4] = 77;
        assertThrows(IllegalStateException.class, () -> SbeMigrator.migrateStream(otherSchema));

        final byte[] unknownVersion = v0Intent(1L);
        unknownVersion[6] = 99;
        assertThrows(IllegalStateException.class, () -> SbeMigrator.migrateStream(unknownVersion));
    }

    @Test
    void archivedSchemasParseToTheSameLayoutAsTheGeneratedCodecs() {
        final SchemaLayout coreV0 = SchemaLayout.fromResource(MigrationStep.archived(SbeMigrator.CORE, 0));
        final SchemaLayout book = SchemaLayout.fromResource(MigrationStep.current(SbeMigrator.BOOK));

        assertEquals(
                group.gnometrading.schemas.v0.IntentEncoder.BLOCK_LENGTH,
                coreV0.message(IntentDecoder.TEMPLATE_ID).blockLength());
        assertEquals(
                Mbp10Decoder.BLOCK_LENGTH,
                book.message(Mbp10Decoder.TEMPLATE_ID).blockLength());
        assertEquals(
                Mbp10Decoder.bidSize0EncodingOffset(),
                book.message(Mbp10Decoder.TEMPLATE_ID).fields().get("bidSize0").offset());
        assertEquals(BOOK, book.schemaId());
        assertEquals(SbeMigrator.currentVersion(BOOK), book.version());
    }

    @Test
    void messageWithALayoutItsVersionNeverHadIsRefused() {
        final byte[] unrecorded = v0Intent(1L);
        unrecorded[0] = (byte) (group.gnometrading.schemas.v0.IntentEncoder.BLOCK_LENGTH - 4);
        final byte[] truncated = Arrays.copyOf(unrecorded, unrecorded.length - 4);

        final IllegalStateException failure =
                assertThrows(IllegalStateException.class, () -> SbeMigrator.migrateStream(truncated));
        assertTrue(failure.getMessage().contains("no archived schema records"), failure.getMessage());
    }

    @Test
    void emptyAndNullPassThrough() {
        assertNull(SbeMigrator.migrateStream(null));
        final byte[] empty = new byte[0];
        assertSame(empty, SbeMigrator.migrateStream(empty));
    }

    private static byte[] v1Trade(final long sequence) {
        final byte[] bytes = new byte[HEADER + group.gnometrading.schemas.v1.TradesEncoder.BLOCK_LENGTH];
        new group.gnometrading.schemas.v1.TradesEncoder()
                .wrapAndApplyHeader(
                        new UnsafeBuffer(bytes), 0, new group.gnometrading.schemas.v1.MessageHeaderEncoder())
                .sequence(sequence);
        return bytes;
    }

    private static byte[] currentTrade(final long sequence, final long size) {
        final byte[] bytes = new byte[HEADER + TradesDecoder.BLOCK_LENGTH];
        new TradesEncoder()
                .wrapAndApplyHeader(
                        new UnsafeBuffer(bytes), 0, new group.gnometrading.schemas.book.MessageHeaderEncoder())
                .sequence(sequence)
                .size(size)
                .price(500_000_000L);
        return bytes;
    }

    private static byte[] currentIntent(final int strategyId) {
        final byte[] bytes = new byte[HEADER + IntentDecoder.BLOCK_LENGTH];
        new IntentEncoder()
                .wrapAndApplyHeader(new UnsafeBuffer(bytes), 0, new MessageHeaderEncoder())
                .strategyId(strategyId);
        return bytes;
    }

    private static byte[] v1MetricRow(final int streamId, final byte[] values) {
        final byte[] bytes =
                new byte[HEADER + group.gnometrading.schemas.v1.MetricRowEncoder.BLOCK_LENGTH + 2 + values.length];
        new group.gnometrading.schemas.v1.MetricRowEncoder()
                .wrapAndApplyHeader(
                        new UnsafeBuffer(bytes), 0, new group.gnometrading.schemas.v1.MessageHeaderEncoder())
                .streamId(streamId)
                .putValues(values, 0, values.length);
        return bytes;
    }

    private static byte[] v0Intent(final long bidSize) {
        final byte[] bytes = new byte[HEADER + group.gnometrading.schemas.v0.IntentEncoder.BLOCK_LENGTH];
        new group.gnometrading.schemas.v0.IntentEncoder()
                .wrapAndApplyHeader(
                        new UnsafeBuffer(bytes), 0, new group.gnometrading.schemas.v0.MessageHeaderEncoder())
                .bidPrice(123_000_000L)
                .bidSize(bidSize)
                .askSize(group.gnometrading.schemas.v0.IntentEncoder.askSizeNullValue());
        return bytes;
    }

    private static byte[] v0Report() {
        final byte[] bytes = new byte[HEADER + group.gnometrading.schemas.v0.OrderExecutionReportEncoder.BLOCK_LENGTH];
        new group.gnometrading.schemas.v0.OrderExecutionReportEncoder()
                .wrapAndApplyHeader(
                        new UnsafeBuffer(bytes), 0, new group.gnometrading.schemas.v0.MessageHeaderEncoder())
                .execType(group.gnometrading.schemas.v0.ExecType.PARTIAL_FILL)
                .filledQty(5_000_000L)
                .cumulativeQty(LARGE_SIZE)
                .leavesQty(group.gnometrading.schemas.v0.OrderExecutionReportEncoder.leavesQtyNullValue());
        return bytes;
    }

    private static byte[] concat(final byte[]... parts) {
        int total = 0;
        for (final byte[] part : parts) {
            total += part.length;
        }
        final byte[] out = new byte[total];
        int offset = 0;
        for (final byte[] part : parts) {
            System.arraycopy(part, 0, out, offset, part.length);
            offset += part.length;
        }
        return out;
    }
}
