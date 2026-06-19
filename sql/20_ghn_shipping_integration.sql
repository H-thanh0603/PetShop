-- GHN Shipping Integration: Add columns to orders table for GHN order forwarding
-- This allows GHN shippers to manage and update order status via 5sao.ghn.dev

-- Add GHN columns to orders table (safe to run multiple times)
SET @exist := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'orders' AND column_name = 'ghn_order_id');

SET @sql := IF(@exist = 0,
    'ALTER TABLE orders
        ADD COLUMN ghn_order_id varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT ''GHN order ID from GHN API'' AFTER status_updated_at,
        ADD COLUMN ghn_tracking_code varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT ''GHN tracking/order code'' AFTER ghn_order_id,
        ADD COLUMN ghn_status varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL COMMENT ''Current status from GHN'' AFTER ghn_tracking_code,
        ADD COLUMN ghn_pushed_at timestamp NULL DEFAULT NULL COMMENT ''When order was pushed to GHN'' AFTER ghn_status,
        ADD COLUMN ghn_last_sync_at timestamp NULL DEFAULT NULL COMMENT ''Last time we synced status from GHN'' AFTER ghn_pushed_at,
        ADD COLUMN ghn_error_message text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL COMMENT ''Error message if push to GHN failed'' AFTER ghn_last_sync_at,
        ADD INDEX idx_ghn_order_id (ghn_order_id ASC) USING BTREE,
        ADD INDEX idx_ghn_tracking_code (ghn_tracking_code ASC) USING BTREE,
        ADD INDEX idx_ghn_pushed_at (ghn_pushed_at ASC) USING BTREE',
    'SELECT ''GHN columns already exist'' AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
