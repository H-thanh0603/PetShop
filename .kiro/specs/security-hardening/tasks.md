# Implementation Plan: Security Hardening

## Overview

This plan implements seven security layers for the PetShop Jakarta EE application: admin authorization filter, brute-force login protection, session security, CSRF protection, XSS/input sanitization, rate limiting, and file upload hardening. Each task builds incrementally, starting with foundational utilities and data layer changes, then filters, then servlet integrations, and finally wiring everything together in `web.xml`.

## Tasks

- [x] 1. Database schema and User model changes for brute-force protection
  - [x] 1.1 Create SQL migration script to add `failed_login_attempts` and `locked_until` columns to the `users` table
    - Create `PetShop/security_hardening.sql` with `ALTER TABLE users ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0, ADD COLUMN locked_until DATETIME NULL DEFAULT NULL;`
    - _Requirements: 2.5_
  - [x] 1.2 Add `failedLoginAttempts` and `lockedUntil` fields to `Model/User.java`
    - Add `private int failedLoginAttempts;` and `private Timestamp lockedUntil;` with getters and setters
    - Update the `mapUser` method in `UserDAO` to read the new columns from `ResultSet`
    - _Requirements: 2.5_
  - [x] 1.3 Implement brute-force DAO methods in `UserDAO.java`
    - Add `getFailedLoginAttempts(String email)` — returns current failed attempt count
    - Add `getLockedUntil(String email)` — returns the `locked_until` timestamp
    - Add `incrementFailedAttempts(String email)` — increments `failed_login_attempts` by 1
    - Add `lockAccount(String email, int lockoutMinutes)` — sets `locked_until` to current time + lockout minutes
    - Add `resetFailedAttempts(String email)` — sets `failed_login_attempts = 0` and `locked_until = NULL`
    - Add `isAccountLocked(String email)` — returns true if `locked_until` is in the future
    - All methods use parameterized queries
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  - [ ]* 1.4 Write property test for brute-force counter increment-reset round trip
    - **Property 3: Brute-force counter increment-reset round trip**
    - **Validates: Requirements 2.1, 2.4**
  - [ ]* 1.5 Write property test for locked account rejection
    - **Property 4: Locked account rejection**
    - **Validates: Requirements 2.3, 2.6**

- [x] 2. Extend `ValidationUtil` with sanitization methods and create `FileUploadValidator`
  - [x] 2.1 Add `stripHtmlTags` and `validateMaxLength` methods to `Util/ValidationUtil.java`
    - `stripHtmlTags(String input)`: removes all HTML tags using regex `<[^>]*>`, trims whitespace, returns `""` for null input
    - `validateMaxLength(String input, int maxLength)`: returns `true` if input is non-null and `input.length() <= maxLength`, `false` otherwise
    - _Requirements: 5.6, 5.7_
  - [ ]* 2.2 Write property test for `stripHtmlTags`
    - **Property 8: stripHtmlTags removes all HTML tags**
    - **Validates: Requirements 5.1, 5.6**
  - [ ]* 2.3 Write property test for `validateMaxLength`
    - **Property 9: validateMaxLength correctness**
    - **Validates: Requirements 5.2, 5.3, 5.5, 5.7**
  - [x] 2.4 Create `Util/FileUploadValidator.java`
    - Implement `isAllowedExtension(String fileName)` — checks extension against whitelist `jpg, jpeg, png, gif, webp` (case-insensitive)
    - Implement `isContentTypeMatchingExtension(String contentType, String extension)` — validates content-type matches extension per mapping
    - Implement `generateSecureFileName(String originalFileName)` — produces `UUID + "_" + timestamp + "." + extension`
    - Implement `validate(Part filePart)` — orchestrates all checks, returns a `ValidationResult` with success/error message
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_
  - [ ]* 2.5 Write property test for file extension whitelist validation
    - **Property 13: File extension whitelist validation**
    - **Validates: Requirements 7.1, 7.3**
  - [ ]* 2.6 Write property test for file content-type and extension matching
    - **Property 14: File content-type and extension matching**
    - **Validates: Requirements 7.2, 7.4**
  - [ ]* 2.7 Write property test for secure filename uniqueness and format
    - **Property 15: Secure filename uniqueness and format**
    - **Validates: Requirements 7.5**

