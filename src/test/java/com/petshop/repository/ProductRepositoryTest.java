package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.Product;
import java.math.BigDecimal;
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
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    private int seedProduct(int stock) {
        int id = repository.addProductAndReturnId("StockProd_" + System.nanoTime(), null,
                BigDecimal.valueOf(100), 0, "desc", stock, 100, "cat", 0);
        assertTrue(id > 0);
        return id;
    }

    @Test
    void reserveStockIncreasesReservedQuantity() {
        int id = seedProduct(10);
        assertTrue(repository.reserveStockAmbient(id, 3));
        Product product = repository.getProductById(id);
        assertEquals(7, product.getStock());
        assertEquals(3, product.getReservedQuantity());
    }

    @Test
    void reserveFinalizeReleaseSequenceKeepsStockConsistent() {
        int id = seedProduct(10);
        assertTrue(repository.reserveStockAmbient(id, 4));
        assertTrue(repository.releaseReservedStockAmbient(id, 1));
        assertTrue(repository.finalizeReservedStockAmbient(id, 3));
        Product product = repository.getProductById(id);
        assertEquals(7, product.getStock());
        assertEquals(0, product.getReservedQuantity());
    }

    @Test
    void findForUpdateLocksRow() {
        int id = seedProduct(5);
        Product product = repository.findForUpdateById(id);
        assertTrue(product != null && product.getId() == id);
    }

    @Test
    void addProductNullNameReturnsFalseOnConstraintViolation() {
        assertEquals(false, repository.addProduct(null, null, BigDecimal.ONE, 0, "desc"));
    }

    @Test
    void getProductByIdFillsReviewAggregates() {
        int id = seedProduct(5);
        Product product = repository.getProductById(id);
        assertTrue(product != null);
        assertEquals(0, product.getReviewCount());
    }
}
