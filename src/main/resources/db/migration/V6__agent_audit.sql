-- Agentic readiness hardening: persistent audit trail.
-- Idempotent so already-migrated databases apply it cleanly.

CREATE TABLE IF NOT EXISTS ai_agent_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agent VARCHAR(16) NOT NULL,
    action VARCHAR(64) NOT NULL,
    actor VARCHAR(128) NOT NULL DEFAULT '',
    session_key VARCHAR(128) NOT NULL DEFAULT '',
    detail TEXT NULL,
    provider VARCHAR(32) NULL,
    model VARCHAR(128) NULL,
    request_id VARCHAR(32) NULL,
    latency_ms BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_session (session_key),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
