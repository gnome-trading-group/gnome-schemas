package group.gnometrading.schemas;

import group.gnometrading.schemas.migration.SbeMigrator;
import group.gnometrading.utils.Copyable;

public abstract class Schema extends SbeMessage implements Copyable<Schema> {

    public final SchemaType schemaType;

    public Schema(SchemaType schemaType) {
        this.schemaType = schemaType;
    }

    /**
     * Retrieve the sequence number for the record. Note, the decoder *must*
     * be wrapped around a valid byte buffer for this to work.
     *
     * @return the sequence number
     */
    public abstract long getSequenceNumber();

    /**
     * Retrieve the event timestamp for the record. Note, the decoder *must*
     * be wrapped around a valid byte buffer for this to work.
     *
     * @return the event timestamp
     */
    public abstract long getEventTimestamp();

    /**
     * Brings stored data written by an older schema version up to the current one, message by message.
     * Returns {@code data} itself when nothing needs converting.
     */
    public final byte[] migrateIfNeeded(byte[] data) {
        return SbeMigrator.migrateStream(data);
    }

    @Override
    public final void copyFrom(Schema other) {
        assert this.schemaType == other.schemaType : "Cannot copy from different schema types";
        this.buffer.putBytes(0, other.buffer, 0, this.totalMessageSize());
    }
}
