package com.petshop.repository;

import com.petshop.model.DailySalesSummary;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SalesSummaryRepository extends JpaRepository<DailySalesSummary, java.sql.Date> {

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "DELETE FROM daily_sales_summary WHERE sale_date >= :since", nativeQuery = true)
    void deleteFrom(@Param("since") java.sql.Date since);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "INSERT INTO daily_sales_summary "
            + "(sale_date, total_orders, pending_orders, completed_orders, cancelled_orders, revenue) "
            + "SELECT DATE(createdAt), COUNT(*), "
            + "SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END), "
            + "SUM(CASE WHEN status = 'Completed' THEN 1 ELSE 0 END), "
            + "SUM(CASE WHEN status = 'Cancelled' THEN 1 ELSE 0 END), "
            + "COALESCE(SUM(CASE WHEN status != 'Cancelled' THEN total_amount ELSE 0 END), 0) "
            + "FROM orders WHERE createdAt >= :since GROUP BY DATE(createdAt)",
            nativeQuery = true)
    void insertWindow(@Param("since") java.sql.Timestamp since);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "DELETE FROM daily_sales_summary", nativeQuery = true)
    void deleteAllRows();

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "INSERT INTO daily_sales_summary "
            + "(sale_date, total_orders, pending_orders, completed_orders, cancelled_orders, revenue) "
            + "SELECT DATE(createdAt), COUNT(*), "
            + "SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END), "
            + "SUM(CASE WHEN status = 'Completed' THEN 1 ELSE 0 END), "
            + "SUM(CASE WHEN status = 'Cancelled' THEN 1 ELSE 0 END), "
            + "COALESCE(SUM(CASE WHEN status != 'Cancelled' THEN total_amount ELSE 0 END), 0) "
            + "FROM orders GROUP BY DATE(createdAt)",
            nativeQuery = true)
    void insertAll();

    @Transactional
    default void refreshRecent(int days) {
        LocalDate since = LocalDate.now().minusDays(Math.max(0, days - 1L));
        try {
            deleteFrom(java.sql.Date.valueOf(since));
            insertWindow(java.sql.Timestamp.valueOf(since.atStartOfDay()));
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(SalesSummaryRepository.class)
                    .error("Error refreshing daily sales summary since {}", since, e);
        }
    }

    @Transactional
    default void rebuildAll() {
        try {
            deleteAllRows();
            insertAll();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(SalesSummaryRepository.class)
                    .error("Error rebuilding daily sales summary", e);
        }
    }
}
