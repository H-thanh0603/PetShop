package com.petshop.repository;

import com.petshop.model.Product;
import com.petshop.model.Promotion;
import com.petshop.model.PromotionCandidate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PromotionRepositoryImpl implements PromotionRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(PromotionRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final ProductRepository productRepository;

    @Autowired
    public PromotionRepositoryImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    @Transactional
    public List<PromotionCandidate> findActivePromotionCandidates(int productId, Timestamp now) {
        List<PromotionCandidate> result = new ArrayList<>();
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager.createNativeQuery(
                    "SELECT p.id, p.name, p.discount_type, p.discount_value, p.promotion_type, p.end_date, "
                            + "pp.sale_quantity, pp.sold_quantity FROM promotions p "
                            + "JOIN promotion_products pp ON pp.promotion_id = p.id "
                            + "WHERE pp.product_id = ? AND p.status = 'ACTIVE' "
                            + "AND ? >= p.start_date AND ? <= p.end_date")
                    .setParameter(1, productId)
                    .setParameter(2, now)
                    .setParameter(3, now)
                    .getResultList();
            for (Object[] row : rows) {
                PromotionCandidate candidate = new PromotionCandidate();
                candidate.setPromotionId(((Number) row[0]).intValue());
                candidate.setPromotionName((String) row[1]);
                candidate.setDiscountType((String) row[2]);
                candidate.setDiscountValue(row[3] == null ? BigDecimal.ZERO : new BigDecimal(row[3].toString()));
                candidate.setPromotionType((String) row[4]);
                candidate.setEndDate(toTimestamp(row[5]));
                candidate.setSaleQuantity(toInteger(row[6]));
                candidate.setSoldQuantity(toInteger(row[7]));
                result.add(candidate);
            }
        } catch (RuntimeException e) {
            log.error("Error fetching active promotion candidates for product id={}", productId, e);
        }
        return result;
    }

    @Override
    @Transactional
    public List<Product> getFlashSaleProducts(int limit) {
        List<Product> products = new ArrayList<>();
        try {
            @SuppressWarnings("unchecked")
            List<Number> ids = entityManager.createNativeQuery(
                    "SELECT pr.id FROM products pr "
                            + "JOIN promotion_products pp ON pp.product_id = pr.id "
                            + "JOIN promotions p ON p.id = pp.promotion_id "
                            + "WHERE pr.is_active = 1 AND p.status = 'ACTIVE' AND p.promotion_type = 'FLASH_SALE' "
                            + "AND NOW() BETWEEN p.start_date AND p.end_date "
                            + "AND pp.sale_quantity IS NOT NULL "
                            + "AND COALESCE(pp.sold_quantity, 0) < pp.sale_quantity "
                            + "GROUP BY pr.id ORDER BY MIN(p.end_date) ASC, pr.id DESC LIMIT " + limit)
                    .getResultList();
            for (Number id : ids) {
                Product product = productRepository.getProductById(id.intValue());
                if (product != null) {
                    products.add(product);
                }
            }
        } catch (RuntimeException e) {
            log.error("Error fetching flash sale products", e);
        }
        return products;
    }

    static final String PROMOTION_COLUMNS =
            "id, name, description, discount_type, discount_value, start_date, "
            + "end_date, status, promotion_type, created_at, updated_at";

    private Promotion mapPromotion(Object[] row) {
        // Column order follows PROMOTION_COLUMNS (never SELECT * — physical
        // order differs between databases).
        Promotion promotion = new Promotion();
        promotion.setId(((Number) row[0]).intValue());
        promotion.setName((String) row[1]);
        promotion.setDescription((String) row[2]);
        promotion.setDiscountType((String) row[3]);
        promotion.setDiscountValue(row[4] == null ? BigDecimal.ZERO : new BigDecimal(row[4].toString()));
        promotion.setStartDate(toTimestamp(row[5]));
        promotion.setEndDate(toTimestamp(row[6]));
        promotion.setStatus((String) row[7]);
        promotion.setPromotionType((String) row[8]);
        promotion.setCreatedAt(toTimestamp(row[9]));
        promotion.setUpdatedAt(toTimestamp(row[10]));
        return promotion;
    }

    private static Timestamp toTimestamp(Object value) {
        // Native queries return LocalDateTime for TIMESTAMP columns.
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp;
        }
        if (value instanceof java.time.LocalDateTime localDateTime) {
            return Timestamp.valueOf(localDateTime);
        }
        return Timestamp.valueOf(value.toString());
    }

    @Override
    @Transactional
    public List<Promotion> getAllPromotions() {
        List<Promotion> list = new ArrayList<>();
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager.createNativeQuery(
                    "SELECT p.id, p.name, p.description, p.discount_type, p.discount_value, "
                            + "p.start_date, p.end_date, p.status, p.promotion_type, p.created_at, p.updated_at, "
                            + "COUNT(pp.id) AS product_count, COALESCE(SUM(pp.sale_quantity), 0) AS total_sale_quantity, "
                            + "COALESCE(SUM(pp.sold_quantity), 0) AS total_sold_quantity "
                            + "FROM promotions p LEFT JOIN promotion_products pp ON pp.promotion_id = p.id "
                            + "GROUP BY p.id ORDER BY p.created_at DESC, p.id DESC")
                    .getResultList();
            for (Object[] row : rows) {
                Promotion promotion = mapPromotion(row);
                promotion.setSaleQuantity(toInteger(row[12]));
                promotion.setSoldQuantity(toInteger(row[13]));
                list.add(promotion);
            }
        } catch (RuntimeException e) {
            log.error("Error fetching all promotions", e);
        }
        return list;
    }

    @Override
    @Transactional
    public Promotion getPromotionById(int id) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager
                    .createNativeQuery("SELECT " + PROMOTION_COLUMNS + " FROM promotions WHERE id = ?1")
                    .setParameter(1, id)
                    .getResultList();
            if (rows.isEmpty()) {
                return null;
            }
            Promotion promotion = mapPromotion(rows.get(0));
            @SuppressWarnings("unchecked")
            List<Number> productIds = entityManager.createNativeQuery(
                    "SELECT product_id FROM promotion_products WHERE promotion_id = ? ORDER BY product_id")
                    .setParameter(1, id)
                    .getResultList();
            List<Integer> ids = new ArrayList<>();
            for (Number pid : productIds) {
                ids.add(pid.intValue());
            }
            promotion.setProductIds(ids);
            @SuppressWarnings("unchecked")
            List<Object> saleQty = entityManager.createNativeQuery(
                    "SELECT sale_quantity FROM promotion_products WHERE promotion_id = ? LIMIT 1")
                    .setParameter(1, id)
                    .getResultList();
            promotion.setSaleQuantity(saleQty.isEmpty() ? null : toInteger(saleQty.get(0)));
            return promotion;
        } catch (RuntimeException e) {
            log.error("Error fetching promotion id={}", id, e);
            return null;
        }
    }

    @Override
    @Transactional
    public int savePromotion(Promotion promotion) {
        try {
            Promotion managed;
            if (promotion.getId() > 0) {
                managed = entityManager.find(Promotion.class, promotion.getId());
                if (managed == null) {
                    return 0;
                }
                managed.setName(promotion.getName());
                managed.setDescription(promotion.getDescription());
                managed.setDiscountType(promotion.getDiscountType());
                managed.setDiscountValue(promotion.getDiscountValue() != null
                        ? promotion.getDiscountValue() : BigDecimal.ZERO);
                managed.setStartDate(promotion.getStartDate());
                managed.setEndDate(promotion.getEndDate());
                managed.setStatus(promotion.getStatus());
                managed.setPromotionType(promotion.getPromotionType());
                entityManager.flush();
            } else {
                managed = new Promotion();
                managed.setName(promotion.getName());
                managed.setDescription(promotion.getDescription());
                managed.setDiscountType(promotion.getDiscountType());
                managed.setDiscountValue(promotion.getDiscountValue() != null
                        ? promotion.getDiscountValue() : BigDecimal.ZERO);
                managed.setStartDate(promotion.getStartDate());
                managed.setEndDate(promotion.getEndDate());
                managed.setStatus(promotion.getStatus());
                managed.setPromotionType(promotion.getPromotionType());
                entityManager.persist(managed);
                entityManager.flush();
            }
            int promotionId = managed.getId();
            entityManager.createNativeQuery("DELETE FROM promotion_products WHERE promotion_id = ?")
                    .setParameter(1, promotionId)
                    .executeUpdate();
            if (promotion.getProductIds() != null) {
                for (Integer productId : promotion.getProductIds()) {
                    if (productId == null) {
                        continue;
                    }
                    entityManager.createNativeQuery(
                            "INSERT INTO promotion_products (promotion_id, product_id, sale_quantity, sold_quantity) "
                                    + "VALUES (?, ?, ?, ?)")
                            .setParameter(1, promotionId)
                            .setParameter(2, productId)
                            .setParameter(3, "FLASH_SALE".equalsIgnoreCase(promotion.getPromotionType())
                                    ? promotion.getSaleQuantity() : null)
                            .setParameter(4, "FLASH_SALE".equalsIgnoreCase(promotion.getPromotionType()) ? 0 : null)
                            .executeUpdate();
                }
            }
            return promotionId;
        } catch (RuntimeException e) {
            log.error("Error saving promotion name={}", promotion.getName(), e);
            try {
                org.springframework.transaction.interceptor.TransactionAspectSupport
                        .currentTransactionStatus().setRollbackOnly();
            } catch (Exception ignored) {
            }
            return 0;
        }
    }

    @Override
    @Transactional
    public boolean deletePromotion(int id) {
        try {
            @SuppressWarnings("unchecked")
            List<Number> used = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM order_items WHERE promotion_id = ?")
                    .setParameter(1, id)
                    .getResultList();
            if (!used.isEmpty() && used.get(0).intValue() != 0) {
                return false;
            }
            entityManager.createNativeQuery("DELETE FROM promotion_products WHERE promotion_id = ?")
                    .setParameter(1, id)
                    .executeUpdate();
            int deleted = entityManager.createNativeQuery("DELETE FROM promotions WHERE id = ?")
                    .setParameter(1, id)
                    .executeUpdate();
            return deleted > 0;
        } catch (RuntimeException e) {
            log.error("Error deleting promotion id={}", id, e);
            try {
                org.springframework.transaction.interceptor.TransactionAspectSupport
                        .currentTransactionStatus().setRollbackOnly();
            } catch (Exception ignored) {
            }
            return false;
        }
    }

    @Override
    @Transactional
    public boolean canDeletePromotion(int id) {
        try {
            @SuppressWarnings("unchecked")
            List<Number> used = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM order_items WHERE promotion_id = ?")
                    .setParameter(1, id)
                    .getResultList();
            return !used.isEmpty() && used.get(0).intValue() == 0;
        } catch (RuntimeException e) {
            log.error("Error checking delete permission for promotion id={}", id, e);
            return false;
        }
    }
}
