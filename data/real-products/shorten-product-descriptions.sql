-- Keep product cards and detail previews concise for the imported real catalog.
-- Source URL remains in data/real-products/paddy-products.json for traceability.

UPDATE products
SET description = TRIM(SUBSTRING_INDEX(description, 'Nguồn tham khảo:', 1))
WHERE image LIKE 'products/paddy_%';

UPDATE products
SET description = CONCAT(LEFT(description, 257), '...')
WHERE image LIKE 'products/paddy_%'
  AND CHAR_LENGTH(description) > 260;
