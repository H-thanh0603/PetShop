-- ============================================================
-- Migration: Promotions / Flash Sale
-- Idempotent - chạy lại nhiều lần không gây lỗi.
-- Đồng bộ schema với code DAO/Service:
--   promotions(start_date, end_date, ...)
--   promotion_products(id, promotion_id, product_id, sale_quantity, sold_quantity, created_at)
--   order_items thêm cột original_price, final_price, discount_amount,
--                       promotion_id, promotion_name, promotion_type
-- ============================================================

USE petvaccine;

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

-- 1) Bổ sung cột vào order_items (nếu thiếu)
DROP PROCEDURE IF EXISTS sp_upgrade_order_items_promotions;
DELIMITER $$
CREATE PROCEDURE sp_upgrade_order_items_promotions()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'original_price'
    ) THEN
        ALTER TABLE order_items ADD COLUMN original_price DECIMAL(18,0) NOT NULL DEFAULT 0 AFTER price;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'final_price'
    ) THEN
        ALTER TABLE order_items ADD COLUMN final_price DECIMAL(18,0) NOT NULL DEFAULT 0 AFTER original_price;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'discount_amount'
    ) THEN
        ALTER TABLE order_items ADD COLUMN discount_amount DECIMAL(18,0) NOT NULL DEFAULT 0 AFTER final_price;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'promotion_id'
    ) THEN
        ALTER TABLE order_items ADD COLUMN promotion_id INT NULL AFTER discount_amount;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'promotion_name'
    ) THEN
        ALTER TABLE order_items ADD COLUMN promotion_name VARCHAR(255) NULL AFTER promotion_id;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'promotion_type'
    ) THEN
        ALTER TABLE order_items ADD COLUMN promotion_type VARCHAR(50) NULL AFTER promotion_name;
    END IF;
END$$
DELIMITER ;

CALL sp_upgrade_order_items_promotions();
DROP PROCEDURE IF EXISTS sp_upgrade_order_items_promotions;

-- 2) Bảng promotions (tạo nếu chưa có), có thể có schema cũ start_time/end_time → đổi tên
CREATE TABLE IF NOT EXISTS promotions (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    promotion_type VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    discount_type VARCHAR(20) NOT NULL DEFAULT 'PERCENT',
    discount_value DECIMAL(18,0) NOT NULL DEFAULT 0,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_promotions_status_dates (status, start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP PROCEDURE IF EXISTS sp_upgrade_promotions_columns;
DELIMITER $$
CREATE PROCEDURE sp_upgrade_promotions_columns()
BEGIN
    -- Đổi tên start_time → start_date nếu schema cũ còn tồn tại
    IF EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'promotions' AND COLUMN_NAME = 'start_time'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'promotions' AND COLUMN_NAME = 'start_date'
    ) THEN
        ALTER TABLE promotions CHANGE COLUMN start_time start_date DATETIME NOT NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'promotions' AND COLUMN_NAME = 'end_time'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'promotions' AND COLUMN_NAME = 'end_date'
    ) THEN
        ALTER TABLE promotions CHANGE COLUMN end_time end_date DATETIME NOT NULL;
    END IF;

    -- Bỏ cột flash_sale_quantity / flash_sale_sold trên promotions (đã chuyển xuống promotion_products)
    IF EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'promotions' AND COLUMN_NAME = 'flash_sale_quantity'
    ) THEN
        ALTER TABLE promotions DROP COLUMN flash_sale_quantity;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'promotions' AND COLUMN_NAME = 'flash_sale_sold'
    ) THEN
        ALTER TABLE promotions DROP COLUMN flash_sale_sold;
    END IF;
END$$
DELIMITER ;

CALL sp_upgrade_promotions_columns();
DROP PROCEDURE IF EXISTS sp_upgrade_promotions_columns;

-- 3) Bảng promotion_products
CREATE TABLE IF NOT EXISTS promotion_products (
    id INT NOT NULL AUTO_INCREMENT,
    promotion_id INT NOT NULL,
    product_id INT NOT NULL,
    sale_quantity INT NULL,
    sold_quantity INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_promotion_products_pair (promotion_id, product_id),
    KEY idx_promotion_products_product (product_id),
    CONSTRAINT fk_promotion_products_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE,
    CONSTRAINT fk_promotion_products_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP PROCEDURE IF EXISTS sp_upgrade_promotion_products_columns;
DELIMITER $$
CREATE PROCEDURE sp_upgrade_promotion_products_columns()
BEGIN
    -- Thêm các cột nếu schema cũ chưa có
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'promotion_products' AND COLUMN_NAME = 'id'
    ) THEN
        -- Bảng cũ dùng composite PK (promotion_id, product_id), cần thay primary key trước khi thêm id auto_increment
        ALTER TABLE promotion_products DROP PRIMARY KEY;
        ALTER TABLE promotion_products
            ADD COLUMN id INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST,
            ADD UNIQUE KEY uq_promotion_products_pair (promotion_id, product_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'promotion_products' AND COLUMN_NAME = 'sale_quantity'
    ) THEN
        ALTER TABLE promotion_products ADD COLUMN sale_quantity INT NULL AFTER product_id;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'promotion_products' AND COLUMN_NAME = 'sold_quantity'
    ) THEN
        ALTER TABLE promotion_products ADD COLUMN sold_quantity INT NULL AFTER sale_quantity;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'promotion_products' AND COLUMN_NAME = 'created_at'
    ) THEN
        ALTER TABLE promotion_products ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
END$$
DELIMITER ;

CALL sp_upgrade_promotion_products_columns();
DROP PROCEDURE IF EXISTS sp_upgrade_promotion_products_columns;

-- 4) Foreign key order_items.promotion_id → promotions(id)
SET @has_fk_order_items_promotion := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'order_items'
      AND CONSTRAINT_NAME = 'fk_order_items_promotion'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql_add_fk := IF(
    @has_fk_order_items_promotion = 0,
    'ALTER TABLE order_items ADD CONSTRAINT fk_order_items_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE SET NULL',
    'SELECT 1'
);
PREPARE stmt_add_fk FROM @sql_add_fk;
EXECUTE stmt_add_fk;
DEALLOCATE PREPARE stmt_add_fk;

-- 5) Cập nhật giá tham chiếu trong order_items cho dữ liệu cũ (không phá dữ liệu hiện hữu)
UPDATE order_items
SET original_price = COALESCE(NULLIF(original_price, 0), price),
    final_price = COALESCE(NULLIF(final_price, 0), price),
    discount_amount = GREATEST(COALESCE(original_price, price) - COALESCE(final_price, price), 0)
WHERE original_price = 0
   OR final_price = 0;

SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;
