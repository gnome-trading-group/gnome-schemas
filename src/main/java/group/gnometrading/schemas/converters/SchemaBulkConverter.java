package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;

import java.util.List;

public interface SchemaBulkConverter<I extends Schema, O extends Schema> {
    /**
     * Convert the source schema into an optional ouput schema.
     *
     * @param source the schema to convert from
     * @return the output schema if the conversion was valid, otherwise null
     */
    List<O> convert(List<I> source);
}
