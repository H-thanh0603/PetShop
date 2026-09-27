package com.petshop.repository;

import com.petshop.model.Promotion;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PromotionRepository extends JpaRepository<Promotion, Integer>, PromotionRepositoryCustom {

    @Query(value = "SELECT p.id AS promotion_id, p.name AS promotion_name, p.discount_type, p.discount_value, "
            + "p.promotion_type, p.end_date, pp.sale_quantity, pp.sold_quantity "
            + "FROM promotions p JOIN promotion_products pp ON pp.promotion_id = p.id "
            + "WHERE pp.product_id = :productId AND p.status = 'ACTIVE' "
            + "AND :now >= p.start_date AND :now <= p.end_date",
            nativeQuery = true)
    List<Object[]> findActiveCandidatesRaw(@Param("productId") int productId, @Param("now") Timestamp now);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE promotion_products SET sold_quantity = COALESCE(sold_quantity, 0) + :qty "
            + "WHERE promotion_id = :promotionId AND product_id = :productId "
            + "AND sale_quantity IS NOT NULL AND COALESCE(sold_quantity, 0) + :qty <= sale_quantity",
            nativeQuery = true)
    int reserveFlashSaleQuantity(@Param("promotionId") int promotionId, @Param("productId") int productId,
                                 @Param("qty") int quantity);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE promotion_products SET sold_quantity = GREATEST(COALESCE(sold_quantity, 0) - :qty, 0) "
            + "WHERE promotion_id = :promotionId AND product_id = :productId",
            nativeQuery = true)
    int releaseFlashSaleQuantity(@Param("promotionId") int promotionId, @Param("productId") int productId,
                                 @Param("qty") int quantity);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE promotions SET status = :status, updated_at = CURRENT_TIMESTAMP WHERE id = :id",
            nativeQuery = true)
    int setStatus(@Param("id") int id, @Param("status") String status);

    @Transactional
    default boolean updatePromotionStatus(int id, String status) {
        try {
            return setStatus(id, status) > 0;
        } catch (org.springframework.dao.DataAccessException e) {
            org.slf4j.LoggerFactory.getLogger(PromotionRepository.class)
                    .error("Error updating promotion status id={}", id, e);
            return false;
        }
    }
}
