/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 100432 (10.4.32-MariaDB)
 Source Host           : localhost:3306
 Source Schema         : petvaccine

 Target Server Type    : MySQL
 Target Server Version : 100432 (10.4.32-MariaDB)
 File Encoding         : 65001

 Date: 15/06/2026 18:31:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for addresses
-- ----------------------------
DROP TABLE IF EXISTS `addresses`;
CREATE TABLE `addresses`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `defaultt` tinyint(1) NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `province` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `district` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `ward` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `is_default` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `addresses_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of addresses
-- ----------------------------
INSERT INTO `addresses` VALUES (9, 8, 1, '2026-03-28 09:30:14', 'Nhà số 30', 'Tỉnh Yên Bái', 'Huyện Lục Yên', 'Xã Minh Xuân', 1);
INSERT INTO `addresses` VALUES (27, 9, 0, '2026-03-28 12:37:58', 'nhà số 36', 'Tỉnh Phú Thọ', 'Huyện Lâm Thao', 'Xã Tứ Xã', 0);
INSERT INTO `addresses` VALUES (28, 9, 0, '2026-03-28 12:38:08', 'Cư xá C, đại học Nông Lâm TPHCM', 'Thành phố Hà Nội', 'Huyện Gia Lâm', 'Xã Đa Tốn', 0);
INSERT INTO `addresses` VALUES (29, 9, 1, '2026-03-28 12:50:45', 'Cư xá C, đại học Nông Lâm TPHCM', 'Tỉnh Cao Bằng', 'Huyện Hà Quảng', 'Xã Cải Viên', 0);
INSERT INTO `addresses` VALUES (30, 10, 0, '2026-03-28 13:21:06', 'Nhà văn hóa Sinh Viên làng đại học', 'Thành phố Hồ Chí Minh', 'Thành phố Thủ Đức', 'Phường Linh Trung', 0);
INSERT INTO `addresses` VALUES (31, 11, 1, '2026-03-30 04:57:05', 'Nhà số 1', 'Thành phố Hà Nội', 'Quận Hoàn Kiếm', 'Phường Phúc Tân', 0);
INSERT INTO `addresses` VALUES (36, 11, 0, '2026-03-29 10:06:03', 'Cư xá C, đại học Nông Lâm TPHCM', 'Tỉnh Điện Biên', 'Huyện Điện Biên', 'Xã Núa Ngam', 0);
INSERT INTO `addresses` VALUES (37, 10, 1, '2026-03-30 06:15:37', 'Dai hoc nong lam', 'Tỉnh Long An', 'Huyện Cần Giuộc', 'Xã Long An', 0);
INSERT INTO `addresses` VALUES (38, 11, 0, '2026-03-30 09:48:02', 'Cư xá C, đại học Nông Lâm TPHCM', 'Thành phố Hà Nội', 'Quận Hoàn Kiếm', 'Phường Phúc Tân', 0);
INSERT INTO `addresses` VALUES (39, 8, 0, '2026-03-30 10:35:38', '(*&(*&*&*^*&%^$&', 'Thành phố Hà Nội', 'Quận Nam Từ Liêm', 'Phường Mễ Trì', 0);
INSERT INTO `addresses` VALUES (40, 2, 0, '2026-04-08 06:48:54', 'Phố 29', 'Thành phố Hà Nội', 'Quận Ba Đình', 'Phường Ngọc Hà', 0);
INSERT INTO `addresses` VALUES (41, 2, 0, '2026-04-08 06:28:32', 'Phố 30', 'Tỉnh Hà Giang', 'Huyện Bắc Quang', 'Xã Việt Hồng', 0);
INSERT INTO `addresses` VALUES (42, 2, 1, '2026-04-08 06:49:00', 'phố 30', 'Thành phố Hà Nội', 'Quận Hai Bà Trưng', 'Phường Nguyễn Du', 1);
INSERT INTO `addresses` VALUES (43, 2, 0, '2026-06-10 11:34:18', 'Nhà số 8', 'Tỉnh Điện Biên', 'Thành phố Điện Biên Phủ', 'Phường Mường Thanh', 0);

-- ----------------------------
-- Table structure for appointments
-- ----------------------------
DROP TABLE IF EXISTS `appointments`;
CREATE TABLE `appointments`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL,
  `customer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `pet_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `pet_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'Chó',
  `service_id` int NULL DEFAULT NULL,
  `doctor_id` int NULL DEFAULT NULL,
  `booking_date` date NOT NULL,
  `note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'Pending',
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `service_id`(`service_id` ASC) USING BTREE,
  INDEX `doctor_id`(`doctor_id` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `appointments_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of appointments
-- ----------------------------
INSERT INTO `appointments` VALUES (1, 2, 'Nguyễn Văn A', '0112233445', 'kiki', 'Chó', 1, 1, '2025-12-24', 'Khám sức khỏe tổng quát', 'Pending', '2026-03-06 18:02:49');
INSERT INTO `appointments` VALUES (2, 2, 'Nguyễn Văn A', '0112233445', 'kiki', 'Mèo', 1, 1, '2025-12-24', 'Khám lại sau điều trị', 'Confirmed', '2026-03-06 18:02:49');
INSERT INTO `appointments` VALUES (3, NULL, 'Phạm Văn B', '0112233442', 'mx', 'Mèo', 3, 12, '2026-01-09', 'Tiêm vaccine định kỳ', 'Pending', '2026-03-06 18:02:49');

-- ----------------------------
-- Table structure for bank_webhook_events
-- ----------------------------
DROP TABLE IF EXISTS `bank_webhook_events`;
CREATE TABLE `bank_webhook_events`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `provider_transaction_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `amount` decimal(15, 2) NOT NULL,
  `bank_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `bank_account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `payment_transaction_id` int NULL DEFAULT NULL,
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `raw_payload` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL,
  `received_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_bank_webhook_events_provider_transaction_id`(`provider_transaction_id` ASC) USING BTREE,
  INDEX `idx_bank_webhook_events_status_received`(`status` ASC, `received_at` ASC) USING BTREE,
  INDEX `idx_bank_webhook_events_payment_transaction`(`payment_transaction_id` ASC) USING BTREE,
  CONSTRAINT `fk_bank_webhook_events_payment_transaction` FOREIGN KEY (`payment_transaction_id`) REFERENCES `payment_transactions` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bank_webhook_events
-- ----------------------------

-- ----------------------------
-- Table structure for blogposts
-- ----------------------------
DROP TABLE IF EXISTS `blogposts`;
CREATE TABLE `blogposts`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_date` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `author` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of blogposts
-- ----------------------------
INSERT INTO `blogposts` VALUES (1, '5 Dấu hiệu nhận biết mèo đang bị stress', 'blog1.jpg', 'Tâm lý thú cưng', '19/12/2025', 'Bs. Ngọc Thành', 'Mèo là loài động vật nhạy cảm. Thay đổi môi trường sống hoặc tiếng ồn lớn có thể khiến chúng bị stress nặng. Hãy cùng tìm hiểu các dấu hiệu...');
INSERT INTO `blogposts` VALUES (2, 'Lịch tiêm phòng đầy đủ cho cún con từ A-Z', 'blog2.jpg', 'Sức khỏe', '18/12/2025', 'Bs. Huyền Trang', 'Tiêm vaccine là cách tốt nhất để bảo vệ cún cưng khỏi các bệnh nguy hiểm như Care, Parvo. Đừng bỏ lỡ các mốc thời gian vàng này nhé.');
INSERT INTO `blogposts` VALUES (3, 'Review các loại cát vệ sinh tốt nhất hiện nay', 'blog3.jpg', 'Review Sản phẩm', '15/12/2025', 'Admin', 'Cát đất sét, cát gỗ hay cát đậu nành? Loại nào khử mùi tốt nhất và tiết kiệm nhất? Bài viết này sẽ giúp bạn chọn được loại cát chân ái.');
INSERT INTO `blogposts` VALUES (4, 'Câu chuyện cảm động về chú chó Hachiko', 'blog4.jpg', 'Chuyện bên lề', '10/12/2025', 'Sưu tầm', 'Lòng trung thành của loài chó luôn là điều khiến con người rơi nước mắt. Cùng đọc lại câu chuyện kinh điển về Hachiko đợi chủ.');
INSERT INTO `blogposts` VALUES (5, 'Chế độ dinh dưỡng cho mèo bị sỏi thận', 'blog5.jpg', 'Dinh dưỡng', '05/12/2025', 'Bs. Mai Phạm', 'Mèo bị sỏi thận cần kiêng ăn gì? Tại sao nên cho ăn nhiều thức ăn ướt hơn hạt khô? Lời khuyên từ chuyên gia dinh dưỡng.');
INSERT INTO `blogposts` VALUES (6, 'Top 10 loại thực phẩm của người TUYỆT ĐỐI không cho chó mèo ăn', 'blog6.jpg', 'Dinh dưỡng', '01/12/2025', 'Bs. Ngọc Thành', 'Socola, hành tây, nho... là những món ăn quen thuộc với con người nhưng lại là thuốc độc với thú cưng. Cùng điểm danh để tránh xa nhé.');
INSERT INTO `blogposts` VALUES (7, 'Ve, rận và bọ chét: Kẻ thù thầm lặng và cách tiêu diệt tận gốc', 'blog7.jpg', 'Sức khỏe', '28/11/2025', 'Bs. Sterenn Genewe', 'Mùa ẩm ướt là thời điểm ve rận sinh sôi mạnh nhất. Chúng không chỉ gây ngứa mà còn truyền ký sinh trùng máu nguy hiểm. Đây là giải pháp cho bạn.');
INSERT INTO `blogposts` VALUES (8, 'Sốc nhiệt ở chó: Dấu hiệu nhận biết và cách sơ cứu khẩn cấp', 'blog8.jpg', 'Cấp cứu', '25/11/2025', 'Bs. Mai Phạm', 'Chó không thể toát mồ hôi như người. Khi nhiệt độ tăng cao, sốc nhiệt có thể cướp đi tính mạng cún cưng chỉ trong vài phút. Hãy học cách sơ cứu ngay.');
INSERT INTO `blogposts` VALUES (9, 'Hướng dẫn cắt móng cho mèo tại nhà mà không bị \"hoàng thượng\" cào', 'blog9.jpg', 'Chăm sóc', '20/11/2025', 'Admin', 'Việc cắt móng cho mèo thường là cuộc chiến đẫm máu. Bài viết này sẽ chia sẻ mẹo nhỏ giúp mèo ngoan ngoãn nằm im cho bạn cắt tỉa.');
INSERT INTO `blogposts` VALUES (10, 'Tại sao chó sủa nhiều? Bí quyết huấn luyện để giảm tiếng ồn', 'blog10.jpg', 'Huấn luyện', '15/11/2025', 'Huấn luyện viên', 'Chó sủa khi có người lạ, sủa khi ở một mình hay sủa vì đòi ăn? Hiểu rõ nguyên nhân sẽ giúp bạn có phương pháp điều chỉnh hành vi hiệu quả.');
INSERT INTO `blogposts` VALUES (11, 'Giải mã tiếng \"Grừ..ừ\" (Purr) bí ẩn của loài mèo', 'blog11.jpg', 'Chuyện bên lề', '10/11/2025', 'Sưu tầm', 'Mèo rừ khi vui, nhưng đôi khi cũng rừ khi đau đớn. Tần số tiếng rừ của mèo còn được chứng minh là có khả năng chữa lành xương khớp.');
INSERT INTO `blogposts` VALUES (12, 'Nên nuôi mèo trong nhà hay thả rông? Lợi ích và rủi ro', 'blog12.jpg', 'Góc nhìn', '05/11/2025', 'Bs. Quốc Trí', 'Tranh cãi muôn thuở của cộng đồng yêu mèo. Nuôi trong nhà thì an toàn nhưng sợ mèo buồn? Thả rông thì tự do nhưng nhiều nguy hiểm?');

