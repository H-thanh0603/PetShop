package services.ai.merchant;

import Context.DBContext;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Persists staged changes (audit trail + approval queue survives restarts). */
public class MerchantChangeDAO {
    private static final Logger log = LoggerFactory.getLogger(MerchantChangeDAO.class);
    private static final Gson GSON = new Gson();

    public void save(StagedChange change) {
        String sql = "INSERT INTO ai_merchant_changes (change_id, kind, status, summary, items_json, "
                + "guardrail_notes, created_by) VALUES (?,?,?,?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE status = VALUES(status), guardrail_notes = VALUES(guardrail_notes), "
                + "approved_by = VALUES(approved_by), approved_at = VALUES(approved_at), "
                + "applied_by = VALUES(applied_by), applied_at = VALUES(applied_at), "
                + "discarded_by = VALUES(discarded_by), discarded_at = VALUES(discarded_at)";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, change.getChangeId());
            ps.setString(2, change.getKind().name());
            ps.setString(3, change.getStatus().name());
            ps.setString(4, change.getSummary());
            ps.setString(5, itemsToJson(change.getItems()));
            ps.setString(6, String.join("; ", change.getGuardrailNotes()));
            ps.setString(7, change.getCreatedBy());
            ps.executeUpdate();
            // Stamp actor/timestamp columns on transitions.
            if (change.getStatus() != StagedChange.Status.STAGED) {
                updateTransition(change);
            }
        } catch (Exception e) {
            log.warn("merchant change persist failed", e);
        }
    }

    public void markApproved(String changeId, String approvedBy) {
        execute("UPDATE ai_merchant_changes SET approved_by = ?, approved_at = CURRENT_TIMESTAMP "
                + "WHERE change_id = ?", approvedBy, changeId);
    }

    private void updateTransition(StagedChange change) {
        if (change.getStatus() == StagedChange.Status.APPLIED) {
            execute("UPDATE ai_merchant_changes SET status = 'APPLIED', applied_by = ?, "
                    + "applied_at = CURRENT_TIMESTAMP WHERE change_id = ?",
                    change.getAppliedBy(), change.getChangeId());
        } else if (change.getStatus() == StagedChange.Status.DISCARDED) {
            execute("UPDATE ai_merchant_changes SET status = 'DISCARDED', discarded_by = ?, "
                    + "discarded_at = CURRENT_TIMESTAMP WHERE change_id = ?",
                    change.getDiscardedBy(), change.getChangeId());
        }
    }

    public boolean isApproved(String changeId) {
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT approved_by FROM ai_merchant_changes WHERE change_id = ?")) {
            ps.setString(1, changeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getString(1) != null;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public List<JsonObject> pendingJson() {
        List<JsonObject> out = new ArrayList<>();
        String sql = "SELECT change_id, kind, summary, items_json, created_by, created_at "
                + "FROM ai_merchant_changes WHERE status = 'STAGED' ORDER BY id DESC LIMIT 50";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                JsonObject o = new JsonObject();
                o.addProperty("changeId", rs.getString(1));
                o.addProperty("kind", rs.getString(2));
                o.addProperty("summary", rs.getString(3));
                try {
                    o.add("items", JsonParser.parseString(rs.getString(4)));
                } catch (Exception e) {
                    o.add("items", new JsonArray());
                }
                o.addProperty("createdBy", rs.getString(5));
                o.addProperty("createdAt", rs.getTimestamp(6) == null ? "" : rs.getTimestamp(6).toString());
                out.add(o);
            }
        } catch (Exception e) {
            log.warn("merchant pending read failed", e);
        }
        return out;
    }

    private void execute(String sql, String arg1, String arg2) {
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, arg1);
            ps.setString(2, arg2);
            ps.executeUpdate();
        } catch (Exception e) {
            log.warn("merchant change update failed", e);
        }
    }

    static String itemsToJson(List<StagedChange.Item> items) {
        JsonArray arr = new JsonArray();
        for (StagedChange.Item it : items) {
            JsonObject o = new JsonObject();
            o.addProperty("target", it.target());
            o.addProperty("field", it.field());
            o.addProperty("before", it.before());
            o.addProperty("after", it.after());
            arr.add(o);
        }
        return arr.toString();
    }
}
