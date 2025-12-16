package group.gnometrading.schemas;

import java.util.function.Supplier;

public enum SchemaType {
    MBO("mbo", MBOSchema::new),
    MBP_10("mbp-10", MBP10Schema::new),
    MBP_1("mbp-1", MBP1Schema::new),
    BBO_1S("bbo-1s", BBO1SSchema::new),
    BBO_1M("bbo-1m", BBO1MSchema::new),
    TRADES("trades", TradesSchema::new),
    OHLCV_1S("ohlcv-1s", OHLCV1SSchema::new),
    OHLCV_1M("ohlcv-1m", OHLCV1MSchema::new),
    OHLCV_1H("ohlcv-1h", OHLCV1HSchema::new),
    ;

    private final String identifier;
    private final Supplier<Schema> instanceSupplier;
    private Schema instance;

    SchemaType(String identifier, Supplier<Schema> instanceSupplier) {
        this.identifier = identifier;
        this.instanceSupplier = instanceSupplier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public Schema getInstance() {
        if (this.instance == null) {
            this.instance = this.instanceSupplier.get();
        }
        return this.instance;
    }

    public Schema newInstance() {
        return this.instanceSupplier.get();
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
