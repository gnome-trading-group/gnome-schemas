package group.gnometrading.schemas.migration;

import group.gnometrading.schemas.SbeMessage;
import java.util.function.IntFunction;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.UnsafeBuffer;

/** Migrates a blob of messages, each by the chain of the schema its header names. */
final class MigrationStreams {

    private MigrationStreams() {}

    /**
     * Returns {@code data} with every message at its schema's current version, in order, minus any messages a step
     * drops. When every message already is current, returns {@code data} itself without copying.
     *
     * @param chains the chain for a schema id; throws for a schema it doesn't know
     */
    static byte[] migrate(final byte[] data, final IntFunction<MigrationChain> chains) {
        if (data == null || data.length == 0) {
            return data;
        }
        final UnsafeBuffer src = new UnsafeBuffer(data);
        int outputLength = 0;
        boolean anyOld = false;
        int offset = 0;
        while (offset < data.length) {
            final MigrationChain chain = chainAt(src, offset, data.length, chains);
            final int length = chain.messageLength(src, offset, data.length);
            if (chain.needsMigration(src, offset)) {
                anyOld = true;
            }
            outputLength += chain.migratedLength(src, offset, length);
            offset += length;
        }
        if (!anyOld) {
            return data;
        }
        final byte[] out = new byte[outputLength];
        final UnsafeBuffer dst = new UnsafeBuffer(out);
        final ExpandableArrayBuffer[] scratch = MigrationChain.scratch();
        int written = 0;
        offset = 0;
        while (offset < data.length) {
            final MigrationChain chain = chainAt(src, offset, data.length, chains);
            final int length = chain.messageLength(src, offset, data.length);
            written += chain.migrate(src, offset, length, dst, written, scratch);
            offset += length;
        }
        return out;
    }

    private static MigrationChain chainAt(
            final UnsafeBuffer src, final int offset, final int limit, final IntFunction<MigrationChain> chains) {
        if (limit - offset < SbeMessage.HEADER_LENGTH) {
            throw new IllegalStateException("Data ends inside a message header at byte " + offset);
        }
        return chains.apply(SbeMessage.schemaId(src, offset));
    }
}
