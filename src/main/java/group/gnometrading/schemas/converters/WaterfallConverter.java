package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;

@SuppressWarnings("rawtypes")
public class WaterfallConverter<I extends Schema, O extends Schema> implements SchemaConverter<I, O> {

    private final SchemaConverter[] converters;

    public WaterfallConverter(SchemaConverter... converters) {
        this.converters = new SchemaConverter[converters.length];
        int i = 0;
        for (var converter : converters) {
            this.converters[i++] = converter;
        }
    }

    @SuppressWarnings("unchecked")
    public O convert(I source) {
        Schema current = source;
        for (SchemaConverter converter : this.converters) {
            current = converter.convert(current);
            if (current == null) {
                return null;
            }
        }
        return (O) current;
    }
}
