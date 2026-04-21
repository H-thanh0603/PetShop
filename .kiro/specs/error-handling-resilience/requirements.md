# Requirements Document

## Introduction

This specification addresses error handling and resilience deficiencies in the PetShop e-commerce application. The current codebase creates a new database connection per request via `DriverManager.getConnection()`, uses only `e.printStackTrace()` for error reporting, lacks email retry logic, exposes raw stack traces to users when the database is unavailable, loads all orders into memory for admin filtering, executes N+1 queries in `OrderDAO.getAllOrders()`, and silently removes unavailable products from the cart without notifying the user. These issues degrade performance, observability, and user experience. This specification defines requirements to introduce connection pooling, structured logging, email failure resilience, graceful degradation, SQL-level pagination, query optimization, and cart removal notifications.

## Glossary

- **Connection_Pool**: A HikariCP-managed pool of reusable JDBC connections to the MySQL database, configured in `DBContext`
- **DBContext**: The `Context.DBContext` class responsible for providing database connections to the application
- **Logger**: An SLF4J logger instance backed by the Logback framework, used for structured log output
- **Email_Service**: The `Util.EmailUtil` class responsible for sending emails via SMTP
- **ForgotPassword_Servlet**: The `controller.auth.ForgotPasswordServlet` servlet handling password reset email flows
- **Error_Page**: A user-friendly JSP error page displayed when an unrecoverable server error occurs
- **ManageOrder_Servlet**: The `controller.admin.ManageOrderServlet` servlet that lists and filters orders for administrators
- **Order_DAO**: The `DAO.OrderDAO` data access class responsible for querying orders and order items from the database
- **Inventory_Service**: The `services.InventoryService` class responsible for refreshing cart product data and validating stock
- **Cart_Servlet**: The `controller.shop.CartServlet` servlet that displays and manages the shopping cart
- **Checkout_Servlet**: The `controller.shop.CheckoutServlet` servlet that handles the checkout flow
- **Pagination_Result**: A data structure containing a page of order records, the current page number, page size, and total record count

## Requirements

### Requirement 1: Database Connection Pooling

**User Story:** As a developer, I want database connections managed through a connection pool, so that the application handles concurrent requests efficiently and recovers from transient connection failures.

#### Acceptance Criteria

1. THE Connection_Pool SHALL maintain a maximum of 20 simultaneous connections to the MySQL database
2. WHEN a connection is requested, THE Connection_Pool SHALL return an available connection within 30 seconds
3. IF a connection request exceeds the 30-second timeout, THEN THE Connection_Pool SHALL throw a SQLException with a descriptive timeout message
4. WHILE a connection has been idle for 10 minutes, THE Connection_Pool SHALL remove the idle connection from the pool
5. WHEN the application starts, THE DBContext SHALL initialize the Connection_Pool using HikariCP with parameters read from `db.properties`
6. THE DBContext SHALL provide connections exclusively through `Connection_Pool.getConnection()` and SHALL NOT use `DriverManager.getConnection()`
7. WHEN the application shuts down, THE DBContext SHALL close the Connection_Pool and release all connections

### Requirement 2: Structured Logging Framework

**User Story:** As a developer, I want structured logging with configurable levels and file rotation, so that I can diagnose production issues without relying on `System.out` or `e.printStackTrace()`.

#### Acceptance Criteria

1. THE Application SHALL use SLF4J as the logging API and Logback as the logging implementation
2. THE Application SHALL NOT contain any calls to `e.printStackTrace()`, `System.out.println()`, or `System.err.println()` for error reporting
3. WHEN an exception occurs, THE Logger SHALL record the exception message and full stack trace at the ERROR level
4. WHEN a recoverable warning condition occurs, THE Logger SHALL record the condition at the WARN level
5. WHEN a significant application event occurs (such as startup, shutdown, or migration completion), THE Logger SHALL record the event at the INFO level
6. THE Logback configuration SHALL write log output to both the console appender and a file appender
7. THE Logback file appender SHALL rotate log files daily and retain log files for 30 days
8. THE Logback configuration SHALL be defined in a `logback.xml` file located in `src/main/resources`

### Requirement 3: Email Failure Handling with Retry

