# Payment Config Checkout Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor checkout into a config-driven payment flow with externalized assets and a future-ready bank-transfer verification architecture.

**Architecture:** Add a payment transaction layer that tracks payment state separately from orders, route checkout through provider-driven status handling, and move checkout CSS/JS into dedicated assets. Centralize runtime config through one utility so deploy paths, bank info, API endpoints, and secrets do not live in JSP or scripts.

**Tech Stack:** Java Servlet/JSP, JSTL, MySQL via JDBC/HikariCP, Gradle, Bootstrap, vanilla JavaScript.

---

### Task 1: Introduce unified config access

**Files:**
- Create: `D:/Petshop2/PetShop/src/main/java/Util/AppConfig.java`
- Create: `D:/Petshop2/PetShop/src/main/resources/app.properties.example`
- Modify: `D:/Petshop2/PetShop/src/main/java/Util/SecretConfig.java`
- Modify: `D:/Petshop2/PetShop/src/main/java/Util/ShippingConfig.java`
- Modify: `D:/Petshop2/PetShop/src/main/java/DAO/DBProperties.java`
- Modify: `D:/Petshop2/PetShop/Start.bat`
- Modify: `D:/Petshop2/PetShop/README.md`

- [ ] Add a shared config loader with precedence `system property > env var > app.properties > legacy files`.
- [ ] Move bank display data, provinces API URL, and deploy settings to config-backed lookups.
- [ ] Keep backward compatibility so existing `secrets.properties` and `ship.properties` still work.

### Task 2: Add payment transaction persistence

**Files:**
- Create: `D:/Petshop2/PetShop/src/main/java/Model/PaymentTransaction.java`
- Create: `D:/Petshop2/PetShop/src/main/java/DAO/PaymentTransactionDAO.java`
- Create: `D:/Petshop2/PetShop/sql/14_payment_transactions.sql`
- Modify: `D:/Petshop2/PetShop/src/main/java/Context/DBContext.java`
- Modify: `D:/Petshop2/PetShop/src/main/java/Model/Order.java`
- Modify: `D:/Petshop2/PetShop/src/main/java/DAO/OrderDAO.java`

- [ ] Add the payment transaction model and DAO.
- [ ] Add startup-safe migration support for the new table.
- [ ] Attach latest payment transaction metadata to order reads for storefront/admin display.

### Task 3: Refactor payment providers and checkout flow

**Files:**
- Modify: `D:/Petshop2/PetShop/src/main/java/services/payment/PaymentResult.java`
- Modify: `D:/Petshop2/PetShop/src/main/java/services/payment/PaymentProvider.java`
- Modify: `D:/Petshop2/PetShop/src/main/java/services/payment/CODPaymentProvider.java`
- Modify: `D:/Petshop2/PetShop/src/main/java/services/payment/MoMoPaymentProvider.java`
- Modify: `D:/Petshop2/PetShop/src/main/java/services/payment/BankTransferPaymentProvider.java`
- Create: `D:/Petshop2/PetShop/src/main/java/services/payment/BankTransferDetails.java`
- Modify: `D:/Petshop2/PetShop/src/main/java/controller/shop/CheckoutServlet.java`

- [ ] Make bank transfer return a pending-verification flow rather than immediate paid success.
- [ ] Generate stable transfer references per order.
- [ ] Persist payment transactions during checkout and include bank-transfer response payload for the frontend.

### Task 4: Split checkout page into view + CSS + JS

**Files:**
- Create: `D:/Petshop2/PetShop/src/main/webapp/assets/css/checkout.css`
- Create: `D:/Petshop2/PetShop/src/main/webapp/assets/js/checkout.js`
- Modify: `D:/Petshop2/PetShop/src/main/webapp/pages/shop/checkout.jsp`

- [ ] Move checkout CSS out of JSP.
- [ ] Move checkout JS out of JSP.
- [ ] Pass only serialized config/state from JSP to JS.

### Task 5: Surface payment state to users and admin

**Files:**
- Modify: `D:/Petshop2/PetShop/src/main/java/controller/shop/MyOrdersServlet.java`
- Modify: `D:/Petshop2/PetShop/src/main/webapp/pages/shop/my-orders.jsp`
- Modify: `D:/Petshop2/PetShop/src/main/webapp/pages/shop/order-detail.jsp`
- Modify: `D:/Petshop2/PetShop/src/main/webapp/pages/admin/orders.jsp`
- Modify: `D:/Petshop2/PetShop/src/main/webapp/pages/admin/order-detail.jsp`

- [ ] Show payment verification status/reference where useful.
- [ ] Keep current order lifecycle intact while making bank transfer clearly “awaiting verification”.

### Task 6: Add regression tests and verify

**Files:**
- Create: `D:/Petshop2/PetShop/src/test/java/controller/shop/CheckoutServletBankTransferPendingTest.java`
- Create: `D:/Petshop2/PetShop/src/test/java/services/payment/BankTransferPaymentProviderTest.java`
- Modify: existing checkout tests only if needed for new response fields

- [ ] Add tests proving bank transfer is unpaid and pending verification.
- [ ] Run targeted Gradle tests for payment + checkout.
- [ ] Push only the intentional feature changes to `demo_new_feature`.