-- ----------------------------
-- Table structure for careitems
-- ----------------------------
DROP TABLE IF EXISTS `careitems`;
CREATE TABLE `careitems`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of careitems
-- ----------------------------
INSERT INTO `careitems` VALUES (1, 'Đồng hành cùng với bạn', '<p>Thú cưng của bạn không thể cho chúng ta biết bất cứ điều gì về cuộc sống hoặc các triệu chứng của các bé. Đó là lý do tại sao dịch vụ chăm sóc thú cưng của ADI bắt đầu bằng việc xây dựng mối quan hệ chặt chẽ giữa bác sĩ thú y và những người chủ</p><p>Vì vậy, ưu tiên hàng đầu của chúng tôi là lắng nghe những người chủ vật nuôi và hợp tác chặt chẽ để cùng nhau mang đến cho những người bạn đồng hành thân yêu của mình một cuộc sống hạnh phúc và khỏe mạnh hơn.</p>');
INSERT INTO `careitems` VALUES (2, 'Trung thực và minh bạch', '<p>Là cha mẹ của các bé, bạn hoàn toàn có quyền minh bạch về mọi thứ liên quan đến chăm sóc y tế cho thú cưng của bạn. Đó là lý do tại sao ADI muốn bạn tham gia vào mọi quyết định liên quan đến việc điều trị cho thú cưng của bạn.</p>');
INSERT INTO `careitems` VALUES (3, 'Nguyên tắc tảng băng trôi', '<p>Phần lớn những gì đặc trưng cho các tiêu chuẩn chăm sóc cao của chúng tôi diễn ra ở hậu trường và do đó khách hàng của chúng tôi không nhìn thấy được.</p>');
INSERT INTO `careitems` VALUES (4, 'Mục Tiêu', '<p>Mọi thứ chúng tôi đề xuất cho thú cưng của bạn là kết quả của quá trình nghiên cứu, cống hiến và chuyên môn tuyệt vời để đảm bảo rằng mọi phương pháp điều trị đều mang lại lợi ích tốt nhất cho thú cưng của bạn.</p>');
INSERT INTO `careitems` VALUES (5, 'Kỹ thuật xuất sắc', '<p>Thú y không chỉ là công việc kinh doanh của chúng tôi. Sức khỏe và phúc lợi động vật là sứ mệnh và niềm đam mê của Animal Doctors International. Thú cưng của bạn là ưu tiên hàng đầu của chúng tôi tại đây.</p><p>Chúng tôi cam kết với đội ngũ bác sĩ thú y có trình độ chuyên môn cao, đội ngũ nhân viên hỗ trợ chuyên nghiệp sẽ giúp cho thú cưng của bạn có được một sức khoẻ tốt nhất.</p>');
INSERT INTO `careitems` VALUES (6, 'Cách tiếp cận phù hợp', '<p>Tại ADI, chúng tôi nhận ra rằng mỗi thú cưng đều có nhu cầu, thói quen và lối sống riêng. Vì vậy, mọi thứ chúng tôi làm cho thú cưng của bạn đều phù hợp với nhu cầu cá nhân của chúng.</p>');
INSERT INTO `careitems` VALUES (7, 'Công tác đào tạo', '<p>Chúng tôi đầu tư vào việc liên tục đào tạo cho các bác sĩ thú y của mình để giúp họ theo kịp những phát triển mới nhất trong ngành thú y. Việc cập nhật liên tục giữ cho kiến ​​thức và kỹ năng của bác sĩ thú y của chúng tôi ở đẳng cấp thế giới.</p><p>Một phòng khám thú ý tốt không chỉ riêng bác sĩ thú y giỏi mà còn nhờ có một đội ngũ xuất sắc phía sau. Nhân viên hỗ trợ của chúng tôi được đào tạo chuyên sâu để liên tục duy trì mức độ xuất sắc.</p>');

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_user_product`(`user_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `cart_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `cart_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 86 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cart
-- ----------------------------
INSERT INTO `cart` VALUES (11, 10, 2, 3, '2026-03-17 21:56:42', '2026-03-18 13:46:06');
INSERT INTO `cart` VALUES (12, 10, 4, 1, '2026-03-17 21:56:51', '2026-03-17 21:56:51');
INSERT INTO `cart` VALUES (13, 10, 3, 2, '2026-03-17 22:07:53', '2026-03-18 13:45:18');
INSERT INTO `cart` VALUES (15, 9, 1, 1, '2026-03-18 23:12:37', '2026-03-18 23:12:37');
INSERT INTO `cart` VALUES (16, 9, 3, 1, '2026-03-18 23:12:39', '2026-03-18 23:12:39');
INSERT INTO `cart` VALUES (59, 11, 5, 4, '2026-05-05 15:50:11', '2026-05-31 21:31:42');
INSERT INTO `cart` VALUES (60, 11, 20, 4, '2026-05-27 15:14:43', '2026-05-31 21:31:42');
INSERT INTO `cart` VALUES (64, 8, 20, 2, '2026-06-02 21:47:15', '2026-06-02 21:47:22');
INSERT INTO `cart` VALUES (65, 8, 19, 1, '2026-06-02 21:47:18', '2026-06-02 21:47:18');

-- ----------------------------
-- Table structure for coupons
-- ----------------------------
DROP TABLE IF EXISTS `coupons`;
CREATE TABLE `coupons`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `discount_percent` int NOT NULL,
  `is_active` tinyint(1) NULL DEFAULT 1,
  `quantity` int NULL DEFAULT 1,
  `start_date` datetime NULL DEFAULT NULL,
  `end_date` datetime NULL DEFAULT NULL,
  `used` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of coupons
-- ----------------------------
INSERT INTO `coupons` VALUES (1, 'GIAM10', 10, 1, 100, '2026-03-28 17:51:42', '2026-06-14 17:51:42', 3);
INSERT INTO `coupons` VALUES (2, 'SALE20', 20, 1, 50, '2026-03-28 17:51:42', '2026-07-01 17:51:42', 1);

-- ----------------------------
-- Table structure for doctors
-- ----------------------------
DROP TABLE IF EXISTS `doctors`;
CREATE TABLE `doctors`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of doctors
-- ----------------------------
INSERT INTO `doctors` VALUES (1, 'Bác sĩ Ngọc Thành', 'webpic14.jpg');
INSERT INTO `doctors` VALUES (2, 'Bác sĩ Huyền Trang', 'webpic15.jpg');
INSERT INTO `doctors` VALUES (3, 'Bác sĩ Sterenn Genewe', 'webpic16.jpg');
INSERT INTO `doctors` VALUES (4, 'Bác sĩ Mai Phạm', 'webpic17.jpg');
INSERT INTO `doctors` VALUES (5, 'Bác sĩ Sterenn Genewe', 'webpic18.jpg');
INSERT INTO `doctors` VALUES (6, 'Bác sĩ Quốc Trí', 'webpic19.jpg');
INSERT INTO `doctors` VALUES (7, 'Bác sĩ Emily Davis', 'webpic20.jpg');
INSERT INTO `doctors` VALUES (8, 'Bác sĩ Michael Brown', 'webpic21.jpg');
INSERT INTO `doctors` VALUES (9, 'Bác sĩ Jessica Wilson', 'webpic22.jpg');
INSERT INTO `doctors` VALUES (10, 'Bác sĩ Ngọc Linh', 'webpic23.jpg');
INSERT INTO `doctors` VALUES (11, 'Bác sĩ Hoàng Long', 'webpic24.jpg');
INSERT INTO `doctors` VALUES (12, 'Bác sĩ Thanh Thảo', 'webpic25.jpg');

-- ----------------------------
-- Table structure for features
-- ----------------------------
DROP TABLE IF EXISTS `features`;
CREATE TABLE `features`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of features
-- ----------------------------
INSERT INTO `features` VALUES (1, 'bx bxs-graduation', 'Đội ngũ chuyên gia quốc tế', 'Các bác sĩ được đào tạo bài bản từ các trường đại học danh tiếng.');
INSERT INTO `features` VALUES (2, 'bx bxs-heart', 'Chăm sóc bằng cả trái tim', 'Chúng tôi đối xử với thú cưng của bạn như chính thú cưng của mình.');
INSERT INTO `features` VALUES (3, 'bx bxs-first-aid', 'Trang thiết bị tối tân', 'Hệ thống máy móc chẩn đoán hình ảnh và xét nghiệm hiện đại nhất.');

-- ----------------------------
-- Table structure for gallery
-- ----------------------------
DROP TABLE IF EXISTS `gallery`;
CREATE TABLE `gallery`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `alt_text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `sort_order` int NULL DEFAULT 0,
  `status` tinyint NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_gallery_product`(`product_id` ASC) USING BTREE,
  CONSTRAINT `fk_gallery_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gallery
-- ----------------------------

-- ----------------------------
-- Table structure for inventory_batches
-- ----------------------------
DROP TABLE IF EXISTS `inventory_batches`;
CREATE TABLE `inventory_batches`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `stock_import_id` int NULL DEFAULT NULL,
  `supplier_id` int NULL DEFAULT NULL,
  `batch_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `received_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `expiry_date` date NULL DEFAULT NULL,
  `received_quantity` int NOT NULL,
  `remaining_quantity` int NOT NULL,
  `unit_cost` decimal(15, 2) NOT NULL DEFAULT 0.00,
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inventory_batches_batch_code`(`batch_code` ASC) USING BTREE,
  INDEX `idx_inventory_batches_product_expiry`(`product_id` ASC, `expiry_date` ASC) USING BTREE,
  INDEX `idx_inventory_batches_remaining_received`(`remaining_quantity` ASC, `received_at` ASC) USING BTREE,
  INDEX `fk_inventory_batches_stock_import`(`stock_import_id` ASC) USING BTREE,
  INDEX `fk_inventory_batches_supplier`(`supplier_id` ASC) USING BTREE,
  CONSTRAINT `fk_inventory_batches_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_batches_stock_import` FOREIGN KEY (`stock_import_id`) REFERENCES `stock_imports` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_batches_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of inventory_batches
