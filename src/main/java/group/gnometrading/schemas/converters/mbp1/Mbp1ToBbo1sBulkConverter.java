package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.Bbo1sSchema;
import group.gnometrading.schemas.Mbp1Schema;
import group.gnometrading.schemas.converters.SamplingSchemaBulkConverter;

public final class Mbp1ToBbo1sBulkConverter extends SamplingSchemaBulkConverter<Mbp1Schema, Bbo1sSchema> {

    public Mbp1ToBbo1sBulkConverter() {
        super(new Mbp1ToBbo1sConverter());
    }

    @Override
    protected Bbo1sSchema newOutputSchema() {
        return new Bbo1sSchema();
    }
}
