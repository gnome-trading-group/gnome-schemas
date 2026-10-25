package group.gnometrading.schemas;

import java.nio.ByteOrder;

public final class Statics {

    private Statics() {}

    public static final long PRICE_SCALING_FACTOR = 1_000_000_000;
    public static final long SIZE_SCALING_FACTOR = 1_000_000;

    /** Every Gnome schema is little-endian. */
    public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
}
