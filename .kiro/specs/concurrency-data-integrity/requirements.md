# Requirements Document

## Introduction

This document specifies requirements for fixing concurrency race conditions and data integrity issues in the PetShop e-commerce application. The system uses Jakarta EE Servlets, JSP, and MySQL with InnoDB. Current issues include stock overselling under concurrent checkouts, coupon double-usage, cart desynchronization across browser tabs, unrestricted product reviews, cascading data loss on product and user deletion, and missing database indexes on frequently queried columns.

## Glossary

- **Checkout_Transaction**: The database transaction initiated by CheckoutServlet that validates stock, applies coupons, creates orders, decreases stock, and clears the cart. Uses `conn.setAutoCommit(false)` with explicit `conn.commit()` and `conn.rollback()`.
- **Product_Row_Lock**: A pessimistic row-level lock acquired via `SELECT ... FOR UPDATE` on a product row within a transaction, preventing concurrent reads of stale stock values.
- **Coupon_Row_Lock**: A pessimistic row-level lock acquired via `SELECT ... FOR UPDATE` on a coupon row within a transaction, preventing concurrent coupon usage validation from reading stale usage counts.
- **Cart_DB**: The `cart` table in the database that stores cart items for logged-in users, keyed by `(user_id, product_id)`.
- **Session_Cart**: The in-memory `Map<Integer, CartItem>` stored in the HTTP session, used as a read cache for cart display.
- **Verified_Purchase**: A completed order (`orders.status = 'Completed'`) containing a specific product in its `order_items`, placed by a specific user.
- **Soft_Delete**: A deletion strategy where a record is marked as inactive (via a status or flag column) rather than physically removed from the database.
- **Product_Archive**: The act of marking a product as inactive instead of deleting the row, preserving referential integrity with order history and reviews.
- **CheckoutServlet**: The servlet at `/checkout` that handles the checkout flow including stock validation, coupon application, order creation, and stock decrement.
- **CartServlet**: The servlet at `/cart` that handles cart display, item removal, quantity updates, and cart clearing.
- **AddReviewServlet**: The servlet at `/add-review` that handles submission of product reviews.
- **ProductDAO**: The data access object responsible for product CRUD operations and stock management.
- **CouponDao**: The data access object responsible for coupon validation and usage tracking.
- **CartDAO**: The data access object responsible for cart persistence in the database.
- **ReviewDAO**: The data access object responsible for review CRUD operations.
- **OrderDAO**: The data access object responsible for order persistence and status management.
- **UserDAO**: The data access object responsible for user CRUD operations including the `deleteUser` method.
- **Admin_ProductServlet**: The servlet at `/pages/admin/products` that handles admin product management including deletion.
- **Admin_UserManageServlet**: The servlet at `/admin/users` that handles admin user management including deletion.

## Requirements

### Requirement 1: Pessimistic Locking on Product Stock During Checkout

**User Story:** As a customer, I want the checkout process to prevent overselling, so that my order is only confirmed when sufficient stock is genuinely available.

#### Acceptance Criteria

1. WHEN the Checkout_Transaction begins processing cart items, THE CheckoutServlet SHALL acquire a Product_Row_Lock on each product row by executing `SELECT ... FOR UPDATE` before reading the stock value.
2. WHILE the Checkout_Transaction holds a Product_Row_Lock on a product row, THE CheckoutServlet SHALL compare the locked stock value against the requested quantity before calling `ProductDAO.decreaseStock`.
3. IF the locked stock value is less than the requested quantity for any cart item, THEN THE CheckoutServlet SHALL roll back the Checkout_Transaction and return an error message identifying the product and its current stock.
4. WHEN the Checkout_Transaction commits successfully, THE ProductDAO SHALL have decreased the stock of each purchased product by exactly the ordered quantity, and the resulting stock value SHALL be greater than or equal to zero.
5. WHEN two concurrent Checkout_Transactions attempt to purchase the same product and the combined quantity exceeds available stock, THE CheckoutServlet SHALL allow only one transaction to succeed and SHALL roll back the other with a stock-insufficient error.

### Requirement 2: Database-Authoritative Cart for Logged-In Users

**User Story:** As a logged-in customer using multiple browser tabs, I want my cart to always reflect the latest state, so that I do not encounter stale quantities or missing items.

#### Acceptance Criteria

1. WHEN a logged-in user requests the cart page, THE CartServlet SHALL reload the cart from Cart_DB before rendering, treating Session_Cart as a write-through cache only.
2. WHEN a logged-in user initiates a cart update (add, remove, or change quantity), THE CartServlet SHALL perform the write operation against Cart_DB first, then update Session_Cart to match.
3. WHEN a logged-in user navigates to the checkout page, THE CheckoutServlet SHALL reload the cart from Cart_DB before validating stock and building the order.
4. WHEN the CartDAO loads the cart from Cart_DB, THE CartDAO SHALL remove any cart entries whose referenced product no longer exists or has zero stock, and SHALL clamp quantities to the current stock level.
5. WHILE a user is not logged in, THE CartServlet SHALL continue to use Session_Cart as the sole cart storage without database reads.

### Requirement 3: Pessimistic Locking on Coupon Usage During Checkout

**User Story:** As a customer, I want coupon usage limits to be enforced accurately, so that a coupon cannot be redeemed beyond its allowed quantity even under concurrent checkouts.

#### Acceptance Criteria

