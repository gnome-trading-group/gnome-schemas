package group.gnometrading.schemas.migration;

import group.gnometrading.schemas.MessageHeaderDecoder;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * Brings stored SBE messages written by an older schema version up to the current one.
 *
 * <p>Works per message: every message's header names its template and the version that wrote it, and
 * the schema has no repeating groups or variable-length fields, so each message is exactly
 * {@code header + blockLength} bytes. A blob may therefore mix versions (files written either side of
 * a release) and template types (a journal). Every step registers here at class load (see
 * {@link MigrationSteps}), planned from the archived schema XML, so no reader can forget one.
 */
public final class SbeMigrator {

    public static final int CURRENT_VERSION = MessageHeaderDecoder.SCHEMA_VERSION;

    private static final MigrationChain CHAIN = new MigrationChain(CURRENT_VERSION);

    static {
        MigrationSteps.registerAll();
    }

    private SbeMigrator() {}

    static void register(final MigrationStep step) {
        CHAIN.register(step);
    }

    static boolean hasStep(final int templateId, final int fromVersion) {
        return CHAIN.hasStep(templateId, fromVersion);
    }

    /**
     * Returns {@code data} with every message at the current version, in order, minus any messages a
     * step drops. When every message already is current, returns {@code data} itself without copying.
     *
     * @throws IllegalStateException if the data ends part way through a message, is not this schema, or
     *     holds a version no migration starts from
     */
    public static byte[] migrateStream(final byte[] data) {
        return CHAIN.migrateStream(data);
    }

    /** Whether the message at {@code offset} was written by an older schema version. */
    public static boolean needsMigration(final DirectBuffer buffer, final int offset) {
        return CHAIN.needsMigration(buffer, offset);
    }

    /**
     * Writes the message at {@code srcOffset} into {@code dst} at the current version.
     *
     * @return the number of bytes written; 0 if a step drops the message
     */
    public static int migrateMessage(
            final DirectBuffer src, final int srcOffset, final MutableDirectBuffer dst, final int dstOffset) {
        return CHAIN.migrateMessage(src, srcOffset, dst, dstOffset);
    }
}
