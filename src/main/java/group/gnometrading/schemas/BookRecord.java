package group.gnometrading.schemas;

import group.gnometrading.schemas.book.MessageHeaderDecoder;
import group.gnometrading.schemas.book.MessageHeaderEncoder;

/** A record of the book family: a venue listing's order book or trades. */
public abstract class BookRecord extends StreamRecord {

    protected final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    protected final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();

    protected BookRecord(final RecordType recordType) {
        super(recordType);
    }
}
