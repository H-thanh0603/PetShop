-- ============================================================
-- Demo accounts for PetShop
-- Password for both: Demo@123
-- Run this AFTER db.sql and the migration scripts.
-- ============================================================

-- BCrypt hash of "Demo@123" (cost 10)
SET @demo_hash = '$2a$10$7kyd2j7ho795flOb9h0nT.bLYprbK3yzztk9Maxh/ryZmruaxNZ/O';

-- Admin account
INSERT INTO users (username, password, fullname, email, phone, role, status, email_verified)
VALUES ('admin_demo', @demo_hash, 'Admin Demo', 'admin@petshop.vn', '0901234567', 'admin', 'active', TRUE)
ON DUPLICATE KEY UPDATE password = @demo_hash, status = 'active', email_verified = TRUE;

-- User account
INSERT INTO users (username, password, fullname, email, phone, role, status, email_verified)
VALUES ('user_demo', @demo_hash, 'Nguyen Van Demo', 'user@petshop.vn', '0909876543', 'user', 'active', TRUE)
ON DUPLICATE KEY UPDATE password = @demo_hash, status = 'active', email_verified = TRUE;

-- Also fix the original admin account from db.sql (if it exists with plain-text password)
UPDATE users SET password = @demo_hash, email_verified = TRUE
WHERE email = 'admin@gmail.com' AND password NOT LIKE '$2a$%';

UPDATE users SET password = @demo_hash, email_verified = TRUE
WHERE email = 'user1@gmail.com' AND password NOT LIKE '$2a$%';

-- Reset any lockouts on demo accounts
UPDATE users SET failed_login_attempts = 0, locked_until = NULL
WHERE email IN ('admin@petshop.vn', 'user@petshop.vn', 'admin@gmail.com', 'user1@gmail.com');

-- Mark all existing users as email verified
UPDATE users SET email_verified = TRUE WHERE email_verified = FALSE AND verification_token IS NULL;
