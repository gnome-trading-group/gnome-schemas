package group.gnometrading.schemas;

import group.gnometrading.utils.Copyable;

/**
 * A record of a market-data stream, in any family. Identity is the stream id alone: which listing or vendor
 * subject a stream carries is the registry's to say. The header fields every record starts with are read here
 * (see {@link StreamHeader}); each family's subclasses add their own codecs.
 */
public abstract class StreamRecord extends SbeMessage implements Copyable<StreamRecord> {

    public final RecordType recordType;

    protected StreamRecord(final RecordType recordType) {
        this.recordType = recordType;
    }

    public final int streamId() {
        return StreamHeader.streamId(this.buffer, 0);
    }

    public final long getEventTimestamp() {
        return StreamHeader.timestampEvent(this.buffer, 0);
    }

    public final long getRecvTimestamp() {
        return StreamHeader.timestampRecv(this.buffer, 0);
    }

    public final long getSequenceNumber() {
        return StreamHeader.sequence(this.buffer, 0);
    }

    /** The record may not reflect its source (a gap, a resync, a lost or silent stream) until a snapshot. */
    public final boolean isBadData() {
        return StreamHeader.isSet(StreamHeader.flags(this.buffer, 0), StreamHeader.BAD_DATA_BIT);
    }

    public final boolean isSnapshot() {
        return StreamHeader.isSet(StreamHeader.flags(this.buffer, 0), StreamHeader.SNAPSHOT_BIT);
    }

    @Override
    public final void copyFrom(final StreamRecord other) {
        assert this.recordType == other.recordType : "Cannot copy from a different record type";
        this.buffer.putBytes(0, other.buffer, 0, this.totalMessageSize());
    }
}
