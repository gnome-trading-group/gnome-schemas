package group.gnometrading.schemas.migration;

import group.gnometrading.schemas.SbeMessage;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * Brings stored SBE messages written by an older version of their schema up to its current one.
 *
 * <p>Each of Gnome's schemas (core, and each record family) versions on its own, so each has its own chain; a
 * message's header names its schema, and that schema's chain migrates it. Every step registers here at class load
 * (see {@link MigrationSteps}), planned from the archived schema XML, so no reader can forget one.
 */
public final class SbeMigrator {

    static final String CORE = "core";
    static final String BOOK = "book";
    static final String[] SCHEMAS = {CORE, BOOK};

    private static final MigrationChain[] CHAINS = new MigrationChain[SCHEMAS.length];

    static {
        for (int i = 0; i < SCHEMAS.length; i++) {
            CHAINS[i] = new MigrationChain(SchemaLayout.fromResource(MigrationStep.current(SCHEMAS[i])));
        }
        MigrationSteps.registerAll();
    }

    private SbeMigrator() {}

    static void register(final MigrationStep step) {
        chain(step.schemaId()).register(step);
    }

    static boolean hasStep(final int schemaId, final int templateId, final int fromVersion) {
        return chain(schemaId).hasStep(templateId, fromVersion);
    }

    /** Whether {@code schemaId} is one of Gnome's schemas, so its messages can be migrated. */
    public static boolean isGnomeSchema(final int schemaId) {
        for (final MigrationChain chain : CHAINS) {
            if (chain.schemaId() == schemaId) {
                return true;
            }
        }
        return false;
    }

    /** The current version of a schema, by its id. */
    public static int currentVersion(final int schemaId) {
        return chain(schemaId).currentVersion();
    }

    /**
     * Returns {@code data} with every message at its schema's current version, in order, minus any messages a step
     * drops. When every message already is current, returns {@code data} itself without copying.
     *
     * @throws IllegalStateException if the data ends part way through a message, is not one of Gnome's schemas,
     *     or holds a version no migration starts from
     */
    public static byte[] migrateStream(final byte[] data) {
        return MigrationStreams.migrate(data, SbeMigrator::chain);
    }

    /** Whether the message at {@code offset} was written by an older version of its schema. */
    public static boolean needsMigration(final DirectBuffer buffer, final int offset) {
        return chain(SbeMessage.schemaId(buffer, offset)).needsMigration(buffer, offset);
    }

    /**
     * Writes the message at {@code srcOffset} into {@code dst} at its schema's current version.
     *
     * @return the number of bytes written; 0 if a step drops the message
     */
    public static int migrateMessage(
            final DirectBuffer src, final int srcOffset, final MutableDirectBuffer dst, final int dstOffset) {
        return chain(SbeMessage.schemaId(src, srcOffset)).migrateMessage(src, srcOffset, dst, dstOffset);
    }

    private static MigrationChain chain(final int schemaId) {
        for (final MigrationChain chain : CHAINS) {
            if (chain.schemaId() == schemaId) {
                return chain;
            }
        }
        throw new IllegalStateException("Not a Gnome SBE message: schema id " + schemaId);
    }
}
