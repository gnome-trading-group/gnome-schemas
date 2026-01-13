package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;

import java.util.List;

@SuppressWarnings("rawtypes")
public class WaterfallBulkConverter<I extends Schema, O extends Schema> implements SchemaBulkConverter<I, O> {

    private final SchemaBulkConverter[] converters;

    public WaterfallBulkConverter(SchemaBulkConverter... converters) {
        this.converters = new SchemaBulkConverter[converters.length];
        int i = 0;
        for (var converter : converters) {
            this.converters[i++] = converter;
        }
    }

    @Override
    public List<O> convert(List<I> source) {
        List current = source;
        for (SchemaBulkConverter converter : this.converters) {
            current = converter.convert(current);
        }
        return current;
    }
}
