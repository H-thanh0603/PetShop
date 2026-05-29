package DAO;

import Context.DBContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SecurityEventDAO {
    private static final Logger log = LoggerFactory.getLogger(SecurityEventDAO.class);

    public void log(String eventType, String principal, String ipAddress, String details) {
        String sql = "INSERT INTO security_events (event_type, principal, ip_address, details) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eventType);
            ps.setString(2, principal);
            ps.setString(3, ipAddress);
            ps.setString(4, details);
            ps.executeUpdate();
        } catch (Exception e) {
            log.error("Failed to write security event {}", eventType, e);
        }
    }
}
