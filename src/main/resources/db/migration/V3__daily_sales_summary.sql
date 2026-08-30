-- Pre-aggregated daily sales rollup for the admin dashboard.
-- Replaces full-table GROUP BY scans over orders on every dashboard view.
-- Refreshed incrementally by SalesSummaryScheduler (last 7 days each run);
-- this migration seeds the whole history once.
CREATE TABLE daily_sales_summary (
    sale_date DATE NOT NULL PRIMARY KEY,
    total_orders INT NOT NULL DEFAULT 0,
    pending_orders INT NOT NULL DEFAULT 0,
    completed_orders INT NOT NULL DEFAULT 0,
    cancelled_orders INT NOT NULL DEFAULT 0,
    revenue DECIMAL(18,0) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO daily_sales_summary
       (sale_date, total_orders, pending_orders, completed_orders, cancelled_orders, revenue)
SELECT DATE(createdAt),
       COUNT(*),
       SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END),
       SUM(CASE WHEN status = 'Completed' THEN 1 ELSE 0 END),
       SUM(CASE WHEN status = 'Cancelled' THEN 1 ELSE 0 END),
       COALESCE(SUM(CASE WHEN status != 'Cancelled' THEN total_amount ELSE 0 END), 0)
FROM orders
GROUP BY DATE(createdAt);
