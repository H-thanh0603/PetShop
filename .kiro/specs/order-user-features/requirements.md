# Requirements Document

## Introduction

This document specifies the requirements for missing order management and user account features in the PetShop e-commerce application. The scope covers six areas: order status audit trail, order cancellation time limit, order confirmation email, change password from account page, admin action logging, and secure "Remember Me" token-based authentication. The application is built with Jakarta EE, MySQL, and uses BCrypt (via PasswordUtil) for password hashing.

## Glossary

- **System**: The PetShop web application as a whole
- **OrderDAO**: The data access object responsible for order persistence and status updates (DAO/OrderDAO.java)
- **UserDAO**: The data access object responsible for user persistence and authentication (DAO/UserDAO.java)
- **Admin_Panel**: The administrative interface used by users with the "admin" role
- **My_Account_Page**: The user-facing account management page at /my-account
- **My_Orders_Page**: The user-facing order listing and detail page at /my-orders
- **Order_Detail_Page**: The page displaying full details of a single order (admin or user variant)
- **Checkout_Service**: The servlet and logic handling order placement at /checkout
- **Email_Service**: The utility responsible for composing and sending emails (Util/EmailUtil.java)
- **Password_Service**: The utility responsible for hashing and verifying passwords using BCrypt (Util/PasswordUtil.java)
- **Auth_Service**: The login and session management logic (controller/auth/LoginServlet.java)
- **Remember_Token_Store**: The database table and DAO logic for persisting hashed remember-me tokens
- **Admin_Action_Logger**: The component responsible for recording admin write operations to the admin_action_log table
- **Status_History_Store**: The database table order_status_history and associated DAO logic for recording order status changes

## Requirements

### Requirement 1: Order Status Audit Trail

**User Story:** As an admin, I want every order status change to be recorded with the previous status, new status, who made the change, and when, so that I can audit order history and resolve disputes.

#### Acceptance Criteria

1. WHEN the OrderDAO updates an order status, THE Status_History_Store SHALL insert a record containing the order identifier, old status value, new status value, identifier of the user who triggered the change, and the timestamp of the change
2. THE System SHALL store status history records in an order_status_history table with columns: id (auto-increment primary key), order_id (foreign key to orders), old_status, new_status, changed_by (foreign key to users), and changed_at (timestamp defaulting to current time)
3. WHEN an admin views the Order_Detail_Page for a specific order, THE Admin_Panel SHALL display the complete status change history for that order in reverse chronological order, showing old status, new status, the name of the user who made the change, and the formatted timestamp
4. WHEN a user cancels an order through the My_Orders_Page, THE Status_History_Store SHALL record the cancellation with the user identifier as the changed_by value
5. IF the Status_History_Store fails to insert a history record during a status update, THEN THE OrderDAO SHALL roll back the entire status update transaction and return a failure result

### Requirement 2: Order Cancellation Time Limit

**User Story:** As a business owner, I want users to only be able to cancel orders within 1 hour of order creation, so that the fulfillment team has a reliable processing window.

#### Acceptance Criteria

1. WHEN a user requests to cancel an order and the order creation timestamp is within 3600 seconds of the current server time, THE System SHALL proceed with the cancellation and update the order status to "Cancelled"
2. WHEN a user requests to cancel an order and the order creation timestamp is more than 3600 seconds before the current server time, THE System SHALL reject the cancellation and return the message "Đã quá thời gian hủy đơn hàng."
3. WHILE an order has status "Pending" or "Confirmed", THE My_Orders_Page SHALL display the cancel button only when the order creation timestamp is within 3600 seconds of the current server time
4. THE System SHALL evaluate the cancellation time limit using the server-side timestamp comparison, not client-side time

### Requirement 3: Order Confirmation Email

**User Story:** As a customer, I want to receive a confirmation email after placing an order, so that I have a record of my purchase details.

#### Acceptance Criteria

1. WHEN the Checkout_Service successfully commits an order transaction, THE Email_Service SHALL send a confirmation email to the email address associated with the ordering user
2. THE Email_Service SHALL include the following information in the order confirmation email: order number (order id), a list of all ordered items with product name and quantity, the total payment amount, and the shipping address
3. THE Email_Service SHALL send the order confirmation email asynchronously so that the checkout response is not delayed by email delivery
4. IF the Email_Service fails to send the confirmation email, THEN THE System SHALL log the failure but SHALL NOT roll back or affect the order transaction
5. THE Email_Service SHALL compose the confirmation email as an HTML message with UTF-8 encoding, consistent with the existing email template style used in the application

