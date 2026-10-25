package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.StreamRecord;

@SuppressWarnings("rawtypes")
public final class WaterfallConverter<I extends StreamRecord, O extends StreamRecord> implements SchemaConverter<I, O> {

    private final SchemaConverter[] converters;

    public WaterfallConverter(SchemaConverter... converters) {
        this.converters = new SchemaConverter[converters.length];
        int idx = 0;
        for (var converter : converters) {
            this.converters[idx++] = converter;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public O convert(I source) {
        StreamRecord current = source;
        for (SchemaConverter converter : this.converters) {
            current = converter.convert(current);
            if (current == null) {
                return null;
            }
        }
        return (O) current;
    }
}