1. WHEN the Checkout_Transaction validates a coupon, THE CheckoutServlet SHALL acquire a Coupon_Row_Lock on the coupon row by executing `SELECT ... FOR UPDATE` before reading the `used` and `quantity` values.
2. WHILE the Coupon_Row_Lock is held, THE CouponDao SHALL compare the locked `used` value against the `quantity` value before incrementing `used`.
3. IF the locked `used` value is greater than or equal to the `quantity` value, THEN THE CheckoutServlet SHALL roll back the Checkout_Transaction and return an error indicating the coupon has been fully redeemed.
4. WHEN two concurrent Checkout_Transactions attempt to use the same coupon and only one redemption remains, THE CheckoutServlet SHALL allow only one transaction to redeem the coupon and SHALL roll back the other with a coupon-exhausted error.
5. WHEN the Checkout_Transaction commits successfully with a coupon applied, THE CouponDao SHALL have incremented the `used` count by exactly one, and the resulting `used` value SHALL be less than or equal to `quantity`.

### Requirement 4: Verified Purchase Requirement for Product Reviews

**User Story:** As a store owner, I want only customers who have purchased and received a product to review it, so that reviews are trustworthy and based on actual experience.

#### Acceptance Criteria

1. WHEN a logged-in user submits a review for a product, THE AddReviewServlet SHALL verify that a Verified_Purchase exists for that user and product before saving the review.
2. WHEN the AddReviewServlet checks for a Verified_Purchase, THE ReviewDAO SHALL query the `order_items` table joined with the `orders` table, filtering by `orders.user_id`, `order_items.product_id`, and `orders.status = 'Completed'`.
3. IF no Verified_Purchase exists for the user and product, THEN THE AddReviewServlet SHALL reject the review submission and return an error message indicating that only customers with completed orders can submit reviews.
4. WHEN the product detail page is rendered for a logged-in user, THE ProductDetailServlet SHALL determine whether the user has a Verified_Purchase for that product and pass this information to the JSP for conditional display of the review form.

### Requirement 5: Product Soft Delete with Referential Integrity Preservation

**User Story:** As an administrator, I want to archive products instead of permanently deleting them, so that order history and review data are preserved.

#### Acceptance Criteria

1. THE database schema SHALL include an `is_active` column (BOOLEAN, default TRUE) on the `products` table to support Product_Archive.
2. WHEN an administrator requests deletion of a product through Admin_ProductServlet, THE ProductDAO SHALL set `is_active = FALSE` on the product row instead of executing a `DELETE` statement.
3. WHILE a product has `is_active = FALSE`, THE ProductDAO SHALL exclude that product from all customer-facing queries (shop listing, search, category browsing, related products).
4. WHILE a product has `is_active = FALSE`, THE OrderDAO SHALL continue to display the product name and image in order history views by joining against the products table regardless of `is_active` status.
5. THE database schema SHALL replace `ON DELETE CASCADE` on the `order_items.product_id` foreign key with `ON DELETE RESTRICT` to prevent accidental data loss if a hard delete is attempted.
6. THE database schema SHALL replace `ON DELETE CASCADE` on the `reviews.product_id` foreign key with `ON DELETE RESTRICT` to prevent accidental review data loss.

### Requirement 6: User Soft Delete with Data Preservation

**User Story:** As an administrator, I want to deactivate user accounts instead of permanently deleting them, so that order history, reviews, and audit trails are preserved.

#### Acceptance Criteria

1. WHEN an administrator requests deletion of a user through Admin_UserManageServlet, THE UserDAO SHALL set the `status` column to `'inactive'` (or equivalent false value) instead of executing a `DELETE FROM users` statement.
2. WHILE a user has `status = inactive`, THE LoginServlet SHALL reject login attempts for that user and return a message indicating the account is deactivated.
3. WHILE a user has `status = inactive`, THE OrderDAO SHALL continue to display orders placed by that user in admin order management views.
4. THE database schema SHALL replace `ON DELETE CASCADE` on the `orders.user_id` foreign key with `ON DELETE RESTRICT` to prevent accidental order data loss.
5. THE database schema SHALL replace `ON DELETE CASCADE` on the `cart.user_id` foreign key with `ON DELETE RESTRICT` to prevent orphaned data issues.
6. THE database schema SHALL replace `ON DELETE CASCADE` on the `reviews.user_id` foreign key with `ON DELETE RESTRICT` to prevent accidental review data loss.

### Requirement 7: Database Indexing for Query Performance

**User Story:** As a system operator, I want frequently queried columns to be indexed, so that page load times remain acceptable as data volume grows.

#### Acceptance Criteria

1. THE database schema SHALL include an index on `orders(user_id)` to accelerate user order history lookups.
2. THE database schema SHALL include an index on `orders(created_at)` to accelerate date-range queries on the admin dashboard and reports.
3. THE database schema SHALL include an index on `orders(status)` to accelerate order filtering by status in admin order management.
4. THE database schema SHALL include an index on `order_items(order_id)` to accelerate order detail retrieval.
5. THE database schema SHALL include an index on `order_items(product_id)` to accelerate product sales aggregation queries.
6. THE database schema SHALL include an index on `reviews(product_id)` to accelerate product review listing.
7. THE database schema SHALL include an index on `reviews(user_id)` to accelerate user review lookups.
8. THE database schema SHALL include an index on `products(pet_type_id)` to accelerate pet-type-based product filtering.
9. THE database schema SHALL include an index on `products(category)` to accelerate category-based product filtering.
10. THE database schema SHALL include an index on `cart(user_id)` to accelerate cart loading for logged-in users.
