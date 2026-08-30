package com.petshop.config;

import Context.DBContext;
import DAO.DBProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Owns the connection pool that previously lived in DBContext's static
 * initializer. Settings are identical to the pre-migration pool; the source
 * of truth is still DBProperties (db.properties + environment variables).
 *
 * Flyway is configured explicitly (auto-config proved unreliable with a
 * custom DataSource bean): on boot it baselines existing databases and runs
 * db.migration.V1__LegacyIdempotentBaseline (the old DBContext migrations).
 */
@Configuration
@EnableScheduling
public class DataSourceConfig {

    @Bean
    public HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + DBProperties.host() + ":" + DBProperties.port()
                + "/" + DBProperties.dbname() + "?" + DBProperties.option());
        config.setUsername(DBProperties.username());
        config.setPassword(DBProperties.password());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool sizing — tuned for production workloads
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.setPoolName("PetShopPool");
        config.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(config);
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(HikariDataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
    }

    /**
     * Publishes the Spring-managed pool to the legacy static accessor so the
     * DAO layer uses the same pool as Flyway and future Spring beans.
     */
    @Bean
    public Object dbContextBinder(HikariDataSource dataSource) {
        DBContext.setSpringDataSource(dataSource);
        return new Object();
    }

    /**
     * Bridges the Spring Environment into the legacy static AppConfig so all
     * existing AppConfig.get(...) call sites see yml/env configuration.
     */
    @Bean
    public Object appConfigBridge(Environment environment) {
        Util.AppConfig.setSpringEnvironment(environment);
        return new Object();
    }
}
