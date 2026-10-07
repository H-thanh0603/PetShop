package com.petshop.repository;

import com.petshop.model.InventoryAgingSnapshot;
import com.petshop.model.InventoryBatch;
import com.petshop.model.ProductAdminInventoryView;
import com.petshop.model.ReorderRecommendation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.LockModeType;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Integer> {

    @Query("SELECT b FROM InventoryBatch b WHERE b.productId = :productId AND b.remainingQuantity > 0 "
            + "AND (b.expiryDate IS NULL OR b.expiryDate > CURRENT_TIMESTAMP) "
            + "ORDER BY CASE WHEN b.expiryDate IS NULL THEN 1 ELSE 0 END, b.expiryDate ASC, b.receivedAt ASC, b.id ASC")
    List<InventoryBatch> findAllocatableBatches(@Param("productId") int productId);

    @Query("SELECT COUNT(b) > 0 FROM InventoryBatch b WHERE b.productId = :productId AND b.remainingQuantity > 0")
    boolean hasTrackedBatches(@Param("productId") int productId);

    @Query("SELECT b FROM InventoryBatch b WHERE b.remainingQuantity > 0 AND b.expiryDate IS NOT NULL "
            + "AND b.expiryDate <= :cutoff ORDER BY b.expiryDate ASC, b.remainingQuantity DESC")
    List<InventoryBatch> findNearExpiry(@Param("cutoff") Timestamp cutoff);

    @Transactional
    default boolean recordImportBatch(InventoryBatch batch, Integer actorUserId) {
        // Multi-statement tx (was manual-tx in the DAO): batch insert + movement
        // insert + product stock bump, all-or-nothing.
        try {
            InventoryBatch managed = new InventoryBatch();
            managed.setProductId(batch.getProductId());
            managed.setSupplierId(batch.getSupplierId());
            managed.setBatchCode(batch.getBatchCode());
            managed.setReceivedAt(batch.getReceivedAt() == null
                    ? Timestamp.valueOf(LocalDateTime.now()) : batch.getReceivedAt());
            managed.setReceivedQuantity(batch.getReceivedQuantity());
            managed.setRemainingQuantity(batch.getRemainingQuantity() > 0
                    ? batch.getRemainingQuantity() : batch.getReceivedQuantity());
            managed.setUnitCost(batch.getUnitCost());
            managed.setExpiryDate(batch.getExpiryDate());
            managed.setNote(batch.getNote());
            managed = saveAndFlush(managed);
            insertMovement(managed.getId(), batch.getProductId(), "IMPORT", batch.getReceivedQuantity(),
                    null, batch.getNote(), actorUserId, null);
            adjustProductStock(batch.getProductId(), batch.getReceivedQuantity());
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(InventoryBatchRepository.class)
                    .error("Unexpected error", e);
            return false;
        }
    }

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "INSERT INTO stock_movements "
            + "(inventory_batch_id, product_id, movement_type, quantity, reference_code, note, created_by, order_id) "
            + "VALUES (:batchId, :productId, :type, :quantity, :reference, :note, :actor, :orderId)",
            nativeQuery = true)
    void insertMovement(@Param("batchId") int batchId, @Param("productId") int productId,
                        @Param("type") String movementType, @Param("quantity") int quantity,
                        @Param("reference") String reference, @Param("note") String note,
                        @Param("actor") Integer actorUserId, @Param("orderId") Integer orderId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE products SET stock = stock + :delta, stock_quantity = stock_quantity + :delta WHERE id = :id",
            nativeQuery = true)
    void adjustProductStock(@Param("id") int productId, @Param("delta") int delta);

    @Transactional
    default List<InventoryBatch> findAllocatableBatchesForProduct(int productId) {
        try {
            return findAllocatableBatches(productId);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(InventoryBatchRepository.class)
                    .error("Unexpected error", e);
            return List.of();
        }
    }

    @Transactional
    default boolean hasTrackedBatchesForProduct(int productId) {
        // Preserved: the old conn-method threw (no ambient version existed);
        // propagate so finalize fails loudly instead of skipping batch consume.
        return hasTrackedBatches(productId);
    }

    @Deprecated(forRemoval = true)
    default boolean hasTrackedBatchesForProduct(java.sql.Connection conn, int productId) {
        // TODO(Task 9): OrderDAO drops conn once @Transactional.
        return hasTrackedBatchesForProduct(productId);
    }

    @Transactional
    default boolean consumeProductStock(int productId, int quantity, int orderId,
                                       int actorUserId, String note) {
        if (quantity <= 0) {
            return false;
        }
        try {
            List<InventoryBatch> batches = findAllocatableBatchesForUpdate(productId);
            List<int[]> allocations = new ArrayList<>();
            int remaining = quantity;
            for (InventoryBatch batch : batches) {
                if (remaining <= 0) {
                    break;
                }
                int allocated = Math.min(batch.getRemainingQuantity(), remaining);
                allocations.add(new int[]{batch.getId(), allocated});
                remaining -= allocated;
            }
            if (remaining > 0) {
                return false;
            }
            for (int[] allocation : allocations) {
                if (deductBatch(allocation[0], allocation[1]) == 0) {
                    throw new IllegalStateException("concurrent batch modification");
                }
                insertMovement(allocation[0], productId, "SALE", -allocation[1],
                        "ORDER-" + orderId, note, actorUserId, orderId);
            }
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(InventoryBatchRepository.class)
                    .error("Unexpected error", e);
            return false;
        }
    }

    @Deprecated(forRemoval = true)
    default boolean consumeProductStock(java.sql.Connection conn, int productId, int quantity, int orderId,
                                       int actorUserId, String note) {
        // TODO(Task 9): OrderDAO drops conn once @Transactional.
        return consumeProductStock(productId, quantity, orderId, actorUserId, note);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM InventoryBatch b WHERE b.productId = :productId AND b.remainingQuantity > 0 "
            + "AND (b.expiryDate IS NULL OR b.expiryDate > CURRENT_TIMESTAMP) "
            + "ORDER BY CASE WHEN b.expiryDate IS NULL THEN 1 ELSE 0 END, b.expiryDate ASC, b.receivedAt ASC, b.id ASC")
    List<InventoryBatch> findAllocatableBatchesForUpdate(@Param("productId") int productId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE InventoryBatch b SET b.remainingQuantity = b.remainingQuantity - :qty "
            + "WHERE b.id = :id AND b.remainingQuantity >= :qty")
    int deductBatch(@Param("id") int batchId, @Param("qty") int quantity);

    @Transactional
    default boolean consumeBatchStock(int batchId, int quantity, String referenceType, Integer referenceId,
                                     Integer actorUserId, String note) {
        try {
            if (deductBatch(batchId, quantity) == 0) {
                return false;
            }
            insertMovement(batchId, productIdOf(batchId), "SALE", -quantity,
                    referenceType, note, actorUserId, referenceId);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(InventoryBatchRepository.class)
                    .error("Unexpected error", e);
            return false;
        }
    }



    default int productIdOf(int batchId) {
        try {
            InventoryBatch batch = findById(batchId).orElse(null);
            return batch == null ? 0 : batch.getProductId();
        } catch (DataAccessException e) {
            return 0;
        }
    }

    @Transactional
    default List<InventoryBatch> getNearExpiryBatches(int withinDays) {
        try {
            return findNearExpiry(new Timestamp(System.currentTimeMillis() + (long) withinDays * 86400000L));
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(InventoryBatchRepository.class)
                    .error("Unexpected error", e);
            return List.of();
        }
    }

    @Transactional
    default List<InventoryAgingSnapshot> getInventoryAgingSnapshots() {
        // Aggregate assembly preserved verbatim from the DAO (pure reads).
        String sql = "SELECT p.id, p.name, "
                + "SUM(CASE WHEN TIMESTAMPDIFF(DAY, ib.received_at, NOW()) <= 1 THEN ib.remaining_quantity ELSE 0 END), "
                + "SUM(CASE WHEN TIMESTAMPDIFF(DAY, ib.received_at, NOW()) BETWEEN 2 AND 7 THEN ib.remaining_quantity ELSE 0 END), "
                + "SUM(CASE WHEN TIMESTAMPDIFF(DAY, ib.received_at, NOW()) BETWEEN 8 AND 30 THEN ib.remaining_quantity ELSE 0 END), "
                + "SUM(CASE WHEN TIMESTAMPDIFF(DAY, ib.received_at, NOW()) > 120 THEN ib.remaining_quantity ELSE 0 END), "
                + "SUM(CASE WHEN ib.expiry_date IS NOT NULL AND ib.expiry_date <= DATE_ADD(NOW(), INTERVAL 30 DAY) AND ib.expiry_date > NOW() THEN ib.remaining_quantity ELSE 0 END), "
                + "SUM(CASE WHEN ib.expiry_date IS NOT NULL AND ib.expiry_date <= NOW() THEN ib.remaining_quantity ELSE 0 END) "
                + "FROM inventory_batches ib JOIN products p ON p.id = ib.product_id "
                + "WHERE ib.remaining_quantity > 0 GROUP BY p.id, p.name "
                + "ORDER BY 6 DESC, 5 DESC, p.name ASC";
        List<InventoryAgingSnapshot> snapshots = new ArrayList<>();
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = ProductRepositoryHolder.entityManager().createNativeQuery(sql).getResultList();
            for (Object[] row : rows) {
                InventoryAgingSnapshot snapshot = new InventoryAgingSnapshot();
                snapshot.setProductId(((Number) row[0]).intValue());
                snapshot.setProductName((String) row[1]);
                snapshot.setFreshQuantity(toInt(row[2]));
                snapshot.setOneWeekQuantity(toInt(row[3]));
                snapshot.setOneMonthQuantity(toInt(row[4]));
                snapshot.setFourMonthQuantity(toInt(row[5]));
                snapshot.setNearExpiryQuantity(toInt(row[6]));
                snapshot.setExpiredQuantity(toInt(row[7]));
                snapshots.add(snapshot);
            }
        } catch (RuntimeException e) {
            LoggerFactory.getLogger(InventoryBatchRepository.class)
                    .error("Unexpected error", e);
        }
        return snapshots;
    }

    @Transactional
    default Map<Integer, ProductAdminInventoryView> getProductAdminInventoryViews(int nearExpiryDays) {
        String sql = "SELECT p.id, "
                + "COUNT(CASE WHEN ib.remaining_quantity > 0 THEN ib.id END), "
                + "COALESCE(SUM(CASE WHEN ib.remaining_quantity > 0 THEN ib.remaining_quantity ELSE 0 END), 0), "
                + "COALESCE(SUM(CASE WHEN ib.remaining_quantity > 0 AND ib.expiry_date IS NOT NULL AND ib.expiry_date <= CURDATE() THEN ib.remaining_quantity ELSE 0 END), 0), "
                + "COALESCE(SUM(CASE WHEN ib.remaining_quantity > 0 AND ib.expiry_date IS NOT NULL AND ib.expiry_date > CURDATE() AND ib.expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) THEN ib.remaining_quantity ELSE 0 END), 0), "
                + "MIN(CASE WHEN ib.remaining_quantity > 0 AND ib.expiry_date IS NOT NULL THEN ib.expiry_date END), "
                + "SUBSTRING_INDEX(GROUP_CONCAT(CASE WHEN ib.remaining_quantity > 0 THEN ib.batch_code END "
                + "ORDER BY CASE WHEN ib.expiry_date IS NULL THEN 1 ELSE 0 END, ib.expiry_date ASC, ib.received_at ASC, ib.id ASC SEPARATOR ','), ',', 1) "
                + "FROM products p LEFT JOIN inventory_batches ib ON ib.product_id = p.id "
                + "WHERE p.is_active = 1 GROUP BY p.id";
        Map<Integer, ProductAdminInventoryView> views = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = ProductRepositoryHolder.entityManager().createNativeQuery(sql)
                    .setParameter(1, nearExpiryDays)
                    .getResultList();
            for (Object[] row : rows) {
                ProductAdminInventoryView view = new ProductAdminInventoryView();
                view.setProductId(((Number) row[0]).intValue());
                view.setActiveBatchCount(toInt(row[1]));
                view.setTrackedQuantity(toInt(row[2]));
                view.setExpiredQuantity(toInt(row[3]));
                view.setNearExpiryQuantity(toInt(row[4]));
                view.setEarliestExpiryDate(toTimestamp(row[5]));
                view.setEarliestBatchCode((String) row[6]);
                views.put(view.getProductId(), view);
            }
        } catch (RuntimeException e) {
            LoggerFactory.getLogger(InventoryBatchRepository.class)
                    .error("Unexpected error", e);
        }
        return views;
    }

    @Transactional
    default List<ReorderRecommendation> getReorderRecommendations(int leadTimeDays, int safetyStock) {
        String sql = "SELECT p.id, p.name, p.stock, "
                + "COALESCE(SUM(CASE WHEN o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN oi.quantity ELSE 0 END), 0) / 30.0 "
                + "FROM products p LEFT JOIN order_items oi ON oi.product_id = p.id "
                + "LEFT JOIN orders o ON o.id = oi.order_id AND o.status IN ('Completed', 'Shipping', 'Confirmed') "
                + "GROUP BY p.id, p.name, p.stock ORDER BY 4 DESC, p.stock ASC";
        List<ReorderRecommendation> recommendations = new ArrayList<>();
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = ProductRepositoryHolder.entityManager().createNativeQuery(sql).getResultList();
            for (Object[] row : rows) {
                java.math.BigDecimal avgDailySales = new java.math.BigDecimal(row[3].toString());
                int reorderPoint = avgDailySales.multiply(java.math.BigDecimal.valueOf(leadTimeDays))
                        .setScale(0, java.math.RoundingMode.UP).intValue() + safetyStock;
                int currentStock = ((Number) row[2]).intValue();
                if (currentStock > reorderPoint) {
                    continue;
                }
                ReorderRecommendation recommendation = new ReorderRecommendation();
                recommendation.setProductId(((Number) row[0]).intValue());
                recommendation.setProductName((String) row[1]);
                recommendation.setCurrentStock(currentStock);
                recommendation.setAverageDailySales(avgDailySales);
                recommendation.setLeadTimeDays(leadTimeDays);
                recommendation.setSafetyStock(safetyStock);
                recommendation.setReorderPoint(reorderPoint);
                recommendation.setRecommendedOrderQuantity(Math.max(reorderPoint + safetyStock - currentStock, safetyStock));
                recommendations.add(recommendation);
            }
        } catch (RuntimeException e) {
            LoggerFactory.getLogger(InventoryBatchRepository.class)
                    .error("Unexpected error", e);
        }
        return recommendations;
    }

    static int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static Timestamp toTimestamp(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp;
        }
        if (value instanceof java.time.LocalDateTime localDateTime) {
            return Timestamp.valueOf(localDateTime);
        }
        if (value instanceof java.sql.Date date) {
            return new Timestamp(date.getTime());
        }
        return Timestamp.valueOf(value.toString());
    }
}
