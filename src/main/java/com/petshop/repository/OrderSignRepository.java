package com.petshop.repository;

import com.petshop.model.OrderSign;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrderSignRepository extends JpaRepository<OrderSign, Integer> {

    OrderSign findByOrderId(int orderId);

    List<OrderSign> findByUserIdOrderByCreatedAtDesc(int userId);

    @Query(value = "SELECT os.* FROM order_signs os "
            + "LEFT JOIN order_signatures osig ON os.order_id = osig.order_id "
            + "WHERE os.user_id = :userId "
            + "AND (osig.verify_status IS NULL OR osig.verify_status != 'verified') "
            + "ORDER BY os.created_at DESC",
            nativeQuery = true)
    List<OrderSign> findPendingByUserId(@Param("userId") int userId);

    @Transactional
    default boolean save(int orderId, int userId, String orderData, String orderHash,
                         String publicKey, String privateKey) {
        try {
            OrderSign sign = new OrderSign();
            sign.setOrderId(orderId);
            sign.setUserId(userId);
            sign.setOrderData(orderData);
            sign.setOrderHash(orderHash);
            sign.setPublicKey(publicKey);
            sign.setPrivateKey(privateKey);
            save(sign);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(OrderSignRepository.class)
                    .error("DB error", e);
            return false;
        }
    }

    default List<OrderSign> findByUserId(int userId) {
        return findByUserIdOrderByCreatedAtDesc(userId);
    }
}
