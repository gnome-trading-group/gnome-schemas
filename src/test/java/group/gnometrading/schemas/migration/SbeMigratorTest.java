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
import group.gnometrading.schemas.Mbp10Decoder;
import group.gnometrading.schemas.MessageHeaderDecoder;
import group.gnometrading.schemas.MessageHeaderEncoder;
import group.gnometrading.schemas.OrderExecutionReportDecoder;
import group.gnometrading.schemas.TradesDecoder;
import group.gnometrading.schemas.TradesEncoder;
import java.util.Arrays;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

class SbeMigratorTest {

    // Above 2^31: an unsigned uint32 read as a signed int would come out negative.
    private static final long LARGE_SIZE = 4_000_000_000L;
    private static final int HEADER = MessageHeaderDecoder.ENCODED_LENGTH;
    private static final int TEMPLATE_COUNT = 14;

    @Test
    void everyTemplateHasAStepFromEveryArchivedVersion() {
        for (int templateId = 1; templateId <= TEMPLATE_COUNT; templateId++) {
            for (int version = 0; version < SbeMigrator.CURRENT_VERSION; version++) {
                assertTrue(SbeMigrator.hasStep(templateId, version), "template " + templateId + " v" + version);
            }
        }
    }

    @Test
    void mbp10FromV0KeepsEveryFieldAndWidensSizes() {
        final byte[] v0 = new byte[HEADER + group.gnometrading.schemas.v0.Mbp10Encoder.BLOCK_LENGTH];
        final var encoder = new group.gnometrading.schemas.v0.Mbp10Encoder()
                .wrapAndApplyHeader(new UnsafeBuffer(v0), 0, new group.gnometrading.schemas.v0.MessageHeaderEncoder());
        encoder.exchangeId(7)
                .securityId(42)
                .timestampEvent(111)
                .timestampRecv(222)
                .sequence(99);
        encoder.price(550_000_000L).size(LARGE_SIZE);
        encoder.bidPrice0(540_000_000L).bidSize0(11_971L).askPrice0(560_000_000L);
        encoder.askSize0(group.gnometrading.schemas.v0.Mbp10Encoder.askSize0NullValue());
        encoder.bidCount0(3);

        final byte[] migrated = SbeMigrator.migrateStream(v0);

        final Mbp10Decoder decoder =
                new Mbp10Decoder().wrapAndApplyHeader(new UnsafeBuffer(migrated), 0, new MessageHeaderDecoder());
        assertEquals(HEADER + Mbp10Decoder.BLOCK_LENGTH, migrated.length);
        assertEquals(SbeMigrator.CURRENT_VERSION, decoder.sbeSchemaVersion());
        assertEquals(7, decoder.exchangeId());
        assertEquals(42, decoder.securityId());
        assertEquals(111, decoder.timestampEvent());
        assertEquals(222, decoder.timestampRecv());
        assertEquals(99, decoder.sequence());
        assertEquals(550_000_000L, decoder.price());
        assertEquals(LARGE_SIZE, decoder.size());
        assertEquals(540_000_000L, decoder.bidPrice0());
        assertEquals(11_971L, decoder.bidSize0());
        assertEquals(560_000_000L, decoder.askPrice0());
        assertEquals(Mbp10Decoder.askSize0NullValue(), decoder.askSize0(), "null stays null");
        assertEquals(3, decoder.bidCount0());
    }

    @Test
    void orderManagementMessagesMigrateToo() {
        final IntentDecoder intent = new IntentDecoder()
                .wrapAndApplyHeader(
                        new UnsafeBuffer(SbeMigrator.migrateStream(v0Intent(LARGE_SIZE))),
                        0,
                        new MessageHeaderDecoder());
        final OrderExecutionReportDecoder report = new OrderExecutionReportDecoder()
                .wrapAndApplyHeader(
                        new UnsafeBuffer(SbeMigrator.migrateStream(v0Report())), 0, new MessageHeaderDecoder());

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
        assertEquals(SbeMigrator.CURRENT_VERSION, migrated[6]);
        assertArrayEquals(
                Arrays.copyOfRange(v0, HEADER, v0.length), Arrays.copyOfRange(migrated, HEADER, migrated.length));
        assertEquals(
                9,
                new CancelOrderDecoder()
                        .wrapAndApplyHeader(new UnsafeBuffer(migrated), 0, new MessageHeaderDecoder())
                        .securityId());
    }

    @Test
    void currentDataIsReturnedWithoutCopying() {
        final byte[] two = concat(currentTrade(5, 1_000_000L), currentTrade(6, 2_000_000L));

        assertSame(two, SbeMigrator.migrateStream(two));
    }

