package group.gnometrading.schemas;

import group.gnometrading.schemas.converters.SchemaVersionConversionRegistry;
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

    public final byte[] migrateIfNeeded(byte[] data) {
        if (data == null || data.length < 8) {
            return data;
        }
        int fileVersion = Byte.toUnsignedInt(data[6]) | (Byte.toUnsignedInt(data[7]) << 8);
        int expectedVersion = getSbeVersion();
        if (fileVersion == expectedVersion) {
            return data;
        }
        return SchemaVersionConversionRegistry.find(this.schemaType, fileVersion)
                .orElseThrow(() -> new UnsupportedOperationException(
                        ("Schema version mismatch for %s: file has version %d, expected %d. "
                                        + "Register a SchemaVersionConverter to migrate this data.")
                                .formatted(this.schemaType, fileVersion, expectedVersion)))
                .convert(data);
    }

    @Override
    public final void copyFrom(Schema other) {
        assert this.schemaType == other.schemaType : "Cannot copy from different schema types";
        this.buffer.putBytes(0, other.buffer, 0, this.totalMessageSize());
    }
}
