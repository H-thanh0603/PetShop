# Payment, Config, and Checkout Refactor Design

**Date:** 2026-05-01

**Status:** Draft for review

## Goal

Refactor the storefront checkout so it is easier to maintain, safer to deploy, and ready for real payment integrations later. The immediate target is:

- Keep `COD`, `MoMo`, and `Bank Transfer` visible in the UI.
- Replace the current "bank transfer succeeds immediately" behavior with a real transfer flow that creates a payment transaction and waits for later verification.
- Prepare a bank verification integration layer so future API credentials can be added without rewriting checkout logic.
- Split the large checkout page into JSP + CSS + JS assets.
- Standardize deploy and runtime configuration so environment changes do not require source-code edits.

## Non-Goals

This refactor does **not** attempt to:

- Integrate a real bank verification API yet.
- Integrate a real MoMo SDK or callback endpoint yet.
- Add image-upload proof-of-payment flow.
- Rebuild the entire storefront or admin UI.
- Change unrelated business flows outside checkout, payment state, and configuration loading.

## Current Problems

### Payment behavior

- `BankTransferPaymentProvider` currently returns success immediately and marks the order as paid, even though no real verification exists.
- `MoMoPaymentProvider` is also still mock-based.
- `CheckoutServlet` directly resolves provider behavior and order creation in one large method, which makes future integrations hard to evolve.

### Checkout maintainability

- `checkout.jsp` mixes HTML, CSS, JavaScript, address management helpers, payment logic, polling, and QR rendering in one oversized file.
- Small payment changes currently require touching a fragile mixed view.

### Deployment and environment drift

- `Start.bat` hard-codes Tomcat location and startup assumptions.
- Bank account info, provinces API base URL, and payment display values are hard-coded in JSP.
- Secret loading is split across `SecretConfig`, `ShippingConfig`, and ad hoc literals instead of one consistent config story.

## Recommended Approach

Use a configuration-driven payment architecture with a separate payment transaction record and a future-ready bank verification adapter.

This keeps checkout focused on order placement, moves payment state into its own model, and makes "real integration later" a provider concern instead of a checkout rewrite. It also lets us improve the UI structure at the same time without mixing deploy/config concerns into the view.

## Architecture

### 1. Payment domain split

Introduce a dedicated payment transaction concept alongside orders.

Each checkout attempt produces:

- an `Order`
- a `PaymentTransaction`

The order remains the fulfillment record. The payment transaction becomes the source of truth for:

- payment provider
- requested amount
- transfer reference
- provider transaction id (future)
- payment status
- verification status
- failure / audit message

This prevents `orders.payment_status` from carrying too much meaning by itself.

### 2. Provider lifecycle

`PaymentProvider` will continue to be resolved through `PaymentRegistry`, but providers will no longer all mean "charge now and return success".

Target behavior by provider:

- `COD`: create order, create transaction if needed as informational, mark unpaid and complete checkout immediately.
- `MoMo`: keep current demo behavior visible in UI, but route through the same transaction-oriented architecture so it can later become a real redirect / callback integration.
- `Bank Transfer`: create order and transaction with status `PENDING_VERIFICATION`, generate transfer reference, show bank transfer instructions, and never mark paid immediately.

### 3. Future bank verification adapter

Create a bank verification service interface that can later support:

- polling reconciliation
- webhook-based reconciliation
- provider-specific signature verification
- raw bank transaction mapping

Initial implementation will be a no-op / stub adapter behind config, but all application code will already call through this abstraction.

### 4. Checkout page separation

Split `checkout.jsp` into:

- JSP view markup
- dedicated checkout stylesheet
- dedicated checkout JavaScript

The JavaScript file will own:

- payment selection behavior
- bank transfer QR updates
- checkout AJAX submission
- lightweight polling / state refresh
- address helper browser calls already in this page

The JSP will keep only server-rendered markup and minimal data handoff.

### 5. Centralized app config

Add a unified configuration utility that can read:

- system properties
- environment variables
- optional `.properties` files

with clear precedence.

This unified config layer will power:

- bank display/account info
- bank verification placeholders
- provinces API base URL
- shipping config
- social login secrets
- Tomcat/deploy script inputs where applicable

## Data Model Design

### Existing order model

Keep existing order fields for compatibility, but change how they are written:

- `payment_method` stays
- `payment_status` becomes a derived "currently paid or not" persistence field

For bank transfer at checkout time:

- `payment_method = BANK_TRANSFER`
- `payment_status = false`
- order status remains existing checkout-compatible status, such as `Pending`

### New payment transaction table

Add a table like `payment_transactions` with fields:

- `id`
- `order_id`
- `user_id`
- `provider_key`
- `provider_display_name`
- `amount`
- `currency`
- `transfer_reference`
- `provider_transaction_id`
- `status`
- `verification_status`
- `verification_message`
- `raw_provider_payload` or `provider_metadata`
- `created_at`
- `updated_at`
- `verified_at`

Suggested status values:

- `INITIATED`
- `PENDING_VERIFICATION`
- `VERIFIED`
- `FAILED`
- `EXPIRED`

This table is the right place for future reconciliation jobs, webhook payloads, and admin inspection.

## User Flow

### COD

