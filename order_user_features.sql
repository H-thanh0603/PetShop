-- ============================================================
-- Migration: Order & User Features
-- Run this script once against the PetShop database
-- ============================================================

-- 1. Order status audit trail table
CREATE TABLE IF NOT EXISTS order_status_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    old_status VARCHAR(50) NOT NULL,
    new_status VARCHAR(50) NOT NULL,
    changed_by INT NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_osh_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_osh_user  FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE RESTRICT
);

-- 2. Admin action log table
CREATE TABLE IF NOT EXISTS admin_action_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id INT,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aal_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- 3. Remember-me tokens table
CREATE TABLE IF NOT EXISTS remember_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
