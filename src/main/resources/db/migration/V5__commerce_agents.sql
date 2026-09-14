-- Commerce Agents full port: durable memory + merchant staged-change ledger.
-- Idempotent (IF NOT EXISTS) so legacy-migrated databases apply it cleanly.

CREATE TABLE IF NOT EXISTS ai_customer_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id VARCHAR(128) NOT NULL,
    fact_key VARCHAR(64) NOT NULL,
    fact_value VARCHAR(200) NOT NULL,
    category VARCHAR(16) NOT NULL DEFAULT 'preference',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_memory_subject_key (subject_id, fact_key),
    INDEX idx_memory_subject (subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_merchant_changes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    change_id VARCHAR(16) NOT NULL,
    kind VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'STAGED',
    summary VARCHAR(255) NOT NULL DEFAULT '',
    items_json MEDIUMTEXT NOT NULL,
    guardrail_notes TEXT NULL,
    created_by VARCHAR(128) NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_by VARCHAR(128) NULL,
    approved_at TIMESTAMP NULL,
    applied_by VARCHAR(128) NULL,
    applied_at TIMESTAMP NULL,
    discarded_by VARCHAR(128) NULL,
    discarded_at TIMESTAMP NULL,
    UNIQUE KEY uq_change_id (change_id),
    INDEX idx_change_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
