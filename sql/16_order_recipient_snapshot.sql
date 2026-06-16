SET @db = DATABASE();

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'recipient_fullname'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE orders ADD COLUMN recipient_fullname VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER address',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'recipient_phone'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE orders ADD COLUMN recipient_phone VARCHAR(20) NULL AFTER recipient_fullname',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_address'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE orders ADD COLUMN shipping_address VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER recipient_phone',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE orders
SET recipient_fullname = COALESCE(NULLIF(recipient_fullname, ''), fullname),
    recipient_phone = COALESCE(NULLIF(recipient_phone, ''), phone),
    shipping_address = COALESCE(NULLIF(shipping_address, ''), address)
WHERE recipient_fullname IS NULL
   OR recipient_fullname = ''
   OR recipient_phone IS NULL
   OR recipient_phone = ''
   OR shipping_address IS NULL
   OR shipping_address = '';

ALTER TABLE orders
    MODIFY recipient_fullname VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY recipient_phone VARCHAR(20) NOT NULL,
    MODIFY shipping_address VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
