package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WishlistRepositoryTest {

    @Autowired
    private WishlistRepository repository;

    @Autowired
    private ProductRepository products;

    @Autowired
    private javax.sql.DataSource dataSource;

    private int seedUser() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            long stamp = System.nanoTime();
            stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('wishuser_" + stamp + "', 'x')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private int seedProduct() {
        return products.addProductAndReturnId("WishProd_" + System.nanoTime(), null,
                BigDecimal.valueOf(20), 0, "desc", 10, 100, "cat", 0);
    }

    @Test
    void addIsInRemoveRoundTrip() throws Exception {
        int userId = seedUser();
        int productId = seedProduct();
        assertFalse(repository.isInWishlist(userId, productId));
        assertTrue(repository.addToWishlist(userId, productId));
        assertTrue(repository.isInWishlist(userId, productId));
        assertTrue(repository.removeFromWishlist(userId, productId));
        assertFalse(repository.isInWishlist(userId, productId));
    }

    @Test
    void toggleFlipsState() throws Exception {
        int userId = seedUser();
        int productId = seedProduct();
        // toggleWishlist returns the success flag (DAO contract), not the state.
        assertTrue(repository.toggleWishlist(userId, productId));
        assertTrue(repository.isInWishlist(userId, productId));
        assertTrue(repository.toggleWishlist(userId, productId));
        assertFalse(repository.isInWishlist(userId, productId));
    }

    @Test
    void toggleAndReturnStateMatchesContract() throws Exception {
        int userId = seedUser();
        int productId = seedProduct();
        assertTrue(repository.toggleWishlistAndReturnState(userId, productId));
        assertFalse(repository.toggleWishlistAndReturnState(userId, productId));
    }

    @Test
    void productsByUserMarksWishlisted() throws Exception {
        int userId = seedUser();
        int productId = seedProduct();
        repository.addToWishlist(userId, productId);
        List<Product> list = repository.getWishlistProductsByUserId(userId);
        assertEquals(1, list.size());
        assertTrue(list.get(0).isWishlisted());
        Set<Integer> ids = repository.getWishlistProductIdsByUserId(userId);
        assertEquals(Set.of(productId), ids);
    }
}
