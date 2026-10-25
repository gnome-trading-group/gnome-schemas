package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.StreamRecord;

public final class PassthroughConverter implements SchemaConverter<StreamRecord, StreamRecord> {
    @Override
    public StreamRecord convert(StreamRecord source) {
        return source;
    }
}
