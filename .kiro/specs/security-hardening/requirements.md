# Requirements Document

## Introduction

The PetShop e-commerce application (Jakarta EE Servlet + JSP + MySQL) has 13 critical security and authentication vulnerabilities. This specification addresses those issues across seven requirement areas: admin authorization enforcement, brute-force protection, session security, CSRF protection, XSS and input sanitization, rate limiting, and file upload hardening. The goal is to bring the application to a baseline-secure state without altering existing business logic.

## Glossary

- **Application**: The PetShop Jakarta EE web application running on a servlet container.
- **Admin_Authorization_Filter**: A Jakarta Servlet `Filter` mapped to admin URL patterns that enforces role-based access control.
- **Session_User**: The `User` object stored in the HTTP session under the attribute key `"user"` after successful authentication.
- **Admin_Role**: The string value `"admin"` stored in the `role` field of a `User` record.
- **Login_Servlet**: The servlet mapped to `/login` that handles user authentication via email and password.
- **CSRF_Filter**: A Jakarta Servlet `Filter` that generates, embeds, and validates a per-session CSRF token on all state-changing (POST) requests.
- **CSRF_Token**: A cryptographically random string generated per session, embedded as a hidden form field, and validated server-side on POST requests.
- **Rate_Limiter**: An in-memory, IP-based request throttle implemented as a Jakarta Servlet `Filter`.
- **Input_Sanitizer**: Server-side logic that strips HTML tags and enforces maximum character lengths on user-supplied text fields.
- **File_Upload_Validator**: Server-side logic in `FileUploadServlet` and `ProductServlet` that validates uploaded file extension, content-type, and generates a random filename.
- **Lockout_Period**: A 15-minute window during which a user account cannot authenticate after exceeding the failed-attempt threshold.
- **Failed_Attempt_Threshold**: The maximum number of consecutive failed login attempts (5) before account lockout is triggered.

## Requirements

### Requirement 1: Admin Authorization Filter

**User Story:** As a system administrator, I want all admin pages to be protected by a centralized authorization check, so that non-admin users cannot access administrative functionality by navigating directly to admin URLs.

#### Acceptance Criteria

1. WHEN an unauthenticated user requests a URL matching `/pages/admin/*` or `/admin/*`, THE Admin_Authorization_Filter SHALL redirect the request to the `/login` page.
2. WHEN an authenticated user whose role is not Admin_Role requests a URL matching `/pages/admin/*` or `/admin/*`, THE Admin_Authorization_Filter SHALL redirect the request to the `/login` page.
3. WHEN an authenticated user whose role is Admin_Role requests a URL matching `/pages/admin/*` or `/admin/*`, THE Admin_Authorization_Filter SHALL allow the request to proceed to the target servlet.
4. THE Admin_Authorization_Filter SHALL skip authorization checks for static resources (CSS, JS, image files, fonts, SVG, and map files).
5. THE Admin_Authorization_Filter SHALL be registered via the `@WebFilter` annotation with URL patterns `/pages/admin/*` and `/admin/*`.

### Requirement 2: Brute-Force Protection and Account Lockout

**User Story:** As a system administrator, I want the login process to lock accounts after repeated failed attempts, so that brute-force password attacks are mitigated.

#### Acceptance Criteria

1. WHEN a login attempt fails, THE Login_Servlet SHALL increment the failed login attempt counter for the corresponding username in the database.
2. WHEN the failed login attempt counter for a username reaches the Failed_Attempt_Threshold, THE Login_Servlet SHALL set the `locked_until` timestamp to the current time plus the Lockout_Period.
3. WHILE a user account has a `locked_until` timestamp in the future, THE Login_Servlet SHALL reject authentication attempts for that account and return a message indicating the account is temporarily locked with the remaining lockout duration.
4. WHEN a login attempt succeeds, THE Login_Servlet SHALL reset the failed login attempt counter and clear the `locked_until` timestamp for that account.
5. THE Application SHALL add `failed_login_attempts` (INT, default 0) and `locked_until` (DATETIME, nullable) columns to the `users` table.
6. WHEN a login attempt fails and the account is not locked, THE Login_Servlet SHALL display the number of remaining attempts before lockout.

### Requirement 3: Session Security

**User Story:** As a user, I want my session to be protected against fixation attacks and to expire after inactivity, so that unauthorized parties cannot hijack my session.

#### Acceptance Criteria

1. THE Application SHALL configure a session timeout of 30 minutes in `web.xml` via the `<session-config>` element.
2. WHEN a user authenticates successfully, THE Login_Servlet SHALL invalidate the existing session and create a new session before storing user attributes (session ID regeneration).
3. THE Application SHALL configure session cookies with the `HttpOnly` attribute via `web.xml` cookie configuration.
4. THE Application SHALL configure session cookies with the `Secure` attribute via `web.xml` cookie configuration.

