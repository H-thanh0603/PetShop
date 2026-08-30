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
    CONSTRAINT fk_payment_transactions_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_payment_transactions_provider_transaction_id (provider_transaction_id),
    UNIQUE KEY uk_payment_transactions_transfer_reference (transfer_reference),
    INDEX idx_payment_transactions_order_id (order_id),
    INDEX idx_payment_transactions_status (status)
);

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
);
