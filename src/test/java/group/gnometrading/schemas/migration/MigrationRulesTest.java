package group.gnometrading.schemas.migration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

/**
 * Every default rule and explicit rule, on test schemas (test-v0/v1/v2.xml) built to exercise them.
 * Messages are written and read through {@link SchemaLayout}, since test schemas have no codecs.
 */
class MigrationRulesTest {

    private static final SchemaLayout V0 = layout("migration/test-v0.xml");
    private static final SchemaLayout V1 = layout("migration/test-v1.xml");
    private static final SchemaLayout V2 = layout("migration/test-v2.xml");
    private static final int HEADER = 8;
    private static final int WIDEN = 1;
    private static final int ENUMS = 2;
    private static final int RULES = 3;
    private static final int GONE = 4;

    // ---------- defaults ----------

    @Test
    void integersWidenSignedAndUnsigned_FloatWidens_NewOptionalFieldIsNull() {
        final MigrationChain chain = chain(1, fullRules());
        final byte[] v0 = message(
                V0,
                WIDEN,
                m -> m.put("u32", 4_000_000_000L).put("i32", -5).put("u8", 250).putFloat("f", 1.5f));

        final byte[] v1 = chain.migrateStream(v0);

        assertEquals(4_000_000_000L, read(V1, WIDEN, v1, "u32"), "unsigned widened, not sign-extended");
        assertEquals(-5, read(V1, WIDEN, v1, "i32"), "signed widened with its sign");
        assertEquals(250, read(V1, WIDEN, v1, "u8"));
        assertEquals(
                1.5,
                new UnsafeBuffer(v1).getDouble(HEADER + field(V1, WIDEN, "f").offset(), Scalars.ORDER));
        assertEquals(Integer.MIN_VALUE, read(V1, WIDEN, v1, "added"), "new optional field is null");
    }

    @Test
    void nullStaysNullAcrossAWidening() {
        final byte[] v0 = message(V0, WIDEN, m -> m.put("u32", 4294967295L));

        final byte[] v1 = chain(1, fullRules()).migrateStream(v0);

        assertEquals(Long.MIN_VALUE, read(V1, WIDEN, v1, "u32"));
    }

    @Test
    void enumsMatchByName_RenamedValuesMap_SetChoicesMoveBits() {
        final byte[] v0 = message(V0, ENUMS, m -> m.put("side", 'A')
                .put("kind", 2) // Y, renamed to Z
                .put("flags", 0b01)); // a only

        final byte[] v1 = chain(1, fullRules()).migrateStream(v0);

        assertEquals('a', read(V1, ENUMS, v1, "side"), "Ask renumbered from 'A' to 'a'");
        assertEquals(9, read(V1, ENUMS, v1, "kind"), "Y mapped to Z");
        assertEquals(1L << 3, read(V1, ENUMS, v1, "flags"), "choice a moved from bit 0 to bit 3");
    }

    @Test
    void unknownEnumValueInDataFailsInsteadOfGuessing() {
        final byte[] v0 = message(V0, ENUMS, m -> m.put("side", 'Q').put("kind", 1));

        assertThrows(IllegalStateException.class, () -> chain(1, fullRules()).migrateStream(v0));
    }

    // ---------- explicit rules ----------

    @Test
    void renameScaleNarrowFillComputeDropAndGrowArray() {
        final byte[] v0 = message(V0, RULES, m -> m.put("qty", 3_000_000_000L)
                .put("price", 7)
                .put("legacy", 1)
                .put("code", 42_000)
                .putBytes("name", "ABCD".getBytes(StandardCharsets.US_ASCII)));

        final byte[] v1 = chain(1, fullRules()).migrateStream(v0);

        assertEquals(3_000_000_000L, read(V1, RULES, v1, "size"), "renamed from qty");
        assertEquals(70, read(V1, RULES, v1, "price"), "multiplied by 10");
        assertEquals(42, read(V1, RULES, v1, "code"), "narrowed by an explicit transform");
        assertEquals(7, read(V1, RULES, v1, "venue"), "required new field filled");
        assertEquals(3_000_000_000L * 7, read(V1, RULES, v1, "notional"), "computed from two source fields");
        final FieldLayout name = field(V1, RULES, "name");
        assertArrayEquals(
                new byte[] {'A', 'B', 'C', 'D', 0, 0, 0, 0},
                Arrays.copyOfRange(v1, HEADER + name.offset(), HEADER + name.offset() + name.length()),
                "array grew, zero padded");
    }

