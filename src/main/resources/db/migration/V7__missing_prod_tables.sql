-- V7: heal production schema drift — six tables that legacy migrations
-- intended but which are missing on databases migrated before those
-- statements existed (or where out-of-band history skipped them).
-- All statements are CREATE TABLE IF NOT EXISTS: no existing data is touched.
-- DDL copied verbatim from db/legacy/04_order_user_features.sql,
-- db/legacy/21_admin_action_log.sql and db/legacy/22_digital_signature.sql,
-- except order_signs.private_key which only exists as a migrator addColumn
-- (kept here so fresh copies of this table match production intent).
-- NOTE: the DROP TABLE statements at the top of 22_digital_signature.sql are
-- deliberately NOT included.

CREATE TABLE IF NOT EXISTS `remember_tokens` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `token_hash` VARCHAR(255) NOT NULL,
    `expires_at` TIMESTAMP NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_rt_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `order_status_history` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL,
    `old_status` VARCHAR(50) NOT NULL,
    `new_status` VARCHAR(50) NOT NULL,
    `changed_by` INT NOT NULL,
    `changed_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_osh_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_osh_user` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `admin_action_log` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `admin_id` INT NOT NULL COMMENT 'User ID of the admin who performed the action',
    `action_type` VARCHAR(100) NOT NULL COMMENT 'Type of action (e.g. PUSH_TO_GHN, UPDATE_STATUS)',
    `target_type` VARCHAR(50) NOT NULL COMMENT 'Type of target entity (e.g. order, product, user)',
    `target_id` INT NULL COMMENT 'ID of the target entity',
    `details` TEXT NULL COMMENT 'Additional details about the action',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_admin_id` (`admin_id` ASC),
    INDEX `idx_action_type` (`action_type` ASC),
    INDEX `idx_target` (`target_type` ASC, `target_id` ASC),
    INDEX `idx_created_at` (`created_at` ASC)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci COMMENT = 'Audit log for admin write operations';

CREATE TABLE IF NOT EXISTS `order_signs` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `order_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `order_data` TEXT NOT NULL,
    `order_hash` VARCHAR(64) NOT NULL,
    `public_key` TEXT NOT NULL,
    `private_key` TEXT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_order_sign` (`order_id`) USING BTREE,
    INDEX `idx_order_signs_user` (`user_id`) USING BTREE,
    CONSTRAINT `fk_order_signs_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_order_signs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `certificates` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `order_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `order_code` VARCHAR(50) NOT NULL,
    `certificate_data` TEXT NOT NULL,
    `cert_subject` VARCHAR(255) NOT NULL,
    `expires_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_order_cert` (`order_id`) USING BTREE,
    INDEX `idx_certificates_user` (`user_id`) USING BTREE,
    CONSTRAINT `fk_certificates_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_certificates_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `order_signatures` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `order_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `signature` TEXT NOT NULL,
    `verify_status` ENUM('pending', 'verified', 'failed') NOT NULL DEFAULT 'pending',
    `verify_message` TEXT NULL DEFAULT NULL,
    `verified_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_order_sig` (`order_id`) USING BTREE,
    INDEX `idx_order_sigs_user` (`user_id`) USING BTREE,
    INDEX `idx_order_sigs_status` (`verify_status`) USING BTREE,
    CONSTRAINT `fk_order_sigs_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_order_sigs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;