    @Test
    void mixedVersionsConvertOnlyTheOldMessagesAndKeepOrder() {
        final byte[] blob = concat(currentTrade(1, 1_000_000L), v0Trade(2, LARGE_SIZE), currentTrade(3, 3_000_000L));

        final byte[] migrated = SbeMigrator.migrateStream(blob);

        final int length = HEADER + TradesDecoder.BLOCK_LENGTH;
        assertEquals(3 * length, migrated.length);
        final UnsafeBuffer buffer = new UnsafeBuffer(migrated);
        final TradesDecoder decoder = new TradesDecoder();
        final long[] sequences = new long[3];
        final long[] sizes = new long[3];
        for (int i = 0; i < 3; i++) {
            decoder.wrapAndApplyHeader(buffer, i * length, new MessageHeaderDecoder());
            sequences[i] = decoder.sequence();
            sizes[i] = decoder.size();
        }
        assertArrayEquals(new long[] {1, 2, 3}, sequences);
        assertArrayEquals(new long[] {1_000_000L, LARGE_SIZE, 3_000_000L}, sizes);
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
        otherSchema[4] = 2;
        assertThrows(IllegalStateException.class, () -> SbeMigrator.migrateStream(otherSchema));

        final byte[] unknownVersion = v0Trade(1, 1L);
        unknownVersion[6] = 99;
        assertThrows(IllegalStateException.class, () -> SbeMigrator.migrateStream(unknownVersion));
    }

    @Test
    void currentSchemaMatchesItsArchivedVersion() {
        final SchemaLayout current = SchemaLayout.fromResource(MigrationStep.CURRENT_SCHEMA);
        final SchemaLayout archived = SchemaLayout.fromResource(MigrationStep.archived(SbeMigrator.CURRENT_VERSION));

        assertEquals(
                archived.messages(),
                current.messages(),
                "schema.xml changed layout without a version bump. Bump its version to "
                        + (SbeMigrator.CURRENT_VERSION + 1) + ", archive it as "
                        + MigrationStep.archived(SbeMigrator.CURRENT_VERSION + 1)
                        + ", and register MigrationStep.from(" + SbeMigrator.CURRENT_VERSION + ")");
    }

    @Test
    void archivedSchemaParsesToTheSameLayoutAsTheGeneratedCodecs() {
        final SchemaLayout v0 = SchemaLayout.fromResource(MigrationStep.archived(0));
        final SchemaLayout current = SchemaLayout.fromResource(MigrationStep.CURRENT_SCHEMA);

        assertEquals(
                group.gnometrading.schemas.v0.Mbp10Encoder.BLOCK_LENGTH,
                v0.message(Mbp10Decoder.TEMPLATE_ID).blockLength());
        assertEquals(
                Mbp10Decoder.BLOCK_LENGTH,
                current.message(Mbp10Decoder.TEMPLATE_ID).blockLength());
        assertEquals(
                Mbp10Decoder.bidSize0EncodingOffset(),
                current.message(Mbp10Decoder.TEMPLATE_ID)
                        .fields()
                        .get("bidSize0")
                        .offset());
        assertEquals(SbeMigrator.CURRENT_VERSION, current.version());
    }

    @Test
    void messageWithALayoutItsVersionNeverHadIsRefused() {
        // Before 2025-09-17 the schema was also "version 0" but MBP-10 was 370 bytes, not 374.
        final byte[] unrecorded = v0Trade(1, 1L);
        unrecorded[0] = (byte) (group.gnometrading.schemas.v0.TradesEncoder.BLOCK_LENGTH - 4);
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

    private static byte[] v0Trade(final long sequence, final long size) {
        final byte[] bytes = new byte[HEADER + group.gnometrading.schemas.v0.TradesEncoder.BLOCK_LENGTH];
        new group.gnometrading.schemas.v0.TradesEncoder()
                .wrapAndApplyHeader(
                        new UnsafeBuffer(bytes), 0, new group.gnometrading.schemas.v0.MessageHeaderEncoder())
                .sequence(sequence)
                .size(size)
                .price(500_000_000L);
        return bytes;
    }

    private static byte[] currentTrade(final long sequence, final long size) {
        final byte[] bytes = new byte[HEADER + TradesDecoder.BLOCK_LENGTH];
        new TradesEncoder()
                .wrapAndApplyHeader(new UnsafeBuffer(bytes), 0, new MessageHeaderEncoder())
                .sequence(sequence)
                .size(size)
                .price(500_000_000L);
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
