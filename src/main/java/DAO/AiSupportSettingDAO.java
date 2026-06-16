package DAO;

import Context.DBContext;
import Model.AiSupportSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AiSupportSettingDAO {
    private static final Logger log = LoggerFactory.getLogger(AiSupportSettingDAO.class);

    private AiSupportSetting mapRow(ResultSet rs) throws SQLException {
        return new AiSupportSetting(
                rs.getInt("id"),
                rs.getString("setting_key"),
                rs.getString("setting_value"),
                rs.getTimestamp("updated_at")
        );
    }

    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM ai_support_settings WHERE setting_key = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String val = rs.getString("setting_value");
                    return val != null ? val : defaultValue;
                }
            }
        } catch (Exception e) {
            log.error("Error getting setting key={}", key, e);
        }
        return defaultValue;
    }

    public boolean updateSetting(String key, String value) {
        String sql = "UPDATE ai_support_settings SET setting_value = ?, updated_at = CURRENT_TIMESTAMP WHERE setting_key = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setString(2, key);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error updating setting key={}", key, e);
        }
        return false;
    }

    public Map<String, String> getAllSettings() {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT setting_key, setting_value FROM ai_support_settings";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        } catch (Exception e) {
            log.error("Error fetching all settings", e);
        }
        return map;
    }
}
