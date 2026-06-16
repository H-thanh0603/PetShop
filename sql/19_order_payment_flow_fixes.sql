-- ============================================================
-- Migration: Order / Payment flow fixes
-- Idempotent - safe to run more than once.
-- Adds immutable order totals, order item product snapshots, and
-- a unique transfer reference guard for bank-transfer reconciliation.
-- ============================================================

USE petvaccine;

SET SQL_SAFE_UPDATES = 0;

DROP PROCEDURE IF EXISTS sp_order_payment_flow_fixes;
DELIMITER $$
CREATE PROCEDURE sp_order_payment_flow_fixes()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'subtotal'
    ) THEN
        ALTER TABLE orders ADD COLUMN subtotal DECIMAL(18,0) NOT NULL DEFAULT 0 AFTER note;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_fee'
    ) THEN
        ALTER TABLE orders ADD COLUMN shipping_fee DECIMAL(18,0) NOT NULL DEFAULT 0 AFTER subtotal;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'discount_amount'
    ) THEN
        ALTER TABLE orders ADD COLUMN discount_amount DECIMAL(18,0) NOT NULL DEFAULT 0 AFTER shipping_fee;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_items'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'product_name_snapshot'
    ) THEN
        ALTER TABLE order_items ADD COLUMN product_name_snapshot VARCHAR(255) NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_items'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'product_image_snapshot'
    ) THEN
        ALTER TABLE order_items ADD COLUMN product_image_snapshot VARCHAR(255) NULL AFTER product_name_snapshot;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_transactions'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'payment_transactions'
          AND INDEX_NAME = 'uk_payment_transactions_transfer_reference'
    ) AND NOT EXISTS (
        SELECT 1
        FROM (
            SELECT transfer_reference
            FROM payment_transactions
            WHERE transfer_reference IS NOT NULL AND transfer_reference <> ''
            GROUP BY transfer_reference
            HAVING COUNT(*) > 1
            LIMIT 1
        ) duplicate_transfer_reference
    ) THEN
        IF EXISTS (
            SELECT 1 FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'payment_transactions'
              AND INDEX_NAME = 'idx_payment_transactions_transfer_reference'
        ) THEN
            ALTER TABLE payment_transactions DROP INDEX idx_payment_transactions_transfer_reference;
        END IF;
        ALTER TABLE payment_transactions
            ADD UNIQUE KEY uk_payment_transactions_transfer_reference (transfer_reference);
    END IF;
END$$
DELIMITER ;

CALL sp_order_payment_flow_fixes();
DROP PROCEDURE IF EXISTS sp_order_payment_flow_fixes;

UPDATE orders
SET subtotal = GREATEST(COALESCE(total_amount, 0) - COALESCE(shipping_fee, 0) + COALESCE(discount_amount, 0), 0)
WHERE COALESCE(subtotal, 0) = 0;

UPDATE order_items oi
LEFT JOIN products p ON p.id = oi.product_id
SET oi.product_name_snapshot = COALESCE(NULLIF(oi.product_name_snapshot, ''), p.name),
    oi.product_image_snapshot = COALESCE(NULLIF(oi.product_image_snapshot, ''), p.image)
WHERE oi.product_name_snapshot IS NULL
   OR oi.product_name_snapshot = ''
   OR oi.product_image_snapshot IS NULL
   OR oi.product_image_snapshot = '';

SET SQL_SAFE_UPDATES = 1;
