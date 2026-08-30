package Context;

import DAO.DBProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Static connection accessor kept for the legacy DAO layer (247 call sites).
 *
 * In production the DataSource is Spring-managed (see
 * com.petshop.config.DataSourceConfig) and pushed here at startup. Outside
 * Spring (unit tests, tools) a lazily-created HikariCP pool built from
 * DBProperties is used, exactly like the pre-Spring behaviour.
 */
public class DBContext {

    private static volatile javax.sql.DataSource springDataSource;
    private static volatile HikariDataSource fallbackPool;

    public static Connection getConnection() throws SQLException {
        javax.sql.DataSource ds = springDataSource;
        if (ds != null) {
            return ds.getConnection();
        }
        return fallbackPool().getConnection();
    }

    /**
     * Called by the Spring configuration after the context starts.
     */
    public static void setSpringDataSource(javax.sql.DataSource dataSource) {
        springDataSource = dataSource;
    }

    /**
     * Closes the fallback pool (Spring owns and closes its own pool).
     */
    public static void shutdown() {
        HikariDataSource pool = fallbackPool;
        if (pool != null && !pool.isClosed()) {
            pool.close();
        }
    }

    private static synchronized HikariDataSource fallbackPool() {
        if (fallbackPool == null || fallbackPool.isClosed()) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + DBProperties.host() + ":" + DBProperties.port()
                    + "/" + DBProperties.dbname() + "?" + DBProperties.option());
            config.setUsername(DBProperties.username());
            config.setPassword(DBProperties.password());
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(20);
            config.setConnectionTimeout(30_000);
            config.setIdleTimeout(600_000);
            config.setMaxLifetime(1_800_000);
            config.setPoolName("PetShopPool");
            config.setConnectionTestQuery("SELECT 1");
            fallbackPool = new HikariDataSource(config);
        }
        return fallbackPool;
    }
}
