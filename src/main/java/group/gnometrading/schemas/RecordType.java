package group.gnometrading.schemas;

import group.gnometrading.schemas.book.Bbo1mDecoder;
import group.gnometrading.schemas.book.Bbo1sDecoder;
import group.gnometrading.schemas.book.MboDecoder;
import group.gnometrading.schemas.book.Mbp10Decoder;
import group.gnometrading.schemas.book.Mbp1Decoder;
import group.gnometrading.schemas.book.Ohlcv1hDecoder;
import group.gnometrading.schemas.book.Ohlcv1mDecoder;
import group.gnometrading.schemas.book.Ohlcv1sDecoder;
import group.gnometrading.schemas.book.TradesDecoder;
import java.util.function.Supplier;

/** Every kind of stream record, across families, by the template id its messages carry. */
public enum RecordType {
    MBO(Family.BOOK, MboDecoder.TEMPLATE_ID, "mbo", MboSchema::new),
    MBP_10(Family.BOOK, Mbp10Decoder.TEMPLATE_ID, "mbp-10", Mbp10Schema::new),
    MBP_1(Family.BOOK, Mbp1Decoder.TEMPLATE_ID, "mbp-1", Mbp1Schema::new),
    BBO_1S(Family.BOOK, Bbo1sDecoder.TEMPLATE_ID, "bbo-1s", Bbo1sSchema::new),
    BBO_1M(Family.BOOK, Bbo1mDecoder.TEMPLATE_ID, "bbo-1m", Bbo1mSchema::new),
    TRADES(Family.BOOK, TradesDecoder.TEMPLATE_ID, "trades", TradesSchema::new),
    OHLCV_1S(Family.BOOK, Ohlcv1sDecoder.TEMPLATE_ID, "ohlcv-1s", Ohlcv1sSchema::new),
    OHLCV_1M(Family.BOOK, Ohlcv1mDecoder.TEMPLATE_ID, "ohlcv-1m", Ohlcv1mSchema::new),
    OHLCV_1H(Family.BOOK, Ohlcv1hDecoder.TEMPLATE_ID, "ohlcv-1h", Ohlcv1hSchema::new);

    private static final RecordType[] BY_TEMPLATE_ID;

    static {
        int maxTemplateId = 0;
        for (final RecordType type : values()) {
            maxTemplateId = Math.max(maxTemplateId, type.templateId);
        }
        BY_TEMPLATE_ID = new RecordType[maxTemplateId + 1];
        for (final RecordType type : values()) {
            BY_TEMPLATE_ID[type.templateId] = type;
        }
    }

    private final Family family;
    private final int templateId;
    private final String identifier;
    private final Supplier<StreamRecord> supplier;

    RecordType(
            final Family family, final int templateId, final String identifier, final Supplier<StreamRecord> supplier) {
        this.family = family;
        this.templateId = templateId;
        this.identifier = identifier;
        this.supplier = supplier;
    }

    public Family family() {
        return this.family;
    }

    public int templateId() {
        return this.templateId;
    }

    /** The record's name in stored data, e.g. {@code mbp-10}. */
    public String identifier() {
        return this.identifier;
    }

    public StreamRecord newInstance() {
        return this.supplier.get();
    }

    /**
     * One new record of each type, indexed by template id (null where no record type has that id). Each caller gets
     * its own records, since it wraps them over its own buffers.
     */
    public static StreamRecord[] newRecordsByTemplateId() {
        final StreamRecord[] records = new StreamRecord[BY_TEMPLATE_ID.length];
        for (final RecordType type : values()) {
            records[type.templateId] = type.newInstance();
        }
        return records;
    }

    /** The record type of a template id, or null if the id names no stream record (a core message, say). */
    public static RecordType forTemplateId(final int templateId) {
        return templateId >= 0 && templateId < BY_TEMPLATE_ID.length ? BY_TEMPLATE_ID[templateId] : null;
    }

    public static RecordType forIdentifier(final String identifier) {
        for (final RecordType type : values()) {
            if (type.identifier.equals(identifier)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown record type: " + identifier);
    }
}
