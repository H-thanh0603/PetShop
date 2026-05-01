package DAO;

import Context.DBContext;
import Model.InventoryAgingSnapshot;
import Model.InventoryBatch;
import Model.ReorderRecommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryBatchDAO {

    public boolean recordImportBatch(InventoryBatch batch, Integer actorUserId) {
        String insertBatch = "INSERT INTO inventory_batches " +
                "(product_id, supplier_id, batch_code, received_at, received_quantity, remaining_quantity, unit_cost, expiry_date, note, stock_import_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NULL)";
        String insertMovement = "INSERT INTO stock_movements " +
                "(inventory_batch_id, product_id, movement_type, quantity, reference_code, note, created_by, order_id) " +
                "VALUES (?, ?, 'IMPORT', ?, NULL, ?, ?, NULL)";
        String updateProduct = "UPDATE products SET stock = stock + ? WHERE id = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement batchPs = conn.prepareStatement(insertBatch, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement movementPs = conn.prepareStatement(insertMovement);
                 PreparedStatement productPs = conn.prepareStatement(updateProduct)) {
                bindBatch(batchPs, batch);
                if (batchPs.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                int batchId;
                try (ResultSet keys = batchPs.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        return false;
                    }
                    batchId = keys.getInt(1);
                }

                movementPs.setInt(1, batchId);
                movementPs.setInt(2, batch.getProductId());
                movementPs.setInt(3, batch.getReceivedQuantity());
                if (actorUserId == null) {
                    movementPs.setNull(4, java.sql.Types.INTEGER);
                } else {
                    movementPs.setInt(4, actorUserId);
                }
                movementPs.setString(5, batch.getNote());
                movementPs.executeUpdate();

                productPs.setInt(1, batch.getReceivedQuantity());
                productPs.setInt(2, batch.getProductId());
                productPs.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<InventoryBatch> findAllocatableBatchesForProduct(int productId) {
        String query = "SELECT * FROM inventory_batches " +
                "WHERE product_id = ? AND remaining_quantity > 0 " +
                "AND (expiry_date IS NULL OR expiry_date > NOW()) " +
                "ORDER BY CASE WHEN expiry_date IS NULL THEN 1 ELSE 0 END, expiry_date ASC, received_at ASC, id ASC";
        List<InventoryBatch> batches = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    batches.add(mapBatch(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return batches;
    }

    public boolean consumeBatchStock(int batchId, int quantity, String referenceType, Integer referenceId,
                                     Integer actorUserId, String note) {
        String deductBatch = "UPDATE inventory_batches SET remaining_quantity = remaining_quantity - ? " +
                "WHERE id = ? AND remaining_quantity >= ?";
        String insertMovement = "INSERT INTO stock_movements " +
                "(inventory_batch_id, product_id, movement_type, quantity, reference_code, note, created_by, order_id) " +
                "SELECT id, product_id, 'SALE', ?, ?, ?, ?, ? FROM inventory_batches WHERE id = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement batchPs = conn.prepareStatement(deductBatch);
                 PreparedStatement movementPs = conn.prepareStatement(insertMovement)) {
                batchPs.setInt(1, quantity);
                batchPs.setInt(2, batchId);
                batchPs.setInt(3, quantity);
                if (batchPs.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                movementPs.setInt(1, -quantity);
                movementPs.setString(2, referenceType);
                movementPs.setString(3, note);
                if (actorUserId == null) {
                    movementPs.setNull(4, java.sql.Types.INTEGER);
                } else {
                    movementPs.setInt(4, actorUserId);
                }
                if (referenceId == null) {
                    movementPs.setNull(5, java.sql.Types.INTEGER);
                } else {
                    movementPs.setInt(5, referenceId);
                }
                movementPs.setInt(6, batchId);
                movementPs.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<InventoryBatch> getNearExpiryBatches(int withinDays) {
        String query = "SELECT * FROM inventory_batches " +
                "WHERE remaining_quantity > 0 AND expiry_date IS NOT NULL " +
                "AND expiry_date <= DATE_ADD(NOW(), INTERVAL ? DAY) " +
                "ORDER BY expiry_date ASC, remaining_quantity DESC";
        List<InventoryBatch> batches = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, withinDays);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    batches.add(mapBatch(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return batches;
    }

    public List<InventoryAgingSnapshot> getInventoryAgingSnapshots() {
        String query = "SELECT p.id AS product_id, p.name, " +
                "SUM(CASE WHEN TIMESTAMPDIFF(DAY, ib.received_at, NOW()) <= 1 THEN ib.remaining_quantity ELSE 0 END) AS fresh_quantity, " +
                "SUM(CASE WHEN TIMESTAMPDIFF(DAY, ib.received_at, NOW()) BETWEEN 2 AND 7 THEN ib.remaining_quantity ELSE 0 END) AS one_week_quantity, " +
                "SUM(CASE WHEN TIMESTAMPDIFF(DAY, ib.received_at, NOW()) BETWEEN 8 AND 30 THEN ib.remaining_quantity ELSE 0 END) AS one_month_quantity, " +
                "SUM(CASE WHEN TIMESTAMPDIFF(DAY, ib.received_at, NOW()) > 120 THEN ib.remaining_quantity ELSE 0 END) AS four_month_quantity, " +
                "SUM(CASE WHEN ib.expiry_date IS NOT NULL AND ib.expiry_date <= DATE_ADD(NOW(), INTERVAL 30 DAY) AND ib.expiry_date > NOW() THEN ib.remaining_quantity ELSE 0 END) AS near_expiry_quantity, " +
                "SUM(CASE WHEN ib.expiry_date IS NOT NULL AND ib.expiry_date <= NOW() THEN ib.remaining_quantity ELSE 0 END) AS expired_quantity " +
                "FROM inventory_batches ib " +
                "JOIN products p ON p.id = ib.product_id " +
                "WHERE ib.remaining_quantity > 0 " +
                "GROUP BY p.product_id, p.name " +
                "ORDER BY four_month_quantity DESC, near_expiry_quantity DESC, p.name ASC";
        List<InventoryAgingSnapshot> snapshots = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InventoryAgingSnapshot snapshot = new InventoryAgingSnapshot();
                snapshot.setProductId(rs.getInt("product_id"));
                snapshot.setProductName(rs.getString("name"));
                snapshot.setFreshQuantity(rs.getInt("fresh_quantity"));
                snapshot.setOneWeekQuantity(rs.getInt("one_week_quantity"));
                snapshot.setOneMonthQuantity(rs.getInt("one_month_quantity"));
                snapshot.setFourMonthQuantity(rs.getInt("four_month_quantity"));
                snapshot.setNearExpiryQuantity(rs.getInt("near_expiry_quantity"));
                snapshot.setExpiredQuantity(rs.getInt("expired_quantity"));
                snapshots.add(snapshot);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return snapshots;
    }

    public List<ReorderRecommendation> getReorderRecommendations(int leadTimeDays, int safetyStock) {
        String query = "SELECT p.id AS product_id, p.name, p.stock, " +
                "COALESCE(SUM(CASE WHEN o.createdAt >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN oi.quantity ELSE 0 END), 0) / 30.0 AS average_daily_sales " +
                "FROM products p " +
                "LEFT JOIN order_items oi ON oi.product_id = p.id " +
                "LEFT JOIN orders o ON o.id = oi.order_id AND o.status IN ('Completed', 'Shipping', 'Confirmed') " +
                "GROUP BY p.id, p.name, p.stock " +
                "HAVING average_daily_sales > 0 " +
                "ORDER BY average_daily_sales DESC, p.stock ASC";
        List<ReorderRecommendation> recommendations = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BigDecimal avgDailySales = rs.getBigDecimal("average_daily_sales");
                int reorderPoint = avgDailySales
                        .multiply(BigDecimal.valueOf(leadTimeDays))
                        .setScale(0, RoundingMode.UP)
                        .intValue() + safetyStock;
                int currentStock = rs.getInt("stock");
                if (currentStock > reorderPoint) {
                    continue;
                }
                ReorderRecommendation recommendation = new ReorderRecommendation();
                recommendation.setProductId(rs.getInt("product_id"));
                recommendation.setProductName(rs.getString("name"));
                recommendation.setCurrentStock(currentStock);
                recommendation.setAverageDailySales(avgDailySales);
                recommendation.setLeadTimeDays(leadTimeDays);
                recommendation.setSafetyStock(safetyStock);
                recommendation.setReorderPoint(reorderPoint);
                recommendation.setRecommendedOrderQuantity(Math.max(reorderPoint + safetyStock - currentStock, safetyStock));
                recommendations.add(recommendation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recommendations;
    }

    private void bindBatch(PreparedStatement ps, InventoryBatch batch) throws SQLException {
        ps.setInt(1, batch.getProductId());
        if (batch.getSupplierId() == null) {
            ps.setNull(2, java.sql.Types.INTEGER);
        } else {
            ps.setInt(2, batch.getSupplierId());
        }
        ps.setString(3, batch.getBatchCode());
        ps.setTimestamp(4, batch.getReceivedAt() == null ? Timestamp.valueOf(LocalDateTime.now()) : batch.getReceivedAt());
        ps.setInt(5, batch.getReceivedQuantity());
        ps.setInt(6, batch.getRemainingQuantity() > 0 ? batch.getRemainingQuantity() : batch.getReceivedQuantity());
        ps.setBigDecimal(7, batch.getUnitCost());
        ps.setTimestamp(8, batch.getExpiryDate());
        ps.setString(9, batch.getNote());
    }

    private InventoryBatch mapBatch(ResultSet rs) throws SQLException {
        InventoryBatch batch = new InventoryBatch();
        batch.setId(rs.getInt("id"));
        batch.setProductId(rs.getInt("product_id"));
        int supplierId = rs.getInt("supplier_id");
        batch.setSupplierId(rs.wasNull() ? null : supplierId);
        batch.setBatchCode(rs.getString("batch_code"));
        batch.setReceivedAt(rs.getTimestamp("received_at"));
        batch.setReceivedQuantity(rs.getInt("received_quantity"));
        batch.setRemainingQuantity(rs.getInt("remaining_quantity"));
        batch.setUnitCost(rs.getBigDecimal("unit_cost"));
        batch.setExpiryDate(rs.getTimestamp("expiry_date"));
        batch.setNote(rs.getString("note"));
        batch.setCreatedAt(rs.getTimestamp("created_at"));
        return batch;
    }
}
