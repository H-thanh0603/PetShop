# Requirements Document

## Introduction

This document specifies requirements for session management improvements and extensibility enhancements in the PetShop e-commerce application. The scope covers five areas: cart persistence across sessions for logged-in users, in-memory caching of pet types with invalidation, review spam protection through rate limiting and duplicate detection, payment method extensibility via a provider registry, and email verification during user registration. The project uses Jakarta EE (Servlets, Filters), MySQL, and existing utilities such as OTPUtil for email delivery.

## Glossary

- **Cart_Persistence_Service**: The component responsible for synchronizing shopping cart data between the HTTP session and the database for logged-in users.
- **Cart_Merge_Engine**: The component that combines a guest session cart with a previously saved database cart when a user logs in.
- **CartDAO**: The existing Data Access Object that reads and writes cart records in the MySQL `cart` table.
- **PetType_Cache**: An in-memory cache backed by ConcurrentHashMap that stores active pet type records with a time-to-live (TTL) expiration.
- **PetTypeFilter**: The existing Jakarta Servlet Filter (`@WebFilter("/*")`) that loads pet types for every non-static request.
- **PetTypeServlet**: The existing admin servlet that handles add, edit, and toggle operations on pet types.
- **Review_Rate_Limiter**: The component that enforces a maximum number of review submissions per user within a rolling time window.
- **Duplicate_Review_Detector**: The component that checks whether a user has recently submitted a review with identical comment text.
- **ReviewDAO**: The existing Data Access Object for the `reviews` table.
- **AddReviewServlet**: The existing servlet that handles review submission at `/add-review`.
- **Payment_Provider**: An interface defining a contract for processing a payment of a given amount, returning a result indicating success or failure.
- **Payment_Registry**: A registry that maps payment method identifiers to their corresponding Payment_Provider implementations.
- **CheckoutServlet**: The existing servlet at `/checkout` that processes orders, currently using a switch-case for payment methods.
- **Email_Verification_Service**: The component responsible for generating verification tokens, sending verification emails, and validating tokens upon user click.
- **Verification_Token**: A unique, time-limited string stored in the database and sent to the user's email address as part of a verification link.
- **RegisterServlet**: The existing servlet at `/register` that handles user registration with OTP-based email confirmation.
- **LoginServlet**: The existing servlet at `/login` that authenticates users.
- **UserDAO**: The existing Data Access Object for the `users` table.
- **OTPUtil**: The existing utility class used for generating and sending OTP codes via email.

## Requirements

### Requirement 1: Cart Persistence for Logged-In Users

**User Story:** As a logged-in customer, I want my shopping cart to be automatically saved to the database, so that I do not lose my cart contents when my session expires or I switch devices.

#### Acceptance Criteria

1. WHILE a User is authenticated, THE Cart_Persistence_Service SHALL persist every cart modification (add, update, remove) to the database via CartDAO within the same request that modifies the session cart.
2. WHEN an authenticated User adds an item to the session cart, THE Cart_Persistence_Service SHALL write the corresponding record to the database cart before the response is committed.
3. WHEN an authenticated User updates the quantity of a cart item, THE Cart_Persistence_Service SHALL update the corresponding database cart record to match the new quantity.
4. WHEN an authenticated User removes an item from the session cart, THE Cart_Persistence_Service SHALL delete the corresponding record from the database cart.
5. WHEN a User logs in and the session contains a guest cart with items, THE Cart_Merge_Engine SHALL merge the session cart into the existing database cart by adding quantities for matching products and inserting new products.
6. WHEN a User logs in and the session contains a guest cart, THE Cart_Merge_Engine SHALL cap the merged quantity for each product at the current stock level.
7. WHEN a User logs in after a previous session has expired, THE Cart_Persistence_Service SHALL restore the cart from the database into the new session.
8. WHEN a User logs in and no guest cart exists in the session, THE Cart_Persistence_Service SHALL load the database cart into the session without modification.
9. IF the database is unreachable during a cart persistence operation, THEN THE Cart_Persistence_Service SHALL log the error and retain the session cart so the user can continue shopping.

### Requirement 2: Pet Type Cache with TTL and Invalidation

**User Story:** As a system operator, I want pet type data to be cached in memory with automatic expiration, so that the database is not queried on every HTTP request while still reflecting admin changes within a bounded time.

#### Acceptance Criteria

1. THE PetType_Cache SHALL store active pet type records in a ConcurrentHashMap with a timestamp indicating when the cache was last populated.
2. WHEN a non-static HTTP request arrives and the PetType_Cache has not been populated or the cache age exceeds 3600 seconds (1 hour), THE PetTypeFilter SHALL reload pet types from the database and update the cache.
3. WHEN a non-static HTTP request arrives and the PetType_Cache age is less than or equal to 3600 seconds, THE PetTypeFilter SHALL serve pet types from the cache without querying the database.
4. WHEN an admin adds a new pet type via PetTypeServlet, THE PetType_Cache SHALL be invalidated immediately so the next request triggers a reload.
5. WHEN an admin edits an existing pet type via PetTypeServlet, THE PetType_Cache SHALL be invalidated immediately.
6. WHEN an admin toggles the active status of a pet type via PetTypeServlet, THE PetType_Cache SHALL be invalidated immediately.
7. IF the database query fails during a cache reload, THEN THE PetType_Cache SHALL retain the previously cached data and log the error.
8. THE PetType_Cache SHALL be thread-safe, allowing concurrent reads and writes without data corruption.

