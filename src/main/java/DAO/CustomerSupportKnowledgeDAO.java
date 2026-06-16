package DAO;

import Context.DBContext;
import Model.CustomerSupportKnowledge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerSupportKnowledgeDAO {
    private static final Logger log = LoggerFactory.getLogger(CustomerSupportKnowledgeDAO.class);

    private CustomerSupportKnowledge mapRow(ResultSet rs) throws SQLException {
        return new CustomerSupportKnowledge(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("category"),
                rs.getString("content"),
                rs.getBoolean("is_active"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );
    }

    public List<CustomerSupportKnowledge> getAllActive() {
        List<CustomerSupportKnowledge> list = new ArrayList<>();
        String sql = "SELECT * FROM customer_support_knowledge WHERE is_active = TRUE ORDER BY id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            log.error("Error fetching active knowledge base", e);
        }
        return list;
    }

    public List<CustomerSupportKnowledge> getAll() {
        List<CustomerSupportKnowledge> list = new ArrayList<>();
        String sql = "SELECT * FROM customer_support_knowledge ORDER BY id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            log.error("Error fetching all knowledge base", e);
        }
        return list;
    }

    public CustomerSupportKnowledge getById(int id) {
        String sql = "SELECT * FROM customer_support_knowledge WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching knowledge base by id={}", id, e);
        }
        return null;
    }

    public boolean create(CustomerSupportKnowledge item) {
        String sql = "INSERT INTO customer_support_knowledge (title, category, content, is_active) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getTitle());
            ps.setString(2, item.getCategory());
            ps.setString(3, item.getContent());
            ps.setBoolean(4, item.isActive());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error creating knowledge base item", e);
        }
        return false;
    }

    public boolean update(CustomerSupportKnowledge item) {
        String sql = "UPDATE customer_support_knowledge SET title = ?, category = ?, content = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getTitle());
            ps.setString(2, item.getCategory());
            ps.setString(3, item.getContent());
            ps.setBoolean(4, item.isActive());
            ps.setInt(5, item.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error updating knowledge base item id={}", item.getId(), e);
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM customer_support_knowledge WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error deleting knowledge base item id={}", id, e);
        }
        return false;
    }
}
