package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;

public final class PassthroughConverter implements SchemaConverter<Schema, Schema> {
    @Override
    public Schema convert(Schema source) {
        return source;
    }
}