- [x] 3. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Implement `AdminAuthFilter`
  - [x] 4.1 Create `controller/filter/AdminAuthFilter.java`
    - Implement `jakarta.servlet.Filter` interface
    - In `doFilter`: cast to `HttpServletRequest`, check if URI is a static resource (skip if so), get `User` from session attribute `"user"`, verify `user != null` and `user.getRole().equals("admin")`, redirect to `/login` if not authorized, call `chain.doFilter()` if authorized
    - Static resource extensions: `.css`, `.js`, `.png`, `.jpg`, `.jpeg`, `.gif`, `.ico`, `.woff`, `.woff2`, `.ttf`, `.svg`, `.map`, `.webp`
    - Do NOT use `@WebFilter` annotation (will be registered in `web.xml` for ordering)
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_
  - [ ]* 4.2 Write property test for admin filter authorization decision
    - **Property 1: Admin filter authorization decision**
    - **Validates: Requirements 1.1, 1.2, 1.3**
  - [ ]* 4.3 Write property test for admin filter static resource bypass
    - **Property 2: Admin filter static resource bypass**
    - **Validates: Requirements 1.4**

- [x] 5. Implement `CsrfFilter`
  - [x] 5.1 Create `controller/filter/CsrfFilter.java`
    - Implement `jakarta.servlet.Filter` interface
    - `generateToken()`: use `SecureRandom` to generate 32 random bytes, convert to 64-char hex string
    - In `doFilter`: skip static resources; if session has no `csrfToken`, generate and store one; on GET requests set `request.setAttribute("csrfToken", token)`; on POST requests compare `request.getParameter("csrfToken")` with session token — return 403 if mismatch or missing
    - Exclude JSON API endpoints: check if `Accept` header contains `application/json`
    - Do NOT use `@WebFilter` annotation (will be registered in `web.xml` for ordering)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_
  - [ ]* 5.2 Write property test for CSRF token validation on POST
    - **Property 5: CSRF token validation on POST**
    - **Validates: Requirements 4.3, 4.4**
  - [ ]* 5.3 Write property test for CSRF token availability on GET
    - **Property 6: CSRF token availability on GET**
    - **Validates: Requirements 4.2**
  - [ ]* 5.4 Write property test for CSRF JSON endpoint exclusion
    - **Property 7: CSRF JSON endpoint exclusion**
    - **Validates: Requirements 4.5**

- [x] 6. Implement `RateLimitFilter`
  - [x] 6.1 Create `controller/filter/RateLimitFilter.java`
    - Implement `jakarta.servlet.Filter` interface
    - Use `ConcurrentHashMap<String, CopyOnWriteArrayList<Long>>` keyed by `clientIp + ":" + endpoint`
    - `resolveClientIp(HttpServletRequest)`: return first IP from `X-Forwarded-For` header if present, otherwise `request.getRemoteAddr()`
    - `getLimitForEndpoint(String path)`: return 10 for `/login`, 30 for `/shop`, 20 for `/add-to-cart`, 5 for `/add-review`, -1 (no limit) for others
    - `isRateLimited(String clientIp, String endpoint)`: count timestamps within last 60 seconds, return true if count exceeds limit
    - In `init()`: start a `ScheduledExecutorService` that runs `evictExpiredEntries()` every 5 minutes
    - In `destroy()`: shut down the executor
    - On 429 response: set `Retry-After` header
    - Do NOT use `@WebFilter` annotation (will be registered in `web.xml` for ordering)
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7_
  - [ ]* 6.2 Write property test for rate limit sliding window correctness
    - **Property 11: Rate limit sliding window correctness**
    - **Validates: Requirements 6.1, 6.6**
  - [ ]* 6.3 Write property test for rate limit IP resolution
    - **Property 12: Rate limit IP resolution**
    - **Validates: Requirements 6.7**

- [x] 7. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Integrate brute-force protection and session security into `LoginServlet`
  - [x] 8.1 Add brute-force logic to `LoginServlet.doPost`
    - Before password verification: call `userDAO.isAccountLocked(email)`. If locked, compute remaining minutes from `getLockedUntil()`, display error message with remaining time, and return
    - On failed login: call `incrementFailedAttempts(email)`. If count reaches 5, call `lockAccount(email, 15)`. Display remaining attempts (`5 - failedAttempts`)
    - On successful login: call `resetFailedAttempts(email)` before proceeding
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.6_
  - [x] 8.2 Add session regeneration to `LoginServlet.doPost`
    - On successful login, before storing user attributes: invalidate the existing session with `oldSession.invalidate()`, create a new session with `request.getSession(true)`, then store `user`, `username`, `role`, cart data on the new session
    - Preserve cart data across session regeneration by saving it before invalidation
    - _Requirements: 3.2_

