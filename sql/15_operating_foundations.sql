ALTER TABLE payment_transactions
    ADD COLUMN expires_at DATETIME NULL;

CREATE TABLE IF NOT EXISTS security_events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    principal VARCHAR(255) NULL,
    ip_address VARCHAR(64) NULL,
    details TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_security_events_type_created (event_type, created_at),
    INDEX idx_security_events_principal_created (principal, created_at)
);

CREATE TABLE IF NOT EXISTS suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255) NULL,
    phone VARCHAR(50) NULL,
    email VARCHAR(255) NULL,
    address VARCHAR(500) NULL,
    notes VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stock_imports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NULL,
    receipt_code VARCHAR(100) NOT NULL,
    received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note TEXT NULL,
    created_by INT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stock_imports_receipt_code (receipt_code),
    CONSTRAINT fk_stock_imports_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE TABLE IF NOT EXISTS inventory_batches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    stock_import_id INT NULL,
    supplier_id INT NULL,
    batch_code VARCHAR(100) NOT NULL,
    received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_date DATE NULL,
    received_quantity INT NOT NULL,
    remaining_quantity INT NOT NULL,
    unit_cost DECIMAL(15,2) NOT NULL DEFAULT 0,
    note VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_inventory_batches_batch_code (batch_code),
    INDEX idx_inventory_batches_product_expiry (product_id, expiry_date),
    INDEX idx_inventory_batches_remaining_received (remaining_quantity, received_at),
    CONSTRAINT fk_inventory_batches_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_inventory_batches_stock_import FOREIGN KEY (stock_import_id) REFERENCES stock_imports(id),
    CONSTRAINT fk_inventory_batches_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE TABLE IF NOT EXISTS stock_movements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    inventory_batch_id INT NULL,
    order_id INT NULL,
    movement_type VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    reference_code VARCHAR(100) NULL,
    note VARCHAR(500) NULL,
    created_by INT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stock_movements_product_created (product_id, created_at),
    INDEX idx_stock_movements_type_created (movement_type, created_at),
    CONSTRAINT fk_stock_movements_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_stock_movements_batch FOREIGN KEY (inventory_batch_id) REFERENCES inventory_batches(id),
    CONSTRAINT fk_stock_movements_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_stock_movements_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_payment_transactions_verification_status
    ON payment_transactions(verification_status, status, created_at);

CREATE INDEX idx_payment_transactions_transfer_reference
    ON payment_transactions(transfer_reference);

CREATE INDEX idx_products_active_category_pet_price
    ON products(is_active, category(100), pet_type_id, price, discount, product_id);

CREATE INDEX idx_orders_status_created_user
    ON orders(status, createdAt, user_id);

CREATE INDEX idx_users_email_username_locked
    ON users(email, username, locked_until);
