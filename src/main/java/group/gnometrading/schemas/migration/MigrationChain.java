package group.gnometrading.schemas.migration;

import group.gnometrading.schemas.MessageHeaderDecoder;
import java.util.HashMap;
import java.util.Map;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

/**
 * The migration logic, for one notion of "current version" (the real schema's in {@link SbeMigrator},
 * a test schema's in tests). Brings stored SBE messages written by an older version up to current.
 *
 * <p>Works per message: every message's header names its template and the version that wrote it, and
 * the schema has no repeating groups or variable-length fields, so each message is exactly
 * {@code header + blockLength} bytes. A blob may therefore mix versions (files written either side of
 * a release) and template types (a journal). Steps go one version at a time and are chained until the
 * message is current; each is planned once, at class load, from the archived schema XML.
 */
final class MigrationChain {

    private static final int HEADER_LENGTH = MessageHeaderDecoder.ENCODED_LENGTH;
    private static final int TEMPLATE_ID_OFFSET = 2;
    private static final int SCHEMA_ID_OFFSET = 4;
    private static final int VERSION_OFFSET = 6;
    private static final int UINT16_MASK = 0xFFFF;
    private static final int TEMPLATE_SHIFT = 16;
    private final int currentVersion;
    private final Map<Integer, Step> steps = new HashMap<>();

    MigrationChain(final int currentVersion) {
        this.currentVersion = currentVersion;
    }

    void register(final MigrationStep step) {
        for (final Map.Entry<Integer, MessageConverter> entry : step.compile().entrySet()) {
            register(entry.getKey(), step.fromVersion(), step.toVersion(), entry.getValue());
        }
    }

    private void register(
            final int templateId, final int fromVersion, final int toVersion, final MessageConverter converter) {
        if (this.steps.putIfAbsent(key(templateId, fromVersion), new Step(toVersion, converter)) != null) {
            throw new IllegalStateException("Duplicate migration for template " + templateId + " v" + fromVersion);
        }
    }

    /** Whether a step exists from {@code fromVersion} for the template, so tests can check every path. */
    boolean hasStep(final int templateId, final int fromVersion) {
        return this.steps.containsKey(key(templateId, fromVersion));
    }

    /**
     * Returns {@code data} with every message at the current version, in order, minus any messages a
     * step drops. When every message already is current, returns {@code data} itself without copying.
     *
     * @throws IllegalStateException if the data ends part way through a message, is not this schema, or
     *     holds a version no migration starts from
     */
    byte[] migrateStream(final byte[] data) {
        if (data == null || data.length == 0) {
            return data;
        }
        final UnsafeBuffer src = new UnsafeBuffer(data);
        final int outputLength = migratedLength(src, data.length);
        if (outputLength < 0) {
            return data;
        }
        final byte[] out = new byte[outputLength];
        final UnsafeBuffer dst = new UnsafeBuffer(out);
        final ExpandableArrayBuffer[] scratch = {new ExpandableArrayBuffer(), new ExpandableArrayBuffer()};
        int written = 0;
        int offset = 0;
        while (offset < data.length) {
            final int length = messageLength(src, offset, data.length);
            if (version(src, offset) == this.currentVersion) {
                dst.putBytes(written, src, offset, length);
                written += length;
            } else {
                written += migrate(src, offset, dst, written, scratch);
            }
            offset += length;
        }
        return out;
    }

    /** Whether the message at {@code offset} was written by an older schema version. */
    boolean needsMigration(final DirectBuffer buffer, final int offset) {
        return version(buffer, offset) != this.currentVersion;
    }

    /**
     * Writes the message at {@code srcOffset} into {@code dst} at the current version.
     *
     * @return the number of bytes written; 0 if a step drops the message
     */
    int migrateMessage(
            final DirectBuffer src, final int srcOffset, final MutableDirectBuffer dst, final int dstOffset) {
        checkSchema(src, srcOffset);
        return migrate(src, srcOffset, dst, dstOffset, new ExpandableArrayBuffer[] {
            new ExpandableArrayBuffer(), new ExpandableArrayBuffer()
        });
    }

