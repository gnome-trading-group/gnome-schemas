package group.gnometrading.schemas.converters.mbp1;

import group.gnometrading.schemas.BBO1MSchema;
import group.gnometrading.schemas.MBP1Schema;
import group.gnometrading.schemas.converters.SamplingSchemaBulkConverter;

public class MBP1ToBBO1MBulkConverter extends SamplingSchemaBulkConverter<MBP1Schema, BBO1MSchema> {

    public MBP1ToBBO1MBulkConverter() {
        super(new MBP1ToBBO1MConverter());
    }

    @Override
    protected BBO1MSchema newOutputSchema() {
        return new BBO1MSchema();
    }

}
