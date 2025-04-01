package group.gnometrading.schemas;

public enum SchemaType {
    MBO("mbo", new MBP10Schema()),
    MBP_10("mbp-10", new MBP10Schema()),
    MBP_1("mbp-1", new MBP1Schema()),
    BBO_1S("bbo-1s", new BBO1SSchema()),
    BBO_1M("bbo-1m", new BBO1MSchema()),
    TRADES("trades", new TradesSchema()),
    OHLCV_1S("ohlcv-1s", new OHLCV1SSchema()),
    OHLCV_1M("ohlcv-1m", new OHLCV1MSchema()),
    OHLCV_1H("ohlcv-1h", new OHLCV1HSchema()),
    ;

    private final String identifier;
    private final Schema<?, ?> instance;

    SchemaType(String identifier, Schema<?, ?> instance) {
        this.identifier = identifier;
        this.instance = instance;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public Schema<?, ?> getInstance() {
        return this.instance;
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
