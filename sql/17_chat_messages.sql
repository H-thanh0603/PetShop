CREATE TABLE `chat_messages` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `sender_id` INT NOT NULL,      -- ID người gửi (User hoặc Admin)
  `receiver_id` INT NOT NULL,    -- ID người nhận
  `message` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL, -- Nội dung chat
  `is_admin_sender` TINYINT(1) DEFAULT 0, -- 1: Admin gửi, 0: User gửi (để dễ phân màu bong bóng chat)
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci;