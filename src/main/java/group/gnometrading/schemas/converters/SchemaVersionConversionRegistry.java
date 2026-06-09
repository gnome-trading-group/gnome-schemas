package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.SchemaType;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SchemaVersionConversionRegistry {

    private static final Map<SchemaType, Map<Integer, SchemaVersionConverter>> CONVERTERS =
            new EnumMap<>(SchemaType.class);

    private SchemaVersionConversionRegistry() {}

    public static void register(SchemaType type, int fromVersion, SchemaVersionConverter converter) {
        CONVERTERS.computeIfAbsent(type, k -> new HashMap<>()).put(fromVersion, converter);
    }

    public static Optional<SchemaVersionConverter> find(SchemaType type, int fromVersion) {
        Map<Integer, SchemaVersionConverter> byVersion = CONVERTERS.get(type);
        if (byVersion == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byVersion.get(fromVersion));
    }
}
