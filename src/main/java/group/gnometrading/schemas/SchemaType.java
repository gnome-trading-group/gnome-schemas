package group.gnometrading.schemas;

import java.util.function.Supplier;

public enum SchemaType {
    MBO("mbo", new MBP10Schema(), MBOSchema::new),
    MBP_10("mbp-10", new MBP10Schema(), MBP10Schema::new),
    MBP_1("mbp-1", new MBP1Schema(), MBP1Schema::new),
    BBO_1S("bbo-1s", new BBO1SSchema(), BBO1SSchema::new),
    BBO_1M("bbo-1m", new BBO1MSchema(), BBO1MSchema::new),
    TRADES("trades", new TradesSchema(), TradesSchema::new),
    OHLCV_1S("ohlcv-1s", new OHLCV1SSchema(), OHLCV1SSchema::new),
    OHLCV_1M("ohlcv-1m", new OHLCV1MSchema(), OHLCV1MSchema::new),
    OHLCV_1H("ohlcv-1h", new OHLCV1HSchema(), OHLCV1HSchema::new),
    ;

    private final String identifier;
    private final Schema instance;
    private final Supplier<Schema> instanceSupplier;

    SchemaType(String identifier, Schema instance, Supplier<Schema> instanceSupplier) {
        this.identifier = identifier;
        this.instance = instance;
        this.instanceSupplier = instanceSupplier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public Schema getInstance() {
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
