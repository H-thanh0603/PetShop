USE petvaccine;

DROP PROCEDURE IF EXISTS add_col_if_missing;
DELIMITER //
CREATE PROCEDURE add_col_if_missing(
    IN table_name_param VARCHAR(64),
    IN column_name_param VARCHAR(64),
    IN column_definition_param TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = table_name_param
          AND COLUMN_NAME = column_name_param
    ) THEN
        SET @ddl = CONCAT(
            'ALTER TABLE `',
            table_name_param,
            '` ADD COLUMN `',
            column_name_param,
            '` ',
            column_definition_param
        );
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

CALL add_col_if_missing('users', 'failed_login_attempts', 'INT NOT NULL DEFAULT 0');
CALL add_col_if_missing('users', 'locked_until', 'DATETIME NULL DEFAULT NULL');
CALL add_col_if_missing('users', 'email_verified', 'BOOLEAN NOT NULL DEFAULT FALSE');
CALL add_col_if_missing('users', 'verification_token', 'VARCHAR(255) NULL');
CALL add_col_if_missing('users', 'verification_token_expiry', 'TIMESTAMP NULL');

CALL add_col_if_missing('products', 'category', 'VARCHAR(255) NULL');
CALL add_col_if_missing('products', 'pet_type_id', 'INT NULL');
CALL add_col_if_missing('products', 'is_active', 'TINYINT(1) NOT NULL DEFAULT 1');
CALL add_col_if_missing('products', 'stock', 'INT NOT NULL DEFAULT 100');
CALL add_col_if_missing('products', 'stock_quantity', 'INT NOT NULL DEFAULT 0');
CALL add_col_if_missing('products', 'reserved_quantity', 'INT NOT NULL DEFAULT 0');
CALL add_col_if_missing('products', 'sold_quantity', 'INT NOT NULL DEFAULT 0');
CALL add_col_if_missing('products', 'weight', 'INT NOT NULL DEFAULT 0');

CALL add_col_if_missing('orders', 'payment_method', 'VARCHAR(50) DEFAULT ''COD''');
CALL add_col_if_missing('orders', 'payment_status', 'TINYINT(1) NOT NULL DEFAULT 0');
CALL add_col_if_missing('orders', 'createdAt', 'TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP');

DROP PROCEDURE IF EXISTS add_col_if_missing;

CREATE TABLE IF NOT EXISTS pet_types (
    id INT NOT NULL AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50) DEFAULT 'bx-paw',
    display_order INT DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pet_types_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO pet_types (code, name, icon, display_order, is_active) VALUES
('dog', 'Cho', 'bxs-dog', 1, 1),
('cat', 'Meo', 'bxs-cat', 2, 1),
('fish', 'Ca', 'bx-water', 3, 0),
('bird', 'Chim', 'bx-leaf', 4, 0),
('hamster', 'Hamster', 'bx-heart', 5, 0),
('rabbit', 'Tho', 'bx-heart', 6, 0);

UPDATE products SET category = 'Thuc an cho meo' WHERE category IS NULL AND id IN (1, 2, 3, 4, 5);
UPDATE products SET is_active = 1 WHERE is_active IS NULL;
UPDATE products SET stock = 100 WHERE stock IS NULL OR stock <= 0;
UPDATE products SET weight = 300 WHERE weight IS NULL OR weight <= 0;
UPDATE products SET pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1)
WHERE pet_type_id IS NULL AND category LIKE '%meo%';
UPDATE products SET pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1)
WHERE pet_type_id IS NULL AND category LIKE '%cho%';

CREATE TABLE IF NOT EXISTS payment_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    user_id INT NOT NULL,
    provider_key VARCHAR(50) NOT NULL,
    provider_display_name VARCHAR(100) NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    transfer_reference VARCHAR(100) NULL,
    provider_transaction_id VARCHAR(255) NULL,
    status VARCHAR(50) NOT NULL,
    verification_status VARCHAR(50) NULL,
    verification_message VARCHAR(255) NULL,
    provider_metadata TEXT NULL,
    amount_received DECIMAL(15,2) NULL,
    bank_content VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    UNIQUE KEY uk_payment_transactions_provider_transaction_id (provider_transaction_id),
    INDEX idx_payment_transactions_order_id (order_id),
    INDEX idx_payment_transactions_status (status),
    INDEX idx_payment_transactions_verification_status (verification_status),
    INDEX idx_payment_transactions_transfer_reference (transfer_reference),
    CONSTRAINT fk_payment_transactions_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bank_webhook_events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    provider_transaction_id VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    bank_content VARCHAR(500) NULL,
    bank_account VARCHAR(100) NULL,
    payment_transaction_id INT NULL,
    status VARCHAR(50) NOT NULL,
    raw_payload TEXT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bank_webhook_events_provider_transaction_id (provider_transaction_id),
    INDEX idx_bank_webhook_events_status_received (status, received_at),
    INDEX idx_bank_webhook_events_payment_transaction (payment_transaction_id),
    CONSTRAINT fk_bank_webhook_events_payment_transaction
        FOREIGN KEY (payment_transaction_id) REFERENCES payment_transactions(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
