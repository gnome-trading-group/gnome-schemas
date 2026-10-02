package group.gnometrading.schemas.migration;

/**
 * Every schema version step. Any layout change to {@code schema.xml} means bumping its version to N+1,
 * archiving it as {@code resources/schemas/schema-v{N+1}.xml}, and adding {@code MigrationStep.from(N)}
 * here, with {@code .message(...)} rules for anything the defaults refuse. A test fails the build when
 * {@code schema.xml} drifts from its archived version, since data written by an unbumped layout would be
 * read with the wrong offsets.
 */
final class MigrationSteps {

    private MigrationSteps() {}

    static void registerAll() {
        // v0 → v1: sizes widen from uint32 to int64, which the defaults cover.
        SbeMigrator.register(MigrationStep.from(0));
    }
}
