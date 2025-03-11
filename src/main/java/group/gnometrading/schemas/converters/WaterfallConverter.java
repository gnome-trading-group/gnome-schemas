package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;

@SuppressWarnings("rawtypes")
public class WaterfallConverter implements SchemaConverter<Schema<?, ?>, Schema<?, ?>> {

    private final SchemaConverter[] converters;

    public WaterfallConverter(SchemaConverter... converters) {
        this.converters = new SchemaConverter[converters.length];
        int i = 0;
        for (var converter : converters) {
            this.converters[i++] = converter;
        }
    }

    @SuppressWarnings("unchecked")
    public Schema<?, ?> convert(Schema<?, ?> source) {
        Schema<?, ?> current = source;
        for (SchemaConverter converter : this.converters) {
            current = converter.convert(current);
            if (current == null) {
                return null;
            }
        }
        return current;
    }
}
