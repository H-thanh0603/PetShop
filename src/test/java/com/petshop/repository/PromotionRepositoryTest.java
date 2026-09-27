package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.Promotion;
import com.petshop.model.PromotionCandidate;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PromotionRepositoryTest {

    @Autowired
    private PromotionRepository repository;

    @Autowired
    private ProductRepository products;

    private Promotion promotion(String name) {
        Promotion promotion = new Promotion();
        promotion.setName(name + "_" + Math.abs(System.nanoTime() % 100000));
        promotion.setDescription("desc");
        promotion.setDiscountType("PERCENT");
        promotion.setDiscountValue(BigDecimal.TEN);
        promotion.setStartDate(new Timestamp(System.currentTimeMillis() - 3600000));
        promotion.setEndDate(new Timestamp(System.currentTimeMillis() + 3600000));
        promotion.setStatus("ACTIVE");
        promotion.setPromotionType("COUPON");
        return promotion;
    }

    private int seedProduct() {
        return products.addProductAndReturnId("PromoProd_" + System.nanoTime(), null,
                BigDecimal.valueOf(100), 0, "desc", 10, 100, "cat", 0);
    }

    @Test
    void saveThenGetById() {
        Promotion promotion = promotion("SaveMe");
        promotion.setProductIds(List.of(seedProduct()));
        int id = repository.savePromotion(promotion);
        assertTrue(id > 0);
        Promotion found = repository.getPromotionById(id);
        assertNotNull(found);
        assertEquals(promotion.getName(), found.getName());
        assertEquals(1, found.getProductIds().size());
    }

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @Test
    void saveWithBadProductRollsBackEntirely() {
        // Rollback pin: mapping insert violates FK -> save returns 0 AND the
        // promotion row itself is absent in a FRESH transaction (atomic, like
        // the old manual-tx rollback). REQUIRES_NEW is used because the test
        // thread itself carries no ambient transaction here.
        Promotion promotion = promotion("BadMapping");
        promotion.setProductIds(List.of(-999));
        assertEquals(0, repository.savePromotion(promotion));
        org.springframework.transaction.support.TransactionTemplate readTx =
                new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        readTx.setPropagationBehavior(
                org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        Boolean absent = readTx.execute(status -> repository.getAllPromotions().stream()
                .noneMatch(p -> promotion.getName().equals(p.getName())));
        assertTrue(Boolean.TRUE.equals(absent));
    }

    @Test
    void updateStatusAndDelete() {
        int id = repository.savePromotion(promotion("ToDelete"));
        assertTrue(id > 0);
        assertTrue(repository.updatePromotionStatus(id, "INACTIVE"));
        assertEquals("INACTIVE", repository.getPromotionById(id).getStatus());
        assertTrue(repository.deletePromotion(id));
        org.junit.jupiter.api.Assertions.assertNull(repository.getPromotionById(id));
    }

    @Test
    void reserveAndReleaseFlashSaleQuantity() {
        Promotion promotion = promotion("Flash");
        promotion.setPromotionType("FLASH_SALE");
        int productId = seedProduct();
        promotion.setProductIds(List.of(productId));
        promotion.setSaleQuantity(10);
        int id = repository.savePromotion(promotion);
        assertTrue(id > 0);
        assertTrue(repository.reserveFlashSaleQuantity(id, productId, 3) > 0);
        assertTrue(repository.releaseFlashSaleQuantity(id, productId, 1) > 0);
        // Over-reserve beyond sale_quantity fails.
        assertEquals(0, repository.reserveFlashSaleQuantity(id, productId, 100));
    }

    @Test
    void findActiveCandidatesListsFlashPromotion() {
        Promotion promotion = promotion("Active");
        promotion.setPromotionType("FLASH_SALE");
        int productId = seedProduct();
        promotion.setProductIds(List.of(productId));
        promotion.setSaleQuantity(10);
        int id = repository.savePromotion(promotion);
        assertTrue(id > 0);
        List<PromotionCandidate> candidates =
                repository.findActivePromotionCandidates(productId, new Timestamp(System.currentTimeMillis()));
        assertTrue(candidates.stream().anyMatch(c -> c.getPromotionId() == id));
    }

    @Test
    void saveDuplicateNameReturnsZeroOnConstraintViolation() {
        // Contract pin is covered by save-failure path; names are not unique,
        // so pin the null-name NOT NULL violation instead.
        Promotion bad = promotion("x");
        bad.setName(null);
        assertEquals(0, repository.savePromotion(bad));
    }
}