- [x] 9. Integrate input sanitization into servlets
  - [x] 9.1 Add sanitization to `AddReviewServlet`
    - After reading the `comment` parameter: call `ValidationUtil.stripHtmlTags(comment)` to remove HTML
    - Call `ValidationUtil.validateMaxLength(comment, 1000)` — if false, set error message and redirect back
    - _Requirements: 5.1, 5.2_
  - [x] 9.2 Add order note validation to `CheckoutServlet`
    - Before using the `note` parameter in `placeOrderWithStockCheck`: call `ValidationUtil.validateMaxLength(note, 500)` — if false, return error JSON response
    - _Requirements: 5.3_
  - [x] 9.3 Add search keyword truncation to `ShopServlet` and `SearchAutocompleteServlet`
    - In `ShopServlet.doGet`: after reading the `search` parameter, truncate to 100 characters using `search.substring(0, Math.min(search.length(), 100))`
    - In `SearchAutocompleteServlet.doGet`: after reading the `q` parameter, truncate to 100 characters
    - _Requirements: 5.4_
  - [ ]* 9.4 Write property test for search keyword truncation
    - **Property 10: Search keyword truncation**
    - **Validates: Requirements 5.4**
  - [x] 9.5 Add fullname length validation to `RegisterServlet`
    - In `handleRegister`: call `ValidationUtil.validateMaxLength(fullname, 200)` — if false, add form error and return
    - _Requirements: 5.5_

- [x] 10. Integrate `FileUploadValidator` into upload servlets
  - [x] 10.1 Refactor `FileUploadServlet` to use `FileUploadValidator`
    - Replace inline file type and size checks with `FileUploadValidator.validate(filePart)`
    - Use `FileUploadValidator.generateSecureFileName()` instead of `FileUploadUtil.saveFile()` for filename generation
    - Return appropriate error JSON if validation fails
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_
  - [x] 10.2 Refactor `ProductServlet` to use `FileUploadValidator`
    - Replace inline `isValidImageType()` and `getFileExtension()` checks with `FileUploadValidator.validate(filePart)`
    - Use `FileUploadValidator.generateSecureFileName()` for filename generation
    - Remove the private `isValidImageType()` and `getFileExtension()` methods
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

- [x] 11. Configure `web.xml` for session security and filter ordering
  - [x] 11.1 Add session configuration to `web.xml`
    - Add `<session-config>` with `<session-timeout>30</session-timeout>` and `<cookie-config>` with `<http-only>true</http-only>` and `<secure>true</secure>`
    - _Requirements: 3.1, 3.3, 3.4_
  - [x] 11.2 Register all three new filters in `web.xml` with explicit ordering
    - Declare `RateLimitFilter`, `AdminAuthFilter`, and `CsrfFilter` as `<filter>` elements
    - Add `<filter-mapping>` entries in order: RateLimitFilter (`/*`), AdminAuthFilter (`/pages/admin/*` and `/admin/*`), CsrfFilter (`/*`)
    - This ensures the filter chain executes in the correct order: rate limiting → admin auth → CSRF
    - _Requirements: 1.5, 4.6, 6.1_

- [x] 12. Add CSRF hidden field to JSP forms
  - [x] 12.1 Add `<input type="hidden" name="csrfToken" value="${csrfToken}" />` to all POST forms in JSP files
    - Update login form (`pages/auth/login.jsp`)
    - Update register form (`pages/auth/register.jsp`)
    - Update review form in product detail page
    - Update checkout form (`pages/shop/checkout.jsp`)
    - Update any admin forms that submit via POST (product add/edit, category, blog, user management)
    - _Requirements: 4.2_

- [x] 13. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document using jqwik
- Unit tests validate specific examples and edge cases
- The SQL migration script (task 1.1) must be run against the database before testing brute-force features
- Filter ordering in `web.xml` is critical: RateLimitFilter → AdminAuthFilter → CsrfFilter → PetTypeFilter (existing `@WebFilter`)
