package com.petshop.repository;

import java.math.BigDecimal;
import java.util.List;

import com.petshop.model.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Admin reporting reads lifted verbatim from ReportDAO (Wave E). The Map
 * shapes became projection interfaces — the getters carry the exact names the
 * old Map keys had, so JSP EL (${item.revenue}, ${overview.totalUsers}) and
 * the hand-built chart JSON keep rendering unchanged.
 *
 * Aggregations stay aggregations; the daily_sales_summary reads hit the
 * pre-aggregated rollup and may lag up to the scheduler interval (as before).
 */
public interface ReportRepository extends JpaRepository<Order, Integer> {

    Logger log = LoggerFactory.getLogger(ReportRepository.class);

    interface OverviewStatsView {
        Integer getTotalUsers();
        Integer getTotalProducts();
        Integer getPendingOrders();
        Integer getCompletedOrders();
        Integer getLowStockProducts();
        Integer getLowRatingReviews();
    }

    interface RevenueByMonthView {
        Integer getMonth();
        BigDecimal getRevenue();
    }

    interface OrdersByStatusView {
        String getStatus();
        Integer getCount();
    }

    interface TopSellingProductView {
        Integer getProductId();
        String getProduct();
        Integer getCount();
        BigDecimal getRevenue();
    }

    interface OrdersByMonthView {
        Integer getMonth();
        Integer getPending();
        Integer getCompleted();
        Integer getCancelled();
        Integer getTotal();
    }

    interface TopCustomerView {
        Integer getUserId();
        String getFullname();
        String getEmail();
        Integer getTotalOrders();
        BigDecimal getTotalSpent();
    }

    interface CouponUsageView {
        String getCode();
        Integer getUsed();
        Integer getQuantity();
        Boolean getActive();
        String getDiscountType();
        BigDecimal getDiscountValue();
        Integer getDiscountPercent();
    }

    interface StoredNotificationView {
        Integer getId();
        String getTitle();
        String getMessage();
        String getType();
        String getLink();
        Boolean getIsRead();
        // java.util.Date (not Timestamp): Hibernate 7 materializes DATETIME as
        // LocalDateTime and JSTL fmt:formatDate needs a Date — Spring Data
        // ships the LocalDateTime→Date converter, Timestamp has none.
        java.util.Date getCreatedAt();
        String getFullname();
    }

    // ---- OverviewStats (single row of scalar subqueries) ----

    String OVERVIEW_SQL = "SELECT " +
            "(SELECT COUNT(*) FROM users WHERE role = 'user') AS total_users, " +
            "(SELECT COUNT(*) FROM products) AS total_products, " +
            "(SELECT COUNT(*) FROM orders WHERE status = 'Pending') AS pending_orders, " +
            "(SELECT COUNT(*) FROM orders WHERE status = 'Completed') AS completed_orders, " +
            "(SELECT COUNT(*) FROM products WHERE stock > 0 AND stock < 10) AS low_stock_products, " +
            "(SELECT COUNT(*) FROM reviews WHERE rating <= 2) AS low_rating_reviews";

    @Query(value = OVERVIEW_SQL, nativeQuery = true)
    OverviewStatsView overviewStatsRaw();

    default OverviewStatsView getOverviewStats() {
        try {
            return overviewStatsRaw();
        } catch (DataAccessException e) {
            log.error("Error fetching overview stats", e);
            return null;
        }
    }

    // ---- Revenue / orders by month (pre-aggregated rollup) ----

    @Query(value = "SELECT MONTH(sale_date) AS month, COALESCE(SUM(revenue), 0) AS revenue " +
            "FROM daily_sales_summary " +
            "WHERE YEAR(sale_date) = :year " +
            "GROUP BY MONTH(sale_date) " +
            "ORDER BY MONTH(sale_date)", nativeQuery = true)
    List<RevenueByMonthView> getRevenueByMonth(@Param("year") int year);

