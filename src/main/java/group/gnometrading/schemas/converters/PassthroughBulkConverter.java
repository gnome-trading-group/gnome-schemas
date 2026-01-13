package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;

import java.util.List;

public class PassthroughBulkConverter implements SchemaBulkConverter<Schema, Schema> {
    @Override
    public List<Schema> convert(List<Schema> source) {
        return source;
    }
}
