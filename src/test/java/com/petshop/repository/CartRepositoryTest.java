package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.CartItem;
import com.petshop.model.Product;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartRepositoryTest {

    @Autowired
    private CartRepository repository;

    @Autowired
    private ProductRepository products;

    @Autowired
    private javax.sql.DataSource dataSource;

    private int seedUser() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            long stamp = System.nanoTime();
            stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('cartuser_" + stamp + "', 'x')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private int seedProduct(int stock) {
        int id = products.addProductAndReturnId("CartProd_" + System.nanoTime(), null,
                BigDecimal.valueOf(50), 0, "desc", stock, 100, "cat", 0);
        assertTrue(id > 0);
        return id;
    }

    @Test
    void addThenGetCartRoundTrip() throws Exception {
        int userId = seedUser();
        int productId = seedProduct(10);
        repository.addToCart(userId, productId, 2);
        Map<Integer, CartItem> cart = repository.getCartByUserId(userId);
        assertEquals(1, cart.size());
        assertEquals(2, cart.get(productId).getQuantity());
        assertEquals(2, repository.getTotalQuantity(userId));
    }

    @Test
    void updateQuantityAndRemove() throws Exception {
        int userId = seedUser();
        int productId = seedProduct(10);
        repository.addToCart(userId, productId, 2);
        assertTrue(repository.updateCartQuantity(userId, productId, 5));
        assertEquals(5, repository.getCartByUserId(userId).get(productId).getQuantity());
        repository.removeFromCart(userId, productId);
        assertTrue(repository.getCartByUserId(userId).isEmpty());
    }

    @Test
    void clearCartEmptiesAll() throws Exception {
        int userId = seedUser();
        repository.addToCart(userId, seedProduct(10), 1);
        repository.addToCart(userId, seedProduct(10), 3);
        assertEquals(4, repository.getTotalQuantity(userId));
        repository.clearCart(userId);
        assertTrue(repository.getCartByUserId(userId).isEmpty());
        assertEquals(0, repository.getTotalQuantity(userId));
    }

    @Test
    void syncCartFromSessionMergesItems() throws Exception {
        int userId = seedUser();
        int productId = seedProduct(10);
        Product product = products.getProductById(productId);
        Map<Integer, CartItem> sessionCart = new HashMap<>();
        sessionCart.put(productId, new CartItem(product, 3));
        repository.syncCartFromSession(userId, sessionCart);
        assertEquals(3, repository.getCartByUserId(userId).get(productId).getQuantity());
    }

    @Test
    void saveCartItemUpserts() throws Exception {
        int userId = seedUser();
        int productId = seedProduct(10);
        repository.saveCartItem(userId, productId, 2);
        repository.saveCartItem(userId, productId, 5);
        assertEquals(5, repository.getCartByUserId(userId).get(productId).getQuantity());
    }
}
