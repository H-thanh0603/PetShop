package Context;

import DAO.DBProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database context using HikariCP connection pool.
 * Pool is initialized once at class load time.
 */
public class DBContext {

    private static final HikariDataSource DATA_SOURCE;

    static {
        HikariConfig config = new HikariConfig();

        String host     = DBProperties.host();
        int    port     = DBProperties.port();
        String dbname   = DBProperties.dbname();
        String username = DBProperties.username();
        String password = DBProperties.password();
        String option   = DBProperties.option();

        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + dbname + "?" + option);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool sizing — tuned for production workloads (Task 11.2 verified)
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(30_000);   // 30 seconds
        config.setIdleTimeout(600_000);         // 10 minutes
        config.setMaxLifetime(1_800_000);       // 30 minutes
        config.setPoolName("PetShopPool");

        // Validation
        config.setConnectionTestQuery("SELECT 1");

        DATA_SOURCE = new HikariDataSource(config);

        runMigrations();
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    /**
     * Shutdown the pool (called on application undeploy).
     */
    public static void shutdown() {
        if (DATA_SOURCE != null && !DATA_SOURCE.isClosed()) {
            DATA_SOURCE.close();
        }
    }

    /**
     * Run schema migrations on startup.
     */
    private static void runMigrations() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE products ADD COLUMN IF NOT EXISTS weight int DEFAULT 0");
            stmt.execute("ALTER TABLE products ADD COLUMN IF NOT EXISTS is_active TINYINT(1) NOT NULL DEFAULT 1");
            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE");
            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_token VARCHAR(255) NULL");
            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_token_expiry TIMESTAMP NULL");
            stmt.execute("CREATE TABLE IF NOT EXISTS payment_transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "order_id INT NOT NULL," +
                    "user_id INT NOT NULL," +
                    "provider_key VARCHAR(50) NOT NULL," +
                    "provider_display_name VARCHAR(100) NULL," +
                    "amount DECIMAL(15,2) NOT NULL," +
                    "currency VARCHAR(10) NOT NULL DEFAULT 'VND'," +
                    "transfer_reference VARCHAR(100) NULL," +
                    "provider_transaction_id VARCHAR(255) NULL," +
                    "status VARCHAR(50) NOT NULL," +
                    "verification_status VARCHAR(50) NULL," +
                    "verification_message VARCHAR(255) NULL," +
                    "provider_metadata TEXT NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "verified_at TIMESTAMP NULL," +
                    "INDEX idx_payment_transactions_order_id (order_id)," +
                    "INDEX idx_payment_transactions_status (status)," +
                    "CONSTRAINT fk_payment_transactions_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_payment_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");
            // Mark all existing users as verified (they registered before email verification was added)
            stmt.execute("UPDATE users SET email_verified = TRUE WHERE email_verified = FALSE AND verification_token IS NULL");
            System.out.println("[DBContext] Migrations applied.");
        } catch (Exception e) {
            System.err.println("[DBContext] Migration warning: " + e.getMessage());
        }
    }
}
