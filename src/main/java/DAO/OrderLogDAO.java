package DAO;

import Context.DBContext;
import Model.OrderLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class OrderLogDAO {
    private static final Logger log = LoggerFactory.getLogger(OrderLogDAO.class);

    public boolean insert(Connection conn, int orderId, String actorType, Integer actorId,
                          String action, String oldStatus, String newStatus, String note) throws Exception {
        ensureTableExists(conn);
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

    private void ensureTableExists(Connection conn) throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS order_logs (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "order_id INT NOT NULL," +
                "actor_type VARCHAR(50) NOT NULL," +
                "actor_id INT NULL," +
                "action VARCHAR(100) NOT NULL," +
                "old_status VARCHAR(50) NULL," +
                "new_status VARCHAR(50) NULL," +
                "note TEXT NULL," +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "INDEX idx_order_logs_order_created (order_id, created_at)," +
                "INDEX idx_order_logs_actor_created (actor_type, actor_id, created_at)," +
                "CONSTRAINT fk_order_logs_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
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
