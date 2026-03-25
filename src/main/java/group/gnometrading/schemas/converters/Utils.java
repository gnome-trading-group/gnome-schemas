package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.MarketUpdateFlagsDecoder;
import group.gnometrading.schemas.MarketUpdateFlagsEncoder;

public final class Utils {

    private Utils() {}

    public static void copyFlags(MarketUpdateFlagsDecoder source, MarketUpdateFlagsEncoder dest) {
        dest.lastMessage(source.lastMessage());
        dest.topOfBook(source.topOfBook());
        dest.snapshot(source.snapshot());
        dest.marketByPrice(source.marketByPrice());
        dest.badTimestampRecv(source.badTimestampRecv());
        dest.maybeBadBook(source.maybeBadBook());
    }
}
