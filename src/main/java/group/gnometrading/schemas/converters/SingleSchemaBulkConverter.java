package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;

import java.util.ArrayList;
import java.util.List;

public abstract class SingleSchemaBulkConverter<I extends Schema, O extends Schema> implements SchemaBulkConverter<I, O> {

    private final SchemaConverter<I, O> converter;

    public SingleSchemaBulkConverter(SchemaConverter<I, O> converter) {
        this.converter = converter;
    }

    protected abstract O newOutputSchema();

    @Override
    public List<O> convert(List<I> source) {
        List<O> result = new ArrayList<>();
        for (var schema : source) {
            var converted = converter.convert(schema);
            if (converted != null) {
                O newSchema = newOutputSchema();
                newSchema.copyFrom(converted);
                result.add(newSchema);
            }
        }
        return result;
    }
}