### Requirement 4: CSRF Protection

**User Story:** As a user, I want all state-changing form submissions to be protected by a CSRF token, so that cross-site request forgery attacks are prevented.

#### Acceptance Criteria

1. WHEN a new session is created, THE CSRF_Filter SHALL generate a cryptographically random CSRF_Token and store it in the session.
2. WHEN a GET request is processed, THE CSRF_Filter SHALL set the CSRF_Token as a request attribute named `csrfToken` so that JSP forms can embed it as a hidden field.
3. WHEN a POST request is received, THE CSRF_Filter SHALL validate that the submitted `csrfToken` parameter matches the CSRF_Token stored in the session.
4. IF a POST request is received without a valid CSRF_Token, THEN THE CSRF_Filter SHALL reject the request with HTTP status 403 (Forbidden).
5. THE CSRF_Filter SHALL exclude AJAX endpoints that return JSON responses (content-type `application/json`) from CSRF validation when those endpoints use a custom request header for authentication.
6. THE CSRF_Filter SHALL be mapped to `/*` and process all non-static requests.

### Requirement 5: XSS and Input Sanitization

**User Story:** As a user, I want all text inputs to be sanitized and length-limited, so that cross-site scripting attacks and oversized payloads are prevented.

#### Acceptance Criteria

1. WHEN a review comment is submitted, THE Application SHALL strip all HTML tags from the comment text before storing it in the database.
2. WHEN a review comment is submitted, THE Application SHALL reject the comment if the text exceeds 1000 characters.
3. WHEN an order note is submitted, THE Application SHALL reject the note if the text exceeds 500 characters.
4. WHEN a search keyword is submitted, THE Application SHALL truncate the keyword to 100 characters before processing.
5. WHEN a user full name is submitted (registration or profile update), THE Application SHALL reject the name if the text exceeds 200 characters.
6. THE Input_Sanitizer SHALL provide a `stripHtmlTags` method that removes all HTML tags from a given string and trims whitespace.
7. THE Input_Sanitizer SHALL provide a `validateMaxLength` method that returns false when a given string exceeds a specified maximum length.

### Requirement 6: Rate Limiting

**User Story:** As a system administrator, I want request rates to be limited per IP address on sensitive endpoints, so that automated abuse (credential stuffing, scraping, cart manipulation) is mitigated.

#### Acceptance Criteria

1. THE Rate_Limiter SHALL track request counts per client IP address using an in-memory data structure with a sliding window of 1 minute.
2. WHEN a client IP exceeds 10 requests per minute to the `/login` endpoint, THE Rate_Limiter SHALL respond with HTTP status 429 (Too Many Requests).
3. WHEN a client IP exceeds 30 requests per minute to the `/shop` endpoint (search requests), THE Rate_Limiter SHALL respond with HTTP status 429 (Too Many Requests).
4. WHEN a client IP exceeds 20 requests per minute to the `/add-to-cart` endpoint, THE Rate_Limiter SHALL respond with HTTP status 429 (Too Many Requests).
5. WHEN a client IP exceeds 5 requests per minute to the `/add-review` endpoint, THE Rate_Limiter SHALL respond with HTTP status 429 (Too Many Requests).
6. THE Rate_Limiter SHALL periodically evict expired entries from the in-memory data structure to prevent memory leaks.
7. THE Rate_Limiter SHALL use `request.getRemoteAddr()` as the client identifier, with support for reading the `X-Forwarded-For` header when the application is behind a reverse proxy.

### Requirement 7: File Upload Security

**User Story:** As a system administrator, I want uploaded files to be strictly validated, so that malicious files cannot be uploaded to the server.

#### Acceptance Criteria

1. WHEN a file is uploaded, THE File_Upload_Validator SHALL validate that the file extension is one of: `jpg`, `jpeg`, `png`, `gif`, `webp` (case-insensitive).
2. WHEN a file is uploaded, THE File_Upload_Validator SHALL validate that the `Content-Type` header matches the declared file extension (e.g., `image/jpeg` for `.jpg`/`.jpeg`, `image/png` for `.png`, `image/gif` for `.gif`, `image/webp` for `.webp`).
3. IF a file is uploaded with an extension not in the whitelist, THEN THE File_Upload_Validator SHALL reject the upload and return an error message.
4. IF a file is uploaded with a Content-Type that does not match the file extension, THEN THE File_Upload_Validator SHALL reject the upload and return an error message.
5. WHEN a file passes validation, THE File_Upload_Validator SHALL generate a random filename using `UUID.randomUUID()` combined with a timestamp, preserving only the validated extension.
6. THE File_Upload_Validator SHALL apply the same validation rules in both `FileUploadServlet` and `ProductServlet` by delegating to a shared utility method.