    private int migrate(
            final DirectBuffer src,
            final int srcOffset,
            final MutableDirectBuffer dst,
            final int dstOffset,
            final ExpandableArrayBuffer[] scratch) {
        final int templateId = templateId(src, srcOffset);
        int version = version(src, srcOffset);
        DirectBuffer from = src;
        int fromOffset = srcOffset;
        int length = 0;
        while (version != this.currentVersion) {
            final Step step = step(templateId, version);
            checkLayout(from, fromOffset, step);
            // Intermediate steps alternate between two scratch buffers; the last writes straight into dst.
            final boolean last = step.toVersion == this.currentVersion;
            final MutableDirectBuffer target = last ? dst : (from == scratch[0] ? scratch[1] : scratch[0]);
            final int targetOffset = last ? dstOffset : 0;
            length = step.converter.convert(from, fromOffset, target, targetOffset);
            if (length == 0) {
                return 0;
            }
            from = target;
            fromOffset = targetOffset;
            version = step.toVersion;
        }
        return length;
    }

    /** Output size if any message needs migrating; -1 if every message is already current. */
    private int migratedLength(final DirectBuffer src, final int limit) {
        int offset = 0;
        int output = 0;
        boolean anyOld = false;
        while (offset < limit) {
            final int length = messageLength(src, offset, limit);
            final int version = version(src, offset);
            if (version == this.currentVersion) {
                output += length;
            } else {
                anyOld = true;
                checkLayout(src, offset, step(templateId(src, offset), version));
                output += chainLength(templateId(src, offset), version);
            }
            offset += length;
        }
        return anyOld ? output : -1;
    }

    /**
     * A message's header states its block length. The schema's version was not always bumped when a
     * layout changed, so a message whose length differs from its claimed version's layout was written by
     * an unrecorded layout; converting it would read the wrong bytes, so it is refused instead.
     */
    private static void checkLayout(final DirectBuffer src, final int offset, final Step step) {
        final int blockLength = src.getShort(offset, Scalars.ORDER) & UINT16_MASK;
        if (blockLength != step.converter.sourceBlockLength()) {
            throw new IllegalStateException("Template " + templateId(src, offset) + " at byte " + offset
                    + " claims schema version " + version(src, offset) + " but has block length " + blockLength
                    + ", not " + step.converter.sourceBlockLength()
                    + ": written by a layout no archived schema records");
        }
    }

    private int chainLength(final int templateId, final int fromVersion) {
        int version = fromVersion;
        int length = 0;
        while (version != this.currentVersion) {
            final Step step = step(templateId, version);
            length = step.converter.targetLength();
            if (length == 0) {
                return 0;
            }
            version = step.toVersion;
        }
        return length;
    }

    private Step step(final int templateId, final int version) {
        final Step step = this.steps.get(key(templateId, version));
        if (step == null) {
            throw new IllegalStateException("No migration for template " + templateId + " from schema version "
                    + version + " (current is " + this.currentVersion + ")");
        }
        return step;
    }

    private static int messageLength(final DirectBuffer src, final int offset, final int limit) {
        if (limit - offset < HEADER_LENGTH) {
            throw new IllegalStateException("Data ends inside a message header at byte " + offset);
        }
        checkSchema(src, offset);
        final int length = HEADER_LENGTH + (src.getShort(offset, Scalars.ORDER) & UINT16_MASK);
        if (offset + length > limit) {
            throw new IllegalStateException("Data ends inside a message at byte " + offset);
        }
        return length;
    }

    private static void checkSchema(final DirectBuffer src, final int offset) {
        final int schemaId = src.getShort(offset + SCHEMA_ID_OFFSET, Scalars.ORDER) & UINT16_MASK;
        if (schemaId != MessageHeaderDecoder.SCHEMA_ID) {
            throw new IllegalStateException("Not a Gnome SBE message: schema id " + schemaId + " at byte " + offset);
        }
    }

    private static int templateId(final DirectBuffer src, final int offset) {
        return src.getShort(offset + TEMPLATE_ID_OFFSET, Scalars.ORDER) & UINT16_MASK;
    }

    private static int version(final DirectBuffer src, final int offset) {
        return src.getShort(offset + VERSION_OFFSET, Scalars.ORDER) & UINT16_MASK;
    }

    private static int key(final int templateId, final int fromVersion) {
        return (templateId << TEMPLATE_SHIFT) | fromVersion;
    }

    private record Step(int toVersion, MessageConverter converter) {}
}
