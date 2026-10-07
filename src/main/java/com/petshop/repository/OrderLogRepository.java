package com.petshop.repository;

import com.petshop.model.OrderLog;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface OrderLogRepository extends JpaRepository<OrderLog, Integer> {

    List<OrderLog> findByOrderIdOrderByCreatedAtAscIdAsc(int orderId);

    @Transactional
    default boolean insert(int orderId, String actorType, Integer actorId,
                           String action, String oldStatus, String newStatus, String note) {
        try {
            OrderLog log = new OrderLog();
            log.setOrderId(orderId);
            log.setActorType(actorType);
            log.setActorId(actorId);
            log.setAction(action);
            log.setOldStatus(oldStatus);
            log.setNewStatus(newStatus);
            log.setNote(note);
            save(log);
            return true;
        } catch (DataAccessException e) {
            OrderStatusHistoryRepository.discardFailedState();
            LoggerFactory.getLogger(OrderLogRepository.class)
                    .error("DB error", e);
            return false;
        }
    }

    @Transactional
    default List<OrderLog> getByOrderId(int orderId) {
        try {
            return findByOrderIdOrderByCreatedAtAscIdAsc(orderId);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(OrderLogRepository.class)
                    .error("Error fetching order logs for order id={}", orderId, e);
            return List.of();
        }
    }
}
