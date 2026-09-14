package services.ai.common;

import Context.DBContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** JDBC MemoryStore over {@code ai_customer_memory} (see V5 migration). */
public class DbMemoryStore implements MemoryStore {
    private static final Logger log = LoggerFactory.getLogger(DbMemoryStore.class);

    @Override
    public List<Fact> getFacts(String subjectId) {
        List<Fact> out = new ArrayList<>();
        String sql = "SELECT fact_key, fact_value, category, updated_at FROM ai_customer_memory "
                + "WHERE subject_id = ? ORDER BY updated_at DESC LIMIT 50";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Fact(rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getTimestamp(4) == null ? 0 : rs.getTimestamp(4).getTime()));
                }
            }
        } catch (Exception e) {
            log.warn("memory read failed", e);
        }
        return out;
    }

    @Override
    public void upsertFacts(String subjectId, List<Fact> facts) {
        String sql = "INSERT INTO ai_customer_memory (subject_id, fact_key, fact_value, category) "
                + "VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE fact_value = VALUES(fact_value), "
                + "category = VALUES(category), updated_at = CURRENT_TIMESTAMP";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (Fact f : facts) {
                ps.setString(1, subjectId);
                ps.setString(2, f.key());
                ps.setString(3, f.value());
                ps.setString(4, f.category());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception e) {
            log.warn("memory write failed", e);
        }
    }

    @Override
    public List<Fact> searchFacts(String subjectId, String query) {
        List<Fact> out = new ArrayList<>();
        String sql = "SELECT fact_key, fact_value, category, updated_at FROM ai_customer_memory "
                + "WHERE subject_id = ? AND (fact_key LIKE ? OR fact_value LIKE ?) LIMIT 10";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, subjectId);
            ps.setString(2, "%" + query + "%");
            ps.setString(3, "%" + query + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Fact(rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getTimestamp(4) == null ? 0 : rs.getTimestamp(4).getTime()));
                }
            }
        } catch (Exception e) {
            log.warn("memory search failed", e);
        }
        return out;
    }

    @Override
    public boolean deleteFact(String subjectId, String key) {
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM ai_customer_memory WHERE subject_id = ? AND fact_key = ?")) {
            ps.setString(1, subjectId);
            ps.setString(2, key);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.warn("memory delete failed", e);
            return false;
        }
    }

    @Override
    public void clear(String subjectId) {
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM ai_customer_memory WHERE subject_id = ?")) {
            ps.setString(1, subjectId);
            ps.executeUpdate();
        } catch (Exception e) {
            log.warn("memory purge failed", e);
        }
    }
}
