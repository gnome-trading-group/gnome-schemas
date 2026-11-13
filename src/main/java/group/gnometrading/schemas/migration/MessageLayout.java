package group.gnometrading.schemas.migration;

import java.util.List;
import java.util.Map;

/**
 * One message template's layout in one schema version: its fixed block, fields in declared order, the names of the
 * variable-length fields that follow the block, and the names of its repeating groups, which migration can't carry.
 */
record MessageLayout(
        String name,
        int templateId,
        int blockLength,
        Map<String, FieldLayout> fields,
        List<String> varData,
        List<String> groups) {}
