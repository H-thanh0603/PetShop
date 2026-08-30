package db.migration;

import Context.LegacySchemaMigrator;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Statement;

/**
 * Flyway wrapper for the legacy idempotent schema migrations.
 *
 * With spring.flyway.baseline-on-migrate=true and baseline-version=0:
 * - a fresh empty database runs this migration and gets the full schema;
 * - an existing (already migrated) database is baselined at version 0 and
 *   then runs this once — every statement is idempotent, so it is a no-op
 *   against an up-to-date schema;
 * - future schema changes go in V2+, V3+ (SQL or Java) and run exactly once.
 */
public class V1__LegacyIdempotentBaseline extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement check = context.getConnection().createStatement()) {
            // Touch the connection so a broken DB fails fast with a clear error
            check.execute("SELECT 1");
        }
        LegacySchemaMigrator.migrate(context.getConnection());
    }
}
