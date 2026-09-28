package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.InventoryBatch;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
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
class InventoryBatchRepositoryTest {

    @Autowired
    private InventoryBatchRepository repository;

    @Autowired
    private ProductRepository products;

    @Autowired
    private javax.sql.DataSource dataSource;

    private int seedProduct() {
        return products.addProductAndReturnId("InvProd_" + System.nanoTime(), null,
                BigDecimal.valueOf(100), 0, "desc", 50, 100, "cat", 0);
    }

    private InventoryBatch batch(int productId, int received, int remaining) {
        InventoryBatch batch = new InventoryBatch();
        batch.setProductId(productId);
        batch.setBatchCode("B-" + System.nanoTime());
        batch.setReceivedAt(new Timestamp(System.currentTimeMillis()));
        batch.setReceivedQuantity(received);
        batch.setRemainingQuantity(remaining);
        batch.setUnitCost(BigDecimal.TEN);
        return batch;
    }

    @Test
    void recordImportBatchPersistsBatchAndBumpsStock() {
        int productId = seedProduct();
        int stockBefore = products.getStock(productId);
        assertTrue(repository.recordImportBatch(batch(productId, 20, 20), 1));
        assertEquals(stockBefore + 20, products.getStock(productId));
        assertTrue(repository.hasTrackedBatchesForProduct(productId));
    }

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @Test
    void failedImportLeavesNoBatchRows() {
        InventoryBatch bad = batch(-999, 5, 5);
        assertEquals(false, repository.recordImportBatch(bad, 1));
        org.springframework.transaction.support.TransactionTemplate readTx =
                new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        readTx.setPropagationBehavior(
                org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        Boolean absent = readTx.execute(status -> repository.findAllocatableBatches(-999).isEmpty()
                || repository.findAllocatableBatchesForProduct(-999).stream()
                        .noneMatch(b -> b.getBatchCode().startsWith("B-") && b.getRemainingQuantity() == 5));
        assertTrue(Boolean.TRUE.equals(absent));
    }

    private int[] seedUserAndOrder() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            long stamp = System.nanoTime();
            stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('invuser_" + stamp + "', 'x')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            int userId;
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                userId = keys.getInt(1);
            }
            stmt.executeUpdate("INSERT INTO orders (user_id, fullname, phone, address, recipient_fullname, recipient_phone, shipping_address, subtotal, shipping_fee, discount_amount, total_amount, status, payment_status) VALUES ("
                    + userId + ", 'U', '090', 'a', 'U', '090', 'a', 100, 0, 0, 100, 'PENDING', 0)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            int orderId;
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                orderId = keys.getInt(1);
            }
            return new int[]{userId, orderId};
        }
    }

    @Test
    void consumeProductStockAllocatesAndDecrements() throws Exception {
        int productId = seedProduct();
        int[] ids = seedUserAndOrder();
        assertTrue(repository.recordImportBatch(batch(productId, 10, 10), ids[0]));
        assertTrue(repository.consumeProductStock(productId, 4, ids[1], ids[0], "order"));
        assertTrue(repository.hasTrackedBatchesForProduct(productId));
        assertEquals(false, repository.consumeProductStock(productId, 100, ids[1], ids[0], "over"));
    }

    @Test
    void consumeBatchStockSingleBatch() throws Exception {
        int productId = seedProduct();
        int[] ids = seedUserAndOrder();
        assertTrue(repository.recordImportBatch(batch(productId, 10, 10), ids[0]));
        List<InventoryBatch> batches = repository.findAllocatableBatchesForProduct(productId);
        assertEquals(1, batches.size());
        assertTrue(repository.consumeBatchStock(batches.get(0).getId(), 3, "ORDER", ids[1], ids[0], "note"));
    }

    @Test
    void aggregatesReturnShapes() {
        int productId = seedProduct();
        repository.recordImportBatch(batch(productId, 10, 10), 1);
        // Batch has no expiry date -> excluded from near-expiry (genuine pin).
        assertTrue(repository.getNearExpiryBatches(365).stream()
                .noneMatch(b -> b.getProductId() == productId));
        Map<Integer, com.petshop.model.ProductAdminInventoryView> views =
                repository.getProductAdminInventoryViews(30);
        assertTrue(views.containsKey(productId));
        assertEquals(10, views.get(productId).getTrackedQuantity());
        assertTrue(repository.getInventoryAgingSnapshots().stream()
                .anyMatch(s -> s.getProductId() == productId));
        // No orders -> zero average sales -> below reorder point (genuine pin).
        assertTrue(repository.getReorderRecommendations(7, 5).stream()
                .noneMatch(r -> r.getProductId() == productId));
    }
}