### Requirement 4: Change Password When Logged In

**User Story:** As a logged-in user, I want to change my password from my account page, so that I can maintain my account security without going through the forgot-password flow.

#### Acceptance Criteria

1. WHEN a logged-in user navigates to the My_Account_Page, THE System SHALL display a "Đổi mật khẩu" section with input fields for current password, new password, and confirm new password
2. WHEN a user submits the change password form, THE Password_Service SHALL verify that the provided current password matches the stored hashed password for the logged-in user
3. IF the provided current password does not match the stored password, THEN THE System SHALL display an error message and SHALL NOT update the password
4. WHEN the current password is verified and the new password passes validation, THE Password_Service SHALL hash the new password using BCrypt and update the stored password for the user
5. THE System SHALL validate that the new password contains a minimum of 8 characters, at least one uppercase letter, at least one lowercase letter, at least one digit, and at least one special character
6. THE System SHALL validate that the new password field and the confirm new password field contain identical values before processing the update
7. IF the new password is identical to the current password, THEN THE System SHALL display an error message indicating the new password must differ from the current password
8. WHEN the password is changed successfully, THE System SHALL display a success message on the My_Account_Page

### Requirement 5: Admin Action Logging

**User Story:** As a system administrator, I want all admin write operations to be logged with the admin identity, action type, target, and timestamp, so that I can audit administrative activity.

#### Acceptance Criteria

1. THE System SHALL store admin action logs in an admin_action_log table with columns: id (auto-increment primary key), admin_id (foreign key to users), action_type (varchar describing the operation), target_type (varchar identifying the entity type), target_id (integer identifying the affected record), details (text for additional context), and created_at (timestamp defaulting to current time)
2. WHEN an admin changes an order status, THE Admin_Action_Logger SHALL insert a log record with action_type "UPDATE_ORDER_STATUS", target_type "order", the order identifier as target_id, and the old and new status values in the details field
3. WHEN an admin deletes a product, THE Admin_Action_Logger SHALL insert a log record with action_type "DELETE_PRODUCT", target_type "product", and the product identifier as target_id
4. WHEN an admin deletes a user, THE Admin_Action_Logger SHALL insert a log record with action_type "DELETE_USER", target_type "user", and the user identifier as target_id
5. WHEN an admin deletes a review, THE Admin_Action_Logger SHALL insert a log record with action_type "DELETE_REVIEW", target_type "review", and the review identifier as target_id
6. IF the Admin_Action_Logger fails to insert a log record, THEN THE System SHALL log the failure to the application error log but SHALL NOT prevent the admin operation from completing

### Requirement 6: Remember Me Secure Token

**User Story:** As a returning user, I want the "Remember Me" option to securely keep me logged in across browser sessions, so that I do not have to re-enter credentials on every visit.

#### Acceptance Criteria

1. WHEN a user logs in with the "Remember Me" option selected, THE Auth_Service SHALL generate a cryptographically secure random token of at least 32 bytes using java.security.SecureRandom
2. WHEN a remember-me token is generated, THE Remember_Token_Store SHALL store a BCrypt hash of the token in the remember_tokens table along with the user identifier and an expiration timestamp set to 7 days from the current time
3. THE System SHALL store the remember_tokens table with columns: id (auto-increment primary key), user_id (foreign key to users), token_hash (varchar storing the BCrypt-hashed token), expires_at (timestamp), and created_at (timestamp defaulting to current time)
4. WHEN a remember-me token is generated, THE Auth_Service SHALL set an HttpOnly, Secure cookie named "remember_token" containing the plain token value, with a max-age of 7 days and path set to the application context path
5. WHEN a user visits the application without an active session and a "remember_token" cookie is present, THE Auth_Service SHALL look up non-expired token records for the cookie value by verifying the cookie value against stored token hashes using BCrypt, and if a match is found, THE Auth_Service SHALL create a new authenticated session for the associated user
6. WHEN a user logs out, THE Auth_Service SHALL delete all remember_tokens records for that user and remove the "remember_token" cookie
7. WHEN a remember-me token is successfully used for auto-login, THE Auth_Service SHALL delete the consumed token record and issue a new token and cookie (token rotation) to limit the window of token reuse
8. THE Auth_Service SHALL stop storing the user email in a plain-text cookie for the "Remember Me" feature and SHALL use only the secure token mechanism
