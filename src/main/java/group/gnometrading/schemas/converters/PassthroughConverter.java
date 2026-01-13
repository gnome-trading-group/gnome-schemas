package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;

public class PassthroughConverter implements SchemaConverter<Schema, Schema> {
    @Override
    public Schema convert(Schema source) {
        return source;
    }
}