    @Test
    void convertingIntoADirtyReusedBufferWritesEveryByte() {
        final MigrationChain chain = chain(1, fullRules());
        final byte[] v0 =
                message(V0, RULES, m -> m.put("qty", 5).putBytes("name", "AB".getBytes(StandardCharsets.US_ASCII)));
        final byte[] clean = chain.migrateStream(v0);
        final byte[] dirty = new byte[clean.length];
        Arrays.fill(dirty, (byte) 0xFF);

        final int written = chain.migrateMessage(new UnsafeBuffer(v0), 0, new UnsafeBuffer(dirty), 0);

        assertEquals(clean.length, written);
        assertArrayEquals(clean, dirty, "padding and grown-array tail must be written, not left over");
    }

    @Test
    void transformThatOverflowsTheTargetFailsRatherThanWraps() {
        final byte[] v0 = message(V0, RULES, m -> m.put("code", 1L << 50));

        assertThrows(IllegalStateException.class, () -> chain(1, fullRules()).migrateStream(v0));
    }

    @Test
    void droppedTemplateDisappearsFromTheStream() {
        final byte[] stream = concat(message(V0, GONE, m -> m.put("x", 1)), message(V0, WIDEN, m -> m.put("i32", 9)));

        final byte[] v1 = chain(1, fullRules()).migrateStream(stream);

        assertEquals(HEADER + V1.message(WIDEN).blockLength(), v1.length);
        assertEquals(9, read(V1, WIDEN, v1, "i32"));
    }

    @Test
    void stepsChainAcrossVersions() {
        final MigrationChain chain = new MigrationChain(2);
        chain.register(fullRules());
        chain.register(MigrationStep.between(V1, V2).dropMessage("Enums").dropMessage("Rules"));
        final byte[] v0 = message(V0, WIDEN, m -> m.put("u32", 4_000_000_000L).put("i32", 3));

        final byte[] v2 = chain.migrateStream(v0);

        assertEquals(4_000_000_000L, read(V2, WIDEN, v2, "u32"));
        assertEquals(3, read(V2, WIDEN, v2, "i32"));
        assertEquals(Long.MIN_VALUE, read(V2, WIDEN, v2, "later"));
        assertEquals(2, new UnsafeBuffer(v2).getShort(6, Scalars.ORDER));
    }

    @Test
    void threeStepChainAlternatesScratchBuffersCorrectly() {
        final SchemaLayout v3 = layout("migration/test-v3.xml");
        final MigrationChain chain = new MigrationChain(3);
        chain.register(fullRules());
        chain.register(MigrationStep.between(V1, V2).dropMessage("Enums").dropMessage("Rules"));
        chain.register(MigrationStep.between(V2, v3));
        final byte[] v0 = message(
                V0, WIDEN, m -> m.put("u32", 4_000_000_000L).put("i32", -3).put("u8", 9));

        final byte[] out = chain.migrateStream(v0);

        assertEquals(4_000_000_000L, read(v3, WIDEN, out, "u32"));
        assertEquals(-3, read(v3, WIDEN, out, "i32"));
        assertEquals(9, read(v3, WIDEN, out, "u8"));
        assertEquals(Integer.MIN_VALUE, read(v3, WIDEN, out, "latest"));
        assertEquals(3, new UnsafeBuffer(out).getShort(6, Scalars.ORDER));
    }

    @Test
    void registeringTheSameStepTwiceFails() {
        final MigrationChain chain = chain(1, fullRules());

        assertThrows(IllegalStateException.class, () -> chain.register(fullRules()));
    }

