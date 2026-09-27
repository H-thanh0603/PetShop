package services.ai.merchant;

import Context.DBContext;
import Util.Json;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
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

    /** Loads staged rows for ledger hydration after a restart. */
    public List<StagedChange> loadStaged() {
        List<StagedChange> out = new ArrayList<>();
        String sql = "SELECT change_id, kind, summary, items_json, created_by, guardrail_notes "
                + "FROM ai_merchant_changes WHERE status = 'STAGED' ORDER BY id ASC LIMIT 200";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                try {
                    StagedChange.Kind kind = StagedChange.Kind.valueOf(rs.getString(2));
                    List<StagedChange.Item> items = new ArrayList<>();
                    JsonNode parsed = Json.MAPPER.readTree(rs.getString(4));
                    // Strict like Gson (review fix): non-array items_json threw
                    // (getAsJsonArray) → row dropped; non-object items threw
                    // (getAsJsonObject) → row dropped; explicit-null fields threw
                    // (getAsString) → row dropped. Missing fields stay "".
                    if (!parsed.isArray()) continue;
                    for (JsonNode el : parsed) {
                        if (!el.isObject()) throw new IllegalArgumentException("not an object");
                        items.add(new StagedChange.Item(
                                !el.has("target") ? "" : gsonString(el.path("target")),
                                !el.has("field") ? "" : gsonString(el.path("field")),
                                !el.has("before") ? "" : gsonString(el.path("before")),
                                !el.has("after") ? "" : gsonString(el.path("after"))));
                    }
                    String notes = rs.getString(6);
                    StagedChange change = new StagedChange(rs.getString(1), kind,
                            rs.getString(3), items, rs.getString(5),
                            notes == null || notes.isBlank() ? List.of() : List.of(notes));
                    out.add(change);
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.warn("merchant staged hydration failed", e);
        }
        return out;
    }

    public boolean isApproved(String changeId) {        try (Connection c = DBContext.getConnection();
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

    public List<ObjectNode> pendingJson() {
        List<ObjectNode> out = new ArrayList<>();
        String sql = "SELECT change_id, kind, summary, items_json, created_by, created_at "
                + "FROM ai_merchant_changes WHERE status = 'STAGED' ORDER BY id DESC LIMIT 50";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ObjectNode o = Json.MAPPER.createObjectNode();
                o.put("changeId", rs.getString(1));
                o.put("kind", rs.getString(2));
                o.put("summary", rs.getString(3));
                try {
                    o.set("items", Json.MAPPER.readTree(rs.getString(4)));
                } catch (Exception e) {
                    o.set("items", Json.MAPPER.createArrayNode());
                }
                o.put("createdBy", rs.getString(5));
                o.put("createdAt", rs.getTimestamp(6) == null ? "" : rs.getTimestamp(6).toString());
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
        ArrayNode arr = Json.MAPPER.createArrayNode();
        for (StagedChange.Item it : items) {
            ObjectNode o = Json.MAPPER.createObjectNode();
            o.put("target", it.target());
            o.put("field", it.field());
            o.put("before", it.before());
            o.put("after", it.after());
            arr.add(o);
        }
        return arr.toString();
    }

    /**
     * Mirrors Gson {@code JsonElement.getAsString}: JSON primitives stringify,
     * but missing / explicit null / objects / arrays throw so corrupt rows are
     * dropped exactly like the Gson code did.
     */
    private static String gsonString(JsonNode n) {
        if (n == null || n.isMissingNode() || n.isNull() || n.isObject() || n.isArray()) {
            throw new IllegalArgumentException("not a JSON primitive");
        }
        return n.asString();
    }
}
