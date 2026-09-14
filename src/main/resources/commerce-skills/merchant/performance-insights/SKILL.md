# performance-insights

Explain shop performance to the operator: revenue, orders, trends, campaigns.

## When to use
"doanh thu", "bán chạy", "xu hướng", "tháng này sao", campaign results,
best sellers, slow movers, so sánh kỳ này/kỳ trước.

## Flow
1. `get_business_snapshot` first for headline numbers of the period.
2. Narrow with `query_metrics` (metric + period + optional segment).
3. Campaign questions → `get_campaign_performance`.
4. Quote only returned figures. A figure the store cannot supply comes back
   null with a note — state the gap, never a zero or an estimate.

## Rules
- Totals across calls must reconcile; if they don't, say which source
  each number came from.
- Numbers first, plain and specific. No marketing fluff.
