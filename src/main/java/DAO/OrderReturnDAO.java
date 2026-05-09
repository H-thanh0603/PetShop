package DAO;

import Context.DBContext;
import Model.OrderReturn;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderReturnDAO {

    public boolean insertReturnRequest(OrderReturn req) {
        String sql        = "INSERT INTO order_returns (order_id, user_id, reason, refund_amount, status) VALUES (?, ?, ?, ?, 'Pending')";
        String updateOrder = "UPDATE orders SET status = 'Return_Requested' WHERE id = ? AND status = 'Delivered'";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = conn.prepareStatement(sql)) {
                    ps1.setInt(1, req.getOrderId());
                    ps1.setInt(2, req.getUserId());
                    ps1.setString(3, req.getReason());
                    ps1.setBigDecimal(4, java.math.BigDecimal.valueOf(req.getRefundAmount()));
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement(updateOrder)) {
                    ps2.setInt(1, req.getOrderId());
                    if (ps2.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateReturnStatus(int returnId, int orderId, String status, String adminComment) {
        String orderStatus = "Approved".equalsIgnoreCase(status) ? "Returned" : "Delivered";
        String sql        = "UPDATE order_returns SET status = ?, admin_comment = ? WHERE id = ?";
        String updateOrder = "UPDATE orders SET status = ? WHERE id = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = conn.prepareStatement(sql)) {
                    ps1.setString(1, status);
                    ps1.setString(2, adminComment);
                    ps1.setInt(3, returnId);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement(updateOrder)) {
                    ps2.setString(1, orderStatus);
                    ps2.setInt(2, orderId);
                    ps2.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<OrderReturn> getAllReturns() {
        List<OrderReturn> list = new ArrayList<>();
        String sql = "SELECT * FROM order_returns ORDER BY created_at DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OrderReturn r = new OrderReturn();
                r.setId(rs.getInt("id"));
                r.setOrderId(rs.getInt("order_id"));
                r.setUserId(rs.getInt("user_id"));
                r.setReason(rs.getString("reason"));
                r.setRefundAmount(rs.getDouble("refund_amount"));
                r.setStatus(rs.getString("status"));
                r.setAdminComment(rs.getString("admin_comment"));
                r.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}