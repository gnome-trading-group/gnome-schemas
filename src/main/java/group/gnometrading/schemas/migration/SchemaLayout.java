package group.gnometrading.schemas.migration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;
import uk.co.real_logic.sbe.xml.XmlSchemaParser;

/**
 * Every message layout in one schema version, read from its XML through SBE's own parser, so field
 * offsets, sizes and null values come from the same source the codecs are generated from.
 */
public final class SchemaLayout {

    private final int version;
    private final Map<Integer, MessageLayout> messages;

    private SchemaLayout(final int version, final Map<Integer, MessageLayout> messages) {
        this.version = version;
        this.messages = messages;
    }

    /** The schema shipped on the classpath at {@code resource}, e.g. {@code schemas/schema-v0.xml}. */
    public static SchemaLayout fromResource(final String resource) {
        try (InputStream in = SchemaLayout.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalArgumentException("No schema on the classpath at " + resource);
            }
            return parse(in);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static SchemaLayout parse(final InputStream xml) {
        final Ir ir;
        try {
            final MessageSchema schema = XmlSchemaParser.parse(
                    xml,
                    ParserOptions.builder()
                            .suppressOutput(true)
                            .stopOnError(true)
                            .build());
            ir = new IrGenerator().generate(schema);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Unreadable SBE schema", e);
        }
        final Map<Integer, MessageLayout> messages = new HashMap<>();
        for (final List<Token> tokens : ir.messages()) {
            final Token begin = tokens.get(0);
            messages.put(
                    (int) begin.id(),
                    new MessageLayout(begin.name(), (int) begin.id(), begin.encodedLength(), fields(tokens)));
        }
        return new SchemaLayout(ir.version(), Collections.unmodifiableMap(messages));
    }

    public int version() {
        return this.version;
    }

    MessageLayout message(final int templateId) {
        return this.messages.get(templateId);
    }

    Map<Integer, MessageLayout> messages() {
        return this.messages;
    }

    private static Map<String, FieldLayout> fields(final List<Token> tokens) {
        final Map<String, FieldLayout> fields = new LinkedHashMap<>();
        int index = 1;
        while (index < tokens.size()) {
            final Token token = tokens.get(index);
            if (token.signal() == Signal.BEGIN_FIELD) {
                final FieldLayout field = field(token, tokens, index + 1);
                if (field.length() > 0) {
                    fields.put(field.name(), field);
                }
                index += token.componentTokenCount();
            } else {
                index++;
            }
        }
        return Collections.unmodifiableMap(fields);
    }

    private static FieldLayout field(final Token fieldToken, final List<Token> tokens, final int typeIndex) {
        final Token type = tokens.get(typeIndex);
        final Encoding encoding = type.encoding();
        final boolean optional = fieldToken.isOptionalEncoding() || type.isOptionalEncoding();
        final PrimitiveType primitive = encoding.primitiveType();
        final long nullValue = primitive == null || isFloating(primitive)
                ? 0
                : encoding.applicableNullValue().longValue();
        return switch (type.signal()) {
            case BEGIN_ENUM -> new FieldLayout(
                    fieldToken.name(),
                    fieldToken.offset(),
                    type.encodedLength(),
                    FieldKind.ENUM,
                    primitive,
                    1,
                    optional,
                    nullValue,
                    namedValues(tokens, typeIndex),
                    "");
            case BEGIN_SET -> new FieldLayout(
                    fieldToken.name(),
                    fieldToken.offset(),
                    type.encodedLength(),
                    FieldKind.SET,
                    primitive,
                    1,
                    optional,
                    nullValue,
                    namedValues(tokens, typeIndex),
                    "");
            case BEGIN_COMPOSITE -> new FieldLayout(
                    fieldToken.name(),
                    fieldToken.offset(),
                    type.encodedLength(),
                    FieldKind.COMPOSITE,
                    null,
                    1,
                    optional,
                    0,
                    Map.of(),
                    signature(tokens, typeIndex));
            default -> new FieldLayout(
                    fieldToken.name(),
                    fieldToken.offset(),
                    type.encodedLength(),
                    type.arrayLength() > 1 ? FieldKind.ARRAY : FieldKind.SCALAR,
                    primitive,
                    type.arrayLength(),
                    optional,
                    nullValue,
                    Map.of(),
                    "");
        };
    }

    private static Map<String, Long> namedValues(final List<Token> tokens, final int typeIndex) {
        final Map<String, Long> values = new LinkedHashMap<>();
        final Token type = tokens.get(typeIndex);
        for (int position = typeIndex + 1; position < typeIndex + type.componentTokenCount() - 1; position++) {
            final Token value = tokens.get(position);
            values.put(value.name(), value.encoding().constValue().longValue());
        }
        return Collections.unmodifiableMap(values);
    }

    /** The composite's internal layout, independent of where in the message it sits. */
    private static String signature(final List<Token> tokens, final int typeIndex) {
        final StringBuilder signature = new StringBuilder();
        final Token type = tokens.get(typeIndex);
        for (int position = typeIndex; position < typeIndex + type.componentTokenCount(); position++) {
            final Token part = tokens.get(position);
            // The composite's own begin and end tokens carry its offset in the message; its parts are
            // relative to it, so only theirs describe the layout.
            final boolean ownToken = position == typeIndex || position == typeIndex + type.componentTokenCount() - 1;
            final int relativeOffset = ownToken ? 0 : part.offset();
            signature
                    .append(part.signal())
                    .append(':')
                    .append(part.name())
                    .append('@')
                    .append(relativeOffset)
                    .append('/')
                    .append(part.encodedLength())
                    .append('/')
                    .append(part.encoding().primitiveType())
                    .append(';');
        }
        return signature.toString();
    }

    private static boolean isFloating(final PrimitiveType primitive) {
        return primitive == PrimitiveType.FLOAT || primitive == PrimitiveType.DOUBLE;
    }
}
