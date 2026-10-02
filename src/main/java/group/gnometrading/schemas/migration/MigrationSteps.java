package group.gnometrading.schemas.migration;

/**
 * Every schema version step. Releasing a schema change means archiving the released schema as
 * {@code resources/schemas/schema-v{N}.xml} and adding {@code MigrationStep.from(N)} here, with
 * {@code .message(...)} rules for anything the defaults refuse.
 */
final class MigrationSteps {

    private MigrationSteps() {}

    static void registerAll() {
        // v0 → v1: sizes widen from uint32 to int64, which the defaults cover.
        SbeMigrator.register(MigrationStep.from(0));
    }
}
