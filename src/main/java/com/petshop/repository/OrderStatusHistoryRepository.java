package com.petshop.repository;

import com.petshop.model.OrderStatusHistory;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Integer> {

    // changedByName comes from the users JOIN (no such column) — filled below.
    @Query("SELECT h.id, h.orderId, h.oldStatus, h.newStatus, h.changedBy, u.fullname, h.changedAt "
            + "FROM OrderStatusHistory h LEFT JOIN com.petshop.model.User u ON u.id = h.changedBy "
            + "WHERE h.orderId = :orderId ORDER BY h.changedAt DESC")
    List<Object[]> findHistoryRows(@Param("orderId") int orderId);

    @Transactional
    default boolean insertHistory(int orderId, String oldStatus, String newStatus, int changedBy) {
        try {
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrderId(orderId);
            history.setOldStatus(oldStatus);
            history.setNewStatus(newStatus);
            history.setChangedBy(changedBy);
            save(history);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(OrderStatusHistoryRepository.class)
                    .error("DB error", e);
            discardFailedState();
            return false;
        }
    }

    static void discardFailedState() {
        // A failed flush poisons the persistence context for subsequent reads
        // in the same transaction; detach everything (flushed rows stay visible).
        try {
            ProductRepositoryHolder.entityManager().clear();
        } catch (Exception ignored) {
        }
    }

    @Transactional
    default List<OrderStatusHistory> getHistoryByOrderId(int orderId) {
        try {
            List<OrderStatusHistory> list = new java.util.ArrayList<>();
            for (Object[] row : findHistoryRows(orderId)) {
                OrderStatusHistory history = new OrderStatusHistory();
                history.setId(((Number) row[0]).intValue());
                history.setOrderId((Integer) row[1]);
                history.setOldStatus((String) row[2]);
                history.setNewStatus((String) row[3]);
                history.setChangedBy(((Number) row[4]).intValue());
                history.setChangedByName((String) row[5]);
                history.setChangedAt(toTimestamp(row[6]));
                list.add(history);
            }
            return list;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(OrderStatusHistoryRepository.class)
                    .error("Error fetching status history for order id={}", orderId, e);
            return List.of();
        }
    }

    static java.sql.Timestamp toTimestamp(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp;
        }
        if (value instanceof java.time.LocalDateTime localDateTime) {
            return java.sql.Timestamp.valueOf(localDateTime);
        }
        return java.sql.Timestamp.valueOf(value.toString());
    }
}
