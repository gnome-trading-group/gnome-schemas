package group.gnometrading.schemas.migration;

import java.util.Map;
import org.agrona.DirectBuffer;

/** Read access to the source message's fields by name, for {@link MessageRules#compute}. */
public final class SourceFields {

    private final Map<String, FieldLayout> fields;
    private DirectBuffer buffer;
    private int body;

    SourceFields(final Map<String, FieldLayout> fields) {
        this.fields = fields;
    }

    SourceFields wrap(final DirectBuffer messageBuffer, final int bodyOffset) {
        this.buffer = messageBuffer;
        this.body = bodyOffset;
        return this;
    }

    /** The integer, enum or set value of a source field. */
    public long get(final String name) {
        final FieldLayout field = field(name);
        return Scalars.read(this.buffer, this.body + field.offset(), field.primitive());
    }

    /** Whether an optional source field holds its null value. */
    public boolean isNull(final String name) {
        final FieldLayout field = field(name);
        return field.optional() && get(name) == field.nullValue();
    }

    private FieldLayout field(final String name) {
        final FieldLayout field = this.fields.get(name);
        if (field == null || !Scalars.isInteger(field.primitive())) {
            throw new IllegalArgumentException("No integer source field " + name);
        }
        return field;
    }
}
