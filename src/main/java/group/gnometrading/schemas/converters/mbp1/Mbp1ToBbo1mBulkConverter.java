package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.Bbo1mSchema;
import group.gnometrading.schemas.Mbp1Schema;
import group.gnometrading.schemas.converters.SamplingSchemaBulkConverter;

public final class Mbp1ToBbo1mBulkConverter extends SamplingSchemaBulkConverter<Mbp1Schema, Bbo1mSchema> {

    public Mbp1ToBbo1mBulkConverter() {
        super(new Mbp1ToBbo1mConverter());
    }

    @Override
    protected Bbo1mSchema newOutputSchema() {
        return new Bbo1mSchema();
    }
}
