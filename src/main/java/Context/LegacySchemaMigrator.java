package Context;

import Util.AppConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The legacy idempotent schema migrations that used to run from DBContext's
 * static initializer on every boot. Invoked exactly once per database by the
 * Flyway Java migration db.migration.V1__LegacyIdempotentBaseline.
 *
 * Everything here is idempotent (CREATE TABLE IF NOT EXISTS,
 * addColumnIfMissing, guarded seeding), so running it against an already
 * migrated database is a no-op.
 */
public final class LegacySchemaMigrator {

    private LegacySchemaMigrator() {
    }

    /**
     * Run schema migrations on startup.
     */
    public static void migrate(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            addColumnIfMissing(conn, stmt, "products", "weight", "INT NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, stmt, "products", "is_active", "TINYINT(1) NOT NULL DEFAULT 1");
            addColumnIfMissing(conn, stmt, "products", "stock", "INT NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, stmt, "products", "category", "VARCHAR(255) NULL");
            addColumnIfMissing(conn, stmt, "products", "pet_type_id", "INT NULL");
            addColumnIfMissing(conn, stmt, "products", "brand", "VARCHAR(100) NULL");
            addColumnIfMissing(conn, stmt, "users", "email_verified", "BOOLEAN NOT NULL DEFAULT FALSE");
            addColumnIfMissing(conn, stmt, "users", "verification_token", "VARCHAR(255) NULL");
            addColumnIfMissing(conn, stmt, "users", "verification_token_expiry", "TIMESTAMP NULL");
            addColumnIfMissing(conn, stmt, "users", "failed_login_attempts", "INT NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, stmt, "users", "locked_until", "DATETIME NULL DEFAULT NULL");
            addColumnIfMissing(conn, stmt, "orders", "payment_method", "VARCHAR(50) DEFAULT 'COD'");
            addColumnIfMissing(conn, stmt, "orders", "payment_status", "TINYINT(1) NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, stmt, "orders", "createdAt", "TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP");
            addColumnIfMissing(conn, stmt, "orders", "recipient_fullname", "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL");
            addColumnIfMissing(conn, stmt, "orders", "recipient_phone", "VARCHAR(20) NULL");
            addColumnIfMissing(conn, stmt, "orders", "shipping_address", "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL");
            addColumnIfMissing(conn, stmt, "orders", "status_updated_at", "TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
            addColumnIfMissing(conn, stmt, "order_items", "original_price", "DECIMAL(18,0) NULL");
            addColumnIfMissing(conn, stmt, "order_items", "final_price", "DECIMAL(18,0) NULL");
            addColumnIfMissing(conn, stmt, "order_items", "discount_amount", "DECIMAL(18,0) NULL");
            addColumnIfMissing(conn, stmt, "order_items", "promotion_id", "INT NULL");
            addColumnIfMissing(conn, stmt, "order_items", "promotion_name", "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL");
            addColumnIfMissing(conn, stmt, "order_items", "promotion_type", "VARCHAR(30) NULL");
            addColumnIfMissing(conn, stmt, "order_items", "product_name_snapshot", "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL");
            addColumnIfMissing(conn, stmt, "order_items", "product_image_snapshot", "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL");
            addColumnIfMissing(conn, stmt, "order_signs", "private_key", "TEXT NULL");
            addColumnIfMissing(conn, stmt, "ai_chat_messages", "is_read", "TINYINT(1) NOT NULL DEFAULT 0");
            executeIgnore(stmt, "UPDATE orders SET createdAt = created_at WHERE createdAt IS NULL AND created_at IS NOT NULL");
            executeIgnore(stmt, "UPDATE orders SET recipient_fullname = COALESCE(NULLIF(recipient_fullname, ''), fullname), " +
                    "recipient_phone = COALESCE(NULLIF(recipient_phone, ''), phone), " +
                    "shipping_address = COALESCE(NULLIF(shipping_address, ''), address) " +
                    "WHERE recipient_fullname IS NULL OR recipient_fullname = '' " +
                    "OR recipient_phone IS NULL OR recipient_phone = '' " +
                    "OR shipping_address IS NULL OR shipping_address = ''");
            stmt.execute("CREATE TABLE IF NOT EXISTS payment_transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "order_id INT NOT NULL," +
                    "user_id INT NOT NULL," +
                    "provider_key VARCHAR(50) NOT NULL," +
                    "provider_display_name VARCHAR(100) NULL," +
                    "amount DECIMAL(15,2) NOT NULL," +
                    "currency VARCHAR(10) NOT NULL DEFAULT 'VND'," +
                    "transfer_reference VARCHAR(100) NULL," +
                    "provider_transaction_id VARCHAR(255) NULL," +
                    "status VARCHAR(50) NOT NULL," +
                    "verification_status VARCHAR(50) NULL," +
                    "verification_message VARCHAR(255) NULL," +
                    "provider_metadata TEXT NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "verified_at TIMESTAMP NULL," +
                    "INDEX idx_payment_transactions_order_id (order_id)," +
                    "INDEX idx_payment_transactions_status (status)," +
                    "CONSTRAINT fk_payment_transactions_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_payment_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS security_events (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "event_type VARCHAR(100) NOT NULL," +
                    "principal VARCHAR(255) NULL," +
                    "ip_address VARCHAR(64) NULL," +
                    "details VARCHAR(500) NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_security_events_type_created (event_type, created_at)," +
                    "INDEX idx_security_events_principal_created (principal, created_at)" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "contact_name VARCHAR(255) NULL," +
                    "phone VARCHAR(50) NULL," +
                    "email VARCHAR(255) NULL," +
                    "address VARCHAR(500) NULL," +
                    "notes VARCHAR(500) NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS stock_imports (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "supplier_id INT NULL," +
                    "receipt_code VARCHAR(100) NOT NULL," +
                    "received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "note VARCHAR(500) NULL," +
                    "created_by INT NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "UNIQUE KEY uk_stock_imports_receipt_code (receipt_code)," +
                    "CONSTRAINT fk_stock_imports_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL," +
                    "CONSTRAINT fk_stock_imports_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory_batches (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "product_id INT NOT NULL," +
                    "stock_import_id INT NULL," +
                    "supplier_id INT NULL," +
                    "batch_code VARCHAR(100) NOT NULL," +
                    "received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "expiry_date DATE NULL," +
                    "received_quantity INT NOT NULL," +
                    "remaining_quantity INT NOT NULL," +
                    "unit_cost DECIMAL(15,2) NOT NULL DEFAULT 0," +
                    "note VARCHAR(500) NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "UNIQUE KEY uk_inventory_batches_batch_code (batch_code)," +
                    "INDEX idx_inventory_batches_product_expiry (product_id, expiry_date)," +
                    "INDEX idx_inventory_batches_remaining_received (remaining_quantity, received_at)," +
                    "CONSTRAINT fk_inventory_batches_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_inventory_batches_stock_import FOREIGN KEY (stock_import_id) REFERENCES stock_imports(id) ON DELETE SET NULL," +
                    "CONSTRAINT fk_inventory_batches_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS stock_movements (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "product_id INT NOT NULL," +
                    "inventory_batch_id INT NULL," +
                    "order_id INT NULL," +
                    "movement_type VARCHAR(50) NOT NULL," +
                    "quantity INT NOT NULL," +
                    "reference_code VARCHAR(100) NULL," +
                    "note VARCHAR(500) NULL," +
                    "created_by INT NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_stock_movements_product_created (product_id, created_at)," +
                    "INDEX idx_stock_movements_type_created (movement_type, created_at)," +
                    "CONSTRAINT fk_stock_movements_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_stock_movements_batch FOREIGN KEY (inventory_batch_id) REFERENCES inventory_batches(id) ON DELETE SET NULL," +
                    "CONSTRAINT fk_stock_movements_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL," +
                    "CONSTRAINT fk_stock_movements_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS order_logs (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "order_id INT NOT NULL," +
                    "actor_type VARCHAR(50) NOT NULL," +
                    "actor_id INT NULL," +
                    "action VARCHAR(100) NOT NULL," +
                    "old_status VARCHAR(50) NULL," +
                    "new_status VARCHAR(50) NULL," +
                    "note TEXT NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_order_logs_order_created (order_id, created_at)," +
                    "INDEX idx_order_logs_actor_created (actor_type, actor_id, created_at)," +
                    "CONSTRAINT fk_order_logs_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS promotions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "description TEXT NULL," +
                    "discount_type VARCHAR(20) NOT NULL," +
                    "discount_value DECIMAL(18,0) NOT NULL DEFAULT 0," +
                    "start_date TIMESTAMP NOT NULL," +
                    "end_date TIMESTAMP NOT NULL," +
                    "status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE'," +
                    "promotion_type VARCHAR(30) NOT NULL DEFAULT 'NORMAL'," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "INDEX idx_promotions_status_time (status, start_date, end_date)," +
                    "INDEX idx_promotions_type_status (promotion_type, status)" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS promotion_products (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "promotion_id INT NOT NULL," +
                    "product_id INT NOT NULL," +
                    "sale_quantity INT NULL," +
                    "sold_quantity INT NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE KEY uk_promotion_products (promotion_id, product_id)," +
                    "INDEX idx_promotion_products_product (product_id)," +
                    "INDEX idx_promotion_products_flash (promotion_id, sale_quantity, sold_quantity)," +
                    "CONSTRAINT fk_promotion_products_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_promotion_products_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE" +
                    ")");
            executeIgnore(stmt, "ALTER TABLE order_items ADD CONSTRAINT fk_order_items_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE SET NULL");
            executeIgnore(stmt, "UPDATE order_items SET original_price = price WHERE original_price IS NULL");
            executeIgnore(stmt, "UPDATE order_items SET final_price = price WHERE final_price IS NULL");
            executeIgnore(stmt, "UPDATE order_items SET discount_amount = GREATEST(COALESCE(original_price, price) - COALESCE(final_price, price), 0) WHERE discount_amount IS NULL");
            executeIgnore(stmt, "ALTER TABLE payment_transactions ADD COLUMN expires_at TIMESTAMP NULL");
            executeIgnore(stmt, "ALTER TABLE payment_transactions ADD COLUMN amount_received DECIMAL(15,2) NULL");
            executeIgnore(stmt, "ALTER TABLE payment_transactions ADD COLUMN bank_content VARCHAR(500) NULL");
            executeIgnore(stmt, "CREATE UNIQUE INDEX uk_payment_transactions_provider_transaction_id ON payment_transactions (provider_transaction_id)");
            executeIgnore(stmt, "CREATE INDEX idx_payment_transactions_verification_status ON payment_transactions (verification_status)");
            executeIgnore(stmt, "CREATE INDEX idx_payment_transactions_transfer_reference ON payment_transactions (transfer_reference)");
            stmt.execute("CREATE TABLE IF NOT EXISTS bank_webhook_events (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "provider_transaction_id VARCHAR(255) NOT NULL," +
                    "amount DECIMAL(15,2) NOT NULL," +
                    "bank_content VARCHAR(500) NULL," +
                    "bank_account VARCHAR(100) NULL," +
                    "payment_transaction_id INT NULL," +
                    "status VARCHAR(50) NOT NULL," +
                    "raw_payload TEXT NULL," +
                    "received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE KEY uk_bank_webhook_events_provider_transaction_id (provider_transaction_id)," +
                    "INDEX idx_bank_webhook_events_status_received (status, received_at)," +
                    "INDEX idx_bank_webhook_events_payment_transaction (payment_transaction_id)," +
                    "CONSTRAINT fk_bank_webhook_events_payment_transaction " +
                    "FOREIGN KEY (payment_transaction_id) REFERENCES payment_transactions(id) ON DELETE SET NULL" +
                    ")");
            executeIgnore(stmt, "CREATE INDEX idx_products_active_category_pet_price ON products (is_active, category(100), pet_type_id, price, discount, id)");
            executeIgnore(stmt, "CREATE INDEX idx_orders_status_created_user ON orders (status, createdAt, user_id)");
            executeIgnore(stmt, "CREATE INDEX idx_users_email_username_locked ON users (email, username, locked_until)");
            addColumnIfMissing(conn, stmt, "addresses", "is_default", "TINYINT(1) NOT NULL DEFAULT 0");
            // Mark all existing users as verified (they registered before email verification was added)
            stmt.execute("UPDATE users SET email_verified = TRUE WHERE email_verified = FALSE AND verification_token IS NULL");
            int pendingMinutes = AppConfig.getInt("payment.bank.pending-minutes", 10);
            executeIgnore(stmt, "UPDATE payment_transactions " +
                    "SET expires_at = DATE_ADD(created_at, INTERVAL " + pendingMinutes + " MINUTE) " +
                    "WHERE expires_at IS NULL AND status = 'PENDING_VERIFICATION'");

            // Migrations for AI Customer Support
            stmt.execute("CREATE TABLE IF NOT EXISTS customer_support_knowledge (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "title VARCHAR(255) NOT NULL," +
                    "category VARCHAR(50) NOT NULL," +
                    "content TEXT NOT NULL," +
                    "is_active BOOLEAN DEFAULT TRUE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS ai_chat_sessions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NULL," +
                    "guest_name VARCHAR(255) NULL," +
                    "guest_email VARCHAR(255) NULL," +
                    "status VARCHAR(50) DEFAULT 'OPEN'," +
                    "need_admin_support BOOLEAN DEFAULT FALSE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP," +
                    "CONSTRAINT fk_ai_chat_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS ai_chat_messages (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "session_id INT NOT NULL," +
                    "sender_type VARCHAR(20) NOT NULL," +
                    "message TEXT NOT NULL," +
                    "intent VARCHAR(50) NULL," +
                    "confidence DECIMAL(4,2) NULL," +
                    "need_admin_support BOOLEAN DEFAULT FALSE," +
                    "suggested_admin_note TEXT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "CONSTRAINT fk_ai_chat_messages_session FOREIGN KEY (session_id) REFERENCES ai_chat_sessions(id) ON DELETE CASCADE" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS ai_support_settings (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "setting_key VARCHAR(100) NOT NULL UNIQUE," +
                    "setting_value TEXT NULL," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NULL," +
                    "title VARCHAR(255) NOT NULL," +
                    "message TEXT NOT NULL," +
                    "type VARCHAR(50) DEFAULT 'info'," +
                    "link VARCHAR(500) NULL," +
                    "is_read TINYINT(1) NOT NULL DEFAULT 0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Seed settings if empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ai_support_settings")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO ai_support_settings (setting_key, setting_value) VALUES " +
                            "('AI_SUPPORT_ENABLED', 'true')," +
                            "('DEEPSEEK_MODEL', 'deepseek-v4-flash')," +
                            "('MAX_PRODUCTS_IN_CONTEXT', '5')," +
                            "('MAX_ORDERS_IN_CONTEXT', '3')," +
                            "('AUTO_ESCALATE_TO_ADMIN', 'true')," +
                            "('MAX_MESSAGE_LENGTH', '1000')");
                }
            }

            // Seed knowledge base if empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM customer_support_knowledge")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO customer_support_knowledge (title, category, content, is_active) VALUES " +
                            "('Chính sách vận chuyển', 'SHIPPING', 'Shop hỗ trợ giao hàng toàn quốc thông qua các đơn vị vận chuyển đối tác.\\n\\n1. Thời gian giao hàng dự kiến:\\n- Nội thành TP.HCM: 1 - 2 ngày làm việc.\\n- Các tỉnh/thành khác: 2 - 5 ngày làm việc.\\n- Khu vực xa hoặc huyện/xã đặc biệt: có thể mất 5 - 7 ngày làm việc.\\n\\n2. Phí vận chuyển:\\n- Phí vận chuyển được tính dựa trên địa chỉ nhận hàng, khối lượng đơn hàng và đơn vị vận chuyển.\\n- Khách hàng sẽ thấy phí vận chuyển trước khi xác nhận đặt hàng nếu hệ thống đã tích hợp tính phí tự động.\\n- Nếu hệ thống chưa hiển thị phí vận chuyển, admin sẽ liên hệ xác nhận phí trước khi giao.\\n\\n3. Đổi địa chỉ nhận hàng:\\n- Khách hàng có thể yêu cầu đổi địa chỉ nếu đơn hàng chưa được bàn giao cho đơn vị vận chuyển.\\n- Nếu đơn hàng đang giao, việc đổi địa chỉ phụ thuộc vào đơn vị vận chuyển và có thể phát sinh thêm phí.', TRUE)," +
                            "('Chính sách đổi trả', 'RETURN_POLICY', 'Shop hỗ trợ đổi trả sản phẩm trong các trường hợp hợp lệ.\\n\\nCác trường hợp được hỗ trợ đổi trả:\\n- Sản phẩm bị lỗi do nhà sản xuất.\\n- Sản phẩm bị hư hỏng trong quá trình vận chuyển.\\n- Shop giao sai sản phẩm so với đơn hàng.\\n- Sản phẩm bị thiếu số lượng so với đơn hàng.\\n\\nĐiều kiện đổi trả:\\n- Có mã đơn hàng.\\n- Có hình ảnh hoặc video sản phẩm.\\n- Yêu cầu gửi trong vòng 24 - 48 giờ kể từ khi nhận hàng.\\n- Sản phẩm không bị hư hỏng do khách sử dụng sai cách.\\n\\nKhông hỗ trợ đổi trả với sản phẩm thức ăn, pate, sữa, bánh thưởng đã mở bao bì hoặc sản phẩm chăm sóc đã mở nắp/sử dụng.', TRUE)," +
                            "('Chính sách hoàn tiền', 'REFUND_POLICY', 'Shop hỗ trợ hoàn tiền trong các trường hợp đủ điều kiện sau khi admin kiểm tra.\\n\\nCác trường hợp có thể hoàn tiền:\\n- Đơn hàng đã thanh toán nhưng shop hết hàng.\\n- Khách thanh toán trùng giao dịch.\\n- Đơn hàng bị hủy hợp lệ trước khi giao.\\n- Sản phẩm lỗi/sai hàng và khách không muốn đổi sản phẩm khác.\\n\\nThời gian xử lý:\\n- Chuyển khoản ngân hàng: 1 - 3 ngày làm việc sau khi được duyệt.\\n- VNPay/cổng thanh toán: phụ thuộc ngân hàng hoặc cổng thanh toán.\\n\\nAI không có quyền xác nhận hoàn tiền. Mọi yêu cầu hoàn tiền phải được admin kiểm tra.', TRUE)," +
                            "('Chính sách bảo hành', 'WARRANTY', 'Một số sản phẩm thiết bị hoặc phụ kiện điện tử cho thú cưng có thể được bảo hành.\\n\\nSản phẩm có thể được bảo hành:\\n- Máy cho ăn tự động.\\n- Máy lọc nước thú cưng.\\n- Máy sấy lông.\\n- Tông đơ.\\n- Nhà vệ sinh tự động.\\n\\nKhông áp dụng bảo hành cho thức ăn, pate, sữa, bánh thưởng, cát vệ sinh, sản phẩm chăm sóc đã mở nắp hoặc sản phẩm tiêu hao.\\n\\nĐiều kiện bảo hành:\\n- Sản phẩm còn trong thời hạn bảo hành.\\n- Có mã đơn hàng.\\n- Lỗi do kỹ thuật hoặc nhà sản xuất.\\n- Không bị rơi vỡ, vào nước, cháy nổ hoặc sử dụng sai hướng dẫn.', TRUE)," +
                            "('Hướng dẫn thanh toán', 'PAYMENT', 'Shop hỗ trợ các phương thức thanh toán tùy theo cấu hình hệ thống.\\n\\n1. COD:\\nKhách thanh toán khi nhận hàng.\\n\\n2. VNPay:\\nKhách chọn VNPay khi đặt hàng, thanh toán trên cổng VNPay. Nếu thanh toán thành công và hệ thống nhận kết quả hợp lệ, đơn hàng sẽ được cập nhật tự động.\\n\\n3. Chuyển khoản ngân hàng / SePay:\\nKhách chuyển khoản theo thông tin hiển thị khi đặt hàng. Nội dung chuyển khoản cần đúng theo hướng dẫn. Nếu đã chuyển khoản nhưng đơn chưa cập nhật, admin cần kiểm tra giao dịch thực tế.\\n\\nShop không yêu cầu khách cung cấp mật khẩu ngân hàng, mã OTP, mã PIN hoặc thông tin thẻ nhạy cảm.', TRUE)," +
                            "('FAQ thường gặp', 'FAQ', '1. Làm sao để đặt hàng?\\nChọn sản phẩm, thêm vào giỏ hàng, nhập thông tin nhận hàng, chọn phương thức thanh toán và xác nhận đặt hàng.\\n\\n2. Làm sao kiểm tra đơn hàng?\\nĐăng nhập tài khoản và vào mục đơn hàng của tôi.\\n\\n3. Sản phẩm hết hàng có đặt được không?\\nThông thường sản phẩm hết hàng sẽ không thể đặt.\\n\\n4. Tôi có thể hủy đơn không?\\nCó thể yêu cầu hủy nếu đơn chưa xử lý hoặc chưa giao cho đơn vị vận chuyển. Admin cần xác nhận.\\n\\n5. AI có thể hoàn tiền không?\\nKhông. AI chỉ ghi nhận yêu cầu và chuyển admin xử lý.', TRUE)," +
                            "('Thông tin liên hệ shop', 'CONTACT', 'Hotline: 0900 000 000\\nEmail: support@petshop-demo.vn\\nĐịa chỉ: 123 Đường Demo, Phường Demo, Quận Demo, TP.HCM\\nFanpage: PetShop Demo\\n\\nLưu ý: Đây là thông tin mẫu. Khi triển khai thật, admin cần cập nhật đúng thông tin chính thức của shop.', TRUE)," +
                            "('Giờ làm việc', 'WORKING_HOURS', 'Thời gian hỗ trợ khách hàng:\\n- Thứ 2 đến Thứ 7: 8:00 - 21:00\\n- Chủ nhật: 9:00 - 18:00\\n- Ngày lễ/Tết: thời gian hỗ trợ có thể thay đổi tùy thông báo của shop.\\n\\nChatbot AI có thể hỗ trợ trả lời tự động ngoài giờ làm việc. Các yêu cầu cần admin xử lý sẽ được xử lý trong giờ làm việc.', TRUE)");
                }
            }

            // NOTE: do not "fix" product data on every startup. These resets used to
            // run here and silently reactivated products, restocked out-of-stock
            // items to 50 and wiped discounts on every redeploy/restart. Data
            // corrections belong in versioned SQL migrations (sql/), not in app boot.

            System.out.println("[DBContext] Migrations applied.");
        } catch (Exception e) {
            System.err.println("[DBContext] Migration warning: " + e.getMessage());
        }
    }

    private static void addColumnIfMissing(Connection conn, Statement stmt,
                                           String tableName, String columnName,
                                           String columnDefinition) {
        try {
            if (!columnExists(conn, tableName, columnName)) {
                stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
            }
        } catch (Exception e) {
            System.err.println("[DBContext] Migration warning for "
                    + tableName + "." + columnName + ": " + e.getMessage());
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void executeIgnore(Statement stmt, String sql) {
        try {
            stmt.execute(sql);
        } catch (Exception ignored) {
        }
    }
}
