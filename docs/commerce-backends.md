# Commerce backends: mapping PetShop systems (port of upstream `docs/backends.md`)

## Step 1 — Caller identity and credentials

- **Shopping:** the host's HTTP session holds the `User`. Every backend
  method receives the user (or guest flag) from the servlet — no route or
  tool argument carries a user id. Guest reads needing an account (orders)
  are refused in code.
- **Merchant:** `AdminMerchantAgentServlet` lives under `/admin/*`
  (AuthorizationFilter role checks). The operator string (`admin:<id>`) is
  stamped on every staged/applied change as the audit actor.
- **MCP:** `McpServlet` derives admin-ness from the session; merchant tools
  refuse non-admin callers.

## Step 2 — Multi-step flows stay in order

Promotion-before-dates, content-edit-before-read, and stage-before-apply are
enforced by gates and backend checks, not by the prompt. The approval queue
(`ai_merchant_changes`) is the durable flow state; checkout completion is
published as an app event the next shopping turn reads.

## Step 3 — Checkout completes in the host

The agent never places orders. The checkout card links to the shop's own
checkout route (`/cart`); after payment the host publishes an
`order_completed` app event. The model never sees payment credentials.

## Step 4 — Products have no variants in PetShop

PetShop rows are plain products (no family/variant matrix), so every product
id is directly usable by cart, price, and restock writes. If variants are
introduced later, add family ids + an options gate mirroring upstream
`check_listing_options`.

## Step 5 — Merchant writes map to products/promotions tables

| Change kind | Live write |
|---|---|
| PRICE_UPDATE, PROMOTION | `ProductDAO.updateProduct` with the new price |
| INVENTORY_ACTION | `ProductDAO.updateStock` |
| LISTING_UPDATE (name/description/category) | `ProductDAO.updateProduct` with edited fields |
| CAMPAIGN | refused: campaigns are not managed by this store (recorded limitation) |

## Step 6 — `None`/notes for missing figures

Unsupplied figures return notes, never zeros: email-channel revenue,
unsupported metric names, ungrouded prices (blocks the move), empty
alert/issue lists (means nothing flagged).
