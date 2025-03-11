package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.Schema;

public interface SchemaConverter<I extends Schema<?, ?>, O extends Schema<?, ?>> {
    /**
     * Convert the source schema into an optional ouput schema.
     *
     * @param source the schema to convert from
     * @return the output schema if the conversion was valid, otherwise null
     */
    O convert(I source);
}
