package group.gnometrading.schemas.converters.mbp10;

import group.gnometrading.schemas.Mbp10Schema;
import group.gnometrading.schemas.Mbp1Schema;
import group.gnometrading.schemas.converters.SingleSchemaBulkConverter;

public final class Mbp10ToMbp1BulkConverter extends SingleSchemaBulkConverter<Mbp10Schema, Mbp1Schema> {

    public Mbp10ToMbp1BulkConverter() {
        super(new Mbp10ToMbp1Converter());
    }

    @Override
    protected Mbp1Schema newOutputSchema() {
        return new Mbp1Schema();
    }
}
