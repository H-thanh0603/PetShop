package com.petshop.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Test-only JPA infrastructure: points at the `petshop_test` database
 * (created in Task 1 Step 1), migrates it with the SAME Flyway locations
 * as production, and scans entities. Used via
 * `@Import(JpaTestConfig.class)` on every `@DataJpaTest`.
 */
@TestConfiguration
@EntityScan("com.petshop.model")
@Import({
        com.petshop.repository.CartRepositoryImpl.class,
        com.petshop.repository.WishlistRepositoryImpl.class,
        com.petshop.repository.PromotionRepositoryImpl.class,
        com.petshop.repository.OrderRepositoryImpl.class
})
public class JpaTestConfig {

    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        String pw = System.getenv("PETSHOP_DB_PASSWORD");
        if (pw == null) {
            pw = System.getenv("DB_PASSWORD");
        }
        if (pw == null || pw.isBlank()) {
            throw new IllegalStateException(
                    "Repository tests need PETSHOP_DB_PASSWORD (or DB_PASSWORD) for the local petshop MySQL user");
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/petshop_test"
                + "?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        config.setUsername("petshop");
        config.setPassword(pw);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(5);
        config.setPoolName("PetShopTestPool");
        return new HikariDataSource(config);
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
    }

    @Bean
    public com.petshop.repository.ProductRepositoryHolder productRepositoryHolder(
            jakarta.persistence.EntityManager entityManager) {
        // @DataJpaTest slices exclude @Components, so the holder would stay
        // empty in tests; declare it explicitly.
        com.petshop.repository.ProductRepositoryHolder holder =
                new com.petshop.repository.ProductRepositoryHolder();
        holder.setEntityManager(entityManager);
        return holder;
    }
}
