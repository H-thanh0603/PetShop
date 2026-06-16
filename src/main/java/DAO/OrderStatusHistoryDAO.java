package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import Context.DBContext;
import Model.OrderStatusHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderStatusHistoryDAO {

    private static final Logger logger = LoggerFactory.getLogger(OrderStatusHistoryDAO.class);

    /**
     * Insert a status history record within an existing transaction.
     * Returns false if insert fails (caller should rollback).
     */
    public boolean insertHistory(Connection conn, int orderId, String oldStatus,
                                  String newStatus, int changedBy) throws Exception {
        String sql = "INSERT INTO order_status_history (order_id, old_status, new_status, changedBy) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, oldStatus);
            ps.setString(3, newStatus);
            ps.setInt(4, changedBy);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Get full status history for an order, newest first.
     */
    public List<OrderStatusHistory> getHistoryByOrderId(int orderId) {
        List<OrderStatusHistory> list = new ArrayList<>();
        String sql = "SELECT h.*, u.fullname AS changed_by_name " +
                     "FROM order_status_history h " +
                     "JOIN users u ON h.changedBy = u.id " +
                     "WHERE h.order_id = ? " +
                     "ORDER BY h.changedAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderStatusHistory h = new OrderStatusHistory();
                    h.setId(rs.getInt("id"));
                    h.setOrderId(rs.getInt("order_id"));
                    h.setOldStatus(rs.getString("old_status"));
                    h.setNewStatus(rs.getString("new_status"));
                    h.setChangedBy(rs.getInt("changedBy"));
                    h.setChangedByName(rs.getString("changed_by_name"));
                    h.setChangedAt(rs.getTimestamp("changedAt"));
                    list.add(h);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching status history for order id={}", orderId, e);
        }
        return list;
    }
}
