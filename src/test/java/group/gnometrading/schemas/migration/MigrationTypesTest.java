package group.gnometrading.schemas.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

/**
 * Field kinds and refusals not covered by {@link MigrationRulesTest}: small integers, composites, new
 * float/double/set/enum fields, and every refusal, each planned on its own message (types-v0/v1.xml).
 */
class MigrationTypesTest {

    private static final SchemaLayout V0 = SchemaLayout.fromResource("migration/types-v0.xml");
    private static final SchemaLayout V1 = SchemaLayout.fromResource("migration/types-v1.xml");
    private static final int HEADER = 8;
    private static final int SMALL = 1;

    @Test
    void smallSignedIntegersWidenWithTheirSign_CompositesAndArraysCopy() {
        final CompiledConverter converter = plan(SMALL, smallRules());
        final byte[] v0 = message(V0, SMALL, m -> {
            m.put("i8", -100);
            m.put("i16", -30_000);
            m.putInt("point", 0, 7);
            m.putInt("point", 4, -8);
            m.putBytes("tag", new byte[] {1, 2, 3, 4});
            m.put("price", 50);
            m.put("narrow", -5);
        });

        final byte[] v1 = convert(converter, v0);

        assertEquals(-100, read(v1, "i8"));
        assertEquals(-30_000, read(v1, "i16"));
        final UnsafeBuffer out = new UnsafeBuffer(v1);
        final int point = HEADER + field("point").offset();
        assertEquals(7, out.getInt(point, Scalars.ORDER));
        assertEquals(-8, out.getInt(point + 4, Scalars.ORDER));
        assertEquals(3, out.getByte(HEADER + field("tag").offset() + 2));
        assertEquals(5, read(v1, "price"), "divided by 10");
        assertEquals(-5, read(v1, "narrow"), "int32 narrowed to int8 by an explicit transform");
    }

    @Test
    void newFloatDoubleSetAndEnumFieldsGetNullEmptyOrTheirFill() {
        final byte[] v1 = convert(plan(SMALL, smallRules()), message(V0, SMALL, m -> m.put("price", 10)));

        final UnsafeBuffer out = new UnsafeBuffer(v1);
        assertTrue(Float.isNaN(out.getFloat(HEADER + field("newFloat").offset(), Scalars.ORDER)));
        assertTrue(Double.isNaN(out.getDouble(HEADER + field("newDouble").offset(), Scalars.ORDER)));
        assertEquals(0, read(v1, "newSet"), "a new set starts empty");
        assertEquals(2, read(v1, "venue"), "filled with CME by name");
    }

    @Test
    void computeCanTellWhenAnOptionalSourceIsNull() {
        final byte[] maybeNull = message(V0, SMALL, m -> m.put("price", 10).put("maybe", Integer.MIN_VALUE));
        final byte[] maybeSet = message(V0, SMALL, m -> m.put("price", 10).put("maybe", 3));

        assertEquals(1, read(convert(plan(SMALL, smallRules()), maybeNull), "wasNull"));
        assertEquals(0, read(convert(plan(SMALL, smallRules()), maybeSet), "wasNull"));
    }

    @Test
    void divideRefusesToLosePrecision() {
        final byte[] v0 = message(V0, SMALL, m -> m.put("price", 15));

        assertThrows(IllegalStateException.class, () -> convert(plan(SMALL, smallRules()), v0));
    }

    @Test
    void narrowingTransformThatDoesNotFitFails() {
        final byte[] v0 = message(V0, SMALL, m -> m.put("price", 10).put("narrow", 200));

        assertThrows(IllegalStateException.class, () -> convert(plan(SMALL, smallRules()), v0));
    }

    @Test
    void computeReadingAMissingSourceFieldFails() {
        final CompiledConverter converter =
                plan(SMALL, smallRules().andThen(m -> m.compute("wasNull", src -> src.get("nope"))));

        assertThrows(IllegalArgumentException.class, () -> convert(converter, message(V0, SMALL, m -> {})));
    }

    // ---------- refusals ----------

    @Test
    void changedCompositeIsRefused() {
        assertRefused(2, m -> {}, "composite");
    }

    @Test
    void shrunkArrayIsRefused() {
        assertRefused(3, m -> {}, "only growing");
    }

