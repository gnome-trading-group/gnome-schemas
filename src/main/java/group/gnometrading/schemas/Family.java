package group.gnometrading.schemas;

/**
 * A family of stream records: one SBE schema, versioned on its own, whose template ids sit in their own hundred
 * so a template id alone names a record wherever records of several families mix.
 */
public enum Family {
    BOOK("book", group.gnometrading.schemas.book.MessageHeaderDecoder.SCHEMA_ID, 100);

    private static final int TEMPLATES_PER_FAMILY = 100;

    private final String identifier;
    private final int schemaId;
    private final int firstTemplateId;

    Family(final String identifier, final int schemaId, final int firstTemplateId) {
        this.identifier = identifier;
        this.schemaId = schemaId;
        this.firstTemplateId = firstTemplateId;
    }

    /** The family's name in stored data and the registry (md.stream.family), e.g. {@code book}. */
    public String identifier() {
        return this.identifier;
    }

    public int schemaId() {
        return this.schemaId;
    }

    public boolean owns(final int templateId) {
        return templateId >= this.firstTemplateId && templateId < this.firstTemplateId + TEMPLATES_PER_FAMILY;
    }

    public static Family forIdentifier(final String identifier) {
        for (final Family family : values()) {
            if (family.identifier.equals(identifier)) {
                return family;
            }
        }
        throw new IllegalArgumentException("Unknown record family: " + identifier);
    }
}