    @Test
    void rulesForAMessageTheTargetLacksFail() {
        final IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> fullRules().message("Nope", m -> {}).compile());
        assertTrue(failure.getMessage().contains("Nope"), failure.getMessage());
    }

    // ---------- refusals: anything ambiguous fails when the step is planned ----------

    @Test
    void removedFieldMustBeDropped() {
        assertRefused(
                m -> m.rename("qty", "size")
                        .multiply("price", 10)
                        .transform("code", v -> v / 1000)
                        .fill("venue", 7)
                        .compute("notional", src -> 0),
                "legacy");
    }

    @Test
    void newRequiredFieldMustBeFilled() {
        assertRefused(
                m -> m.rename("qty", "size")
                        .multiply("price", 10)
                        .transform("code", v -> v / 1000)
                        .drop("legacy")
                        .compute("notional", src -> 0),
                "venue");
    }

    @Test
    void narrowingMustBeExplicit() {
        assertRefused(
                m -> m.rename("qty", "size")
                        .multiply("price", 10)
                        .drop("legacy")
                        .fill("venue", 7)
                        .compute("notional", src -> 0),
                "code");
    }

    @Test
    void renamedFieldIsNotGuessed() {
        assertRefused(
                m -> m.multiply("price", 10)
                        .transform("code", v -> v / 1000)
                        .drop("legacy")
                        .fill("venue", 7)
                        .compute("notional", src -> 0),
                "size");
    }

    @Test
    void enumValueWithNoCounterpartMustBeMapped() {
        final IllegalStateException failure =
                assertThrows(IllegalStateException.class, () -> MigrationStep.between(V0, V1)
                        .message("Rules", fullRuleSet())
                        .dropMessage("Gone")
                        .compile());
        assertTrue(failure.getMessage().contains("Y"), failure.getMessage());
    }

    @Test
    void removedTemplateMustBeDropped() {
        final IllegalStateException failure =
                assertThrows(IllegalStateException.class, () -> MigrationStep.between(V0, V1)
                        .message("Rules", fullRuleSet())
                        .message("Enums", m -> m.mapEnum("kind", Map.of("Y", "Z")))
                        .compile());
        assertTrue(failure.getMessage().contains("Gone"), failure.getMessage());
    }

    @Test
    void rulesNamingFieldsThatDoNotExistFail() {
        assertRefused(m -> fullRuleSet().accept(m.fill("typo", 1)), "typo");
    }

    @Test
    void stepMustAdvanceOneVersion() {
        assertThrows(IllegalArgumentException.class, () -> MigrationStep.between(V0, V2));
    }

    @Test
    void identicalFieldsCompileToFewMergedCopies() {
        final Map<Integer, MessageConverter> converters = MigrationStep.between(V1, V2)
                .dropMessage("Enums")
                .dropMessage("Rules")
                .compile();

        // Five unchanged fields become one bulk copy; the new field is one constant.
        assertEquals(2, ((CompiledConverter) converters.get(WIDEN)).opCount());
    }

    // ---------- helpers ----------

    private static MigrationStep fullRules() {
        return MigrationStep.between(V0, V1)
                .message("Rules", fullRuleSet())
                .message("Enums", m -> m.mapEnum("kind", Map.of("Y", "Z")))
                .dropMessage("Gone");
    }

    private static Consumer<MessageRules> fullRuleSet() {
        return m -> m.rename("qty", "size")
                .multiply("price", 10)
                .transform("code", v -> v / 1000)
                .drop("legacy")
                .fill("venue", 7)
                .compute("notional", src -> src.get("qty") * src.get("price"));
    }

    private static void assertRefused(final Consumer<MessageRules> rules, final String mentions) {
        final IllegalStateException failure =
                assertThrows(IllegalStateException.class, () -> MigrationStep.between(V0, V1)
                        .message("Rules", rules)
                        .message("Enums", m -> m.mapEnum("kind", Map.of("Y", "Z")))
                        .dropMessage("Gone")
                        .compile());
        assertTrue(failure.getMessage().contains(mentions), failure.getMessage());
    }

    private static MigrationChain chain(final int current, final MigrationStep step) {
        final MigrationChain chain = new MigrationChain(current);
        chain.register(step);
        return chain;
    }

    private static SchemaLayout layout(final String resource) {
        return SchemaLayout.fromResource(resource);
    }

    private static FieldLayout field(final SchemaLayout schema, final int templateId, final String name) {
        return schema.message(templateId).fields().get(name);
    }

    private static long read(final SchemaLayout schema, final int templateId, final byte[] data, final String name) {
        final FieldLayout field = field(schema, templateId, name);
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
        // Fields not set are written as valid defaults: enums take their first value.
        for (final FieldLayout field : layout.fields().values()) {
            if (field.kind() == FieldKind.ENUM) {
                Scalars.write(
                        buffer,
                        HEADER + field.offset(),
                        field.primitive(),
                        field.namedValues().values().iterator().next());
            }
        }
        fields.accept(new Writer(layout, buffer));
        return bytes;
    }

    private static byte[] concat(final byte[]... parts) {
        int total = 0;
        for (final byte[] part : parts) {
            total += part.length;
        }
        final byte[] out = new byte[total];
        int offset = 0;
        for (final byte[] part : parts) {
            System.arraycopy(part, 0, out, offset, part.length);
            offset += part.length;
        }
        return out;
    }

    private record Writer(MessageLayout layout, UnsafeBuffer buffer) {
        Writer put(final String name, final long value) {
            final FieldLayout field = this.layout.fields().get(name);
            Scalars.write(this.buffer, HEADER + field.offset(), field.primitive(), value);
            return this;
        }

        Writer putFloat(final String name, final float value) {
            this.buffer.putFloat(HEADER + this.layout.fields().get(name).offset(), value, Scalars.ORDER);
            return this;
        }

        Writer putBytes(final String name, final byte[] value) {
            this.buffer.putBytes(HEADER + this.layout.fields().get(name).offset(), value);
            return this;
        }
    }
}
