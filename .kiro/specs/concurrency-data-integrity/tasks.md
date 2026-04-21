# Implementation Plan: Concurrency & Data Integrity

## Overview

This plan implements six concurrency and data integrity improvements plus performance indexing for the PetShop application. The work is organized into: (1) SQL migration for schema changes, (2) DAO-layer locking and soft-delete methods, (3) servlet-layer integration, and (4) wiring and final verification. Each task builds incrementally on the previous, ensuring no orphaned code.

## Tasks

- [ ] 1. Create SQL migration script for schema changes
  - Create `PetShop/concurrency_data_integrity.sql` migration script containing:
    - `ALTER TABLE products ADD COLUMN IF NOT EXISTS is_active TINYINT(1) NOT NULL DEFAULT 1`
    - Drop and re-create FK on `order_items.product_id` with `ON DELETE RESTRICT` (replacing CASCADE)
    - Drop and re-create FK on `reviews.product_id` with `ON DELETE RESTRICT` (replacing CASCADE)
    - Drop and re-create FK on `orders.user_id` with `ON DELETE RESTRICT` (replacing CASCADE)
    - Drop and re-create FK on `cart.user_id` with `ON DELETE RESTRICT` (replacing CASCADE)
    - Drop and re-create FK on `reviews.user_id` with `ON DELETE RESTRICT` (replacing CASCADE)
    - Create 10 indexes (use `CREATE INDEX IF NOT EXISTS` or check `information_schema`): `idx_orders_user_id`, `idx_orders_created_at`, `idx_orders_status`, `idx_order_items_order_id`, `idx_order_items_product_id`, `idx_reviews_product_id`, `idx_reviews_user_id`, `idx_products_pet_type_id`, `idx_products_category`, `idx_cart_user_id`
  - _Requirements: 5.1, 5.5, 5.6, 6.4, 6.5, 6.6, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9, 7.10_

- [ ] 2. Add `isActive` field to Product model and update ProductDAO
  - [ ] 2.1 Add `isActive` boolean field to `Model/Product.java`
    - Add `private boolean isActive = true;` field
    - Add `isActive` getter and setter
    - Update `ProductDAO.mapProduct` to read `is_active` from ResultSet (with try/catch defaulting to true)
    - _Requirements: 5.1_

  - [ ] 2.2 Add `ProductDAO.getProductByIdForUpdate(Connection conn, int id)` method
    - Implement `SELECT p.*, COALESCE(AVG(r.rating), 0) AS average_rating, COUNT(r.id) AS review_count FROM products p LEFT JOIN reviews r ON r.product_id = p.id WHERE p.id = ? GROUP BY p.id FOR UPDATE` using the provided `Connection`
    - Return a `Product` object using `mapProduct`
    - _Requirements: 1.1_

  - [ ] 2.3 Add `ProductDAO.softDeleteProduct(int id)` method
    - Implement `UPDATE products SET is_active = 0 WHERE id = ?`
    - Return boolean success
    - _Requirements: 5.2_

  - [ ] 2.4 Update all customer-facing ProductDAO queries to filter by `is_active`
    - Add `WHERE p.is_active = 1` (or `AND p.is_active = 1`) to: `getAllProducts`, `getProductsByPetType`, `getProductsByPetTypeFallback`, `getProductsByCategory`, `searchProducts`, `searchProductsLimit`, `getDiscountedProductsList`, `getDiscountedProductsPage`, `getAllProductsPage`, `getPopularProductsPage`, `getRelatedProducts`, `getProductsByPage`
    - Update count queries: `getTotalProductsCount`, `getTotalDiscountedProductsCount`, `getTotalPopularProductsCount`, `getTotalProducts`, `getDiscountedProducts`
    - Update `getCategoriesByPetType` and `getAllCategories` to filter by `is_active = 1`
    - Update `getPopularCategories` to filter by `is_active = 1`
    - Do NOT add `is_active` filter to `getProductById` or `getProductById(Connection, int)` — these are used by order history and admin views
    - Do NOT add `is_active` filter to `getLowStockProducts` or `getOutOfStockProducts` — these are admin views
    - _Requirements: 5.3, 5.4_

  - [ ]* 2.5 Write property test for product soft delete (Property 5)
    - **Property 5: Product soft delete preserves row with inactive flag**
    - **Validates: Requirements 5.2**

  - [ ]* 2.6 Write property test for inactive products excluded from customer queries (Property 6)
    - **Property 6: Inactive products excluded from customer-facing queries**
    - **Validates: Requirements 5.3**

- [ ] 3. Add `CouponDao.getCouponByIdForUpdate` method
  - [ ] 3.1 Add `getCouponByIdForUpdate(Connection conn, int couponId)` to `CouponDao.java`
    - Implement `SELECT * FROM coupons WHERE id = ? FOR UPDATE` using the provided `Connection`
    - Map result to `Coupon` object (same mapping as `getValidCouponByCode`)
    - _Requirements: 3.1_

