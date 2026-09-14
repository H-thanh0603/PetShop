# inventory-operations

Act on stock health: alerts, restocks, availability.

## When to use
"sắp hết hàng", "tồn kho", "nhập thêm", "slow mover", "hết hàng",
order exceptions ("đơn lỗi", "đơn pending").

## Flow
1. `get_inventory_alerts` for low-stock/slow movers; `get_order_issues`
   for open order exceptions.
2. `stage_inventory_action` for restocks (quantity added per line, within
   the per-change restock cap).
3. Empty alert/issue lists mean nothing is flagged — report that, don't dig.

## Rules
- Restocks name concrete quantities; never stage from invented numbers.
- One line per target; a target repeated in one change is refused.
