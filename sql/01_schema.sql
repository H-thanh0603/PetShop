CREATE DATABASE IF NOT EXISTS `petvaccine` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `petvaccine`;

-- Table structure for table `users`
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `fullname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `role` varchar(20) DEFAULT 'user',
  `status` varchar(20) DEFAULT 'active',
  `phone` varchar(20) DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `has_used_discount` tinyint(1) NOT NULL DEFAULT 0,
  `reset_token` varchar(255) DEFAULT NULL,
  `reset_token_expiry` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `users` (`username`, `password`, `fullname`, `email`, `role`, `status`, `phone`, `address`) VALUES
('admin', 'Admin@123', 'Quản trị viên', 'admin@gmail.com', 'admin', 'active', '0901234567', 'Số 1 Đường ABC, Quận 1, TP.HCM'),
('user1', 'Thanh@123', 'Nguyễn Văn A', 'user1@gmail.com', 'user', 'active', '0904567890', 'Số 10 Nguyễn Huệ, Quận 1, TP.HCM');

-- Table structure for table `products`
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` decimal(18,0) DEFAULT NULL,
  `old_price` decimal(18,0) DEFAULT '0',
  `discount` int DEFAULT '0',
  `description` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `products` VALUES 
(1,'Thức Ăn Hạt Cho Mèo Trưởng Thành Nuôi Trong Nhà Royal Canin Indoor 27','prod_royal1.jpg',132000,0,0,'Thức ăn hạt cao cấp dành cho mèo trưởng thành nuôi trong nhà'),
(2,'Thức Ăn Hạt Cho Mèo Con Royal Canin Kitten 36','prod_royal2.jpg',135000,0,0,'Thức ăn hạt dinh dưỡng cho mèo con từ 4-12 tháng tuổi'),
(3,'Thức Ăn Hạt Cho Mèo Sỏi Thận Royal Canin Urinary S/O','prod_royal3.jpg',185000,0,0,'Thức ăn chuyên dụng hỗ trợ điều trị sỏi thận cho mèo'),
(4,'Pate Cho Mèo Trưởng Thành Royal Canin Instinctive 85g','prod_royal4.jpg',27000,34000,20,'Pate thơm ngon cho mèo trưởng thành'),
(5,'Pate Cho Mèo Con Royal Canin Kitten Instinctive 85g','prod_royal5.jpg',30000,0,0,'Pate dinh dưỡng cho mèo con');

-- Table structure for table `orders`
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `fullname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) NOT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `recipient_fullname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `recipient_phone` varchar(20) NOT NULL,
  `shipping_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `total_amount` decimal(18,0) NOT NULL,
  `status` varchar(50) DEFAULT 'Pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table structure for table `order_items`
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL,
  `price` decimal(18,0) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table structure for table `features`
DROP TABLE IF EXISTS `features`;
CREATE TABLE `features` (
  `id` int NOT NULL AUTO_INCREMENT,
  `icon` varchar(100) NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `features` VALUES 
(1,'bx bxs-graduation','Đội ngũ chuyên gia quốc tế','Các bác sĩ được đào tạo bài bản từ các trường đại học danh tiếng.'),
(2,'bx bxs-heart','Chăm sóc bằng cả trái tim','Chúng tôi đối xử với thú cưng của bạn như chính thú cưng của mình.'),
(3,'bx bxs-first-aid','Trang thiết bị tối tân','Hệ thống máy móc chẩn đoán hình ảnh và xét nghiệm hiện đại nhất.');

-- =====================================================
-- Bảng pet_types - Quản lý loại thú cưng (mở rộng)
-- =====================================================
DROP TABLE IF EXISTS `pet_types`;
CREATE TABLE `pet_types` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `icon` varchar(50) DEFAULT 'bx-paw',
  `display_order` int DEFAULT 0,
  `is_active` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pet_types_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `pet_types` (`code`, `name`, `icon`, `display_order`, `is_active`) VALUES
('dog', 'Chó', 'bxs-dog', 1, 1),
('cat', 'Mèo', 'bxs-cat', 2, 1),
('fish', 'Cá', 'bx-water', 3, 0),
('bird', 'Chim', 'bx-leaf', 4, 0),
('hamster', 'Hamster', 'bx-heart', 5, 0),
('rabbit', 'Thỏ', 'bx-heart', 6, 0);

-- Thêm cột pet_type_id, category, stock vào products
ALTER TABLE `products`
  ADD COLUMN IF NOT EXISTS `category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `pet_type_id` int DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `stock` int DEFAULT 100;

ALTER TABLE `products`
  ADD CONSTRAINT `fk_product_pet_type` FOREIGN KEY (`pet_type_id`) REFERENCES `pet_types`(`id`) ON DELETE SET NULL;

-- =====================================================
-- Bảng cart - Giỏ hàng lưu trong database
-- =====================================================
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT 1,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_product` (`user_id`, `product_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Bảng reviews - Đánh giá sản phẩm
-- =====================================================
DROP TABLE IF EXISTS `reviews`;
CREATE TABLE `reviews` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `user_id` int NOT NULL,
  `rating` int NOT NULL,
  `comment` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_product_review` (`user_id`, `product_id`),
  FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Bảng wishlist - Sản phẩm yêu thích
-- =====================================================
DROP TABLE IF EXISTS `wishlist`;
CREATE TABLE `wishlist` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_product_wishlist` (`user_id`, `product_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Bảng coupons - Mã giảm giá
-- =====================================================
DROP TABLE IF EXISTS `coupons`;
CREATE TABLE `coupons` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL UNIQUE,
  `discount_type` enum('percent', 'fixed') DEFAULT 'percent',
  `discount_value` decimal(18,0) NOT NULL DEFAULT 0,
  `discount_percent` int DEFAULT 0,
  `min_order` decimal(18,0) DEFAULT 0,
  `max_discount` decimal(18,0) DEFAULT NULL,
  `usage_limit` int DEFAULT NULL,
  `used` int DEFAULT 0,
  `quantity` int DEFAULT 0,
  `start_date` timestamp NULL,
  `end_date` timestamp NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `coupons` (`code`, `discount_type`, `discount_value`, `discount_percent`, `min_order`, `max_discount`, `usage_limit`, `used`, `quantity`, `is_active`) VALUES
('WELCOME10', 'percent', 10, 10, 100000, 50000, 100, 0, 100, 1),
('SAVE50K', 'fixed', 50000, 0, 500000, NULL, 50, 0, 50, 1);

-- =====================================================
-- Bảng notifications - Thông báo đẩy
-- =====================================================
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci,
  `type` varchar(50) DEFAULT 'system',
  `link` varchar(500) DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
