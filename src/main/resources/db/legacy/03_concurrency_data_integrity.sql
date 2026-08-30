-- ============================================================
-- Migration: Concurrency & Data Integrity
-- Run this script once against the PetShop database
-- ============================================================

-- 1. Add is_active column to products (soft delete support)
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_active TINYINT(1) NOT NULL DEFAULT 1;

-- 2. Performance indexes (safe to run multiple times via IF NOT EXISTS check)
SET @db = DATABASE();

-- orders indexes
SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'orders' AND index_name = 'idx_orders_user_id';
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_orders_user_id ON orders(user_id)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'orders' AND index_name = 'idx_orders_created_at';
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_orders_created_at ON orders(createdAt)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'orders' AND index_name = 'idx_orders_status';
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_orders_status ON orders(status)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- order_items indexes
SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'order_items' AND index_name = 'idx_order_items_order_id';
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_order_items_order_id ON order_items(order_id)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'order_items' AND index_name = 'idx_order_items_product_id';
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_order_items_product_id ON order_items(product_id)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- reviews indexes
SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'reviews' AND index_name = 'idx_reviews_product_id';
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_reviews_product_id ON reviews(product_id)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'reviews' AND index_name = 'idx_reviews_user_id';
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_reviews_user_id ON reviews(user_id)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- products indexes
SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'products' AND index_name = 'idx_products_pet_type_id';
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_products_pet_type_id ON products(pet_type_id)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'products' AND index_name = 'idx_products_category';
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_products_category ON products(category)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- cart index
SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'cart' AND index_name = 'idx_cart_user_id';
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_cart_user_id ON cart(user_id)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. Foreign key constraint changes: CASCADE -> RESTRICT
--    (Drop existing FKs and re-add with RESTRICT)
--    Note: FK names may vary by installation; use information_schema to find them.

-- order_items.product_id
SET @fk_name = NULL;
SELECT CONSTRAINT_NAME INTO @fk_name
  FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'product_id'
    AND REFERENCED_TABLE_NAME = 'products'
  LIMIT 1;
SET @sql = IF(@fk_name IS NOT NULL,
  CONCAT('ALTER TABLE order_items DROP FOREIGN KEY ', @fk_name),
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'order_items' AND index_name = 'fk_oi_product_id';
SET @sql = IF(@idx_exists = 0,
  'ALTER TABLE order_items ADD CONSTRAINT fk_oi_product_id FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- reviews.product_id
SET @fk_name = NULL;
SELECT CONSTRAINT_NAME INTO @fk_name
  FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'reviews' AND COLUMN_NAME = 'product_id'
    AND REFERENCED_TABLE_NAME = 'products'
  LIMIT 1;
SET @sql = IF(@fk_name IS NOT NULL,
  CONCAT('ALTER TABLE reviews DROP FOREIGN KEY ', @fk_name),
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'reviews' AND index_name = 'fk_reviews_product_id';
SET @sql = IF(@idx_exists = 0,
  'ALTER TABLE reviews ADD CONSTRAINT fk_reviews_product_id FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- orders.user_id
SET @fk_name = NULL;
SELECT CONSTRAINT_NAME INTO @fk_name
  FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'user_id'
    AND REFERENCED_TABLE_NAME = 'users'
  LIMIT 1;
SET @sql = IF(@fk_name IS NOT NULL,
  CONCAT('ALTER TABLE orders DROP FOREIGN KEY ', @fk_name),
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'orders' AND index_name = 'fk_orders_user_id';
SET @sql = IF(@idx_exists = 0,
  'ALTER TABLE orders ADD CONSTRAINT fk_orders_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- cart.user_id
SET @fk_name = NULL;
SELECT CONSTRAINT_NAME INTO @fk_name
  FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'cart' AND COLUMN_NAME = 'user_id'
    AND REFERENCED_TABLE_NAME = 'users'
  LIMIT 1;
SET @sql = IF(@fk_name IS NOT NULL,
  CONCAT('ALTER TABLE cart DROP FOREIGN KEY ', @fk_name),
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'cart' AND index_name = 'fk_cart_user_id';
SET @sql = IF(@idx_exists = 0,
  'ALTER TABLE cart ADD CONSTRAINT fk_cart_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- reviews.user_id
SET @fk_name = NULL;
SELECT CONSTRAINT_NAME INTO @fk_name
  FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'reviews' AND COLUMN_NAME = 'user_id'
    AND REFERENCED_TABLE_NAME = 'users'
  LIMIT 1;
SET @sql = IF(@fk_name IS NOT NULL,
  CONCAT('ALTER TABLE reviews DROP FOREIGN KEY ', @fk_name),
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @idx_exists FROM information_schema.statistics
  WHERE table_schema = @db AND table_name = 'reviews' AND index_name = 'fk_reviews_user_id';
SET @sql = IF(@idx_exists = 0,
  'ALTER TABLE reviews ADD CONSTRAINT fk_reviews_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
