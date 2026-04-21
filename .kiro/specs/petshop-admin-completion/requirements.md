# Requirements Document

## Introduction

Đây là tài liệu yêu cầu cho việc hoàn thiện 3 tính năng admin còn thiếu trong đồ án e-commerce PetShop (Java Servlet + JSP + MySQL):

1. **Quản lý sản phẩm đầy đủ** — Bổ sung các trường `stock`, `weight`, `category`, `pet_type_id` vào form thêm/sửa sản phẩm và bảng danh sách admin.
2. **Review moderation** — Trang admin quản lý đánh giá: xem tất cả review, xóa review vi phạm, lọc theo rating thấp.
3. **Trang lỗi 404/500** — Thay thế trang lỗi mặc định của Tomcat bằng trang lỗi đúng vibe PetShop.

---

## Glossary

- **ProductServlet**: Servlet tại `/pages/admin/products` xử lý CRUD sản phẩm cho admin.
- **ProductDAO**: Data Access Object thao tác bảng `products` trong MySQL.
- **Product**: Model Java ánh xạ bảng `products`, đã có các field `stock`, `weight`, `category`, `pet_type_id`.
- **ReviewDAO**: Data Access Object thao tác bảng `reviews` trong MySQL.
- **Review**: Model Java ánh xạ bảng `reviews`, có các field `id`, `productId`, `userId`, `userName`, `rating`, `comment`, `createdAt`.
- **ReviewModerationServlet**: Servlet mới tại `/pages/admin/reviews` xử lý quản lý review cho admin.
- **PetTypeDAO**: Data Access Object thao tác bảng `pet_types`, có `getAllPetTypes()` trả về danh sách loại thú cưng.
- **Admin**: Người dùng có role admin, đã đăng nhập và có quyền truy cập các trang `/pages/admin/*`.
- **Admin_Sidebar**: Component JSP tại `/components/admin-sidebar.jsp` hiển thị menu điều hướng admin.
- **Error_Page**: Trang JSP hiển thị khi xảy ra lỗi HTTP 404 hoặc 500.
- **web.xml**: File cấu hình deployment descriptor tại `WEB-INF/web.xml`, dùng để khai báo `<error-page>`.

---

## Requirements

### Requirement 1: Bổ sung trường sản phẩm vào form admin

**User Story:** As an Admin, I want to set stock, weight, category, and pet_type_id when adding or editing a product, so that product data is complete and consistent with what the shop displays to customers.

#### Acceptance Criteria

1. WHEN the Admin opens the add-product modal, THE ProductServlet SHALL load the list of active pet types from PetTypeDAO and pass it to the JSP as the attribute `petTypes`.
2. WHEN the Admin opens the add-product modal, THE products.jsp SHALL render input fields for `stock` (số nguyên ≥ 0), `weight` (số nguyên ≥ 0, đơn vị gram), `category` (text), and a `<select>` dropdown for `pet_type_id` populated from the `petTypes` attribute.
3. WHEN the Admin opens the edit-product modal, THE products.jsp SHALL pre-fill the fields `stock`, `weight`, `category`, and `pet_type_id` with the current values of the product being edited.
4. WHEN the Admin submits the add-product form with valid data, THE ProductServlet SHALL call `ProductDAO.addProduct()` with all seven parameters: `name`, `image`, `price`, `discount`, `description`, `stock`, `weight`, `category`, `pet_type_id`.
5. WHEN the Admin submits the edit-product form with valid data, THE ProductServlet SHALL call `ProductDAO.updateProduct()` with all parameters including `stock`, `weight`, `category`, `pet_type_id`.
6. IF the Admin submits a `stock` value that is not a non-negative integer, THEN THE ProductServlet SHALL reject the request and return an error message "Tồn kho phải là số nguyên không âm.".
7. IF the Admin submits a `weight` value that is not a non-negative integer, THEN THE ProductServlet SHALL reject the request and return an error message "Trọng lượng phải là số nguyên không âm (gram).".
8. THE products.jsp table SHALL display columns `stock` (tồn kho) and `category` (danh mục) for each product in the product list.
9. WHEN a product has `stock` equal to 0, THE products.jsp SHALL display a visual indicator (e.g., badge "Hết hàng") in the stock column for that row.
10. THE ProductDAO SHALL expose an `addProduct(String name, String image, double price, int discount, String description, int stock, int weight, String category, int petTypeId)` method that inserts all fields into the `products` table.
11. THE ProductDAO SHALL expose an `updateProduct(int id, String name, String image, double price, int discount, String description, int stock, int weight, String category, int petTypeId)` method that updates all fields in the `products` table.

