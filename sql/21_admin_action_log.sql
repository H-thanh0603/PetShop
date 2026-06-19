-- Admin Action Log table
-- Tracks admin write operations for audit purposes

CREATE TABLE IF NOT EXISTS admin_action_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL COMMENT 'User ID of the admin who performed the action',
    action_type VARCHAR(50) NOT NULL COMMENT 'Type of action (e.g. PUSH_TO_GHN, UPDATE_STATUS)',
    target_type VARCHAR(50) NOT NULL COMMENT 'Type of target entity (e.g. order, product, user)',
    target_id INT NULL COMMENT 'ID of the target entity',
    details TEXT NULL COMMENT 'Additional details about the action',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_id (admin_id ASC),
    INDEX idx_action_type (action_type ASC),
    INDEX idx_target (target_type ASC, target_id ASC),
    INDEX idx_created_at (created_at ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci COMMENT='Audit log for admin write operations';
