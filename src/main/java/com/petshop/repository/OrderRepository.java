package com.petshop.repository;

import com.petshop.model.Order;
import com.petshop.model.OrderItem;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrderRepository extends JpaRepository<Order, Integer>, OrderRepositoryCustom {

    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.id = :id")
    Order findByIdWithUser(@Param("id") int orderId);

    @Query("SELECT o FROM Order o JOIN FETCH o.user ORDER BY o.createdAt DESC")
    List<Order> findAllWithUser();

    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithUser(@Param("userId") int userId);

    @Query("SELECT i FROM OrderItem i WHERE i.orderId = :orderId")
    List<OrderItem> findItemsByOrderId(@Param("orderId") int orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Order findByIdForUpdate(@Param("id") int orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.userId = :userId")
    Order findByIdAndUserIdForUpdate(@Param("id") int orderId, @Param("userId") int userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'Pending'")
    int countPendingNative();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.status = 'Pending'")
    int countPendingByUserNative(@Param("userId") int userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.status = 'Completed'")
    int countCompletedByUserNative(@Param("userId") int userId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.userId = :userId AND o.status = 'Completed'")
    BigDecimal totalSpentByUserNative(@Param("userId") int userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId")
    int countByUserNative(@Param("userId") int userId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o "
            + "WHERE DATE(o.createdAt) = CURRENT_DATE AND o.status != 'Cancelled'")
    BigDecimal todayRevenueNative();

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :id")
    int setStatusNative(@Param("id") int orderId, @Param("status") String status);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Order o SET o.status = 'Paid', o.payment_status = TRUE WHERE o.id = :id")
    int markOrderAsPaidNative(@Param("id") int orderId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Order o SET o.status = :status, o.payment_method = :method, o.payment_status = :paid WHERE o.id = :id")
    int markOnlinePaymentNative(@Param("id") int orderId, @Param("status") String status,
                                @Param("method") String paymentMethod, @Param("paid") boolean paid);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE orders SET payment_status = :paid WHERE id = :id", nativeQuery = true)
    int setPaymentStatusNative(@Param("id") int orderId, @Param("paid") boolean paid);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE orders SET status = :status WHERE id = :id AND status = :expected", nativeQuery = true)
    int markAwaitingPaidNative(@Param("id") int orderId, @Param("status") String status,
                               @Param("expected") String expectedStatus);

    @Query("SELECT o FROM Order o WHERE o.status = 'Delivered' AND o.statusUpdatedAt < :cutoff")
    List<Order> findDeliveredBefore(@Param("cutoff") java.sql.Timestamp cutoff);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE orders SET payment_status = :status WHERE id = :id", nativeQuery = true)
    int setPaymentStatusStringNative(@Param("id") int orderId, @Param("status") String status);
}