**User Story:** As a user, I want the system to retry sending my password reset email and show me a specific error message if it still fails, so that I am not left without recourse when email delivery encounters a transient failure.

#### Acceptance Criteria

1. WHEN an email send attempt fails, THE Email_Service SHALL retry the send operation up to 3 times
2. WHILE retrying an email send, THE Email_Service SHALL wait 2 seconds between each retry attempt
3. IF all 3 retry attempts fail, THEN THE Email_Service SHALL log the failure details including the recipient address, the exception message, and the number of attempts at the ERROR level
4. IF all retry attempts for a password reset email fail, THEN THE ForgotPassword_Servlet SHALL display an error message stating that the email could not be sent and providing alternative contact information (support email address and phone number)
5. WHEN an email send attempt fails on a retryable attempt, THE Email_Service SHALL log the attempt number and exception message at the WARN level

### Requirement 4: Graceful Degradation When Database is Unavailable

**User Story:** As a user, I want to see a friendly error page instead of a raw stack trace when the database is down, so that I understand the system is temporarily unavailable.

#### Acceptance Criteria

1. IF a database connection cannot be obtained, THEN THE DBContext SHALL log the connection failure details at the ERROR level and throw a SQLException with a descriptive message
2. IF a database operation fails in any servlet, THEN THE servlet SHALL catch the exception, log the error at the ERROR level, and forward the request to the Error_Page
3. THE Error_Page SHALL display a user-friendly message indicating the service is temporarily unavailable and suggesting the user try again later
4. THE Error_Page SHALL NOT display any stack trace, SQL statement, or internal system detail to the user
5. THE Error_Page SHALL be configured in `web.xml` as the default error page for HTTP 500 responses and for `java.lang.Exception`

### Requirement 5: Admin Order Pagination

**User Story:** As an administrator, I want orders displayed in paginated pages, so that the admin panel remains responsive even with a large number of orders.

#### Acceptance Criteria

1. WHEN the administrator requests the order list, THE ManageOrder_Servlet SHALL accept optional `page` and `size` query parameters
2. THE ManageOrder_Servlet SHALL default to page 1 with a page size of 20 WHEN the `page` or `size` parameters are absent
3. THE Order_DAO SHALL execute a SQL query using `LIMIT` and `OFFSET` clauses to retrieve only the requested page of orders
4. THE Order_DAO SHALL execute a SQL `COUNT` query to determine the total number of orders matching the current filter criteria
5. THE ManageOrder_Servlet SHALL pass the Pagination_Result (current page, page size, total count, and order list) to the JSP view
6. THE orders JSP view SHALL render pagination controls (previous page, next page, and page numbers) based on the Pagination_Result
7. THE ManageOrder_Servlet SHALL apply status and keyword filters at the SQL level using `WHERE` clauses instead of filtering in Java memory

### Requirement 6: Order Query Optimization (N+1 Fix)

**User Story:** As a developer, I want orders and their items fetched in a minimal number of queries, so that the order listing page loads efficiently regardless of order count.

#### Acceptance Criteria

1. WHEN fetching a page of orders with items, THE Order_DAO SHALL retrieve order items using a batch query that loads items for all orders in the page in a single SQL statement
2. THE Order_DAO SHALL NOT execute a separate `getOrderItems` query for each individual order when loading a list of orders
3. WHEN fetching a page of orders with items, THE Order_DAO SHALL execute at most 3 SQL statements: one for the order page, one for the total count, and one for all order items in the page

### Requirement 7: Cart Item Removal Notification

**User Story:** As a shopper, I want to be notified when products are removed from my cart because they became unavailable, so that I am aware of changes before proceeding to checkout.

#### Acceptance Criteria

1. WHEN the Inventory_Service removes products from the cart during a refresh, THE Inventory_Service SHALL return a list of removed product names
2. WHEN products have been removed from the cart, THE Cart_Servlet SHALL display a toast notification listing the names of the removed products
3. WHEN products have been removed from the cart during checkout entry, THE Checkout_Servlet SHALL display a toast notification listing the names of the removed products
4. THE toast notification message SHALL include the specific product names that were removed (for example: "The following products were removed from your cart because they are no longer available: Product A, Product B")