-- ----------------------------

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'system',
  `link` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_read` tinyint(1) NULL DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_read`(`user_id` ASC, `is_read` ASC) USING BTREE,
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notifications
-- ----------------------------

-- ----------------------------
-- Table structure for order_items
-- ----------------------------
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL,
  `price` decimal(10, 2) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_order_items_order`(`order_id` ASC) USING BTREE,
  INDEX `fk_order_items_product`(`product_id` ASC) USING BTREE,
  CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_order_items_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 112 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_items
-- ----------------------------
INSERT INTO `order_items` VALUES (1, 1, 1, 3, 132000.00);
INSERT INTO `order_items` VALUES (2, 1, 2, 1, 135000.00);
INSERT INTO `order_items` VALUES (3, 1, 3, 1, 185000.00);
INSERT INTO `order_items` VALUES (4, 1, 5, 3, 30000.00);
INSERT INTO `order_items` VALUES (5, 2, 2, 1, 135000.00);
INSERT INTO `order_items` VALUES (6, 3, 1, 3, 132000.00);
INSERT INTO `order_items` VALUES (7, 3, 2, 2, 135000.00);
INSERT INTO `order_items` VALUES (8, 3, 3, 1, 185000.00);
INSERT INTO `order_items` VALUES (9, 3, 5, 3, 30000.00);
INSERT INTO `order_items` VALUES (10, 4, 3, 6, 185000.00);
INSERT INTO `order_items` VALUES (11, 5, 2, 1, 135000.00);
INSERT INTO `order_items` VALUES (12, 6, 1, 1, 132000.00);
INSERT INTO `order_items` VALUES (13, 6, 4, 1, 27000.00);
INSERT INTO `order_items` VALUES (14, 7, 1, 1, 132000.00);
INSERT INTO `order_items` VALUES (15, 7, 2, 1, 135000.00);
INSERT INTO `order_items` VALUES (16, 7, 3, 6, 185000.00);
INSERT INTO `order_items` VALUES (17, 7, 4, 1, 27000.00);
INSERT INTO `order_items` VALUES (18, 8, 1, 1, 132000.00);
INSERT INTO `order_items` VALUES (19, 8, 2, 1, 135000.00);
INSERT INTO `order_items` VALUES (20, 8, 3, 6, 185000.00);
INSERT INTO `order_items` VALUES (21, 8, 4, 1, 27000.00);
INSERT INTO `order_items` VALUES (22, 9, 1, 1, 132000.00);
INSERT INTO `order_items` VALUES (23, 9, 2, 1, 135000.00);
INSERT INTO `order_items` VALUES (24, 9, 3, 6, 185000.00);
INSERT INTO `order_items` VALUES (25, 9, 4, 1, 27000.00);
INSERT INTO `order_items` VALUES (26, 10, 1, 1, 132000.00);
INSERT INTO `order_items` VALUES (27, 10, 2, 1, 135000.00);
INSERT INTO `order_items` VALUES (28, 10, 3, 6, 185000.00);
INSERT INTO `order_items` VALUES (29, 10, 4, 1, 27000.00);
INSERT INTO `order_items` VALUES (30, 11, 2, 3, 135000.00);
INSERT INTO `order_items` VALUES (31, 11, 3, 2, 185000.00);
INSERT INTO `order_items` VALUES (32, 11, 4, 1, 27000.00);
INSERT INTO `order_items` VALUES (33, 12, 2, 3, 135000.00);
INSERT INTO `order_items` VALUES (34, 12, 3, 2, 185000.00);
INSERT INTO `order_items` VALUES (35, 12, 4, 1, 27000.00);
INSERT INTO `order_items` VALUES (36, 13, 3, 1, 185000.00);
INSERT INTO `order_items` VALUES (37, 14, 4, 1, 27000.00);
INSERT INTO `order_items` VALUES (38, 15, 3, 1, 185000.00);
INSERT INTO `order_items` VALUES (39, 15, 4, 1, 27000.00);
INSERT INTO `order_items` VALUES (40, 16, 2, 5, 135000.00);
INSERT INTO `order_items` VALUES (41, 16, 3, 6, 185000.00);
INSERT INTO `order_items` VALUES (42, 17, 24, 6, 250000.00);
INSERT INTO `order_items` VALUES (43, 18, 3, 3, 185000.00);
INSERT INTO `order_items` VALUES (44, 19, 4, 15, 27000.00);
INSERT INTO `order_items` VALUES (45, 20, 5, 2, 30000.00);
INSERT INTO `order_items` VALUES (46, 20, 24, 1, 250000.00);
INSERT INTO `order_items` VALUES (47, 21, 5, 1, 30000.00);
INSERT INTO `order_items` VALUES (48, 22, 8, 1, 95000.00);
INSERT INTO `order_items` VALUES (49, 23, 8, 1, 95000.00);
INSERT INTO `order_items` VALUES (50, 24, 8, 1, 95000.00);
INSERT INTO `order_items` VALUES (51, 25, 5, 1, 30000.00);
INSERT INTO `order_items` VALUES (52, 26, 29, 1, 75000.00);
INSERT INTO `order_items` VALUES (53, 27, 29, 1, 75000.00);
INSERT INTO `order_items` VALUES (54, 28, 5, 1, 30000.00);
INSERT INTO `order_items` VALUES (55, 29, 5, 3, 30000.00);
INSERT INTO `order_items` VALUES (56, 29, 9, 1, 85000.00);
INSERT INTO `order_items` VALUES (57, 30, 5, 3, 30000.00);
INSERT INTO `order_items` VALUES (58, 30, 9, 1, 85000.00);
INSERT INTO `order_items` VALUES (59, 31, 5, 4, 30000.00);
INSERT INTO `order_items` VALUES (60, 31, 9, 1, 85000.00);
INSERT INTO `order_items` VALUES (63, 40, 30, 1, 65000.00);
INSERT INTO `order_items` VALUES (64, 41, 20, 1, 45000.00);
INSERT INTO `order_items` VALUES (65, 42, 20, 1, 45000.00);
INSERT INTO `order_items` VALUES (66, 43, 20, 1, 45000.00);
INSERT INTO `order_items` VALUES (67, 44, 32, 1, 25000.00);
INSERT INTO `order_items` VALUES (68, 45, 5, 1, 30000.00);
INSERT INTO `order_items` VALUES (69, 46, 20, 1, 45000.00);
INSERT INTO `order_items` VALUES (70, 47, 20, 1, 45000.00);
INSERT INTO `order_items` VALUES (71, 48, 5, 1, 30000.00);
INSERT INTO `order_items` VALUES (72, 49, 5, 1, 30000.00);
INSERT INTO `order_items` VALUES (73, 50, 20, 1, 45000.00);
INSERT INTO `order_items` VALUES (74, 51, 20, 1, 45000.00);
INSERT INTO `order_items` VALUES (75, 52, 19, 1, 89000.00);
INSERT INTO `order_items` VALUES (76, 53, 20, 1, 45000.00);
INSERT INTO `order_items` VALUES (77, 54, 31, 1, 195000.00);
INSERT INTO `order_items` VALUES (78, 55, 31, 1, 195000.00);
INSERT INTO `order_items` VALUES (79, 56, 32, 1, 25000.00);
INSERT INTO `order_items` VALUES (80, 57, 31, 1, 195000.00);
INSERT INTO `order_items` VALUES (81, 58, 31, 1, 195000.00);
INSERT INTO `order_items` VALUES (82, 59, 20, 1, 45000.00);
INSERT INTO `order_items` VALUES (83, 60, 19, 1, 89000.00);
INSERT INTO `order_items` VALUES (84, 61, 20, 1, 45000.00);
INSERT INTO `order_items` VALUES (85, 62, 19, 1, 89000.00);
INSERT INTO `order_items` VALUES (86, 63, 20, 1, 45000.00);
INSERT INTO `order_items` VALUES (87, 64, 19, 1, 89000.00);
INSERT INTO `order_items` VALUES (88, 65, 5, 1, 30000.00);
INSERT INTO `order_items` VALUES (89, 66, 30, 1, 65000.00);
INSERT INTO `order_items` VALUES (90, 67, 5, 1, 30000.00);
INSERT INTO `order_items` VALUES (91, 68, 18, 1, 150000.00);
INSERT INTO `order_items` VALUES (92, 69, 18, 1, 150000.00);
INSERT INTO `order_items` VALUES (93, 70, 30, 1, 65000.00);
INSERT INTO `order_items` VALUES (94, 71, 19, 1, 89000.00);
INSERT INTO `order_items` VALUES (95, 72, 19, 1, 89000.00);
INSERT INTO `order_items` VALUES (96, 73, 12, 1, 35000.00);
INSERT INTO `order_items` VALUES (97, 74, 31, 1, 195000.00);
INSERT INTO `order_items` VALUES (98, 75, 19, 1, 89000.00);
INSERT INTO `order_items` VALUES (99, 76, 19, 1, 89000.00);
INSERT INTO `order_items` VALUES (100, 77, 31, 1, 195000.00);
INSERT INTO `order_items` VALUES (101, 78, 19, 1, 89000.00);
INSERT INTO `order_items` VALUES (102, 79, 31, 1, 195000.00);
INSERT INTO `order_items` VALUES (103, 80, 31, 1, 195000.00);
INSERT INTO `order_items` VALUES (104, 81, 32, 1, 25000.00);
INSERT INTO `order_items` VALUES (105, 82, 31, 1, 195000.00);
INSERT INTO `order_items` VALUES (106, 83, 31, 1, 195000.00);
INSERT INTO `order_items` VALUES (107, 84, 19, 1, 89000.00);
INSERT INTO `order_items` VALUES (108, 85, 18, 1, 150000.00);
INSERT INTO `order_items` VALUES (109, 86, 32, 1, 25000.00);
INSERT INTO `order_items` VALUES (110, 87, 32, 1, 25000.00);
INSERT INTO `order_items` VALUES (111, 88, 32, 1, 25000.00);

-- ----------------------------
-- Table structure for order_logs
-- ----------------------------
DROP TABLE IF EXISTS `order_logs`;
CREATE TABLE `order_logs`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `actor_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `actor_id` int NULL DEFAULT NULL,
  `action` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `old_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `new_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL,
  `created_at` datetime NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  CONSTRAINT `order_logs_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 71 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_logs
-- ----------------------------
INSERT INTO `order_logs` VALUES (6, 40, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-10 12:08:51');
INSERT INTO `order_logs` VALUES (7, 40, 'ADMIN', 1, 'UPDATE_STATUS', 'Pending', 'Confirmed', 'Cập nhật trạng thái đơn hàng', '2026-06-10 12:24:30');
INSERT INTO `order_logs` VALUES (8, 40, 'CUSTOMER', 2, 'CANCEL_ORDER', 'Confirmed', 'Cancelled', 'Khách hủy đơn', '2026-06-10 12:25:50');
INSERT INTO `order_logs` VALUES (9, 41, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 11:07:15');
INSERT INTO `order_logs` VALUES (10, 41, 'ADMIN', 1, 'UPDATE_STATUS', 'Pending', 'Confirmed', 'Cập nhật trạng thái đơn hàng', '2026-06-12 11:07:55');
INSERT INTO `order_logs` VALUES (11, 41, 'CUSTOMER', 2, 'CANCEL_ORDER', 'Confirmed', 'Cancelled', 'Khách hủy đơn', '2026-06-12 11:08:04');
INSERT INTO `order_logs` VALUES (12, 42, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 11:12:05');
INSERT INTO `order_logs` VALUES (13, 43, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 11:12:49');
INSERT INTO `order_logs` VALUES (14, 43, 'ADMIN', 1, 'UPDATE_STATUS', 'Pending', 'Confirmed', 'Cập nhật trạng thái đơn hàng', '2026-06-12 11:13:26');
INSERT INTO `order_logs` VALUES (15, 42, 'ADMIN', 1, 'UPDATE_STATUS', 'Pending', 'Confirmed', 'Cập nhật trạng thái đơn hàng', '2026-06-12 11:13:58');
INSERT INTO `order_logs` VALUES (16, 44, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 11:44:28');
INSERT INTO `order_logs` VALUES (17, 45, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 11:45:14');
INSERT INTO `order_logs` VALUES (18, 46, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 11:49:04');
INSERT INTO `order_logs` VALUES (19, 47, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 12:02:06');
INSERT INTO `order_logs` VALUES (20, 48, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 12:04:41');
INSERT INTO `order_logs` VALUES (21, 49, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 12:05:53');
INSERT INTO `order_logs` VALUES (22, 50, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 12:09:33');
INSERT INTO `order_logs` VALUES (23, 51, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 12:11:07');
INSERT INTO `order_logs` VALUES (24, 52, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 12:12:41');
INSERT INTO `order_logs` VALUES (25, 53, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 12:27:09');
INSERT INTO `order_logs` VALUES (26, 54, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 12:30:47');
INSERT INTO `order_logs` VALUES (27, 55, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 12:37:20');
INSERT INTO `order_logs` VALUES (28, 56, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 12:39:09');
INSERT INTO `order_logs` VALUES (29, 57, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 12:43:45');
INSERT INTO `order_logs` VALUES (30, 58, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 13:20:03');
INSERT INTO `order_logs` VALUES (31, 58, 'ADMIN', 1, 'UPDATE_STATUS', 'Pending', 'Confirmed', 'Cập nhật trạng thái đơn hàng', '2026-06-12 13:20:25');
INSERT INTO `order_logs` VALUES (32, 59, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 13:54:25');
INSERT INTO `order_logs` VALUES (33, 59, 'ADMIN', 1, 'UPDATE_STATUS', 'Pending', 'Confirmed', 'Cập nhật trạng thái đơn hàng', '2026-06-12 13:54:50');
INSERT INTO `order_logs` VALUES (34, 60, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 14:52:56');
INSERT INTO `order_logs` VALUES (35, 61, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 14:58:54');
INSERT INTO `order_logs` VALUES (36, 62, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 15:06:08');
INSERT INTO `order_logs` VALUES (37, 63, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 15:09:29');
INSERT INTO `order_logs` VALUES (38, 64, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 15:14:59');
INSERT INTO `order_logs` VALUES (39, 65, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 15:21:09');
INSERT INTO `order_logs` VALUES (40, 66, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 15:24:51');
INSERT INTO `order_logs` VALUES (41, 67, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 15:30:39');
INSERT INTO `order_logs` VALUES (42, 68, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 15:42:50');
INSERT INTO `order_logs` VALUES (43, 69, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 15:45:29');
INSERT INTO `order_logs` VALUES (44, 70, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 15:53:56');
INSERT INTO `order_logs` VALUES (45, 71, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 16:01:26');
INSERT INTO `order_logs` VALUES (46, 72, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 16:18:08');
INSERT INTO `order_logs` VALUES (47, 72, 'ADMIN', 1, 'UPDATE_STATUS', 'Pending', 'Confirmed', 'Cập nhật trạng thái đơn hàng', '2026-06-12 16:24:07');
INSERT INTO `order_logs` VALUES (48, 73, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 16:31:08');
INSERT INTO `order_logs` VALUES (49, 74, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 16:33:31');
INSERT INTO `order_logs` VALUES (50, 75, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 16:45:53');
INSERT INTO `order_logs` VALUES (51, 76, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 16:53:18');
INSERT INTO `order_logs` VALUES (52, 77, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 17:30:42');
INSERT INTO `order_logs` VALUES (53, 78, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 17:48:55');
INSERT INTO `order_logs` VALUES (54, 79, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 18:20:31');
INSERT INTO `order_logs` VALUES (55, 80, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 18:24:36');
INSERT INTO `order_logs` VALUES (56, 81, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 18:33:09');
INSERT INTO `order_logs` VALUES (57, 82, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 18:35:57');
INSERT INTO `order_logs` VALUES (58, 83, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-12 18:37:21');
INSERT INTO `order_logs` VALUES (59, 84, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-15 10:18:14');
INSERT INTO `order_logs` VALUES (60, 85, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-15 14:06:39');
INSERT INTO `order_logs` VALUES (61, 86, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-15 14:13:23');
INSERT INTO `order_logs` VALUES (62, 87, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-15 14:22:40');
INSERT INTO `order_logs` VALUES (63, 88, 'CUSTOMER', 2, 'CREATE_ORDER', NULL, 'Pending', 'Khách tạo đơn', '2026-06-15 14:26:26');
INSERT INTO `order_logs` VALUES (64, 88, 'ADMIN', 1, 'UPDATE_STATUS', 'Paid', 'Confirmed', 'Cập nhật trạng thái đơn hàng', '2026-06-15 14:45:45');
INSERT INTO `order_logs` VALUES (65, 88, 'ADMIN', 1, 'UPDATE_STATUS', 'Confirmed', 'Shipping', 'Cập nhật trạng thái đơn hàng', '2026-06-15 14:45:58');
INSERT INTO `order_logs` VALUES (66, 87, 'ADMIN', 14, 'UPDATE_STATUS', 'Paid', 'Confirmed', 'Cập nhật trạng thái đơn hàng', '2026-06-15 16:37:10');
INSERT INTO `order_logs` VALUES (67, 87, 'ADMIN', 14, 'UPDATE_STATUS', 'Confirmed', 'Shipping', 'Cập nhật trạng thái đơn hàng', '2026-06-15 16:37:24');
INSERT INTO `order_logs` VALUES (68, 77, 'ADMIN', 15, 'UPDATE_STATUS', 'Pending', 'Confirmed', 'Cập nhật trạng thái đơn hàng', '2026-06-15 17:07:50');
INSERT INTO `order_logs` VALUES (69, 77, 'ADMIN', 14, 'UPDATE_STATUS', 'Confirmed', 'Shipping', 'Cập nhật trạng thái đơn hàng', '2026-06-15 17:08:34');
INSERT INTO `order_logs` VALUES (70, 72, 'ADMIN', 14, 'UPDATE_STATUS', 'Confirmed', 'Shipping', 'Cập nhật trạng thái đơn hàng', '2026-06-15 17:08:57');

-- ----------------------------
-- Table structure for order_status_history
-- ----------------------------
DROP TABLE IF EXISTS `order_status_history`;
CREATE TABLE `order_status_history`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `old_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `new_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `changedBy` int NULL DEFAULT NULL,
  `changedByName` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `changedAt` datetime NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  CONSTRAINT `order_status_history_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_status_history