---

### Requirement 2: Review moderation cho admin

**User Story:** As an Admin, I want to view all customer reviews, delete violating reviews, and filter by low rating, so that I can maintain content quality on the product pages.

#### Acceptance Criteria

1. THE ReviewModerationServlet SHALL be mapped to the URL `/pages/admin/reviews` and require Admin authentication before serving any request.
2. WHEN the Admin navigates to `/pages/admin/reviews`, THE ReviewModerationServlet SHALL load all reviews from ReviewDAO and pass them to the JSP as the attribute `reviews`.
3. THE ReviewDAO SHALL expose a `getAllReviews()` method that returns all reviews joined with `users.fullname` and `products.name`, ordered by `created_at DESC`.
4. WHEN the Admin applies a rating filter (e.g., rating ≤ 2), THE ReviewModerationServlet SHALL load only reviews matching the filter and pass them as the attribute `reviews`.
5. THE ReviewDAO SHALL expose a `getReviewsByMaxRating(int maxRating)` method that returns all reviews where `rating <= maxRating`, ordered by `created_at DESC`.
6. WHEN the Admin clicks "Xóa" on a review, THE ReviewModerationServlet SHALL call `ReviewDAO.deleteReview(int reviewId)` and redirect back to `/pages/admin/reviews` with a success message.
7. THE ReviewDAO SHALL expose a `deleteReview(int reviewId)` method that deletes the review with the given ID from the `reviews` table and returns `true` if successful.
8. IF the Admin attempts to delete a review with an invalid or non-existent ID, THEN THE ReviewModerationServlet SHALL redirect back to `/pages/admin/reviews` with an error message "Review không tồn tại hoặc đã bị xóa.".
9. THE reviews.jsp SHALL display each review with: product name, user name, star rating (1–5), comment text, and creation date.
10. THE reviews.jsp SHALL provide a filter control allowing the Admin to filter reviews by maximum rating (options: Tất cả, ≤ 1 sao, ≤ 2 sao, ≤ 3 sao).
11. THE Admin_Sidebar SHALL include a navigation link "Quản lý Review" pointing to `/pages/admin/reviews` in the "Thương mại" section.

---

### Requirement 3: Trang lỗi 404 và 500

**User Story:** As a customer or admin, I want to see a friendly, branded error page when a URL is not found or a server error occurs, so that the experience remains consistent with the PetShop brand instead of showing a raw Tomcat error page.

#### Acceptance Criteria

1. THE web.xml SHALL declare an `<error-page>` mapping HTTP status code `404` to `/pages/error/404.jsp`.
2. THE web.xml SHALL declare an `<error-page>` mapping HTTP status code `500` to `/pages/error/500.jsp`.
3. WHEN a request is made to a URL that does not match any servlet or static resource, THE Web_Container SHALL render `/pages/error/404.jsp` with HTTP status 404.
4. WHEN an unhandled exception occurs in a servlet, THE Web_Container SHALL render `/pages/error/500.jsp` with HTTP status 500.
5. THE 404.jsp SHALL display a message indicating the page was not found, a link to return to the homepage (`/home`), and visual elements consistent with the PetShop brand (logo, color scheme, pet-themed illustration or icon).
6. THE 500.jsp SHALL display a message indicating a server error occurred, a link to return to the homepage (`/home`), and visual elements consistent with the PetShop brand.
7. THE 404.jsp SHALL include the standard PetShop `<head>` component (meta, CSS) but SHALL NOT include the navbar or footer components to avoid cascading errors.
8. THE 500.jsp SHALL include the standard PetShop `<head>` component (meta, CSS) but SHALL NOT include the navbar or footer components to avoid cascading errors.
9. WHERE the request originates from an `/pages/admin/*` path, THE 404.jsp SHALL display an additional link "Về Admin Dashboard" pointing to `/pages/admin/dashboard`.
