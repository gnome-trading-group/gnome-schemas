package group.gnometrading.schemas.migration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * One schema version step for every message template. Defaults cover mechanical changes (see
 * {@link MessagePlanner}); {@link #message} adds explicit rules for one template.
 *
 * <pre>{@code
 * MigrationStep.from("core", 1)
 *         .message("Order", m -> m.rename("qty", "size").multiply("price", 10).drop("legacyFlag"))
 *         .dropMessage("Ohlcv1h");
 * }</pre>
 */
public final class MigrationStep {

    private final SchemaLayout source;
    private final SchemaLayout target;
    private final Map<String, MessageRules> rules = new HashMap<>();
    private final Set<String> droppedMessages = new HashSet<>();

    private MigrationStep(final SchemaLayout source, final SchemaLayout target) {
        if (target.schemaId() != source.schemaId()) {
            throw new IllegalArgumentException(
                    "A step stays in one schema, not " + source.schemaId() + " to " + target.schemaId());
        }
        if (target.version() != source.version() + 1) {
            throw new IllegalArgumentException(
                    "A step goes up one version, not v" + source.version() + " to v" + target.version());
        }
        this.source = source;
        this.target = target;
    }

    /** The step from {@code version} of the named schema (e.g. {@code core}) to the next, from its archives. */
    public static MigrationStep from(final String schema, final int version) {
        return new MigrationStep(
                SchemaLayout.fromResource(archived(schema, version)),
                SchemaLayout.fromResource(archived(schema, version + 1)));
    }

    static MigrationStep between(final SchemaLayout source, final SchemaLayout target) {
        return new MigrationStep(source, target);
    }

    /** The current version of the named schema, e.g. {@code core.xml}. */
    static String current(final String schema) {
        return schema + ".xml";
    }

    static String archived(final String schema, final int version) {
        return "schemas/" + schema + "-v" + version + ".xml";
    }

    /** Explicit rules for one message, by its name in the target version. */
    public MigrationStep message(final String name, final Consumer<MessageRules> configure) {
        configure.accept(this.rules.computeIfAbsent(name, n -> new MessageRules()));
        return this;
    }

    /** Messages of this template (by source name) are removed from migrated data. */
    public MigrationStep dropMessage(final String name) {
        this.droppedMessages.add(name);
        return this;
    }

    SchemaLayout source() {
        return this.source;
    }

    int schemaId() {
        return this.source.schemaId();
    }

    int fromVersion() {
        return this.source.version();
    }

    int toVersion() {
        return this.target.version();
    }

    /** Plans every template; any change the rules do not cover fails here. */
    Map<Integer, MessageConverter> compile() {
        final Map<Integer, MessageConverter> converters = new HashMap<>();
        for (final MessageLayout from : this.source.messages().values()) {
            if (this.droppedMessages.contains(from.name())) {
                converters.put(from.templateId(), new Dropped(from.blockLength()));
                continue;
            }
            final MessageLayout to = this.target.message(from.templateId());
            if (to == null) {
                throw new IllegalStateException("Template " + from.name() + " is gone in v" + toVersion()
                        + "; dropMessage it if its data can go");
            }
            if (!from.varData().equals(to.varData())) {
                throw new IllegalStateException("Template " + from.name() + " changes its variable-length fields in v"
                        + toVersion() + " (" + from.varData() + " to " + to.varData()
                        + "); migration copies them unchanged, so that needs a new template instead");
            }
            converters.put(
                    from.templateId(),
                    MessagePlanner.plan(
                            from, to, this.rules.getOrDefault(to.name(), new MessageRules()), schemaId(), toVersion()));
        }
        for (final String name : this.rules.keySet()) {
            if (this.target.messages().values().stream().noneMatch(m -> m.name().equals(name))) {
                throw new IllegalStateException("Rules name message " + name + ", which v" + toVersion() + " lacks");
            }
        }
        return converters;
    }

    /** Removes the message: writes nothing. */
    private record Dropped(int sourceBlockLength) implements MessageConverter {

        @Override
        public int targetLength() {
            return 0;
        }

        @Override
        public int convert(
                final org.agrona.DirectBuffer src,
                final int srcOffset,
                final org.agrona.MutableDirectBuffer dst,
                final int dstOffset) {
            return 0;
        }
    }
}
