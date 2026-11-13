package group.gnometrading.schemas.migration;

/**
 * Every schema version step, per schema. Any layout change to a schema file ({@code core.xml}, {@code book.xml},
 * ...) means bumping its version to N+1, archiving it as {@code resources/schemas/{schema}-v{N+1}.xml}, and adding
 * {@code MigrationStep.from(schema, N)} here, with {@code .message(...)} rules for anything the defaults refuse. A
 * test fails the build when a schema drifts from its archived version, since data written by an unbumped layout
 * would be read with the wrong offsets. A message with a repeating group can't be migrated (the step refuses it): drop
 * it if no stored data holds it. Gateway messages have groups, so shipping the gateway journal needs group support
 * here before gateway.xml's next version.
 */
final class MigrationSteps {

    private MigrationSteps() {}

    static void registerAll() {
        // core v0 → v1: sizes widen from uint32 to int64, which the defaults cover. Execution reports trade the
        // gateway-local orderId, which nothing ever read, for the venue's own order ID, which old reports lack.
        SbeMigrator.register(MigrationStep.from(SbeMigrator.CORE, 0)
                .message("OrderExecutionReport", rules -> rules.drop("orderId")));
        // core v1 → v2: market data moved to the book family, keyed by stream rather than exchange and security. A
        // v1 book record can't be keyed without the registry, so stored data was converted once, offline, and none
        // reaches a reader; any that did would have no stream to belong to.
        SbeMigrator.register(MigrationStep.from(SbeMigrator.CORE, 1)
                .dropMessage("Mbo")
                .dropMessage("Mbp10")
                .dropMessage("Mbp1")
                .dropMessage("Bbo1s")
                .dropMessage("Bbo1m")
                .dropMessage("Trades")
                .dropMessage("Ohlcv1s")
                .dropMessage("Ohlcv1m")
                .dropMessage("Ohlcv1h"));
        // core v2 → v3: intents and orders carry what they were a reaction to. Older ones recorded none; the new
        // sequences and timestamps default to null, and the kind is NONE. ShardStatus is never journaled, so no stored
        // one exists, and migration can't carry its repeating group: dropped rather than planned.
        SbeMigrator.register(MigrationStep.from(SbeMigrator.CORE, 2)
                .dropMessage("ShardStatus")
                .message("Intent", rules -> rules.fillEnum("triggerKind", "NONE"))
                .message("Order", rules -> rules.fillEnum("triggerKind", "NONE"))
                .message("ModifyOrder", rules -> rules.fillEnum("triggerKind", "NONE"))
                .message("CancelOrder", rules -> rules.fillEnum("triggerKind", "NONE")));
    }
}
