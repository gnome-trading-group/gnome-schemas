package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.BBO1SSchema;
import group.gnometrading.schemas.MBP1Schema;
import group.gnometrading.schemas.converters.SamplingSchemaBulkConverter;

public class MBP1ToBBO1SBulkConverter extends SamplingSchemaBulkConverter<MBP1Schema, BBO1SSchema> {
    public MBP1ToBBO1SBulkConverter() {
        super(new MBP1ToBBO1SConverter());
    }

    @Override
    protected BBO1SSchema newOutputSchema() {
        return new BBO1SSchema();
    }
}
