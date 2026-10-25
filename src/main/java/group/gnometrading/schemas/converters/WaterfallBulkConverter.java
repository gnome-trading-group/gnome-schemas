package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.StreamRecord;
import java.util.List;

@SuppressWarnings("rawtypes")
public final class WaterfallBulkConverter<I extends StreamRecord, O extends StreamRecord>
        implements SchemaBulkConverter<I, O> {

    private final SchemaBulkConverter[] converters;

    public WaterfallBulkConverter(SchemaBulkConverter... converters) {
        this.converters = new SchemaBulkConverter[converters.length];
        int idx = 0;
        for (var converter : converters) {
            this.converters[idx++] = converter;
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
