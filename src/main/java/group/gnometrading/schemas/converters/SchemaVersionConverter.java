package group.gnometrading.schemas.converters;

@FunctionalInterface
public interface SchemaVersionConverter {
    byte[] convert(byte[] data);
}
