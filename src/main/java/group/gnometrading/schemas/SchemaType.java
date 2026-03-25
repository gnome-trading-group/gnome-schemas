package group.gnometrading.schemas;

import java.util.function.Supplier;

public enum SchemaType {
    MBO("mbo", MboSchema::new),
    MBP_10("mbp-10", Mbp10Schema::new),
    MBP_1("mbp-1", Mbp1Schema::new),
    BBO_1S("bbo-1s", Bbo1sSchema::new),
    BBO_1M("bbo-1m", Bbo1mSchema::new),
    TRADES("trades", TradesSchema::new),
    OHLCV_1S("ohlcv-1s", Ohlcv1sSchema::new),
    OHLCV_1M("ohlcv-1m", Ohlcv1mSchema::new),
    OHLCV_1H("ohlcv-1h", Ohlcv1hSchema::new),
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
