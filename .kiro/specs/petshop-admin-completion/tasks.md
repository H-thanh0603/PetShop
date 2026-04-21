# Implementation Plan: PetShop Admin Completion

## Overview

This plan implements three features to complete the PetShop admin panel: enhanced product management fields (stock, weight, category, pet_type_id), review moderation, and custom error pages. All changes follow the existing Java Servlet + JSP + MySQL conventions. Tasks are ordered so each step builds on the previous, with no orphaned code.

## Tasks

- [x] 1. Database migration and DAO layer updates
  - [x] 1.1 Create SQL migration to add weight column to products table
    - Create `update_weight.sql` with `ALTER TABLE products ADD COLUMN IF NOT EXISTS weight int DEFAULT 0;`
    - _Requirements: 1.10, 1.11_

  - [x] 1.2 Add overloaded `addProduct` method to ProductDAO with all fields
    - Add `addProduct(String name, String image, double price, int discount, String description, int stock, int weight, String category, int petTypeId)` method
    - SQL: `INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`
    - Keep existing 5-parameter `addProduct` for backward compatibility
    - _Requirements: 1.4, 1.10_

  - [x] 1.3 Add overloaded `updateProduct` method to ProductDAO with all fields
    - Add `updateProduct(int id, String name, String image, double price, int discount, String description, int stock, int weight, String category, int petTypeId)` method
    - SQL: `UPDATE products SET name=?, image=?, price=?, discount=?, description=?, stock=?, weight=?, category=?, pet_type_id=? WHERE id=?`
    - Keep existing 6-parameter `updateProduct` for backward compatibility
    - _Requirements: 1.5, 1.11_

  - [ ]* 1.4 Write property test: Product add round-trip (Property 1)
    - **Property 1: Product add round-trip**
    - For any valid product data, adding via `ProductDAO.addProduct()` and retrieving by ID should return matching fields
    - **Validates: Requirements 1.4, 1.10**

  - [ ]* 1.5 Write property test: Product update round-trip (Property 2)
    - **Property 2: Product update round-trip**
    - For any existing product and valid updated data, updating via `ProductDAO.updateProduct()` and retrieving by ID should return matching fields
    - **Validates: Requirements 1.5, 1.11**

- [x] 2. ProductServlet enhancements — validation and pet type loading
  - [x] 2.1 Load pet types in ProductServlet doGet
    - Import `PetTypeDAO` and `Model.PetType`
    - In `doGet()`, call `new PetTypeDAO().getAllPetTypes()` and set as request attribute `petTypes`
    - _Requirements: 1.1_

  - [x] 2.2 Add stock/weight validation and new field parsing in ProductServlet doPost
    - Parse `stock`, `weight`, `category`, `petTypeId` from request parameters
    - Validate `stock` is a non-negative integer; reject with error "Tồn kho phải là số nguyên không âm." if invalid
    - Validate `weight` is a non-negative integer; reject with error "Trọng lượng phải là số nguyên không âm (gram)." if invalid
    - Call the new overloaded `addProduct`/`updateProduct` with all parameters instead of the 5/6-parameter versions
    - _Requirements: 1.4, 1.5, 1.6, 1.7_

  - [ ]* 2.3 Write property test: Non-negative integer field validation (Property 3)
    - **Property 3: Non-negative integer field validation**
    - For any input string that is not a non-negative integer, the validation logic should reject the request
    - **Validates: Requirements 1.6, 1.7**

- [x] 3. Enhance products.jsp with new form fields and table columns
  - [x] 3.1 Add stock, weight, category, pet_type_id fields to the add/edit modal form
    - Add `stock` input (type number, min=0) with label "Tồn kho"
    - Add `weight` input (type number, min=0) with label "Trọng lượng (gram)"
    - Add `category` text input with label "Danh mục"
    - Add `<select>` dropdown for `pet_type_id` populated from `${petTypes}` with label "Loại thú cưng"
    - _Requirements: 1.2_

  - [x] 3.2 Pre-fill new fields in edit modal
    - Add `data-stock`, `data-weight`, `data-category`, `data-pet-type-id` attributes to each product table row
    - Update `openEditModal()` JavaScript to read and populate the new fields
    - Update `openAddModal()` to reset the new fields to defaults
    - _Requirements: 1.3_

  - [x] 3.3 Add stock and category columns to the product table
    - Add "Tồn kho" and "Danh mục" column headers to `<thead>`
    - Add corresponding `<td>` cells in the `<c:forEach>` loop
    - Show "Hết hàng" badge (red) when `p.stock == 0`
    - _Requirements: 1.8, 1.9_

- [x] 4. Checkpoint — Verify product management enhancements
  - Ensure all product management changes compile and work together. Run the SQL migration. Test add/edit product with new fields. Ask the user if questions arise.

