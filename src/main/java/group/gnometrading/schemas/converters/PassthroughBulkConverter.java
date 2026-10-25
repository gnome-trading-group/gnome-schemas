package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.StreamRecord;
import java.util.List;

public final class PassthroughBulkConverter implements SchemaBulkConverter<StreamRecord, StreamRecord> {
    @Override
    public List<StreamRecord> convert(List<StreamRecord> source) {
        return source;
    }
}
