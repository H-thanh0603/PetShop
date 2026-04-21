# Cluster 2 Shopping Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the storefront feel more like a full ecommerce site by adding a working wishlist, real review/rating data, and cleaner product detail/shop presentation.

**Architecture:** Extend the existing product/review flow instead of replacing it. Add wishlist DAO + servlets for persistence, enrich product queries with review aggregates, and update JSPs so cards/detail pages render real counts, real stock states, and interactive wishlist controls for logged-in users.

**Tech Stack:** Java Servlet/JSP, JDBC/MySQL, Maven, JUnit 5.

---

### Task 1: Add test support and write failing tests for product review aggregates

**Files:**
- Modify: `pom.xml`
- Create: `src/test/java/Model/ProductCommerceTest.java`

- [ ] **Step 1: Add JUnit 5 and Surefire**

Add `org.junit.jupiter:junit-jupiter` with test scope and `maven-surefire-plugin` to `pom.xml`.

- [ ] **Step 2: Write failing tests for new product commerce helpers**

Create tests that expect:
- a product can report average rating text with 1 decimal
- a product can report review count safely when zero
- a product can expose discounted percentage/labels consistently

- [ ] **Step 3: Run the tests and verify RED**

Run: `mvn -q -Dtest=ProductCommerceTest test`

Expected: FAIL because the new Product helper fields/methods do not exist yet

### Task 2: Add real review aggregate data to Product

**Files:**
- Modify: `src/main/java/Model/Product.java`
- Modify: `src/main/java/DAO/ProductDAO.java`

- [ ] **Step 1: Add review aggregate fields to Product**

Add:
- `double averageRating`
- `int reviewCount`

and helper getters for safe display values.

- [ ] **Step 2: Update product mapping to read aggregate columns when present**

Teach `ProductDAO.mapProduct(...)` to populate `average_rating` and `review_count` when queries include them.

- [ ] **Step 3: Update storefront-facing product queries**

Update main product list/detail/related/popular/discount queries to `LEFT JOIN reviews` and return:
- `COALESCE(AVG(r.rating), 0) AS average_rating`
- `COUNT(r.id) AS review_count`

- [ ] **Step 4: Run tests and verify GREEN**

Run: `mvn -q -Dtest=ProductCommerceTest test`

Expected: PASS

### Task 3: Implement wishlist persistence and routes

**Files:**
- Create: `src/main/java/DAO/WishlistDAO.java`
- Create: `src/main/java/controller/shop/WishlistServlet.java`
- Create: `src/main/java/controller/shop/ToggleWishlistServlet.java`
- Create: `src/main/webapp/pages/shop/wishlist.jsp`

- [ ] **Step 1: Add DAO for wishlist table**

Implement methods:
- `getWishlistProductsByUserId(int userId)`
- `getWishlistProductIdsByUserId(int userId)`
- `isInWishlist(int userId, int productId)`
- `addToWishlist(int userId, int productId)`
- `removeFromWishlist(int userId, int productId)`
- `toggleWishlist(int userId, int productId)`

- [ ] **Step 2: Add servlet to view wishlist**

Map `/wishlist`, require login, load wishlist products, forward to `pages/shop/wishlist.jsp`.

- [ ] **Step 3: Add servlet to toggle wishlist**

Map `/toggle-wishlist`, require login, accept product id + optional redirect target, then add/remove and redirect back.

### Task 4: Wire wishlist and real rating data into storefront pages

**Files:**
- Modify: `src/main/java/controller/shop/ShopServlet.java`
- Modify: `src/main/java/controller/shop/ProductDetailServlet.java`
- Modify: `src/main/webapp/components/navbar.jsp`
- Modify: `src/main/webapp/components/layout/header-home.jsp`
- Modify: `src/main/webapp/components/navbar-white.jsp`
- Modify: `src/main/webapp/pages/shop/shop.jsp`
- Modify: `src/main/webapp/pages/shop/shop-pet.jsp`
- Modify: `src/main/webapp/pages/shop/product.jsp`

- [ ] **Step 1: Load wishlist state for logged-in users**

In `ShopServlet` and `ProductDetailServlet`, load the current user wishlist product IDs into request scope.

- [ ] **Step 2: Add wishlist entry points**

Add “Yêu thích” link into logged-in dropdowns in active navbars.

- [ ] **Step 3: Replace hardcoded rating/review text with real data**

Use `detail.reviewCount`, `detail.averageRating`, and per-card review metrics where appropriate.

- [ ] **Step 4: Replace inert heart buttons with working wishlist forms/links**

On list/detail pages, clicking heart should call `/toggle-wishlist`.

- [ ] **Step 5: Add wishlist page UI**

Show wishlist items with product image, price, stock state, remove button, and add-to-cart button.

### Task 5: Harden the review submission flow

**Files:**
- Modify: `src/main/java/DAO/ReviewDAO.java`
- Modify: `src/main/java/controller/shop/AddReviewServlet.java`
- Modify: `src/main/java/controller/shop/ProductDetailServlet.java`
- Modify: `src/main/webapp/pages/shop/product.jsp`

- [ ] **Step 1: Add DAO helpers**

Implement:
- `hasUserReviewedProduct(int userId, int productId)`
- `getAverageRatingByProductId(int productId)` if needed

- [ ] **Step 2: Validate review submissions**

Reject:
- unauthenticated review
- rating outside 1..5
- blank comment
- duplicate review from the same user for the same product

- [ ] **Step 3: Show review form state in product page**

If user already reviewed, show a notice instead of review form.

### Task 6: Verify the cluster

**Files:**
- Verify only

- [ ] **Step 1: Run targeted tests**

Run: `mvn -q -Dtest=ProductCommerceTest test`

Expected: PASS

- [ ] **Step 2: Compile the web app**

Run: `mvn -q -DskipTests compile`

Expected: exit code `0`

- [ ] **Step 3: Inspect working tree**

Run: `git status --short`

Expected: only intended Cluster 2 files are modified plus unrelated local artifacts
