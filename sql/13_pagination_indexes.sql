-- ============================================================
-- Migration 13: Pagination Query Performance Indexes
-- Ensures ORDER BY columns used in paginated queries are indexed.
-- Safe to run multiple times (checks existence before creating).
-- ============================================================

SET @db = DATABASE();

-- -------------------------------------------------------
-- orders.createdAt
-- Used in: OrderDAO.getOrdersPage()  ORDER BY createdAt DESC
--          OrderDAO.getAllOrders()    ORDER BY createdAt DESC
-- Status: already created in 03_concurrency_data_integrity.sql
--         (idx_orders_created_at). Included here for completeness
--         and to ensure the index exists if migrations are run
--         out of order.
-- -------------------------------------------------------
SELECT COUNT(*) INTO @idx_exists
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name   = 'orders'
    AND index_name   = 'idx_orders_created_at';
SET @sql = IF(@idx_exists = 0,
  'CREATE INDEX idx_orders_created_at ON orders(createdAt)',
  'SELECT 1 /* idx_orders_created_at already exists */');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------
-- products.discount
-- Used in: ProductDAO.getDiscountedProductsPage()
--            ORDER BY p.discount DESC, p.id DESC
--          ProductDAO.getPopularProductsPage()
--            ORDER BY total_sold DESC, p.discount DESC, p.id DESC
-- Status: NOT previously indexed — new index required.
-- -------------------------------------------------------
SELECT COUNT(*) INTO @idx_exists
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name   = 'products'
    AND index_name   = 'idx_products_discount';
SET @sql = IF(@idx_exists = 0,
  'CREATE INDEX idx_products_discount ON products(discount)',
  'SELECT 1 /* idx_products_discount already exists */');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------
-- products(is_active, discount) — composite covering index
-- Supports the WHERE p.is_active = 1 filter combined with
-- ORDER BY p.discount DESC used in getDiscountedProductsPage().
-- A composite index lets MySQL satisfy both the filter and
-- the sort without a filesort on large tables.
-- -------------------------------------------------------
SELECT COUNT(*) INTO @idx_exists
  FROM information_schema.statistics
  WHERE table_schema = @db
    AND table_name   = 'products'
    AND index_name   = 'idx_products_is_active_discount';
SET @sql = IF(@idx_exists = 0,
  'CREATE INDEX idx_products_is_active_discount ON products(is_active, discount)',
  'SELECT 1 /* idx_products_is_active_discount already exists */');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------
-- products.id (primary key) — already indexed by definition.
-- Used in: ProductDAO.getAllProductsPage()   ORDER BY p.id DESC
--          ProductDAO.getProductsByPage()    ORDER BY p.id DESC
--          ProductDAO.getRelatedProducts()   ORDER BY p.id
-- No action needed; documented here for audit traceability.
-- -------------------------------------------------------
-- SELECT 1; -- p.id is the PRIMARY KEY, always indexed.