1. User submits checkout with `COD`.
2. Order is created.
3. Payment transaction is created in a simple non-paid state if we decide to track all payment methods uniformly.
4. Checkout returns success and redirects to order history.

### MoMo

1. User selects `MoMo`.
2. Order and payment transaction are created through the same payment orchestration layer.
3. For now the provider can stay in demo mode behind config, but it should no longer bypass the shared payment transaction flow.
4. Later this provider can switch to redirect/callback without changing checkout architecture.

### Bank transfer

1. User selects bank transfer.
2. Checkout creates the order.
3. Checkout creates a payment transaction with `PENDING_VERIFICATION`.
4. Backend generates a stable transfer reference, for example `PS<orderId><shortToken>`.
5. Response returns:
   - success state for order creation
   - bank account display info
   - transfer reference
   - payment transaction id
   - `pending verification` message
6. Frontend shows QR and transfer instructions using config-driven values.
7. User sees the order as created but unpaid until future verification updates the transaction and the order.

## Order and Payment State Rules

To avoid confusion, the system should follow these rules:

- Creating an order does not imply payment success.
- `Bank Transfer` must never mark an order paid before reconciliation.
- `MoMo` demo mode must be clearly isolated so it can later be replaced by a real provider.
- Admin/order screens should be able to distinguish:
  - order lifecycle status
  - payment lifecycle status

If needed, we will add admin-facing badges sourced from payment transactions later, but the core schema should support it now.

## Configuration Design

### New configuration buckets

Introduce clear config groups:

- `app.*`
- `payment.bank.*`
- `payment.momo.*`
- `shipping.*`
- `api.provinces.*`
- `social.google.*`
- `social.facebook.*`

### Bank configuration

Move these out of JSP:

- bank code / bank id
- account number
- account name
- QR template settings
- default currency
- transfer content prefix
- verification mode
- future API endpoint placeholder
- future API credentials placeholder

### Deploy configuration

Make `Start.bat` read deploy values from environment first, with safe fallbacks:

- `PETSHOP_TOMCAT_HOME`
- `PETSHOP_BASE_URL`
- optionally `PETSHOP_CONTEXT_PATH`

This removes machine-specific assumptions from source.

## File Structure Direction

### Payment backend

New or refactored files will likely include:

- `src/main/java/services/payment/...`
- `src/main/java/DAO/...PaymentTransaction...`
- `src/main/java/Model/...PaymentTransaction...`
- `src/main/java/Util/...AppConfig...`

### Checkout frontend

Split into:

- `src/main/webapp/pages/shop/checkout.jsp`
- `src/main/webapp/assets/css/checkout.css`
- `src/main/webapp/assets/js/checkout.js`

If checkout-specific helper fragments are useful, we can also introduce a tiny JSP include for payment data hydration.

## Error Handling

### Payment creation

- If order creation fails: return checkout JSON failure as today.
- If order is created but payment transaction cannot be recorded: rollback the transaction and fail the checkout.
- If bank transfer config is missing: fail gracefully with a user-facing message and log the exact missing config server-side.

### Future verification

- Verification adapter failures must not break storefront checkout.
- Verification errors should leave the transaction in `PENDING_VERIFICATION` or move to `FAILED` with a machine-readable message.

## Testing Strategy

### Backend

Add tests for:

- bank transfer creates `PENDING_VERIFICATION` payment transaction and leaves order unpaid
- checkout JSON includes bank transfer reference / pending message
- missing bank config produces safe failure
- config utility precedence: system property > env > properties file
- MoMo still resolves through provider registry

### Frontend

At minimum verify:

- checkout script builds request correctly
- bank transfer view renders config-driven instructions
- MoMo remains visible in UI
- JSP no longer embeds large inline payment logic

### Regression protection

Preserve existing tests around:

- checkout error JSON behavior
- invalid payment method handling
- cart and stock validation

## Migration Strategy

### Database

Add a SQL migration for the new payment transaction table.

Do not remove existing order payment columns yet. Keep backward compatibility first, then clean up only after the new flow is stable.

### Runtime

Default runtime behavior after this refactor:

- COD works as before
- MoMo remains visible and functional in current demo-compatible mode
- bank transfer no longer marks orders paid instantly

## Open Decisions Resolved

The following product decisions are now locked for this implementation:

- Keep MoMo visible.
- Bank transfer becomes the first "real flow" direction.
- There is no real banking API credential yet.
- Therefore, we will build the integration-ready architecture now, with config-driven placeholders for future verification API wiring.

## Risks and Tradeoffs

### Why not fully hide MoMo?

Because the user explicitly wants it kept. The architecture will therefore keep MoMo in the registry and UI, but isolate it so replacing demo behavior later is easy.

### Why add a payment transaction table now?

Because otherwise future verification data gets awkwardly shoved into `orders`, making the later real integration harder and riskier.

### Why split checkout in the same effort?

Because payment changes in the current monolithic checkout page would otherwise make the view harder to maintain. Splitting now reduces future cost.

## Implementation Boundary

The implementation following this design should deliver:

- checkout refactored into JSP + CSS + JS assets
- config centralized and deploy settings externalized
- bank transfer flow changed from "immediate paid success" to "pending verification"
- transaction-oriented payment architecture ready for later real API integration

It should **not** claim that the banking API is truly integrated yet, because no real provider credentials or docs exist today.
