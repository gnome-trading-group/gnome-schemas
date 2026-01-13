package group.gnometrading.schemas.converters.mbp10;

import group.gnometrading.schemas.MBP10Schema;
import group.gnometrading.schemas.MBP1Schema;
import group.gnometrading.schemas.converters.SingleSchemaBulkConverter;

public class MBP10ToMBP1BulkConverter extends SingleSchemaBulkConverter<MBP10Schema, MBP1Schema> {

    public MBP10ToMBP1BulkConverter() {
        super(new MBP10ToMBP1Converter());
    }

    @Override
    protected MBP1Schema newOutputSchema() {
        return new MBP1Schema();
    }
}
