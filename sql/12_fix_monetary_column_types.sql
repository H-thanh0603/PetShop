-- ============================================================
-- Migration: Verify and fix monetary column types
--
-- Ensures ALL monetary columns across the schema use DECIMAL(18,2)
-- for fixed-precision arithmetic. This migration is idempotent and
-- safe to run even if 11_fix_monetary_precision.sql was already applied.
--
-- Affected tables and columns:
--   products.price, products.old_price
--   orders.total_amount
--   order_items.price
--   coupons.discount_value, coupons.min_order, coupons.max_discount
--
-- Validates: Requirement 7.3 — monetary columns must use
-- DECIMAL(18,2) or equivalent fixed-precision types, never
-- FLOAT or DOUBLE.
-- ============================================================

USE `petvaccine`;

-- Products table: price and old_price
ALTER TABLE `products`
  MODIFY COLUMN `price` DECIMAL(18,2) DEFAULT NULL,
  MODIFY COLUMN `old_price` DECIMAL(18,2) DEFAULT 0.00;

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
