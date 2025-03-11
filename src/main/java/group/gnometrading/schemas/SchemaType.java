package group.gnometrading.schemas;

public enum SchemaType {
    MBO("mbo"),
    MBP_10("mbp-10"),
    MBP_1("mbp-1"),
    BBO_1S("bbo-1s"),
    BBO_1M("bbo-1m"),
    TRADES("trades"),
    OHLCV_1S("ohlcv-1s"),
    OHLCV_1M("ohlcv-1m"),
    OHLCV_1H("ohlcv-1h"),
    ;

    private final String identifier;

    SchemaType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public static SchemaType findById(final String id) {
        for (SchemaType type : SchemaType.values()) {
            if (type.getIdentifier().equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid schema ID provided: %s".formatted(id));
    }
}
