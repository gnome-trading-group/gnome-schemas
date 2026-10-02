package group.gnometrading.schemas.migration;

import java.util.Map;
import uk.co.real_logic.sbe.PrimitiveType;

/**
 * Where and how one field of one message is encoded in one schema version, as SBE's IR describes it.
 *
 * @param primitive the encoding of a scalar, the element of an array, or the encoding of an enum or set;
 *     null for a composite
 * @param arrayLength elements in an array; 1 otherwise
 * @param optional whether the field may hold its null value
 * @param nullValue the null value as a long (scalars, arrays' elements, enums); unused for floats
 * @param namedValues enum value name → encoded value, or set choice name → bit position; empty otherwise
 * @param compositeSignature the nested layout of a composite, compared to decide if it is unchanged
 */
record FieldLayout(
        String name,
        int offset,
        int length,
        FieldKind kind,
        PrimitiveType primitive,
        int arrayLength,
        boolean optional,
        long nullValue,
        Map<String, Long> namedValues,
        String compositeSignature) {}
