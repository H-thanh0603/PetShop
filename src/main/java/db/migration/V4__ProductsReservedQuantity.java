package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * sql/03 and sql/15 add products.reserved_quantity using MariaDB's
 * "ADD COLUMN IF NOT EXISTS" syntax, which MySQL 8 rejects — so the column
 * was silently missing on fresh databases and every reserve fell back to
 * the legacy stock-only decrement. Adds it idempotently here.
 */
public class V4__ProductsReservedQuantity extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        if (columnExists(context, "products", "reserved_quantity")) {
            return;
        }
        try (Statement stmt = context.getConnection().createStatement()) {
            stmt.execute("ALTER TABLE products ADD COLUMN reserved_quantity INT NOT NULL DEFAULT 0");
        }
    }

    private boolean columnExists(Context context, String table, String column) throws Exception {
        String sql = "SELECT 1 FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        try (PreparedStatement ps = context.getConnection().prepareStatement(sql)) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