- [x] 5. ReviewDAO — New methods for admin review moderation
  - [x] 5.1 Implement `getAllReviews()` in ReviewDAO
    - SQL: `SELECT r.*, u.fullname, p.name AS product_name FROM reviews r JOIN users u ON r.user_id = u.id JOIN products p ON r.product_id = p.id ORDER BY r.created_at DESC`
    - Map `fullname` to `userName` and `product_name` to `productName` on each Review object
    - _Requirements: 2.2, 2.3_

  - [x] 5.2 Implement `getReviewsByMaxRating(int maxRating)` in ReviewDAO
    - SQL: Same as `getAllReviews()` but with `WHERE r.rating <= ?` clause
    - _Requirements: 2.4, 2.5_

  - [x] 5.3 Implement `deleteReview(int reviewId)` in ReviewDAO
    - SQL: `DELETE FROM reviews WHERE id = ?`
    - Return `true` if `executeUpdate() > 0`
    - _Requirements: 2.6, 2.7_

  - [ ]* 5.4 Write property test: Rating filter correctness (Property 4)
    - **Property 4: Rating filter correctness**
    - For any set of reviews and any maxRating 1–5, `getReviewsByMaxRating(maxRating)` returns exactly reviews with rating ≤ maxRating, each with non-null userName and productName
    - **Validates: Requirements 2.4, 2.5**

- [x] 6. ReviewModerationServlet — New servlet for admin review management
  - [x] 6.1 Create `ReviewModerationServlet` with doGet
    - Create `controller/admin/ReviewModerationServlet.java` mapped to `/pages/admin/reviews`
    - Check admin session (redirect to login if not admin)
    - Read optional `maxRating` query parameter
    - If `maxRating` present and valid, call `ReviewDAO.getReviewsByMaxRating(maxRating)`; otherwise call `ReviewDAO.getAllReviews()`
    - Set `reviews` attribute, forward to `/pages/admin/reviews.jsp`
    - _Requirements: 2.1, 2.2, 2.4_

  - [x] 6.2 Implement doPost for review deletion
    - Check admin session
    - Read `action` parameter; if `"delete"`, read and parse `reviewId` using `ValidationUtil.parseIntOrNull()`
    - If null or `deleteReview()` returns false, set error "Review không tồn tại hoặc đã bị xóa."
    - Otherwise set success message
    - Redirect to `/pages/admin/reviews`
    - _Requirements: 2.6, 2.8_

- [x] 7. Create reviews.jsp — Admin review moderation page
  - [x] 7.1 Create `reviews.jsp` with layout, stats cards, and filter bar
    - Include shared admin components: `admin-sidebar.jsp` (currentPage="reviews"), `admin-styles.jsp`, `admin-header-dropdown.jsp`, `admin-toast.jsp`
    - Stats cards: total reviews count, average rating, low-rating count (≤ 2 stars)
    - Filter bar with `<select>` for maxRating: Tất cả, ≤ 1 sao, ≤ 2 sao, ≤ 3 sao
    - On filter change, navigate to `?maxRating=<value>` or clear param for "Tất cả"
    - _Requirements: 2.9, 2.10_

  - [x] 7.2 Add reviews table and delete confirmation modal
    - Table columns: #, Product Name, User Name, Rating (star icons), Comment, Date, Actions (delete button)
    - Delete confirmation modal following the same pattern as products.jsp delete modal
    - Delete form POSTs `action=delete` and `reviewId` to the servlet
    - _Requirements: 2.6, 2.9_

- [x] 8. Add "Quản lý Review" link to admin sidebar
  - Add a new link in `admin-sidebar.jsp` in the "Thương mại" section, after "Quản lý Đơn hàng"
  - Link: `href="${pageContext.request.contextPath}/pages/admin/reviews"` with icon `bx bxs-star-half`
  - Active state: `"reviews".equals(currentPage)`
  - _Requirements: 2.11_

- [x] 9. Checkpoint — Verify review moderation feature
  - Ensure ReviewDAO methods, ReviewModerationServlet, reviews.jsp, and sidebar link all work together. Ask the user if questions arise.

- [x] 10. Custom error pages and web.xml configuration
  - [x] 10.1 Create `404.jsp` error page
    - Create at `src/main/webapp/pages/error/404.jsp`
    - Include `meta.jsp` and `head.jsp` only (no navbar, no footer)
    - Centered layout with Boxicons `bx-error` icon
    - Vietnamese message: "Trang không tìm thấy"
    - "Về trang chủ" link to `/home`
    - Conditional "Về Admin Dashboard" link when request URI starts with `/pages/admin/`
    - _Requirements: 3.1, 3.3, 3.5, 3.7, 3.9_

  - [x] 10.2 Create `500.jsp` error page
    - Create at `src/main/webapp/pages/error/500.jsp`
    - Include `meta.jsp` and `head.jsp` only (no navbar, no footer)
    - Centered layout with Boxicons `bx-error-circle` icon
    - Vietnamese message: "Lỗi máy chủ"
    - "Về trang chủ" link to `/home`
    - _Requirements: 3.2, 3.4, 3.6, 3.8_

  - [x] 10.3 Add error-page declarations to web.xml
    - Add `<error-page>` for 404 → `/pages/error/404.jsp`
    - Add `<error-page>` for 500 → `/pages/error/500.jsp`
    - _Requirements: 3.1, 3.2_

- [x] 11. Final checkpoint — Ensure all features work together
  - Ensure all tests pass and all three features (product management, review moderation, error pages) are integrated. Ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation after each major feature
- Property tests validate universal correctness properties from the design document (requires jqwik + JUnit 5)
- The existing 5-parameter `addProduct` and 6-parameter `updateProduct` are preserved for backward compatibility