    @Test
    void removedSetChoiceIsRefused() {
        assertRefused(4, m -> {}, "no longer exists");
    }

    @Test
    void changedKindIsRefused() {
        assertRefused(5, m -> {}, "changed from SCALAR to ENUM");
    }

    @Test
    void optionalBecomingRequiredIsRefused() {
        assertRefused(6, m -> {}, "became required");
    }

    @Test
    void newCompositeIsRefused() {
        assertRefused(7, m -> {}, "new composite");
    }

    @Test
    void transformingAnArrayIsRefused() {
        assertRefused(SMALL, smallRules().andThen(m -> m.transform("tag", v -> v)), "not a single integer");
    }

    @Test
    void fillEnumWithAnUnknownValueIsRefused() {
        assertRefused(SMALL, smallRules().andThen(m -> m.fillEnum("venue", "LSE")), "no enum value LSE");
    }

    @Test
    void droppingAFieldTheSourceLacksIsRefused() {
        assertRefused(SMALL, smallRules().andThen(m -> m.drop("nope")), "not a field of the source");
    }

    @Test
    void unreadableSchemasFailClearly() {
        assertThrows(IllegalArgumentException.class, () -> SchemaLayout.fromResource("migration/missing.xml"));
        assertThrows(
                IllegalArgumentException.class,
                () -> SchemaLayout.parse(new ByteArrayInputStream("<not-sbe/>".getBytes(StandardCharsets.UTF_8))));
    }

    // ---------- helpers ----------

    private static Consumer<MessageRules> smallRules() {
        return m -> m.divide("price", 10)
                .transform("narrow", v -> v)
                .fillEnum("venue", "CME")
                .compute("wasNull", src -> src.isNull("maybe") ? 1 : 0);
    }

    private static CompiledConverter plan(final int templateId, final Consumer<MessageRules> configure) {
        final MessageRules rules = new MessageRules();
        configure.accept(rules);
        return MessagePlanner.plan(V0.message(templateId), V1.message(templateId), rules, 1, 1);
    }

    private static void assertRefused(final int templateId, final Consumer<MessageRules> rules, final String mentions) {
        final IllegalStateException failure = assertThrows(IllegalStateException.class, () -> plan(templateId, rules));
        assertTrue(failure.getMessage().contains(mentions), failure.getMessage());
    }

    private static byte[] convert(final CompiledConverter converter, final byte[] v0) {
        final byte[] out = new byte[converter.targetLength()];
        converter.convert(new UnsafeBuffer(v0), 0, new UnsafeBuffer(out), 0);
        return out;
    }

    private static FieldLayout field(final String name) {
        return V1.message(SMALL).fields().get(name);
    }

    private static long read(final byte[] data, final String name) {
        final FieldLayout field = field(name);
        return Scalars.read(new UnsafeBuffer(data), HEADER + field.offset(), field.primitive());
    }

    private static byte[] message(final SchemaLayout schema, final int templateId, final Consumer<Writer> fields) {
        final MessageLayout layout = schema.message(templateId);
        final byte[] bytes = new byte[HEADER + layout.blockLength()];
        final UnsafeBuffer buffer = new UnsafeBuffer(bytes);
        buffer.putShort(0, (short) layout.blockLength(), Scalars.ORDER);
        buffer.putShort(2, (short) templateId, Scalars.ORDER);
        buffer.putShort(4, (short) 1, Scalars.ORDER);
        buffer.putShort(6, (short) schema.version(), Scalars.ORDER);
        fields.accept(new Writer(layout, buffer));
        return bytes;
    }

    private record Writer(MessageLayout layout, UnsafeBuffer buffer) {
        Writer put(final String name, final long value) {
            final FieldLayout field = this.layout.fields().get(name);
            Scalars.write(this.buffer, HEADER + field.offset(), field.primitive(), value);
            return this;
        }

        void putInt(final String name, final int offsetInField, final int value) {
            this.buffer.putInt(HEADER + this.layout.fields().get(name).offset() + offsetInField, value, Scalars.ORDER);
        }

        void putBytes(final String name, final byte[] value) {
            this.buffer.putBytes(HEADER + this.layout.fields().get(name).offset(), value);
        }
    }
}
