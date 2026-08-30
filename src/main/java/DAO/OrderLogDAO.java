package DAO;

import Context.DBContext;
import Model.OrderLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class OrderLogDAO {
    private static final Logger log = LoggerFactory.getLogger(OrderLogDAO.class);

    public boolean insert(Connection conn, int orderId, String actorType, Integer actorId,
                          String action, String oldStatus, String newStatus, String note) throws Exception {
        // NOTE: no DDL here — CREATE TABLE (even IF NOT EXISTS) triggers an
        // implicit commit in MySQL, which used to commit the caller's checkout
        // transaction mid-flight and leave ghost orders behind on failure.
        // The table is created by the Flyway baseline migration instead.
        String sql = "INSERT INTO order_logs " +
                "(order_id, actor_type, actor_id, action, old_status, new_status, note) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, actorType);
            if (actorId == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, actorId);
            }
            ps.setString(4, action);
            ps.setString(5, oldStatus);
            ps.setString(6, newStatus);
            ps.setString(7, note);
            return ps.executeUpdate() > 0;
        }
    }

    public List<OrderLog> getByOrderId(int orderId) {
        List<OrderLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM order_logs WHERE order_id = ? ORDER BY created_at ASC, id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(map(rs));
                }
            }
        } catch (Exception e) {
            log.error("Error fetching order logs for order id={}", orderId, e);
        }
        return logs;
    }

    private OrderLog map(ResultSet rs) throws Exception {
        OrderLog orderLog = new OrderLog();
        orderLog.setId(rs.getInt("id"));
        orderLog.setOrderId(rs.getInt("order_id"));
        orderLog.setActorType(rs.getString("actor_type"));
        int actorId = rs.getInt("actor_id");
        orderLog.setActorId(rs.wasNull() ? null : actorId);
        orderLog.setAction(rs.getString("action"));
        orderLog.setOldStatus(rs.getString("old_status"));
        orderLog.setNewStatus(rs.getString("new_status"));
        orderLog.setNote(rs.getString("note"));
        orderLog.setCreatedAt(rs.getTimestamp("created_at"));
        return orderLog;
    }
}