- [ ] 4. Add `ReviewDAO.hasUserPurchasedProduct` method
  - [ ] 4.1 Add `hasUserPurchasedProduct(int userId, int productId)` to `ReviewDAO.java`
    - Implement query: `SELECT 1 FROM order_items oi JOIN orders o ON oi.order_id = o.id WHERE o.user_id = ? AND oi.product_id = ? AND o.status = 'Completed'`
    - Return boolean
    - _Requirements: 4.1, 4.2_

  - [ ]* 4.2 Write property test for review purchase verification (Property 4)
    - **Property 4: Review submission requires verified purchase**
    - **Validates: Requirements 4.1, 4.3**

- [ ] 5. Add `UserDAO.deactivateUser` method
  - [ ] 5.1 Add `deactivateUser(int userId)` to `UserDAO.java`
    - Implement `UPDATE users SET status = 'inactive' WHERE id = ?`
    - Return boolean success
    - _Requirements: 6.1_

  - [ ]* 5.2 Write property test for user deactivation (Property 8)
    - **Property 8: User deactivation preserves row with inactive status**
    - **Validates: Requirements 6.1**

- [ ] 6. Checkpoint - Ensure all DAO changes compile
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. Update CheckoutServlet to use pessimistic locking
  - [ ] 7.1 Replace `productDAO.getProductById(conn, ...)` with `productDAO.getProductByIdForUpdate(conn, ...)` in `placeOrderWithStockCheck`
    - In the cart item loop inside the transaction, change `productDAO.getProductById(conn, item.getProduct().getId())` to `productDAO.getProductByIdForUpdate(conn, item.getProduct().getId())`
    - Keep all existing stock validation and error handling logic unchanged
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [ ] 7.2 Add coupon row locking before `increaseUsedIfAvailable` in `placeOrderWithStockCheck`
    - After `getValidCouponByCode(conn, ...)` returns a coupon, call `couponDao.getCouponByIdForUpdate(conn, latestCoupon.getId())` to acquire the row lock
    - Use the locked coupon's `used` and `quantity` values for validation
    - If `used >= quantity`, rollback and return coupon-exhausted error
    - Then call `increaseUsedIfAvailable` as before
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

  - [ ]* 7.3 Write property test for stock validation correctness (Property 1)
    - **Property 1: Stock validation and decrement correctness**
    - **Validates: Requirements 1.2, 1.3, 1.4**

  - [ ]* 7.4 Write property test for coupon validation correctness (Property 3)
    - **Property 3: Coupon validation and increment correctness**
    - **Validates: Requirements 3.2, 3.3, 3.5**

- [ ] 8. Update AddReviewServlet to require verified purchase
  - [ ] 8.1 Add purchase verification check in `AddReviewServlet.doPost`
    - After the login check and before the existing `hasUserReviewedProduct` check, add a call to `reviewDAO.hasUserPurchasedProduct(user.getId(), productId)`
    - If no verified purchase exists, set session error message: `"Chỉ khách hàng đã mua và nhận sản phẩm mới có thể đánh giá."`
    - Redirect back to product detail page
    - _Requirements: 4.1, 4.3_

- [ ] 9. Update ProductDetailServlet to pass `hasPurchased` attribute
  - [ ] 9.1 Add `hasPurchased` attribute to request in `ProductDetailServlet.doGet`
    - After the existing `hasReviewed` check, add: `boolean hasPurchased = user != null && new ReviewDAO().hasUserPurchasedProduct(user.getId(), id);`
    - Set `request.setAttribute("hasPurchased", hasPurchased);`
    - The JSP can use this to conditionally show/hide the review form
    - _Requirements: 4.4_

- [ ] 10. Update Admin ProductServlet to use soft delete
  - [ ] 10.1 Change delete action in `ProductServlet.doPost` to call `softDeleteProduct`
    - Replace `dao.deleteProduct(id)` with `dao.softDeleteProduct(id)` in the `"delete"` action branch
    - Update success message to "Ẩn sản phẩm thành công!" (or similar)
    - _Requirements: 5.2_

- [ ] 11. Update Admin UserManageServlet to use deactivation
  - [ ] 11.1 Change delete action in `UserManageServlet.doPost` to call `deactivateUser`
    - Replace `userDAO.deleteUser(deleteId)` with `userDAO.deactivateUser(deleteId)` in the `"delete"` action branch
    - Update success message to "Đã vô hiệu hóa tài khoản thành công!" (or similar)
    - _Requirements: 6.1_

- [ ] 12. Update LoginServlet to reject inactive users
  - [ ] 12.1 Add inactive user check in `LoginServlet.doPost`
    - After `User user = dao.loginByEmail(email, password)` succeeds (user != null), check if the user's status indicates inactive
    - The `User.status` field is read as a boolean by `mapUser` — check `!user.isStatus()` (false means inactive)
    - If inactive, set error message: `"Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên."` and forward to login page
    - Do NOT reset failed attempts for inactive users
    - _Requirements: 6.2_

  - [ ]* 12.2 Write property test for login rejects inactive users (Property 9)
    - **Property 9: Login rejects inactive users**
    - **Validates: Requirements 6.2**

- [ ] 13. Checkpoint - Ensure all servlet changes compile and integrate correctly
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 14. Final checkpoint - Verify all requirements are covered
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- The SQL migration script (task 1) should be run before testing any DAO changes
- The `getProductById` methods intentionally do NOT filter by `is_active` so order history and admin views continue to work
