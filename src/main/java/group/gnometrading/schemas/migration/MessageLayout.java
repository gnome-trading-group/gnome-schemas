package group.gnometrading.schemas.migration;

import java.util.List;
import java.util.Map;

/**
 * One message template's layout in one schema version: its fixed block, fields in declared order, and the names of
 * the variable-length fields that follow the block.
 */
record MessageLayout(
        String name, int templateId, int blockLength, Map<String, FieldLayout> fields, List<String> varData) {}
