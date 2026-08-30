-- ============================================================
-- Migration: Fix monetary column precision
-- 
-- Changes all monetary columns from DECIMAL(18,0) to DECIMAL(18,2)
-- to support fractional currency values and align with BigDecimal
-- usage in the Java application layer.
--
-- Affected tables and columns:
--   products.price, products.old_price
--   orders.total_amount
--   order_items.price
--   coupons.discount_value, coupons.min_order, coupons.max_discount
--
-- Note: This is a non-destructive change. Existing integer values
-- (e.g., 132000) will be stored as 132000.00 after migration.
-- ============================================================

-- Products table: price and old_price
ALTER TABLE `products`
  MODIFY COLUMN `price` DECIMAL(18,2) DEFAULT NULL,
  MODIFY COLUMN `old_price` DECIMAL(18,2) DEFAULT '0.00';

-- Orders table: total_amount
ALTER TABLE `orders`
  MODIFY COLUMN `total_amount` DECIMAL(18,2) NOT NULL;

-- Order items table: price
ALTER TABLE `order_items`
  MODIFY COLUMN `price` DECIMAL(18,2) NOT NULL;

-- Coupons table: discount_value, min_order, max_discount
ALTER TABLE `coupons`
  MODIFY COLUMN `discount_value` DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  MODIFY COLUMN `min_order` DECIMAL(18,2) DEFAULT 0.00,
  MODIFY COLUMN `max_discount` DECIMAL(18,2) DEFAULT NULL;
