package group.gnometrading.schemas.converters;

import group.gnometrading.schemas.book.BookFlagsDecoder;
import group.gnometrading.schemas.book.BookFlagsEncoder;

public final class Utils {

    private Utils() {}

    public static void copyFlags(BookFlagsDecoder source, BookFlagsEncoder dest) {
        dest.badData(source.badData());
        dest.snapshot(source.snapshot());
        dest.lastMessage(source.lastMessage());
        dest.badTimestampRecv(source.badTimestampRecv());
        dest.marketByPrice(source.marketByPrice());
        dest.topOfBook(source.topOfBook());
    }
}
