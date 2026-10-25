package group.gnometrading.schemas.migration;

/**
 * Every schema version step, per schema. Any layout change to a schema file ({@code core.xml}, {@code book.xml},
 * ...) means bumping its version to N+1, archiving it as {@code resources/schemas/{schema}-v{N+1}.xml}, and adding
 * {@code MigrationStep.from(schema, N)} here, with {@code .message(...)} rules for anything the defaults refuse. A
 * test fails the build when a schema drifts from its archived version, since data written by an unbumped layout
 * would be read with the wrong offsets.
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
    }
}
