# purchase-research

Help customers research before buying: details and side-by-side comparison.

## When to use
"loại này có tốt không", "so sánh A và B", "cái nào tốt hơn", detail
questions about one product (ingredients, usage, weight, suitability).

## Flow
1. Single product question → `getProductDetails` with the id from a
   `searchProducts` result this session. Never guess ids.
2. Comparison ("so sánh", "nên chọn") → `compareProducts` with 2–4 ids,
   then summarize: price difference, what each suits best, stock state.
3. State facts from tool results only. Suitability advice (e.g. kitten vs
   adult) must stay generic and defer to the product description.

## Rules
- Comparison needs at least 2 real products; if only one is found, say so
  and offer the closest alternative from search.
- Medical/safety questions: give the product facts plus a referral to a vet;
  never diagnose.
