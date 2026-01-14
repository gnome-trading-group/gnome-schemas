package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;

import java.util.ArrayList;
import java.util.List;

public abstract class SamplingSchemaBulkConverter<I extends Schema, O extends Schema> implements SchemaBulkConverter<I, O> {

    private final SamplingSchemaConverter<I, O> converter;

    public SamplingSchemaBulkConverter(SamplingSchemaConverter<I, O> converter) {
        this.converter = converter;
    }

    protected abstract O newOutputSchema();

    @Override
    public List<O> convert(List<I> source) {
        List<O> result = new ArrayList<>();
        boolean hasPendingSample = false;

        for (var schema : source) {
            var converted = this.converter.convert(schema);
            hasPendingSample = true;

            if (converted != null) {
                O newSchema = newOutputSchema();
                newSchema.copyFrom(converted);
                result.add(newSchema);
            }
        }

        if (hasPendingSample) {
            var sampled = this.converter.sample();
            if (sampled != null) {
                O newSchema = newOutputSchema();
                newSchema.copyFrom(sampled);
                result.add(newSchema);
            }
        }

        return result;
    }
}