    @Query(value = "SELECT MONTH(sale_date) AS month, " +
            "SUM(pending_orders) AS pending, " +
            "SUM(completed_orders) AS completed, " +
            "SUM(cancelled_orders) AS cancelled, " +
            "SUM(total_orders) AS total " +
            "FROM daily_sales_summary " +
            "WHERE YEAR(sale_date) = :year " +
            "GROUP BY MONTH(sale_date) " +
            "ORDER BY MONTH(sale_date)", nativeQuery = true)
    List<OrdersByMonthView> getOrdersByMonthWithStatus(@Param("year") int year);

    @Query(value = "SELECT COALESCE(SUM(revenue), 0) FROM daily_sales_summary", nativeQuery = true)
    BigDecimal getTotalRevenueRaw();

    default BigDecimal getTotalRevenue() {
        try {
            BigDecimal result = getTotalRevenueRaw();
            return result != null ? result : BigDecimal.ZERO;
        } catch (DataAccessException e) {
            log.error("Error fetching total revenue", e);
            return BigDecimal.ZERO;
        }
    }

    @Query(value = "SELECT COALESCE(SUM(revenue), 0) FROM daily_sales_summary " +
            "WHERE sale_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01')", nativeQuery = true)
    BigDecimal getCurrentMonthRevenueRaw();

    default BigDecimal getCurrentMonthRevenue() {
        try {
            BigDecimal result = getCurrentMonthRevenueRaw();
            return result != null ? result : BigDecimal.ZERO;
        } catch (DataAccessException e) {
            log.error("Error fetching current month revenue", e);
            return BigDecimal.ZERO;
        }
    }

    // ---- Status / product / customer aggregations ----

    @Query(value = "SELECT status, COUNT(*) AS count FROM orders GROUP BY status ORDER BY count DESC",
            nativeQuery = true)
    List<OrdersByStatusView> getOrdersByStatus();

    @Query(value = "SELECT p.id AS product_id, p.name AS product, SUM(oi.quantity) AS count, " +
            "SUM(oi.quantity * oi.price) AS revenue " +
            "FROM order_items oi " +
            "JOIN products p ON oi.product_id = p.id " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.status != 'Cancelled' " +
            "GROUP BY p.id, p.name " +
            "ORDER BY count DESC, revenue DESC " +
            "LIMIT :lim", nativeQuery = true)
    List<TopSellingProductView> getTopSellingProducts(@Param("lim") int limit);

    @Query(value = "SELECT u.id AS user_id, u.fullname, u.email, COUNT(o.id) AS total_orders, " +
            "COALESCE(SUM(CASE WHEN o.status != 'Cancelled' THEN o.total_amount ELSE 0 END), 0) AS total_spent " +
            "FROM users u " +
            "JOIN orders o ON o.user_id = u.id " +
            "WHERE u.role = 'user' " +
            "GROUP BY u.id, u.fullname, u.email " +
            "ORDER BY total_spent DESC, total_orders DESC " +
            "LIMIT :lim", nativeQuery = true)
    List<TopCustomerView> getTopCustomers(@Param("lim") int limit);

    @Query(value = "SELECT COUNT(*) FROM orders WHERE status = 'Completed'", nativeQuery = true)
    int getCompletedOrdersCount();

    // ---- Recent orders (entity rows, like the old SELECT * + mapOrder) ----

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(PageRequest pageable);

    default List<Order> getRecentOrders(int limit) {
        try {
            return findRecentOrders(PageRequest.of(0, limit));
        } catch (DataAccessException e) {
            log.error("Error fetching recent orders limit={}", limit, e);
            return List.of();
        }
    }

    // ---- Coupons / notifications ----

    @Query(value = "SELECT code, used, quantity, is_active AS active, discount_type, discount_value, discount_percent " +
            "FROM coupons ORDER BY used DESC, quantity DESC LIMIT :lim", nativeQuery = true)
    List<CouponUsageView> getCouponUsage(@Param("lim") int limit);

    @Query(value = "SELECT n.id, n.title, n.message, n.type, n.link, n.is_read AS isRead, " +
            "n.created_at AS createdAt, u.fullname " +
            "FROM notifications n JOIN users u ON u.id = n.user_id " +
            "ORDER BY n.created_at DESC LIMIT :lim", nativeQuery = true)
    List<StoredNotificationView> getStoredNotifications(@Param("lim") int limit);
}
