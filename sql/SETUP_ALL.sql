-- ============================================================
-- PetShop - SETUP TỔNG HỢP
-- Chạy trong MySQL Workbench: File > Open SQL Script > chọn file này
-- Sau đó nhấn Query > Execute SQL Script (KHÔNG dùng Ctrl+Shift+Enter)
-- Lỗi "Duplicate column" là bình thường, bỏ qua
-- ============================================================

USE petvaccine;
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- BƯỚC 1: Thêm cột vào users (bỏ qua nếu đã có)
-- ============================================================
DROP PROCEDURE IF EXISTS sp_add_col;
DELIMITER $$
CREATE PROCEDURE sp_add_col()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='failed_login_attempts') THEN
        ALTER TABLE users ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='locked_until') THEN
        ALTER TABLE users ADD COLUMN locked_until DATETIME NULL DEFAULT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='email_verified') THEN
        ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='verification_token') THEN
        ALTER TABLE users ADD COLUMN verification_token VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='verification_token_expiry') THEN
        ALTER TABLE users ADD COLUMN verification_token_expiry TIMESTAMP NULL;
    END IF;
    -- products columns
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='products' AND COLUMN_NAME='is_active') THEN
        ALTER TABLE products ADD COLUMN is_active TINYINT(1) NOT NULL DEFAULT 1;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='products' AND COLUMN_NAME='stock') THEN
        ALTER TABLE products ADD COLUMN stock INT NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='products' AND COLUMN_NAME='stock_quantity') THEN
        ALTER TABLE products ADD COLUMN stock_quantity INT NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='products' AND COLUMN_NAME='reserved_quantity') THEN
        ALTER TABLE products ADD COLUMN reserved_quantity INT NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='products' AND COLUMN_NAME='sold_quantity') THEN
        ALTER TABLE products ADD COLUMN sold_quantity INT NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='products' AND COLUMN_NAME='weight') THEN
        ALTER TABLE products ADD COLUMN weight INT NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='products' AND COLUMN_NAME='category') THEN
        ALTER TABLE products ADD COLUMN category VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='products' AND COLUMN_NAME='pet_type_id') THEN
        ALTER TABLE products ADD COLUMN pet_type_id INT NULL;
    END IF;
    -- orders columns
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='orders' AND COLUMN_NAME='payment_method') THEN
        ALTER TABLE orders ADD COLUMN payment_method VARCHAR(50) DEFAULT 'COD';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='orders' AND COLUMN_NAME='payment_status') THEN
        ALTER TABLE orders ADD COLUMN payment_status TINYINT(1) NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='orders' AND COLUMN_NAME='createdAt') THEN
        ALTER TABLE orders ADD COLUMN createdAt TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='orders' AND COLUMN_NAME='recipient_fullname') THEN
        ALTER TABLE orders ADD COLUMN recipient_fullname VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER address;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='orders' AND COLUMN_NAME='recipient_phone') THEN
        ALTER TABLE orders ADD COLUMN recipient_phone VARCHAR(20) NULL AFTER recipient_fullname;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='orders' AND COLUMN_NAME='shipping_address') THEN
        ALTER TABLE orders ADD COLUMN shipping_address VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER recipient_phone;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_items')
       AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_items' AND COLUMN_NAME='original_price') THEN
        ALTER TABLE order_items ADD COLUMN original_price DECIMAL(18,0) NOT NULL DEFAULT 0 AFTER price;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_items')
       AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_items' AND COLUMN_NAME='final_price') THEN
        ALTER TABLE order_items ADD COLUMN final_price DECIMAL(18,0) NOT NULL DEFAULT 0 AFTER original_price;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_items')
       AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_items' AND COLUMN_NAME='discount_amount') THEN
        ALTER TABLE order_items ADD COLUMN discount_amount DECIMAL(18,0) NOT NULL DEFAULT 0 AFTER final_price;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_items')
       AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_items' AND COLUMN_NAME='promotion_id') THEN
        ALTER TABLE order_items ADD COLUMN promotion_id INT NULL AFTER discount_amount;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_items')
       AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_items' AND COLUMN_NAME='promotion_name') THEN
        ALTER TABLE order_items ADD COLUMN promotion_name VARCHAR(255) NULL AFTER promotion_id;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_items')
       AND NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_items' AND COLUMN_NAME='promotion_type') THEN
        ALTER TABLE order_items ADD COLUMN promotion_type VARCHAR(50) NULL AFTER promotion_name;
    END IF;
