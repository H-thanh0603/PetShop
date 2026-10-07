package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.Coupon;
import com.petshop.model.DailySalesSummary;
import com.petshop.model.Notification;
import com.petshop.model.Order;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Row-count/value assertions against the Flyway-seeded petshop_test database.
 * Seeds minimal rows through repositories, never SQL dumps (P2 spec).
 */
@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reports;

    @Autowired
    private SalesSummaryRepository salesSummaries;

    @Autowired
    private OrderRepository orders;

    @Autowired
    private ProductRepository products;

    @Autowired
    private CouponRepository coupons;

    @Autowired
    private NotificationRepository notifications;

    @Autowired
    private javax.sql.DataSource dataSource;

    private int seedUser(String username) throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO users (username, password, fullname, role) VALUES ('"
                    + username + "_" + System.nanoTime() + "', 'x', 'Report User', 'user')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private int seedOrder(int userId, String status) {
        Order order = new Order();
        order.setUserId(userId);
        order.setFullname("Report User");
        order.setPhone("090");
        order.setAddress("addr");
        order.setRecipientFullname("Report User");
        order.setRecipientPhone("090");
        order.setShippingAddress("addr");
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setStatus(status);
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return orders.saveOrder(order);
    }

    @Test
    void overviewStatsCountsSeededRows() throws Exception {
        int userId = seedUser("ovstats");
        seedOrder(userId, "Pending");
        seedOrder(userId, "Completed");

        ReportRepository.OverviewStatsView stats = reports.getOverviewStats();
        assertNotNull(stats);
        assertTrue(stats.getTotalUsers() >= 1);
        assertTrue(stats.getTotalProducts() >= 1);
        assertTrue(stats.getPendingOrders() >= 1);
        assertTrue(stats.getCompletedOrders() >= 1);
        assertNotNull(stats.getLowStockProducts());
        assertNotNull(stats.getLowRatingReviews());
    }

    @Test
    void revenueByMonthReadsTheDailyRollup() {
        Date today = Date.valueOf(LocalDate.now());
        DailySalesSummary summary = new DailySalesSummary();
        summary.setSaleDate(today);
        summary.setTotalOrders(3);
        summary.setPendingOrders(1);
        summary.setCompletedOrders(2);
        summary.setCancelledOrders(0);
        summary.setRevenue(new BigDecimal("1234500"));
        salesSummaries.save(summary);

        int year = LocalDate.now().getYear();
        var revenue = reports.getRevenueByMonth(year);
        assertTrue(revenue.stream().anyMatch(r ->
                r.getMonth() == today.toLocalDate().getMonthValue()
                && new BigDecimal("1234500").compareTo(r.getRevenue()) == 0));

        var byStatus = reports.getOrdersByMonthWithStatus(year);
        assertTrue(byStatus.stream().anyMatch(m ->
                m.getMonth() == today.toLocalDate().getMonthValue()
                && m.getTotal() >= 3 && m.getCompleted() >= 2));

        assertTrue(reports.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(reports.getCurrentMonthRevenue());
    }

    @Test
    void recentOrdersRespectTheLimit() throws Exception {
        int userId = seedUser("recent");
        for (int i = 0; i < 3; i++) {
            seedOrder(userId, "Pending");
        }
        assertEquals(3, reports.getRecentOrders(3).size());
        assertTrue(reports.getRecentOrders(2).size() <= 2);
    }

    @Test
    void completedOrdersCountMatchesSeededRows() throws Exception {
        int userId = seedUser("completed");
        int before = reports.getCompletedOrdersCount();
        seedOrder(userId, "Completed");
        seedOrder(userId, "Pending");
        assertEquals(before + 1, reports.getCompletedOrdersCount());
    }

    @Test
    void couponUsageBindsTheExactAliases() {
        Coupon coupon = new Coupon();
        coupon.setCode("REPORT_" + System.nanoTime());
        coupon.setDiscountType("percent");
        coupon.setDiscountValue(BigDecimal.valueOf(10));
        coupon.setDiscountPercent(10);
        coupon.setActive(true);
        coupon.setQuantity(50);
        coupon.setUsed(7);
        coupons.save(coupon);

        var usage = reports.getCouponUsage(10);
        var row = usage.stream()
                .filter(c -> coupon.getCode().equals(c.getCode()))
                .findFirst()
                .orElseThrow();
        assertEquals(7, row.getUsed());
        assertEquals(50, row.getQuantity());
        assertEquals(Boolean.TRUE, row.getActive());
        assertEquals("percent", row.getDiscountType());
        assertEquals(10, row.getDiscountPercent());
    }

    @Test
    void storedNotificationsBindTheExactAliases() throws Exception {
        int userId = seedUser("notif");
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle("Report test");
        notification.setMessage("stored notification row");
        notification.setType("order");
        notification.setLink("/pages/my-orders");
        notification.setRead(false);
        notifications.save(notification);

        var rows = reports.getStoredNotifications(20);
        var row = rows.stream()
                .filter(n -> "Report test".equals(n.getTitle()))
                .findFirst()
                .orElseThrow();
        assertEquals(userId, notifications.findById(row.getId()).orElseThrow().getUserId());
        assertEquals("order", row.getType());
        assertEquals(Boolean.FALSE, row.getIsRead());
        assertNotNull(row.getCreatedAt());
        assertEquals("Report User", row.getFullname());
    }
}
