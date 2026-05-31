-- Optional cleanup after importing data/real-products/paddy-products.sql.
-- Use this if the old demo products are showing wrong/missing images.
-- It does not delete old products; it only hides them from storefront queries.

UPDATE products
SET is_active = 0
WHERE image IS NULL
   OR image = ''
   OR image NOT LIKE 'products/paddy_%';

UPDATE products
SET is_active = 1
WHERE image LIKE 'products/paddy_%';