END$$
DELIMITER ;
CALL sp_add_col();
DROP PROCEDURE IF EXISTS sp_add_col;

UPDATE orders
SET recipient_fullname = COALESCE(NULLIF(recipient_fullname, ''), fullname),
    recipient_phone = COALESCE(NULLIF(recipient_phone, ''), phone),
    shipping_address = COALESCE(NULLIF(shipping_address, ''), address)
WHERE recipient_fullname IS NULL
   OR recipient_fullname = ''
   OR recipient_phone IS NULL
   OR recipient_phone = ''
   OR shipping_address IS NULL
   OR shipping_address = '';

ALTER TABLE orders
    MODIFY recipient_fullname VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY recipient_phone VARCHAR(20) NOT NULL,
    MODIFY shipping_address VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

-- ============================================================
-- BƯỚC 2: Tạo bảng mới
-- ============================================================
CREATE TABLE IF NOT EXISTS pet_types (
    id INT NOT NULL AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50) DEFAULT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_pet_type_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cart (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_cart_user_product (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wishlist (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_wishlist (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reviews (
    id INT NOT NULL AUTO_INCREMENT,
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS coupons (
    id INT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    discount_type VARCHAR(20) NOT NULL DEFAULT 'percent',
    discount_value DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    discount_percent INT NOT NULL DEFAULT 0,
    min_order DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    max_discount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    usage_limit INT NOT NULL DEFAULT 1,
    used INT NOT NULL DEFAULT 0,
    quantity INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'available',
    locked_count INT NOT NULL DEFAULT 0,
    per_user_limit INT NOT NULL DEFAULT 1,
    start_date DATE NULL,
    end_date DATE NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_coupon_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) DEFAULT 'info',
    link VARCHAR(500) NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS addresses (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    province VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    ward VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    is_default TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_status_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    old_status VARCHAR(50) NOT NULL,
    new_status VARCHAR(50) NOT NULL,
    changed_by INT NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    actor_type VARCHAR(30) NOT NULL,
    actor_id INT NULL,
    action VARCHAR(100) NOT NULL,
    old_status VARCHAR(50) NULL,
    new_status VARCHAR(50) NULL,
    note TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_logs_order_created (order_id, created_at),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS coupon_products (
    coupon_id INT NOT NULL,
    product_id INT NOT NULL,
    PRIMARY KEY (coupon_id, product_id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS coupon_locks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    coupon_id INT NOT NULL,
    user_id INT NOT NULL,
    order_id INT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'locked',
    locked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    released_at DATETIME NULL,
    INDEX idx_coupon_locks_coupon_user_status (coupon_id, user_id, status),
    INDEX idx_coupon_locks_expires_status (expires_at, status),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS promotions (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    promotion_type VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    discount_type VARCHAR(20) NOT NULL DEFAULT 'PERCENT',
    discount_value DECIMAL(18,0) NOT NULL DEFAULT 0,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_promotions_status_dates (status, start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS promotion_products (
    id INT NOT NULL AUTO_INCREMENT,
    promotion_id INT NOT NULL,
    product_id INT NOT NULL,
    sale_quantity INT NULL,
    sold_quantity INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_promotion_products_pair (promotion_id, product_id),
    KEY idx_promotion_products_product (product_id),
    FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS admin_action_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id INT,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS remember_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS blogs (
    id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(500) NOT NULL,
    content TEXT,
    image VARCHAR(255) NULL,
    author_id INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- BƯỚC 3: Dữ liệu pet_types
-- ============================================================
INSERT IGNORE INTO pet_types (code, name, icon, display_order, is_active) VALUES
('dog',     'Chó',     '🐶', 1, 1),
('cat',     'Mèo',     '🐱', 2, 1),
('fish',    'Cá',      '🐟', 3, 1),
('bird',    'Chim',    '🐦', 4, 1),
('hamster', 'Hamster', '🐹', 5, 1),
('rabbit',  'Thỏ',     '🐰', 6, 1);

-- ============================================================
-- BƯỚC 4: Cập nhật + thêm sản phẩm
-- ============================================================
UPDATE products SET is_active = 1;

UPDATE products SET
    image = 'prod_royal1.jpg', category = 'Thức Ăn Cho Mèo', stock = 50, weight = 400, is_active = 1,
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1)
WHERE id = 1;
UPDATE products SET
    image = 'prod_royal2.jpg', category = 'Thức Ăn Cho Mèo', stock = 40, weight = 400, is_active = 1,
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1)
WHERE id = 2;
UPDATE products SET
    image = 'prod_royal3.jpg', category = 'Chăm Sóc Sức Khoẻ Cho Mèo', stock = 30, weight = 300, is_active = 1,
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1)
WHERE id = 3;
UPDATE products SET
    image = 'prod_royal4.jpg', category = 'Thức Ăn Cho Mèo', stock = 100, weight = 85, is_active = 1,
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1)
WHERE id = 4;
UPDATE products SET
    image = 'prod_royal5.jpg', category = 'Thức Ăn Cho Mèo', stock = 80, weight = 85, is_active = 1,
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1)
WHERE id = 5;

INSERT IGNORE INTO products (id, name, image, price, discount, description, category, stock, weight, is_active, pet_type_id) VALUES
(6,  'Thức Ăn Hạt Cho Chó Pedigree Adult 1.5kg',      'prod_pedigree1.jpg', 95000,  0,  'Thức ăn hạt dinh dưỡng cho chó trưởng thành',                    'Thức Ăn Cho Chó',              60,  1500, 1, (SELECT id FROM pet_types WHERE code='dog' LIMIT 1)),
(7,  'Thức Ăn Hạt Cho Chó Con Pedigree Puppy 1.5kg',  'prod_pedigree2.jpg', 105000, 10, 'Thức ăn hạt cho chó con từ 2-12 tháng tuổi',                     'Thức Ăn Cho Chó',              50,  1500, 1, (SELECT id FROM pet_types WHERE code='dog' LIMIT 1)),
(8,  'Pate Cho Chó Pedigree Thịt Bò 130g',            'prod_pedigree3.jpg', 18000,  0,  'Pate thịt bò thơm ngon cho chó mọi lứa tuổi',                    'Thức Ăn Cho Chó',              120, 130,  1, (SELECT id FROM pet_types WHERE code='dog' LIMIT 1)),
(9,  'Vòng Cổ Chó Có Khóa Inox Size M',               'prod_collar1.jpg',   85000,  15, 'Vòng cổ chắc chắn, khóa inox không gỉ, size M',                  'Phụ Kiện Cho Chó',             40,  150,  1, (SELECT id FROM pet_types WHERE code='dog' LIMIT 1)),
(10, 'Đồ Chơi Bóng Cao Su Cho Chó',                  'prod_toy1.jpg',      45000,  0,  'Bóng cao su bền, an toàn, giúp chó vận động',                    'Đồ Chơi - Huấn Luyện Cho Chó', 80,  200,  1, (SELECT id FROM pet_types WHERE code='dog' LIMIT 1)),
(11, 'Cát Vệ Sinh Cho Mèo Bentonite 5L',              'prod_litter1.jpg',   75000,  0,  'Cát bentonite vón cục tốt, khử mùi hiệu quả',                    'Cát Vệ Sinh Cho Mèo',          70,  5000, 1, (SELECT id FROM pet_types WHERE code='cat' LIMIT 1)),
(12, 'Cát Vệ Sinh Tofu Cho Mèo 6L',                  'prod_litter2.jpg',   120000, 5,  'Cát đậu phụ thân thiện môi trường, thấm hút tốt',                'Cát Vệ Sinh Cho Mèo',          55,  2500, 1, (SELECT id FROM pet_types WHERE code='cat' LIMIT 1)),
(13, 'Đồ Chơi Cần Câu Lông Vũ Cho Mèo',              'prod_toy2.jpg',      35000,  0,  'Cần câu lông vũ kích thích bản năng săn mồi của mèo',            'Đồ Chơi Cho Mèo',              90,  100,  1, (SELECT id FROM pet_types WHERE code='cat' LIMIT 1)),
(14, 'Bát Ăn Inox Cho Mèo Đôi',                      'prod_bowl1.jpg',     55000,  0,  'Bát đôi inox 304 không gỉ, dễ vệ sinh',                          'Dụng Cụ Ăn Uống Cho Mèo',     60,  300,  1, (SELECT id FROM pet_types WHERE code='cat' LIMIT 1)),
(15, 'Sữa Tắm Cho Chó Mèo Bio-Groom 355ml',          'prod_shampoo1.jpg',  145000, 20, 'Sữa tắm dịu nhẹ, hương thơm tự nhiên',                          'Chăm Sóc Sức Khoẻ Cho Chó',   45,  355,  1, (SELECT id FROM pet_types WHERE code='dog' LIMIT 1)),
(16, 'Thức Ăn Hạt Cho Mèo Whiskas Adult 1.2kg',      'prod_whiskas1.jpg',  89000,  0,  'Thức ăn hạt Whiskas đầy đủ dinh dưỡng cho mèo trưởng thành',     'Thức Ăn Cho Mèo',              75,  1200, 1, (SELECT id FROM pet_types WHERE code='cat' LIMIT 1)),
(17, 'Snack Thưởng Cho Mèo Temptations 85g',          'prod_snack1.jpg',    65000,  0,  'Snack giòn tan, mèo cực thích, hương vị cá hồi',                 'Thức Ăn Cho Mèo',              100, 85,   1, (SELECT id FROM pet_types WHERE code='cat' LIMIT 1)),
(18, 'Nhà Cào Móng Cho Mèo Dạng Trụ',                'prod_scratch1.jpg',  185000, 10, 'Trụ cào móng bằng dây thừng tự nhiên, cao 45cm',                 'Đồ Chơi Cho Mèo',              30,  800,  1, (SELECT id FROM pet_types WHERE code='cat' LIMIT 1)),
(19, 'Thức Ăn Hạt Cho Chó Royal Canin Medium Adult', 'prod_rc_dog1.jpg',   320000, 0,  'Thức ăn hạt cao cấp cho chó cỡ vừa từ 1-7 tuổi',                'Thức Ăn Cho Chó',              35,  1000, 1, (SELECT id FROM pet_types WHERE code='dog' LIMIT 1)),
(20, 'Dây Dắt Chó Có Tay Cầm Chống Trượt 1.5m',     'prod_leash1.jpg',    75000,  0,  'Dây dắt chó bền chắc, tay cầm bọc cao su chống trượt',           'Phụ Kiện Cho Chó',             50,  250,  1, (SELECT id FROM pet_types WHERE code='dog' LIMIT 1));

UPDATE order_items
SET original_price = COALESCE(NULLIF(original_price, 0), price),
    final_price = COALESCE(NULLIF(final_price, 0), price),
    discount_amount = GREATEST(COALESCE(original_price, price) - COALESCE(final_price, price), 0)
WHERE original_price = 0
   OR final_price = 0;

-- ============================================================
-- BƯỚC 5: Tài khoản demo
-- ============================================================
SET @demo_hash = '$2a$10$7kyd2j7ho795flOb9h0nT.bLYprbK3yzztk9Maxh/ryZmruaxNZ/O';

INSERT INTO users (username, password, fullname, email, phone, role, status, email_verified)
VALUES ('admin_demo', @demo_hash, 'Admin Demo', 'admin@petshop.vn', '0901234567', 'admin', 'active', TRUE)
ON DUPLICATE KEY UPDATE password = @demo_hash, status = 'active', email_verified = TRUE;

INSERT INTO users (username, password, fullname, email, phone, role, status, email_verified)
VALUES ('user_demo', @demo_hash, 'Nguyen Van Demo', 'user@petshop.vn', '0909876543', 'user', 'active', TRUE)
ON DUPLICATE KEY UPDATE password = @demo_hash, status = 'active', email_verified = TRUE;

UPDATE users SET password = @demo_hash, email_verified = TRUE
WHERE email = 'admin@gmail.com' AND password NOT LIKE '$2a$%';

UPDATE users SET password = @demo_hash, email_verified = TRUE
WHERE email = 'user1@gmail.com' AND password NOT LIKE '$2a$%';

UPDATE users SET failed_login_attempts = 0, locked_until = NULL
WHERE email IN ('admin@petshop.vn','user@petshop.vn','admin@gmail.com','user1@gmail.com');

UPDATE users SET email_verified = TRUE WHERE email_verified = FALSE AND verification_token IS NULL;

-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;
-- XONG! Chạy Start.bat, nhập mật khẩu MySQL khi được hỏi.
-- Đăng nhập: admin@petshop.vn / Demo@123
-- ============================================================
