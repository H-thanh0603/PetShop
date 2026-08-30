DROP TABLE IF EXISTS `order_signatures`;
DROP TABLE IF EXISTS `certificates`;
DROP TABLE IF EXISTS `order_signs`;

-- Bảng lưu dữ liệu đơn hàng cần ký
CREATE TABLE `order_signs` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `order_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    -- Dữ liệu đơn hàng dạng JSON text (trừ status)
    `order_data` TEXT NOT NULL,
    -- Hàm băm SHA-256 của dữ liệu đơn hàng (hex string 64 ký tự)
    `order_hash` VARCHAR(64) NOT NULL,
    -- Public key dạng Base64 (dùng để verify signature)
    `public_key` TEXT NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_order_sign` (`order_id`) USING BTREE,
    INDEX `idx_order_signs_user` (`user_id`) USING BTREE,
    CONSTRAINT `fk_order_signs_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_order_signs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- Bảng lưu chứng chỉ điện tử
CREATE TABLE `certificates` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `order_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    -- Mã đơn hàng dùng để tạo certificate
    `order_code` VARCHAR(50) NOT NULL,
    -- Chứng chỉ X509 dạng PEM text
    `certificate_data` TEXT NOT NULL,
    -- Subject của certificate
    `cert_subject` VARCHAR(255) NOT NULL,
    -- Ngày hết hạn
    `expires_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_order_cert` (`order_id`) USING BTREE,
    INDEX `idx_certificates_user` (`user_id`) USING BTREE,
    CONSTRAINT `fk_certificates_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_certificates_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- Bảng lưu signature sau khi user upload lên
CREATE TABLE `order_signatures` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `order_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    -- Chữ ký điện tử dạng Base64
    `signature` TEXT NOT NULL,
    -- Trạng thái xác thực: pending / verified / failed
    `verify_status` ENUM('pending', 'verified', 'failed') NOT NULL DEFAULT 'pending',
    -- Thông báo lỗi nếu xác thực thất bại
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