### Requirement 3: Review Spam Protection

**User Story:** As a store owner, I want to prevent users from flooding the review system with excessive or duplicate reviews, so that product reviews remain genuine and useful.

#### Acceptance Criteria

1. WHEN a User submits a review for a product the User has already reviewed, THE AddReviewServlet SHALL reject the submission and return an error message indicating the User has already reviewed the product.
2. WHEN a User has submitted 5 or more reviews within the preceding 60 minutes, THE Review_Rate_Limiter SHALL reject the new review submission and return an error message indicating the rate limit has been exceeded.
3. WHEN a User submits a review and the Review_Rate_Limiter count for the User is below 5 within the preceding 60 minutes, THE AddReviewServlet SHALL accept the review for further processing.
4. WHEN a User submits a review whose comment text matches exactly the comment text of any review submitted by the same User within the preceding 24 hours, THE Duplicate_Review_Detector SHALL reject the submission and return an error message indicating duplicate content.
5. WHEN a User submits a review whose comment text does not match any of the User's reviews from the preceding 24 hours, THE Duplicate_Review_Detector SHALL allow the review to proceed.
6. THE Review_Rate_Limiter SHALL count reviews based on the `created_at` timestamp in the `reviews` table, using a database query rather than in-memory state.
7. THE Duplicate_Review_Detector SHALL perform an exact text comparison of the submitted comment against the User's reviews from the preceding 24 hours using a database query.
8. IF the rate limit or duplicate check query fails due to a database error, THEN THE AddReviewServlet SHALL reject the review submission and return a generic error message rather than allowing the review through.

### Requirement 4: Payment Method Extensibility

**User Story:** As a developer, I want to add new payment methods without modifying the CheckoutServlet source code, so that the payment system is extensible and maintainable.

#### Acceptance Criteria

1. THE Payment_Provider interface SHALL define a `process(double amount)` method that returns a Payment_Result containing a success flag, a payment method database identifier, and a payment status flag.
2. THE Payment_Registry SHALL provide a method to register a Payment_Provider implementation under a string key corresponding to the payment method identifier (e.g., "cod", "momo", "bank_transfer").
3. THE Payment_Registry SHALL provide a method to retrieve a Payment_Provider by its string key.
4. WHEN the application starts, THE Payment_Registry SHALL contain registered providers for "cod" (CODPaymentProvider), "momo" (MoMoPaymentProvider), and "bank_transfer" (BankTransferPaymentProvider).
5. THE CODPaymentProvider SHALL return a successful Payment_Result with payment method "COD" and payment status false (unpaid at order time).
6. THE MoMoPaymentProvider SHALL call the MoMo payment API and return a Payment_Result reflecting the API response.
7. THE BankTransferPaymentProvider SHALL call the bank transfer API and return a Payment_Result reflecting the API response.
8. WHEN a checkout request is received, THE CheckoutServlet SHALL look up the Payment_Provider from the Payment_Registry using the submitted payment method key instead of using a switch-case statement.
9. IF the submitted payment method key does not match any registered Payment_Provider, THEN THE CheckoutServlet SHALL return an error indicating an invalid payment method.
10. IF a Payment_Provider's `process` method returns a failure result, THEN THE CheckoutServlet SHALL roll back the transaction and return the failure message to the user.

### Requirement 5: Email Verification on Registration

**User Story:** As a store owner, I want users to verify their email address after registration, so that only users with valid email addresses can access the store.

#### Acceptance Criteria

1. WHEN a User completes the registration form and submits valid data, THE RegisterServlet SHALL create the user account with the `email_verified` column set to false.
2. WHEN a user account is created with `email_verified` set to false, THE Email_Verification_Service SHALL generate a unique Verification_Token with a 24-hour expiry and store it in the database.
3. WHEN a Verification_Token is generated, THE Email_Verification_Service SHALL send an email to the registered address containing a clickable verification link that includes the token.
4. WHEN a User clicks the verification link with a valid and non-expired Verification_Token, THE Email_Verification_Service SHALL set the `email_verified` column to true and clear the token from the database.
5. IF a User clicks a verification link with an expired Verification_Token, THEN THE Email_Verification_Service SHALL display an error message and offer to resend a new verification email.
6. IF a User clicks a verification link with an invalid or already-used Verification_Token, THEN THE Email_Verification_Service SHALL display an error message indicating the link is invalid.
7. WHEN a User with `email_verified` set to false attempts to log in, THE LoginServlet SHALL reject the login attempt and display a message instructing the User to verify the email address first.
8. WHEN a User with `email_verified` set to false attempts to log in, THE LoginServlet SHALL provide an option to resend the verification email.
9. WHEN a User with `email_verified` set to true attempts to log in with valid credentials, THE LoginServlet SHALL proceed with normal authentication.
10. THE Email_Verification_Service SHALL use the existing OTPUtil infrastructure for sending emails.
11. THE `users` table SHALL include an `email_verified` BOOLEAN column defaulting to false, a `verification_token` VARCHAR column, and a `verification_token_expiry` TIMESTAMP column.