-- ----------------------------
INSERT INTO `order_status_history` VALUES (1, 40, 'Pending', 'Confirmed', 1, NULL, '2026-06-10 12:24:30');
INSERT INTO `order_status_history` VALUES (2, 40, 'Confirmed', 'Cancelled', 2, NULL, '2026-06-10 12:25:50');
INSERT INTO `order_status_history` VALUES (3, 41, 'Pending', 'Confirmed', 1, NULL, '2026-06-12 11:07:55');
INSERT INTO `order_status_history` VALUES (4, 41, 'Confirmed', 'Cancelled', 2, NULL, '2026-06-12 11:08:04');
INSERT INTO `order_status_history` VALUES (5, 43, 'Pending', 'Confirmed', 1, NULL, '2026-06-12 11:13:26');
INSERT INTO `order_status_history` VALUES (6, 42, 'Pending', 'Confirmed', 1, NULL, '2026-06-12 11:13:58');
INSERT INTO `order_status_history` VALUES (7, 58, 'Pending', 'Confirmed', 1, NULL, '2026-06-12 13:20:25');
INSERT INTO `order_status_history` VALUES (8, 59, 'Pending', 'Confirmed', 1, NULL, '2026-06-12 13:54:50');
INSERT INTO `order_status_history` VALUES (9, 72, 'Pending', 'Confirmed', 1, NULL, '2026-06-12 16:24:07');
INSERT INTO `order_status_history` VALUES (10, 88, 'Paid', 'Confirmed', 1, NULL, '2026-06-15 14:45:45');
INSERT INTO `order_status_history` VALUES (11, 88, 'Confirmed', 'Shipping', 1, NULL, '2026-06-15 14:45:58');
INSERT INTO `order_status_history` VALUES (12, 87, 'Paid', 'Confirmed', 14, NULL, '2026-06-15 16:37:10');
INSERT INTO `order_status_history` VALUES (13, 87, 'Confirmed', 'Shipping', 14, NULL, '2026-06-15 16:37:24');
INSERT INTO `order_status_history` VALUES (14, 77, 'Pending', 'Confirmed', 15, NULL, '2026-06-15 17:07:50');
INSERT INTO `order_status_history` VALUES (15, 77, 'Confirmed', 'Shipping', 14, NULL, '2026-06-15 17:08:34');
INSERT INTO `order_status_history` VALUES (16, 72, 'Confirmed', 'Shipping', 14, NULL, '2026-06-15 17:08:57');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `fullname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `address` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL,
  `total_amount` decimal(10, 0) NOT NULL,
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `payment_method` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `payment_status` bit(1) NULL DEFAULT NULL,
  `createdAt` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
  `payment_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `payment_provider_transaction_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `payment_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `status_updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_orders_users`(`user_id` ASC) USING BTREE,
  INDEX `idx_orders_status_created_user`(`status` ASC, `createdAt` ASC, `user_id` ASC) USING BTREE,
  CONSTRAINT `fk_orders_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 89 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 2, 'Nguyễn Văn B', '0867943315', 'Quận/Huyện', NULL, 806000, 'Pending', NULL, NULL, '2026-06-12 17:15:16', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (2, 2, 'Nguyễn Văn B', '0867943315', 'Quận/Huyện', NULL, 135000, 'Pending', NULL, NULL, '2026-06-12 17:15:16', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (3, 2, 'Nguyễn Văn B', '0867943315', 'Quận/Huyện', NULL, 941000, 'Pending', NULL, NULL, '2026-06-12 17:15:16', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (4, 11, 'Nguyen Van C', '0123456789', 'Nhà số 1, Phường Phúc Tân, Quận Hoàn Kiếm, Thành phố Hà Nội', '', 1144000, 'Pending', 'COD', b'0', '2026-03-30 05:36:38', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (5, 11, 'Nguyen Van C', '0123456789', 'Nhà số 1, Phường Phúc Tân, Quận Hoàn Kiếm, Thành phố Hà Nội', '', 169000, 'Pending', 'MOMO', b'1', '2026-03-30 05:38:05', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (6, 11, 'Nguyen Van C', '0123456789', 'Nhà số 1, Phường Phúc Tân, Quận Hoàn Kiếm, Thành phố Hà Nội', '', 193000, 'Pending', 'COD', b'0', '2026-03-30 05:39:51', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (7, 11, 'Nguyen Van C', '0123456789', 'Nhà số 1, Phường Phúc Tân, Quận Hoàn Kiếm, Thành phố Hà Nội', '', 1443000, 'Pending', 'COD', b'0', '2026-03-30 05:45:36', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (8, 11, 'Nguyen Van C', '0123456789', 'Nhà số 1, Phường Phúc Tân, Quận Hoàn Kiếm, Thành phố Hà Nội', '', 1443000, 'Pending', 'COD', b'0', '2026-03-30 05:52:03', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (9, 11, 'Nguyen Van C', '0123456789', 'Nhà số 1, Phường Phúc Tân, Quận Hoàn Kiếm, Thành phố Hà Nội', '', 1443000, 'Pending', 'COD', b'0', '2026-03-30 05:57:32', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (10, 11, 'Nguyen Van C', '0123456789', 'Nhà số 1, Phường Phúc Tân, Quận Hoàn Kiếm, Thành phố Hà Nội', '', 1443000, 'Pending', 'COD', b'0', '2026-03-30 05:59:55', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (11, 10, 'Phạm Hữu Đạt', '0867943315', 'Dai hoc nong lam, Xã Long An, Huyện Cần Giuộc, Tỉnh Long An', 'Nhớ cẩn thận nha shop', 838501, 'Pending', 'COD', b'0', '2026-03-30 06:16:07', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (12, 10, 'Phạm Hữu Đạt', '0867943315', 'Dai hoc nong lam, Xã Long An, Huyện Cần Giuộc, Tỉnh Long An', '', 838501, 'Pending', 'COD', b'0', '2026-03-30 06:32:14', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (13, 8, 'Đạt Phạm Hữu', '0867943315', 'Nhà số 30, Xã Minh Xuân, Huyện Lục Yên, Tỉnh Yên Bái', 'shop nhớ cẩn thận', 229000, 'Pending', 'COD', b'0', '2026-03-30 06:46:35', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (14, 11, 'Nguyen Van C', '0123456789', 'Nhà số 1, Phường Phúc Tân, Quận Hoàn Kiếm, Thành phố Hà Nội', '', 61000, 'Pending', 'MOMO', b'1', '2026-03-30 06:57:05', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (15, 11, 'Nguyen Van C', '0123456789', 'Nhà số 1, Phường Phúc Tân, Quận Hoàn Kiếm, Thành phố Hà Nội', '', 246000, 'Cancelled', 'COD', b'0', '2026-03-30 09:48:09', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (16, 2, 'Nguyễn Văn B', '0867943315', 'Phố 30, Phường Ngọc Hà, Quận Ba Đình, Thành phố Hà Nội', 'Nhớ cẩn thận nha shop', 1833999, 'Pending', 'COD', b'0', '2026-04-06 07:25:00', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (17, 2, 'Nguyễn Văn B', '0867943315', 'Phố 30, Phường Ngọc Hà, Quận Ba Đình, Thành phố Hà Nội', 'nhớ cẩn thận', 1239000, 'Pending', 'COD', b'0', '2026-04-06 09:04:05', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (18, 2, 'Nguyễn Văn B', '0867943315', 'Phố 30, Phường Ngọc Hà, Quận Ba Đình, Thành phố Hà Nội', '', 589000, 'Pending', 'COD', b'0', '2026-04-06 09:05:16', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (19, 2, 'Nguyễn Văn B', '0867943315', 'Phố 30, Phường Ngọc Hà, Quận Ba Đình, Thành phố Hà Nội', '', 458998, 'Pending', 'COD', b'0', '2026-04-06 09:14:09', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (20, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 344000, 'Pending', 'COD', b'0', '2026-04-09 10:13:35', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (21, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 64000, 'Pending', 'COD', b'0', '2026-04-09 10:28:12', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (22, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 129000, 'Cancelled', 'MOMO', b'0', '2026-04-09 10:29:30', NULL, NULL, 'Chua cau hinh thong tin MoMo trong secrets.properties.', '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (23, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 129000, 'Cancelled', 'VNPAY', b'0', '2026-04-09 10:29:41', NULL, NULL, 'Chua cau hinh thong tin VNPAY trong secrets.properties.', '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (24, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 129000, 'Pending', 'BANK_TRANSFER', b'0', '2026-04-09 10:29:49', NULL, NULL, 'Cho xac nhan chuyen khoan ngan hang.', '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (25, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 64000, 'Cancelled', 'MOMO', b'0', '2026-04-09 10:36:00', NULL, NULL, 'Chua cau hinh thong tin MoMo trong secrets.properties.', '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (26, 11, 'Nguyen Van C', '0123456789', 'Nhà số 1, Phường Phúc Tân, Quận Hoàn Kiếm, Thành phố Hà Nội', '', 109000, 'Cancelled', 'MOMO', b'0', '2026-05-05 15:46:19', NULL, NULL, 'Chua cau hinh MoMo: momo_partner_code, momo_access_key, momo_secret_key', '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (27, 11, 'Nguyen Van C', '0123456789', 'Nhà số 1, Phường Phúc Tân, Quận Hoàn Kiếm, Thành phố Hà Nội', '', 109000, 'Cancelled', 'COD', b'0', '2026-05-05 15:47:16', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (28, 11, 'Nguyen Van C', '0123456789', 'Nhà số 1, Phường Phúc Tân, Quận Hoàn Kiếm, Thành phố Hà Nội', '', 64000, 'Cancelled', 'MOMO', b'0', '2026-05-05 15:50:35', NULL, NULL, 'Chua cau hinh MoMo: momo_partner_code, momo_access_key, momo_secret_key', '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (29, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 209000, 'Cancelled', 'BANK_TRANSFER', b'0', '2026-05-31 21:33:03', NULL, NULL, 'Chua cau hinh bank gateway: bank_gateway_create_url, bank_gateway_partner_code, bank_gateway_api_key, bank_gateway_secret_key', '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (30, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 209000, 'Cancelled', 'BANK_TRANSFER', b'0', '2026-05-31 21:55:53', NULL, NULL, 'Chua cau hinh bank gateway: bank_gateway_create_url, bank_gateway_partner_code, bank_gateway_api_key, bank_gateway_secret_key', '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (31, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 239000, 'Cancelled', 'BANK_TRANSFER', b'0', '2026-05-31 22:00:48', NULL, NULL, 'Chua cau hinh bank gateway: bank_gateway_create_url, bank_gateway_partner_code, bank_gateway_api_key, bank_gateway_secret_key', '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (40, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 99000, 'Cancelled', 'COD', b'0', '2026-06-10 12:08:51', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (41, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 79000, 'Cancelled', 'COD', b'0', '2026-06-12 11:07:15', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (42, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 79000, 'Confirmed', 'COD', b'0', '2026-06-12 11:12:05', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (43, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', 'nhớ cẩn thận giúp em', 79000, 'Confirmed', 'COD', b'0', '2026-06-12 11:12:49', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (44, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 59000, 'Pending', 'COD', b'0', '2026-06-12 11:44:28', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (45, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 64000, 'Pending', 'COD', b'0', '2026-06-12 11:45:14', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (46, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 79000, 'Pending', 'COD', b'0', '2026-06-12 11:49:04', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (47, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 79000, 'Pending', 'COD', b'0', '2026-06-12 12:02:06', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (48, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 64000, 'Pending', 'COD', b'0', '2026-06-12 12:04:41', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (49, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 64000, 'Pending', 'COD', b'0', '2026-06-12 12:05:53', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (50, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 79000, 'Pending', 'COD', b'0', '2026-06-12 12:09:33', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (51, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 79000, 'Pending', 'COD', b'0', '2026-06-12 12:11:07', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (52, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 123000, 'Pending', 'COD', b'0', '2026-06-12 12:12:41', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (53, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 79000, 'Pending', 'COD', b'0', '2026-06-12 12:27:09', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (54, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 229000, 'Pending', 'COD', b'0', '2026-06-12 12:30:47', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (55, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 229000, 'Pending', 'COD', b'0', '2026-06-12 12:37:20', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (56, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 59000, 'Pending', 'COD', b'0', '2026-06-12 12:39:09', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (57, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 229000, 'Pending', 'COD', b'0', '2026-06-12 12:43:45', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (58, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', 'Nhớ cẩn thận', 229000, 'Confirmed', 'COD', b'0', '2026-06-12 13:20:03', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (59, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', 'Dạ cẩn thận dùm em nhen', 74500, 'Confirmed', 'COD', b'0', '2026-06-12 13:54:25', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (60, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 123000, 'Pending', 'COD', b'0', '2026-06-12 14:52:56', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (61, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 79000, 'Pending', 'COD', b'0', '2026-06-12 14:58:54', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (62, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 123000, 'Pending', 'COD', b'0', '2026-06-12 15:06:08', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (63, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 79000, 'Pending', 'COD', b'0', '2026-06-12 15:09:29', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (64, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 123000, 'Pending', 'COD', b'0', '2026-06-12 15:14:59', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (65, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 64000, 'Pending', 'COD', b'0', '2026-06-12 15:21:09', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (66, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 99000, 'Pending', 'COD', b'0', '2026-06-12 15:24:51', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (67, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 64000, 'Pending', 'COD', b'0', '2026-06-12 15:30:39', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (68, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 184000, 'Pending', 'COD', b'0', '2026-06-12 15:42:50', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (69, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 184000, 'Pending', 'COD', b'0', '2026-06-12 15:45:29', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (70, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 99000, 'Pending', 'COD', b'0', '2026-06-12 15:53:56', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (71, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', 'Nhớ cẩn thận giúp em', 114100, 'Pending', 'COD', b'0', '2026-06-12 16:01:26', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (72, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', 'Nhớ cẩn thận cho em nhen', 114100, 'Shipping', 'COD', b'0', '2026-06-15 17:08:57', NULL, NULL, NULL, '2026-06-15 17:08:57');
INSERT INTO `orders` VALUES (73, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 69000, 'Pending', 'COD', b'0', '2026-06-12 16:31:07', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (74, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 229000, 'Pending', 'COD', b'0', '2026-06-12 16:33:31', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (75, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 123000, 'Pending', 'COD', b'0', '2026-06-12 16:45:53', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (76, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 123000, 'Pending', 'COD', b'0', '2026-06-12 16:53:18', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (77, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 229000, 'Shipping', 'COD', b'0', '2026-06-15 17:08:34', NULL, NULL, NULL, '2026-06-15 17:08:34');
INSERT INTO `orders` VALUES (78, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 123000, 'Awaiting Payment', 'VNPAY', b'0', '2026-06-12 17:48:55', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (79, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 229000, 'Awaiting Payment', 'VNPAY', b'0', '2026-06-12 18:20:31', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (80, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 229000, 'Paid', 'VNPAY', b'1', '2026-06-12 18:36:31', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (81, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 59000, 'Paid', 'VNPAY', b'1', '2026-06-12 18:33:44', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (82, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 229000, 'Awaiting Payment', 'VNPAY', b'0', '2026-06-12 18:35:57', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (83, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 229000, 'Awaiting Payment', 'VNPAY', b'0', '2026-06-12 18:37:21', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (84, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 123000, 'Awaiting Payment', 'VNPAY', b'0', '2026-06-15 10:18:14', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (85, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 184000, 'Paid', 'VNPAY', b'1', '2026-06-15 14:12:24', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (86, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 59000, 'Paid', 'VNPAY', b'1', '2026-06-15 14:14:51', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (87, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 59000, 'Shipping', 'VNPAY', b'1', '2026-06-15 16:37:24', NULL, NULL, NULL, '2026-06-15 16:57:10');
INSERT INTO `orders` VALUES (88, 2, 'Nguyễn Văn B', '0867943315', 'phố 30, Phường Nguyễn Du, Quận Hai Bà Trưng, Thành phố Hà Nội', '', 59000, 'Shipping', 'VNPAY', b'1', '2026-06-15 14:45:58', NULL, NULL, NULL, '2026-06-15 16:57:10');

-- ----------------------------
-- Table structure for payment_transactions
-- ----------------------------
DROP TABLE IF EXISTS `payment_transactions`;
CREATE TABLE `payment_transactions`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `user_id` int NOT NULL,
  `provider_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `provider_display_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `provider` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `provider_order_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `request_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `amount` double NULL DEFAULT NULL,
  `currency` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `transfer_reference` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `coupon_id` int NULL DEFAULT NULL,
  `discount_reserved` tinyint(1) NULL DEFAULT 0,
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `verification_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `verification_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL,
  `provider_metadata` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL,
  `payment_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `provider_transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `response_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `provider_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `redirect_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `raw_payload` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
  `verified_at` timestamp NULL DEFAULT NULL,
  `completed_at` timestamp NULL DEFAULT NULL,
  `expires_at` timestamp NULL DEFAULT NULL,
  `amount_received` decimal(15, 2) NULL DEFAULT NULL,
  `bank_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_payment_transactions_provider_transaction_id`(`provider_transaction_id` ASC) USING BTREE,
  INDEX `idx_payment_transactions_verification_status`(`verification_status` ASC) USING BTREE,
  INDEX `idx_payment_transactions_transfer_reference`(`transfer_reference` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of payment_transactions
-- ----------------------------
INSERT INTO `payment_transactions` VALUES (1, 22, 2, NULL, NULL, 'MOMO', 'MOMO_2_1775730570048', 'REQ_MOMO_2_105960866854900', 129000, NULL, NULL, NULL, 0, 'FAILED', NULL, NULL, NULL, NULL, NULL, 'CONFIG_MISSING', 'Chua cau hinh thong tin MoMo trong secrets.properties.', NULL, NULL, '2026-04-09 17:29:30', '2026-04-09 17:29:30', NULL, '2026-04-09 10:29:30', NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (2, 23, 2, NULL, NULL, 'VNPAY', 'VNPAY_2_1775730581814', 'REQ_VNPAY_2_105972630819200', 129000, NULL, NULL, NULL, 0, 'FAILED', NULL, NULL, NULL, NULL, NULL, 'CONFIG_MISSING', 'Chua cau hinh thong tin VNPAY trong secrets.properties.', NULL, NULL, '2026-04-09 17:29:41', '2026-04-09 17:29:41', NULL, '2026-04-09 10:29:41', NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (3, 25, 2, NULL, NULL, 'MOMO', 'MOMO_2_1775730960143', 'REQ_MOMO_2_106350960403300', 64000, NULL, NULL, NULL, 0, 'FAILED', NULL, NULL, NULL, NULL, NULL, 'CONFIG_MISSING', 'Chua cau hinh thong tin MoMo trong secrets.properties.', NULL, NULL, '2026-04-09 17:36:00', '2026-04-09 17:36:00', NULL, '2026-04-09 10:36:00', NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (4, 26, 11, NULL, NULL, 'MOMO', 'MOMO_11_1777970779947', 'REQ_MOMO_11_451604269887100', 109000, NULL, NULL, NULL, 0, 'FAILED', NULL, NULL, NULL, NULL, NULL, 'CONFIG_MISSING', 'Chua cau hinh MoMo: momo_partner_code, momo_access_key, momo_secret_key', NULL, NULL, '2026-05-05 15:46:20', '2026-05-05 15:46:20', NULL, '2026-05-05 15:46:20', NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (5, 28, 11, NULL, NULL, 'MOMO', 'MOMO_11_1777971035121', 'REQ_MOMO_11_451859437121800', 64000, NULL, NULL, NULL, 0, 'FAILED', NULL, NULL, NULL, NULL, NULL, 'CONFIG_MISSING', 'Chua cau hinh MoMo: momo_partner_code, momo_access_key, momo_secret_key', NULL, NULL, '2026-05-05 15:50:35', '2026-05-05 15:50:35', NULL, '2026-05-05 15:50:35', NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (6, 29, 2, NULL, NULL, 'BANK_TRANSFER', 'BANK_TRANSFER_2_1780237983461', 'REQ_BANK_TRANSFER_2_1323758679123800', 209000, NULL, NULL, NULL, 0, 'FAILED', NULL, NULL, NULL, NULL, NULL, 'CONFIG_MISSING', 'Chua cau hinh bank gateway: bank_gateway_create_url, bank_gateway_partner_code, bank_gateway_api_key, bank_gateway_secret_key', NULL, NULL, '2026-05-31 21:33:03', '2026-05-31 21:33:03', NULL, '2026-05-31 21:33:03', NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (7, 30, 2, NULL, NULL, 'BANK_TRANSFER', 'BANK_TRANSFER_2_1780239353847', 'REQ_BANK_TRANSFER_2_1325129067626200', 209000, NULL, NULL, NULL, 0, 'FAILED', NULL, NULL, NULL, NULL, NULL, 'CONFIG_MISSING', 'Chua cau hinh bank gateway: bank_gateway_create_url, bank_gateway_partner_code, bank_gateway_api_key, bank_gateway_secret_key', NULL, NULL, '2026-05-31 21:55:53', '2026-05-31 21:55:53', NULL, '2026-05-31 21:55:53', NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (8, 31, 2, NULL, NULL, 'BANK_TRANSFER', 'BANK_TRANSFER_2_1780239648795', 'REQ_BANK_TRANSFER_2_1325424017206400', 239000, NULL, NULL, NULL, 0, 'FAILED', NULL, NULL, NULL, NULL, NULL, 'CONFIG_MISSING', 'Chua cau hinh bank gateway: bank_gateway_create_url, bank_gateway_partner_code, bank_gateway_api_key, bank_gateway_secret_key', NULL, NULL, '2026-05-31 22:00:48', '2026-05-31 22:00:48', NULL, '2026-05-31 22:00:48', NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (9, 40, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 99000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-10 12:08:51', '2026-06-10 12:08:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (10, 41, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 79000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 11:07:15', '2026-06-12 11:07:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (11, 42, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 79000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 11:12:05', '2026-06-12 11:12:05', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (12, 43, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 79000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 11:12:49', '2026-06-12 11:12:49', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (13, 44, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 59000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 11:44:28', '2026-06-12 11:44:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (14, 45, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 64000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 11:45:14', '2026-06-12 11:45:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (15, 46, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 79000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 11:49:04', '2026-06-12 11:49:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (16, 47, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 79000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 12:02:06', '2026-06-12 12:02:06', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (17, 48, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 64000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 12:04:41', '2026-06-12 12:04:41', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (18, 49, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 64000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 12:05:53', '2026-06-12 12:05:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (19, 50, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 79000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 12:09:33', '2026-06-12 12:09:33', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (20, 51, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 79000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 12:11:08', '2026-06-12 12:11:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (21, 52, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 123000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 12:12:41', '2026-06-12 12:12:41', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (22, 53, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 79000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 12:27:09', '2026-06-12 12:27:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (23, 54, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 229000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 12:30:47', '2026-06-12 12:30:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (24, 55, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 229000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 12:37:20', '2026-06-12 12:37:20', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (25, 56, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 59000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 12:39:09', '2026-06-12 12:39:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (26, 57, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 229000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 12:43:45', '2026-06-12 12:43:45', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (27, 58, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 229000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 13:20:03', '2026-06-12 13:20:03', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (28, 59, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 74500, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 13:54:25', '2026-06-12 13:54:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (29, 60, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 123000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 14:52:56', '2026-06-12 14:52:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (30, 61, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 79000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 14:58:54', '2026-06-12 14:58:54', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (31, 62, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 123000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 15:06:08', '2026-06-12 15:06:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (32, 63, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 79000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 15:09:29', '2026-06-12 15:09:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (33, 64, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 123000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 15:14:59', '2026-06-12 15:14:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (34, 65, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 64000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 15:21:09', '2026-06-12 15:21:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (35, 66, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 99000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 15:24:51', '2026-06-12 15:24:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (36, 67, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 64000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 15:30:39', '2026-06-12 15:30:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (37, 68, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 184000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 15:42:50', '2026-06-12 15:42:50', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (38, 69, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 184000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 15:45:29', '2026-06-12 15:45:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (39, 70, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 99000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 15:53:56', '2026-06-12 15:53:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (40, 71, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 114100, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 16:01:26', '2026-06-12 16:01:26', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (41, 72, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 114100, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 16:18:08', '2026-06-12 16:18:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (42, 73, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 69000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 16:31:08', '2026-06-12 16:31:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (43, 74, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 229000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 16:33:31', '2026-06-12 16:33:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (44, 75, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 123000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 16:45:53', '2026-06-12 16:45:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (45, 76, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 123000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 16:53:18', '2026-06-12 16:53:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (46, 77, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 229000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 17:30:42', '2026-06-12 17:30:42', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (47, 78, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 123000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 17:48:55', '2026-06-12 17:48:55', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (48, 79, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 229000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 18:20:31', '2026-06-12 18:20:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (49, 80, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 229000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 18:24:36', '2026-06-12 18:24:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (50, 81, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 59000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 18:33:09', '2026-06-12 18:33:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (51, 82, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 229000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 18:35:57', '2026-06-12 18:35:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (52, 83, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 229000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-12 18:37:21', '2026-06-12 18:37:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (53, 84, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 123000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-15 10:18:14', '2026-06-15 10:18:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (54, 85, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 184000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-15 14:06:39', '2026-06-15 14:06:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (55, 86, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 59000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-15 14:13:23', '2026-06-15 14:13:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (56, 87, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 59000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-15 14:22:40', '2026-06-15 14:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `payment_transactions` VALUES (57, 88, 2, 'COD', 'Cash On Delivery', NULL, NULL, NULL, 59000, 'VND', NULL, NULL, 0, 'CREATED', 'NOT_REQUIRED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-15 14:26:26', '2026-06-15 14:26:26', NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for pet_types
-- ----------------------------
DROP TABLE IF EXISTS `pet_types`;
CREATE TABLE `pet_types`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'bx-paw',
  `display_order` int NULL DEFAULT 0,
  `is_active` tinyint(1) NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pet_types_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_types
-- ----------------------------
INSERT INTO `pet_types` VALUES (1, 'dog', 'Chó', 'bxs-dog', 1, 1);
INSERT INTO `pet_types` VALUES (2, 'cat', 'Mèo', 'bxs-cat', 2, 1);
INSERT INTO `pet_types` VALUES (3, 'fish', 'Cá', 'bx-water', 3, 1);
INSERT INTO `pet_types` VALUES (4, 'bird', 'Chim', 'bx-leaf', 4, 1);
INSERT INTO `pet_types` VALUES (5, 'hamster', 'Hamster', 'bx-heart', 5, 1);
INSERT INTO `pet_types` VALUES (6, 'rabbit', 'Thỏ', 'bx-heart', 6, 1);

-- ----------------------------
-- Table structure for products
-- ----------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `price` decimal(18, 0) NULL DEFAULT NULL,
  `old_price` decimal(18, 0) NULL DEFAULT 0,
  `discount` int NULL DEFAULT 0,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `weight` int NULL DEFAULT NULL,
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `stock` int NULL DEFAULT 100,
  `pet_type_id` int NULL DEFAULT NULL,
  `brand` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_products_pet_type`(`pet_type_id` ASC) USING BTREE,
  INDEX `idx_products_active_category_pet_price`(`is_active` ASC, `category` ASC, `pet_type_id` ASC, `price` ASC, `discount` ASC, `id` ASC) USING BTREE,
  CONSTRAINT `fk_product_pet_type` FOREIGN KEY (`pet_type_id`) REFERENCES `pet_types` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of products
-- ----------------------------
INSERT INTO `products` VALUES (1, 'Thức Ăn Hạt Cho Mèo Trưởng Thành Nuôi Trong Nhà Royal Canin Indoor 27', 'prod_royal1.jpg', 132000, 0, 0, NULL, 200, 'Thức ăn cho mèo', 0, 2, 'Royal Canin', 1);
INSERT INTO `products` VALUES (2, 'Thức Ăn Hạt Cho Mèo Con Royal Canin Kitten 36', 'prod_royal2.jpg', 135000, 0, 0, NULL, 200, 'Thức ăn cho mèo', 0, 2, 'Royal Canin', 1);
INSERT INTO `products` VALUES (3, 'Thức Ăn Hạt Cho Mèo Sỏi Thận Royal Canin Urinary S/O', 'prod_royal3.jpg', 185000, 0, 0, NULL, 200, 'Thức ăn cho mèo', 0, 2, 'Royal Canin', 1);
INSERT INTO `products` VALUES (4, 'Pate Cho Mèo Trưởng Thành Royal Canin Instinctive 85g', 'prod_royal4.jpg', 27000, 34000, 20, NULL, 200, 'Thức ăn cho mèo', 0, 2, 'Royal Canin', 1);
INSERT INTO `products` VALUES (5, 'Pate Cho Mèo Con Royal Canin Kitten Instinctive 85g', 'prod_royal5.jpg', 30000, 0, 0, NULL, 200, 'Thức ăn cho mèo', 12, 2, 'Royal Canin', 1);
INSERT INTO `products` VALUES (8, 'Thức Ăn Hạt Cho Chó Trưởng Thành Pedigree 1.5kg', 'prod_dog_food1.jpg', 95000, 120000, 20, 'Thức ăn hạt dinh dưỡng cho chó trưởng thành', 200, 'Thức Ăn Cho Chó', 9, 1, 'Pedigree', 1);
INSERT INTO `products` VALUES (9, 'Thức Ăn Hạt SmartHeart Cho Chó Con 1.5kg', 'prod_dog_food2.webp', 85000, 0, 0, 'Thức ăn hạt cho chó con từ 2-12 tháng', 200, 'Thức Ăn Cho Chó', 10, 1, 'SmartHeart', 1);
INSERT INTO `products` VALUES (10, 'Thức Ăn Hạt Royal Canin Mini Adult 2kg', 'prod_dog_food3.jpg', 245000, 280000, 12, 'Dành cho chó nhỏ trưởng thành', 200, 'Thức Ăn Cho Chó', 10, 1, 'Royal Canin', 1);
INSERT INTO `products` VALUES (11, 'Sữa Bột Bio Milk Cho Chó Con 100g', 'prod_dog_milk1.jpg', 65000, 0, 0, 'Sữa bột dinh dưỡng cho chó con', 200, 'Sữa Cho Chó', 10, 1, 'Bio Milk', 1);
INSERT INTO `products` VALUES (12, 'Sữa Tươi Pet Milk Cho Chó 200ml', 'prod_dog_milk2.jpg', 35000, 45000, 22, 'Sữa tươi bổ sung canxi cho chó', 200, 'Sữa Cho Chó', 9, 1, 'Pet Milk', 1);
INSERT INTO `products` VALUES (13, 'Vitamin Bổ Sung Cho Chó Nutri-Vet 60 viên', 'prod_dog_health1.jpg', 180000, 0, 0, 'Vitamin tổng hợp cho chó', 200, 'Chăm Sóc Sức Khoẻ Cho Chó', 10, 1, 'Nutri-Vet', 1);
INSERT INTO `products` VALUES (14, 'Thuốc Xổ Giun Cho Chó Drontal Plus', 'prod_dog_health2.jpg', 45000, 55000, 18, 'Thuốc xổ giun hiệu quả cho chó', 200, 'Chăm Sóc Sức Khoẻ Cho Chó', 10, 1, 'Drontal', 1);
INSERT INTO `products` VALUES (15, 'Bát Ăn Inox Chống Lật Cho Chó', 'prod_dog_bowl1.jpg', 55000, 0, 0, 'Bát ăn inox chống lật, dễ vệ sinh', 200, 'Dụng Cụ Ăn Uống Cho Chó', 10, 1, 'Pawise', 1);
INSERT INTO `products` VALUES (16, 'Bình Nước Tự Động Cho Chó 2.5L', 'prod_dog_bowl2.jpg', 120000, 150000, 20, 'Bình nước tự động tiện lợi', 200, 'Dụng Cụ Ăn Uống Cho Chó', 10, 1, 'Pawise', 1);
INSERT INTO `products` VALUES (17, 'Bóng Cao Su Cho Chó Gặm', 'prod_dog_toy1.jpg', 35000, 0, 0, 'Bóng cao su bền, an toàn cho chó', 200, 'Đồ Chơi - Huấn Luyện Cho Chó', 10, 1, 'Pawise', 1);
INSERT INTO `products` VALUES (18, 'Dây Dắt Chó Tự Cuốn 5m', 'prod_dog_toy2.jpg', 150000, 180000, 17, 'Dây dắt tự cuốn tiện lợi khi dạo phố', 200, 'Đồ Chơi - Huấn Luyện Cho Chó', 7, 1, 'Pawise', 1);
INSERT INTO `products` VALUES (19, 'Sữa Tắm SOS Cho Chó 530ml', 'prod_dog_shampoo1.jpg', 89000, 110000, 19, 'Sữa tắm khử mùi, mượt lông cho chó', 200, 'Sữa Tắm - Dụng Cụ Vệ Sinh Cho Chó', 0, 1, 'SOS', 1);
INSERT INTO `products` VALUES (20, 'Lược Chải Lông Cho Chó Mèo', 'prod_dog_shampoo2.jpg', 45000, 0, 0, 'Lược chải lông chuyên dụng', 200, 'Sữa Tắm - Dụng Cụ Vệ Sinh Cho Chó', 0, 1, 'Pawise', 1);
INSERT INTO `products` VALUES (21, 'Vitamin Lysine Cho Mèo 60 viên', 'prod_cat_health1.png', 120000, 0, 0, 'Hỗ trợ miễn dịch cho mèo', 200, 'Chăm Sóc Sức Khoẻ Cho Mèo', 10, 2, 'Nutri-Vet', 1);
INSERT INTO `products` VALUES (22, 'Thuốc Nhỏ Gáy Trị Ve Rận Cho Mèo', 'prod_cat_health2.jpg', 75000, 90000, 17, 'Thuốc nhỏ gáy hiệu quả cho mèo', 200, 'Chăm Sóc Sức Khoẻ Cho Mèo', 10, 2, 'Nutri-Vet', 1);
INSERT INTO `products` VALUES (23, 'Bát Ăn Đôi Cho Mèo Có Giá Đỡ', 'prod_cat_bowl1.jpg', 85000, 100000, 15, 'Bát ăn đôi nghiêng 15 độ bảo vệ cổ mèo', 200, 'Dụng Cụ Ăn Uống Cho Mèo', 10, 2, 'Pawise', 1);
INSERT INTO `products` VALUES (24, 'Máy Lọc Nước Tự Động Cho Mèo 2L', 'prod_cat_bowl2.png', 250000, 320000, 22, 'Máy lọc nước tuần hoàn cho mèo', 200, 'Dụng Cụ Ăn Uống Cho Mèo', 3, 2, 'Pawise', 1);
INSERT INTO `products` VALUES (25, 'Cần Câu Đồ Chơi Cho Mèo', 'prod_cat_toy1.jpg', 25000, 0, 0, 'Cần câu lông vũ kích thích mèo vận động', 200, 'Đồ Chơi - Huấn Luyện Cho Mèo', 10, 2, 'Pawise', 1);
INSERT INTO `products` VALUES (26, 'Tháp Bóng 3 Tầng Cho Mèo', 'prod_cat_toy2.jpg', 95000, 120000, 21, 'Đồ chơi tháp bóng giải trí cho mèo', 200, 'Đồ Chơi - Huấn Luyện Cho Mèo', 10, 2, 'Pawise', 1);
INSERT INTO `products` VALUES (27, 'Sữa Tắm SOS Cho Mèo 530ml', 'prod_cat_shampoo1.webp', 89000, 0, 0, 'Sữa tắm dưỡng lông cho mèo', 200, 'Sữa Tắm - Dụng Cụ Vệ Sinh Cho Mèo', 10, 2, 'SOS', 1);
INSERT INTO `products` VALUES (28, 'Khăn Ướt Vệ Sinh Cho Mèo 80 tờ', 'prod_cat_shampoo2.jpg', 45000, 55000, 18, 'Khăn ướt lau chân, mặt cho mèo', 200, 'Sữa Tắm - Dụng Cụ Vệ Sinh Cho Mèo', 10, 2, 'Pawise', 1);
INSERT INTO `products` VALUES (29, 'Cát Vệ Sinh Đậu Nành Cho Mèo 6L', 'prod_cat_sand1.jpg', 75000, 95000, 21, 'Cát đậu nành khử mùi tốt, thân thiện môi trường', 200, 'Cát Vệ Sinh Cho Mèo', 10, 2, 'Cature', 1);
INSERT INTO `products` VALUES (30, 'Cát Bentonite Cho Mèo 10L', 'prod_cat_sand2.webp', 65000, 0, 0, 'Cát bentonite vón cục nhanh', 200, 'Cát Vệ Sinh Cho Mèo', 8, 2, 'Cature', 1);
INSERT INTO `products` VALUES (31, 'Nhà Vệ Sinh Cho Mèo Có Nắp', 'prod_cat_toilet1.png', 195000, 250000, 22, 'Nhà vệ sinh kín chống bắn cát', 200, 'Dụng Cụ Vệ Sinh Cho Mèo', 0, 2, 'IRIS Ohyama', 1);
INSERT INTO `products` VALUES (32, 'Xẻng Xúc Cát Vệ Sinh Cho Mèo', 'prod_cat_toilet2.jpg', 25000, 0, 0, 'Xẻng xúc cát bền, tiện lợi', 200, 'Dụng Cụ Vệ Sinh Cho Mèo', 4, 2, 'Pawise', 1);

-- ----------------------------
-- Table structure for reviews
-- ----------------------------
DROP TABLE IF EXISTS `reviews`;
CREATE TABLE `reviews`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `user_id` int NOT NULL,
  `rating` int NOT NULL,
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` tinyint NULL DEFAULT 1,
  `admin_reply` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of reviews
-- ----------------------------
INSERT INTO `reviews` VALUES (1, 1, 1, 5, 'Sản phẩm rất tốt, mèo nhà mình rất thích!', '2026-03-07 16:32:47', 1, NULL);
INSERT INTO `reviews` VALUES (2, 1, 2, 4, 'Chất lượng ổn, giao hàng nhanh', '2026-03-07 16:32:47', 1, 'Dạ petshop xin cảm ơn ạ');
INSERT INTO `reviews` VALUES (3, 2, 1, 5, 'Thức ăn chất lượng cao, đóng gói cẩn thận', '2026-03-07 16:32:47', 1, NULL);
INSERT INTO `reviews` VALUES (4, 2, 2, 5, 'sản phẩm tốt', '2026-03-15 12:57:51', 1, NULL);
INSERT INTO `reviews` VALUES (5, 2, 8, 5, 'Hàng tốt lắm nha mọi người ơi', '2026-03-30 17:25:31', 1, NULL);
INSERT INTO `reviews` VALUES (6, 2, 11, 2, 'hàng dở tệ luôn, giống fake', '2026-05-29 21:08:08', 0, NULL);
INSERT INTO `reviews` VALUES (7, 2, 11, 5, 'hàng tốt', '2026-05-29 21:22:51', 1, NULL);
INSERT INTO `reviews` VALUES (8, 2, 11, 2, 'tệ quá', '2026-05-29 21:42:04', 0, NULL);
INSERT INTO `reviews` VALUES (9, 2, 1, 5, 'hàng đẹp ', '2026-05-29 21:53:28', 1, NULL);
INSERT INTO `reviews` VALUES (10, 12, 2, 5, 'Hàng tốt quá', '2026-06-02 22:53:14', 1, NULL);
INSERT INTO `reviews` VALUES (11, 20, 2, 5, 'hàng tốt', '2026-06-02 22:54:58', 1, NULL);
INSERT INTO `reviews` VALUES (12, 18, 2, 5, 'hàng tạm được', '2026-06-02 22:55:29', 1, NULL);
INSERT INTO `reviews` VALUES (13, 13, 2, 3, 'Hàng cũng ok nhưng chó không thích', '2026-06-02 22:56:55', 1, 'Hàng anh dùng ok không ạ');
INSERT INTO `reviews` VALUES (14, 31, 2, 5, 'Hàng quá tốt', '2026-06-02 22:57:20', 0, NULL);

-- ----------------------------
-- Table structure for security_events
-- ----------------------------
DROP TABLE IF EXISTS `security_events`;
CREATE TABLE `security_events`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `event_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `principal` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `details` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_security_events_type_created`(`event_type` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_security_events_principal_created`(`principal` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of security_events
-- ----------------------------
INSERT INTO `security_events` VALUES (1, 'AUTH_FAIL', 'user1@gmail.com', '0:0:0:0:0:0:0:1', 'User with role \'user\' tried to access admin panel.', '2026-06-15 16:23:59');

-- ----------------------------
-- Table structure for services
-- ----------------------------
DROP TABLE IF EXISTS `services`;
CREATE TABLE `services`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `price` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of services
-- ----------------------------
INSERT INTO `services` VALUES (1, 'Khám & Điều trị', '150,000đ', 'Khám tổng quát và điều trị bệnh');
INSERT INTO `services` VALUES (2, 'Phẫu thuật', 'Theo ca', 'Phẫu thuật các loại');
INSERT INTO `services` VALUES (3, 'Tiêm phòng Vaccine', 'Tùy loại', 'Tiêm vaccine phòng bệnh');
INSERT INTO `services` VALUES (4, 'Spa & Làm đẹp', '350,000đ', 'Tắm, cắt tỉa lông, làm đẹp');
INSERT INTO `services` VALUES (5, 'Khách Sạn Thú Cưng', '200,000đ/ngày', 'Gửi thú cưng qua đêm');

-- ----------------------------
-- Table structure for stock_imports
-- ----------------------------
DROP TABLE IF EXISTS `stock_imports`;
CREATE TABLE `stock_imports`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `supplier_id` int NULL DEFAULT NULL,
  `receipt_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `received_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `created_by` int NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stock_imports_receipt_code`(`receipt_code` ASC) USING BTREE,
  INDEX `fk_stock_imports_supplier`(`supplier_id` ASC) USING BTREE,
  INDEX `fk_stock_imports_created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `fk_stock_imports_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_stock_imports_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of stock_imports
-- ----------------------------

-- ----------------------------
-- Table structure for stock_movements
-- ----------------------------
DROP TABLE IF EXISTS `stock_movements`;
CREATE TABLE `stock_movements`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `inventory_batch_id` int NULL DEFAULT NULL,
  `order_id` int NULL DEFAULT NULL,
  `movement_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `quantity` int NOT NULL,
  `reference_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `created_by` int NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_stock_movements_product_created`(`product_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_stock_movements_type_created`(`movement_type` ASC, `created_at` ASC) USING BTREE,
  INDEX `fk_stock_movements_batch`(`inventory_batch_id` ASC) USING BTREE,
  INDEX `fk_stock_movements_order`(`order_id` ASC) USING BTREE,
  INDEX `fk_stock_movements_created_by`(`created_by` ASC) USING BTREE,
  CONSTRAINT `fk_stock_movements_batch` FOREIGN KEY (`inventory_batch_id`) REFERENCES `inventory_batches` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_stock_movements_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_stock_movements_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_stock_movements_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of stock_movements
-- ----------------------------

-- ----------------------------
-- Table structure for suppliers
-- ----------------------------
DROP TABLE IF EXISTS `suppliers`;
CREATE TABLE `suppliers`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `contact_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of suppliers
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `fullname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'user',
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `status` bit(1) NOT NULL,
  `phone` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `has_used_discount` tinyint(1) NOT NULL DEFAULT 0,
  `email_verified` tinyint(1) NOT NULL DEFAULT 0,
  `verification_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `verification_token_expiry` timestamp NULL DEFAULT NULL,
  `failed_login_attempts` int NOT NULL DEFAULT 0,
  `locked_until` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_users_email_username_locked`(`email` ASC, `username` ASC, `locked_until` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '123456', 'Quản trị viên', 'admin@petvaccine.com', 'admin', '2026-03-06 17:59:18', b'1', NULL, NULL, 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `users` VALUES (2, 'user1', '123456', 'Nguyễn Văn B', 'user1@gmail.com', 'user', '2026-03-06 17:59:18', b'1', '0867943315', NULL, 1, 1, NULL, NULL, 0, NULL);
INSERT INTO `users` VALUES (3, 'doctor1', '123456', 'Bác sĩ Ngọc Thành', 'doctor1@petvaccine.com', 'user', '2026-03-06 17:59:18', b'1', NULL, NULL, 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `users` VALUES (8, 'Dat Pham', 'null', 'Đạt Phạm Hữu', 'phamdat7879@gmail.com', 'user', '2026-03-17 19:37:17', b'1', '0867943315', NULL, 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `users` VALUES (9, 'Đạt Phạm', 'null', 'Nguyen Van Tai Em', 'phamdat12457@gmail.com', 'user', '2026-03-17 19:38:45', b'1', '0123456788', NULL, 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `users` VALUES (10, 'Dat', 'null', 'Phạm Hữu Đạt', 'dat41092@gmail.com', 'user', '2026-03-17 21:55:33', b'1', '0867943315', NULL, 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `users` VALUES (11, 'Đạt Phạm Hữu', 'null', 'Nguyen Van C', '23130056@st.hcmuaf.edu.vn', 'user', '2026-03-17 22:56:58', b'1', '0123456789', NULL, 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `users` VALUES (12, 'Thư Trương Thị Minh', 'null', NULL, '23130319@st.hcmuaf.edu.vn', 'user', '2026-03-31 12:03:03', b'1', NULL, NULL, 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `users` VALUES (13, 'Tài con', '$2a$12$99UJ2MOfiqORapH5f/.YMOvh.sWx/w8utYxrm7BsbdS4JTXNPiAda', 'Nguyễn Văn Tài', 'taicon@gmail.com', 'user', '2026-06-15 15:40:51', b'0', '0387427844', NULL, 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `users` VALUES (14, 'Đạt lã', '$2a$12$MTG5BZskMW0Q0Q251uR4A.Tavm/cQDQA3NuUAmf9a3FNO2NY7Xnl.', 'Lã Phương Tiến Đạt', 'datla@gmail.com', 'shiper', '2026-06-15 15:43:46', b'1', '0287323847', NULL, 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `users` VALUES (15, 'Tài', '$2a$12$CRpK9Ikmawd6FOFo5Vc9RumQEVdIY7OXbTnfND7abCfeLO6J/sRuC', 'Phạm Tài', 'tai@gmail.com', 'staff', '2026-06-15 15:44:36', b'1', '0873647123', NULL, 0, 1, NULL, NULL, 0, NULL);

-- ----------------------------
-- Table structure for wishlist
-- ----------------------------
DROP TABLE IF EXISTS `wishlist`;
CREATE TABLE `wishlist`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_product_wishlist`(`user_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `wishlist_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `wishlist_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wishlist
-- ----------------------------
INSERT INTO `wishlist` VALUES (1, 2, 32, '2026-06-12 19:19:01');
INSERT INTO `wishlist` VALUES (2, 2, 18, '2026-06-12 19:19:27');

SET FOREIGN_KEY_CHECKS = 1;
