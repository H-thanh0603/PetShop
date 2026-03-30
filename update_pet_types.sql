-- =====================================================
-- SCRIPT CẬP NHẬT DATABASE ĐỂ HỖ TRỢ NHIỀU LOẠI THÚ CƯNG
-- Chạy script này để mở rộng hệ thống
-- =====================================================

USE `petvaccine`;

-- 1. Tạo bảng pet_types để quản lý các loại thú cưng
CREATE TABLE IF NOT EXISTS `pet_types` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `icon` varchar(50) DEFAULT 'bx-paw',
  `display_order` int DEFAULT 0,
  `is_active` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pet_types_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Thêm dữ liệu mẫu cho pet_types
INSERT IGNORE INTO `pet_types` (`code`, `name`, `icon`, `display_order`, `is_active`) VALUES
('dog', 'Chó', 'bxs-dog', 1, 1),
('cat', 'Mèo', 'bxs-cat', 2, 1),
('fish', 'Cá', 'bx-water', 3, 0),
('bird', 'Chim', 'bx-leaf', 4, 0),
('hamster', 'Hamster', 'bx-heart', 5, 0),
('rabbit', 'Thỏ', 'bx-heart', 6, 0);

-- 3. Thêm cột pet_type_id vào bảng products
-- Kiểm tra và thêm cột nếu chưa có
SET @dbname = 'petvaccine';
SET @tablename = 'products';
SET @columnname = 'pet_type_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE products ADD COLUMN pet_type_id int DEFAULT NULL'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 4. Thêm foreign key (bỏ qua nếu đã tồn tại)
SET @fkname = 'fk_product_pet_type';
SET @preparedStatement2 = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
   WHERE CONSTRAINT_SCHEMA = @dbname AND TABLE_NAME = @tablename AND CONSTRAINT_NAME = @fkname) > 0,
  'SELECT 1',
  'ALTER TABLE products ADD CONSTRAINT fk_product_pet_type FOREIGN KEY (pet_type_id) REFERENCES pet_types(id) ON DELETE SET NULL'
));
PREPARE addFkIfNotExists FROM @preparedStatement2;
EXECUTE addFkIfNotExists;
DEALLOCATE PREPARE addFkIfNotExists;

-- 5. Cập nhật pet_type_id cho các sản phẩm hiện có dựa trên category
UPDATE `products` SET `pet_type_id` = 1 WHERE `category` LIKE '%Chó%' AND `pet_type_id` IS NULL;
UPDATE `products` SET `pet_type_id` = 2 WHERE `category` LIKE '%Mèo%' AND `pet_type_id` IS NULL;

-- 6. Tạo index để tối ưu query (bỏ qua nếu đã tồn tại)
SET @indexname = 'idx_products_pet_type';
SET @preparedStatement3 = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND INDEX_NAME = @indexname) > 0,
  'SELECT 1',
  'CREATE INDEX idx_products_pet_type ON products(pet_type_id)'
));
PREPARE addIndexIfNotExists FROM @preparedStatement3;
EXECUTE addIndexIfNotExists;
DEALLOCATE PREPARE addIndexIfNotExists;

SELECT 'Script hoàn thành!' AS Result;
