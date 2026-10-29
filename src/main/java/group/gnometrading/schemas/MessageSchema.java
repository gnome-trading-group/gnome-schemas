package group.gnometrading.schemas;

/**
 * Every SBE schema in the system with the block of template ids it owns, so a template id alone names a message in a
 * ring slot, an Aeron frame or a journal record, whichever schemas mix there. Stream-record families ({@link Family})
 * and message schemas (core trading messages, the order gateway's session protocol) share this one numbering.
 * A schema listed here without a schema file yet is reserved: its ids must not be used by anything else.
 */
public enum MessageSchema {
    CORE(group.gnometrading.schemas.MessageHeaderDecoder.SCHEMA_ID, 0, 100),
    BOOK(group.gnometrading.schemas.book.MessageHeaderDecoder.SCHEMA_ID, 100, 100),
    /** Reserved for the order gateway's session protocol, {@code gateway.xml}. */
    GATEWAY(3, 200, 100),
    /** Reserved for the esports family: many record types, so three hundred ids. */
    ESPORTS(4, 300, 300);

    private static final MessageSchema[] VALUES = values();

    private final int schemaId;
    private final int firstTemplateId;
    private final int templateCount;

    MessageSchema(final int schemaId, final int firstTemplateId, final int templateCount) {
        this.schemaId = schemaId;
        this.firstTemplateId = firstTemplateId;
        this.templateCount = templateCount;
    }

    public int schemaId() {
        return this.schemaId;
    }

    public int firstTemplateId() {
        return this.firstTemplateId;
    }

    public int templateCount() {
        return this.templateCount;
    }

    public boolean owns(final int templateId) {
        return templateId >= this.firstTemplateId && templateId < this.firstTemplateId + this.templateCount;
    }

    /** The schema owning {@code templateId}, or null when no schema does. */
    public static MessageSchema forTemplateId(final int templateId) {
        for (final MessageSchema schema : VALUES) {
            if (schema.owns(templateId)) {
                return schema;
            }
        }
        return null;
    }
}
