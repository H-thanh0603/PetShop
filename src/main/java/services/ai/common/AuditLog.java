package services.ai.common;

import Context.DBContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persistent audit trail for agent actions (tool calls, approvals, applies,
 * provider usage). Best-effort: audit failures are logged, never break the
 * turn. Query from the admin console or SQL directly.
 */
public final class AuditLog {
    private static final Logger log = LoggerFactory.getLogger(AuditLog.class);

    private AuditLog() {}

    public static void record(String agent, String action, String actor, String sessionKey,
                              String detail, String provider, String model,
                              String requestId, long latencyMs) {
        String sql = "INSERT INTO ai_agent_audit (agent, action, actor, session_key, detail, "
                + "provider, model, request_id, latency_ms) VALUES (?,?,?,?,?,?,?,?,?)";
        try (var c = DBContext.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, trim(agent, 16));
            ps.setString(2, trim(action, 64));
            ps.setString(3, trim(actor, 128));
            ps.setString(4, trim(sessionKey, 128));
            ps.setString(5, detail == null ? "" : detail.substring(0, Math.min(detail.length(), 2000)));
            ps.setString(6, trim(provider, 32));
            ps.setString(7, trim(model, 128));
            ps.setString(8, trim(requestId, 32));
            ps.setLong(9, latencyMs);
            ps.executeUpdate();
        } catch (Exception e) {
            log.warn("audit record failed (non-fatal)", e);
        }
    }

    private static String trim(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }
}
