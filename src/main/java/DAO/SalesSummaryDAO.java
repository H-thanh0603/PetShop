package DAO;

import Context.DBContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Maintains the daily_sales_summary rollup table.
 *
 * Refreshing recomputes only the recent window (orders can retroactively
 * change status for a few days: payment confirmation, cancellation...).
 * A full rebuild covers history after data fixes or schema changes.
 */
public class SalesSummaryDAO {

    private static final Logger logger = LoggerFactory.getLogger(SalesSummaryDAO.class);

    /**
     * Recomputes rollup rows for the last {@code days} days (inclusive of today).
     */
    public void refreshRecent(int days) {
        LocalDate since = LocalDate.now().minusDays(Math.max(0, days - 1L));
        String delete = "DELETE FROM daily_sales_summary WHERE sale_date >= ?";
        String insert = "INSERT INTO daily_sales_summary " +
                "(sale_date, total_orders, pending_orders, completed_orders, cancelled_orders, revenue) " +
                "SELECT DATE(createdAt), COUNT(*), " +
                "SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN status = 'Completed' THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN status = 'Cancelled' THEN 1 ELSE 0 END), " +
                "COALESCE(SUM(CASE WHEN status != 'Cancelled' THEN total_amount ELSE 0 END), 0) " +
                "FROM orders WHERE createdAt >= ? GROUP BY DATE(createdAt)";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement(delete);
                 PreparedStatement ins = conn.prepareStatement(insert)) {
                del.setDate(1, java.sql.Date.valueOf(since));
                del.executeUpdate();
                ins.setTimestamp(1, Timestamp.valueOf(since.atStartOfDay()));
                ins.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            logger.error("Error refreshing daily sales summary since {}", since, e);
        }
    }

    /**
     * Full rebuild from the orders table. Run after data fixes or when the
     * rollup is suspected to have drifted; also used to seed an empty table.
     */
    public void rebuildAll() {
        String delete = "DELETE FROM daily_sales_summary";
        String insert = "INSERT INTO daily_sales_summary " +
                "(sale_date, total_orders, pending_orders, completed_orders, cancelled_orders, revenue) " +
                "SELECT DATE(createdAt), COUNT(*), " +
                "SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN status = 'Completed' THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN status = 'Cancelled' THEN 1 ELSE 0 END), " +
                "COALESCE(SUM(CASE WHEN status != 'Cancelled' THEN total_amount ELSE 0 END), 0) " +
                "FROM orders GROUP BY DATE(createdAt)";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement(delete);
                 PreparedStatement ins = conn.prepareStatement(insert)) {
                del.executeUpdate();
                ins.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            logger.error("Error rebuilding daily sales summary", e);
        }
    }
}
