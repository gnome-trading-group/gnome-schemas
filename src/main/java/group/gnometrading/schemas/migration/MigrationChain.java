package group.gnometrading.schemas.migration;

import group.gnometrading.schemas.SbeMessage;
import java.util.HashMap;
import java.util.Map;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * The migration logic for one schema (core, book, ...): brings its stored messages written by an older version up
 * to its current one. Each schema versions on its own, so each has its own chain.
 *
 * <p>Works per message: every message's header names its schema, its template and the version that wrote it. A
 * message is its header, its block ({@code blockLength} bytes) and then its variable-length fields, each a uint16
 * length and that many bytes, so its full length is walked from the layout of the version that wrote it. A blob
 * may therefore mix versions (files written either side of a release) and templates (a journal). Steps go one
 * version at a time and are chained until the message is current; each is planned once, at class load, from the
 * archived schema XML. Variable-length fields are copied unchanged: a step may not alter them.
 */
final class MigrationChain {

    private static final int HEADER_LENGTH = SbeMessage.HEADER_LENGTH;
    private static final int VAR_DATA_LENGTH_SIZE = 2;
    private static final int UINT16_MASK = 0xFFFF;
    private static final int TEMPLATE_SHIFT = 16;

    private final int schemaId;
    private final int currentVersion;
    private final Map<Integer, Step> steps = new HashMap<>();
    private final Map<Integer, Integer> varDataCounts = new HashMap<>();

    MigrationChain(final SchemaLayout current) {
        this.schemaId = current.schemaId();
        this.currentVersion = current.version();
        recordVarData(current);
    }

    int schemaId() {
        return this.schemaId;
    }

    int currentVersion() {
        return this.currentVersion;
    }

    void register(final MigrationStep step) {
        if (step.schemaId() != this.schemaId) {
            throw new IllegalArgumentException(
                    "Step for schema " + step.schemaId() + " registered on schema " + this.schemaId + "'s chain");
        }
        recordVarData(step.source());
        for (final Map.Entry<Integer, MessageConverter> entry : step.compile().entrySet()) {
            registerConverter(entry.getKey(), step.fromVersion(), step.toVersion(), entry.getValue());
        }
    }

    private void recordVarData(final SchemaLayout layout) {
        for (final MessageLayout message : layout.messages().values()) {
            this.varDataCounts.put(
                    key(message.templateId(), layout.version()),
                    message.varData().size());
        }
    }

    private void registerConverter(
            final int templateId, final int fromVersion, final int toVersion, final MessageConverter converter) {
        if (this.steps.putIfAbsent(key(templateId, fromVersion), new Step(toVersion, converter)) != null) {
            throw new IllegalStateException("Duplicate migration for template " + templateId + " v" + fromVersion);
        }
    }

    /** Whether a step exists from {@code fromVersion} for the template, so tests can check every path. */
    boolean hasStep(final int templateId, final int fromVersion) {
        return this.steps.containsKey(key(templateId, fromVersion));
    }

    /** This schema's messages in {@code data}, all brought to the current version; see {@link SbeMigrator}. */
    byte[] migrateStream(final byte[] data) {
        return MigrationStreams.migrate(data, schemaId -> {
            if (schemaId != this.schemaId) {
                throw new IllegalStateException("Not schema " + this.schemaId + ": schema id " + schemaId);
            }
            return this;
        });
    }

    /** Whether the message at {@code offset} was written by an older version of this schema. */
    boolean needsMigration(final DirectBuffer buffer, final int offset) {
        return SbeMessage.version(buffer, offset) != this.currentVersion;
    }

    /**
     * Writes the message at {@code srcOffset} into {@code dst} at the current version.
     *
     * @return the number of bytes written; 0 if a step drops the message
     */
    int migrateMessage(
            final DirectBuffer src, final int srcOffset, final MutableDirectBuffer dst, final int dstOffset) {
        final int length = messageLength(src, srcOffset, src.capacity());
        return migrate(src, srcOffset, length, dst, dstOffset, scratch());
    }

