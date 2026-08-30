package DAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

import Context.DBContext;

/**
 * Logs admin write operations to admin_action_log table.
 * Failures are logged but never block the admin operation.
 */
public class AdminActionLogDAO {

    private static final Logger logger = LoggerFactory.getLogger(AdminActionLogDAO.class);


    public void log(int adminId, String actionType, String targetType, Integer targetId, String details) {
        try (Connection conn = DBContext.getConnection()) {
            log(conn, adminId, actionType, targetType, targetId, details);
        } catch (Exception e) {
            logger.warn("[AdminActionLog] Failed to log action: " + e.getMessage());
        }
    }

    public void log(Connection conn, int adminId, String actionType, String targetType,
                    Integer targetId, String details) {
        String sql = "INSERT INTO admin_action_log (admin_id, action_type, target_type, target_id, details) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setString(2, actionType);
            ps.setString(3, targetType);
            if (targetId != null) ps.setInt(4, targetId); else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("[AdminActionLog] Failed to log action: " + e.getMessage());
        }
    }
}
