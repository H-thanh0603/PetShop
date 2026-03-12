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

-- Table structure for table `blogposts`
DROP TABLE IF EXISTS `blogposts`;
CREATE TABLE `blogposts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_date` varchar(50) DEFAULT NULL,
  `author` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `blogposts` VALUES 
(1,'5 Dấu hiệu nhận biết mèo đang bị stress','blog1.jpg','Tâm lý thú cưng','19/12/2025','Bs. Ngọc Thành','Mèo là loài động vật nhạy cảm...'),
(2,'Lịch tiêm phòng đầy đủ cho cún con từ A-Z','blog2.jpg','Sức khỏe','18/12/2025','Bs. Huyền Trang','Tiêm vaccine là cách tốt nhất...');

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
