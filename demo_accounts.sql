-- ============================================================
-- Demo accounts for PetShop
-- Password for both: Demo@123
-- ============================================================

-- Admin account
INSERT INTO users (username, password, fullname, email, phone, role, status, email_verified)
VALUES ('admin_demo', '$2a$10$7kyd2j7ho795flOb9h0nT.bLYprbK3yzztk9Maxh/ryZmruaxNZ/O', 'Admin Demo', 'admin@petshop.vn', '0901234567', 'admin', 1, TRUE)
ON DUPLICATE KEY UPDATE username = username;

-- User account
INSERT INTO users (username, password, fullname, email, phone, role, status, email_verified)
VALUES ('user_demo', '$2a$10$7kyd2j7ho795flOb9h0nT.bLYprbK3yzztk9Maxh/ryZmruaxNZ/O', 'Nguyen Van Demo', 'user@petshop.vn', '0909876543', 'user', 1, TRUE)
ON DUPLICATE KEY UPDATE username = username;

-- Mark all existing users as email verified (for users created before email verification feature)
UPDATE users SET email_verified = TRUE WHERE email_verified = FALSE AND verification_token IS NULL;
