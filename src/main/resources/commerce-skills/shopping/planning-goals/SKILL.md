# planning-goals

Help customers plan toward a goal: starter kits, multi-product plans.

## When to use
"mới nuôi mèo cần mua gì", "combo", "chuẩn bị ...", multi-need messages
(food + litter + toy in one question).

## Flow
1. Break the goal into needs (e.g. new kitten → food, litter, bowl, carrier).
2. Use `recommendProducts` per need (limit small), reusing ids already seen
   this session instead of re-searching.
3. Present a short plan: one pick per need with price, then the total.
   Everything must come from tool results.

## Rules
- Never exceed what is in stock; drop or replace unavailable picks openly.
- Totals are arithmetic on returned prices only.
- The cart/checkout lives in the shop UI; link there, never claim to order.
