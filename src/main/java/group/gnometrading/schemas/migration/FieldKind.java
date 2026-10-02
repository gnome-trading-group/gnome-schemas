package group.gnometrading.schemas.migration;

/** How a field is encoded, which decides how it may be converted between versions. */
enum FieldKind {
    /** A single integer or floating point value. */
    SCALAR,
    /** A fixed-length array of one primitive, e.g. a char string or a 16-byte id. */
    ARRAY,
    /** An enum: one primitive holding one of a named set of values. */
    ENUM,
    /** A bit set: one primitive whose bits are named choices. */
    SET,
    /** A composite of other types. */
    COMPOSITE
}
