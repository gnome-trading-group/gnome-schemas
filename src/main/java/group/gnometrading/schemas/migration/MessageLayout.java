package group.gnometrading.schemas.migration;

import java.util.Map;

/** One message template's block in one schema version; fields keep their declared order. */
record MessageLayout(String name, int templateId, int blockLength, Map<String, FieldLayout> fields) {}
