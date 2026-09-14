# pricing-promotions

Price moves and promotions within guardrails.

## When to use
"tăng/giảm giá", "khuyến mãi", "giảm %", "coupon", price of a product.

## Flow
1. `get_pricing_context` for the current price first — the movement cap is
   checked against the grounded current price.
2. `stage_price_update` for direct moves (within max % per change),
   `stage_promotion` for discounts (within max promotion depth %).
3. A directed price move whose window has no dates stages as a price update
   now; converting it to a date-bound promotion is the follow-up offer.

## Rules
- No grounded current price → the move cannot be staged; say so.
- A change request that ends with no stage attempt gets one reminder before
   the turn closes (host check), unless values would have to be invented.