    /** The full length, header to last variable-length byte, of the message at {@code offset}. */
    int messageLength(final DirectBuffer src, final int offset, final int limit) {
        if (limit - offset < HEADER_LENGTH) {
            throw new IllegalStateException("Data ends inside a message header at byte " + offset);
        }
        final Integer varDataCount =
                this.varDataCounts.get(key(SbeMessage.templateId(src, offset), SbeMessage.version(src, offset)));
        if (varDataCount == null) {
            throw new IllegalStateException("No layout for template " + SbeMessage.templateId(src, offset)
                    + " at schema " + this.schemaId + " version " + SbeMessage.version(src, offset)
                    + " (current is " + this.currentVersion + ")");
        }
        int length = HEADER_LENGTH + SbeMessage.blockLength(src, offset);
        for (int field = 0; field < varDataCount; field++) {
            if (offset + length + VAR_DATA_LENGTH_SIZE > limit) {
                throw new IllegalStateException("Data ends inside a message at byte " + offset);
            }
            length += VAR_DATA_LENGTH_SIZE + (src.getShort(offset + length, Scalars.ORDER) & UINT16_MASK);
        }
        if (offset + length > limit) {
            throw new IllegalStateException("Data ends inside a message at byte " + offset);
        }
        return length;
    }

    /** Bytes the message of {@code length} at {@code offset} takes once current; 0 if a step drops it. */
    int migratedLength(final DirectBuffer src, final int offset, final int length) {
        int version = SbeMessage.version(src, offset);
        if (version == this.currentVersion) {
            return length;
        }
        final int templateId = SbeMessage.templateId(src, offset);
        checkLayout(src, offset, step(templateId, version));
        final int varLength = length - HEADER_LENGTH - SbeMessage.blockLength(src, offset);
        int converted = 0;
        while (version != this.currentVersion) {
            final Step step = step(templateId, version);
            converted = step.converter.targetLength();
            if (converted == 0) {
                return 0;
            }
            version = step.toVersion;
        }
        return converted + varLength;
    }

    /**
     * Writes the message of {@code length} at {@code srcOffset} into {@code dst} at the current version.
     *
     * @return the number of bytes written; 0 if a step drops the message
     */
    int migrate(
            final DirectBuffer src,
            final int srcOffset,
            final int length,
            final MutableDirectBuffer dst,
            final int dstOffset,
            final ExpandableArrayBuffer[] scratch) {
        int version = SbeMessage.version(src, srcOffset);
        if (version == this.currentVersion) {
            dst.putBytes(dstOffset, src, srcOffset, length);
            return length;
        }
        final int templateId = SbeMessage.templateId(src, srcOffset);
        final int varLength = length - HEADER_LENGTH - SbeMessage.blockLength(src, srcOffset);
        DirectBuffer from = src;
        int fromOffset = srcOffset;
        int written = 0;
        while (version != this.currentVersion) {
            final Step step = step(templateId, version);
            checkLayout(from, fromOffset, step);
            // Intermediate steps alternate between two scratch buffers; the last writes straight into dst.
            final boolean last = step.toVersion == this.currentVersion;
            final MutableDirectBuffer target = last ? dst : (from == scratch[0] ? scratch[1] : scratch[0]);
            final int targetOffset = last ? dstOffset : 0;
            written = step.converter.convert(from, fromOffset, target, targetOffset);
            if (written == 0) {
                return 0;
            }
            target.putBytes(
                    targetOffset + written,
                    from,
                    fromOffset + HEADER_LENGTH + step.converter.sourceBlockLength(),
                    varLength);
            from = target;
            fromOffset = targetOffset;
            version = step.toVersion;
        }
        return written + varLength;
    }

    static ExpandableArrayBuffer[] scratch() {
        return new ExpandableArrayBuffer[] {new ExpandableArrayBuffer(), new ExpandableArrayBuffer()};
    }

    /**
     * A message's header states its block length. The schema's version was not always bumped when a
     * layout changed, so a message whose length differs from its claimed version's layout was written by
     * an unrecorded layout; converting it would read the wrong bytes, so it is refused instead.
     */
    private static void checkLayout(final DirectBuffer src, final int offset, final Step step) {
        final int blockLength = SbeMessage.blockLength(src, offset);
        if (blockLength != step.converter.sourceBlockLength()) {
            throw new IllegalStateException("Template " + SbeMessage.templateId(src, offset) + " at byte " + offset
                    + " claims schema version " + SbeMessage.version(src, offset) + " but has block length "
                    + blockLength + ", not " + step.converter.sourceBlockLength()
                    + ": written by a layout no archived schema records");
        }
    }

    private Step step(final int templateId, final int version) {
        final Step step = this.steps.get(key(templateId, version));
        if (step == null) {
            throw new IllegalStateException("No migration for template " + templateId + " of schema " + this.schemaId
                    + " from version " + version + " (current is " + this.currentVersion + ")");
        }
        return step;
    }

    private static int key(final int templateId, final int version) {
        return (templateId << TEMPLATE_SHIFT) | version;
    }

    private record Step(int toVersion, MessageConverter converter) {}
}
