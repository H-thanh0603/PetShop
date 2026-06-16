package services;

import DAO.PromotionDAO;
import Model.Product;
import Model.ProductPricing;
import Model.PromotionCandidate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProductPricingServiceTest {

    @Test
    void choosesLowestFinalPriceAmongValidPromotions() {
        PromotionDAO promotionDAO = mock(PromotionDAO.class);
        ProductPricingService service = new ProductPricingService(promotionDAO);
        Product product = new Product();
        product.setId(10);
        product.setPrice(BigDecimal.valueOf(200_000));

        PromotionCandidate percentPromotion = new PromotionCandidate();
        percentPromotion.setPromotionId(1);
        percentPromotion.setPromotionName("Giảm 10%");
        percentPromotion.setDiscountType("PERCENT");
        percentPromotion.setDiscountValue(BigDecimal.TEN);
        percentPromotion.setPromotionType("NORMAL");

        PromotionCandidate fixedPromotion = new PromotionCandidate();
        fixedPromotion.setPromotionId(2);
        fixedPromotion.setPromotionName("Giảm 50K");
        fixedPromotion.setDiscountType("FIXED");
        fixedPromotion.setDiscountValue(BigDecimal.valueOf(50_000));
        fixedPromotion.setPromotionType("NORMAL");

        when(promotionDAO.findActivePromotionCandidates(eq(10), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(percentPromotion, fixedPromotion));

        ProductPricing pricing = service.calculatePricing(product, Timestamp.valueOf(LocalDateTime.now()));

        assertNotNull(pricing);
        assertEquals(0, BigDecimal.valueOf(150_000).compareTo(pricing.getFinalPrice()));
        assertEquals(Integer.valueOf(2), pricing.getPromotionId());
        assertEquals("Giảm 50K", pricing.getPromotionName());
        assertEquals(0, BigDecimal.valueOf(50_000).compareTo(pricing.getDiscountAmount()));
        assertEquals(25, pricing.getDiscountPercent());
    }

    @Test
    void ignoresFlashSaleWhenQuantityIsExhausted() {
        PromotionDAO promotionDAO = mock(PromotionDAO.class);
        ProductPricingService service = new ProductPricingService(promotionDAO);
        Product product = new Product();
        product.setId(11);
        product.setPrice(BigDecimal.valueOf(120_000));

        PromotionCandidate flashSale = new PromotionCandidate();
        flashSale.setPromotionId(5);
        flashSale.setPromotionName("Flash sale tối nay");
        flashSale.setDiscountType("PERCENT");
        flashSale.setDiscountValue(BigDecimal.valueOf(50));
        flashSale.setPromotionType("FLASH_SALE");
        flashSale.setSaleQuantity(3);
        flashSale.setSoldQuantity(3);

        when(promotionDAO.findActivePromotionCandidates(eq(11), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(flashSale));

        ProductPricing pricing = service.calculatePricing(product, Timestamp.valueOf(LocalDateTime.now()));

        assertNotNull(pricing);
        assertEquals(0, BigDecimal.valueOf(120_000).compareTo(pricing.getFinalPrice()));
        assertFalse(pricing.hasPromotion());
        assertNull(pricing.getPromotionId());
    }

    @Test
    void appliesLegacyDiscountWhenNoDatabasePromotionExists() {
        PromotionDAO promotionDAO = mock(PromotionDAO.class);
        ProductPricingService service = new ProductPricingService(promotionDAO);
        Product product = new Product();
        product.setId(12);
        product.setPrice(BigDecimal.valueOf(80_000));
        product.setDiscount(20);

        when(promotionDAO.findActivePromotionCandidates(eq(12), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        ProductPricing pricing = service.calculatePricing(product, Timestamp.valueOf(LocalDateTime.now()));

        assertTrue(pricing.hasPromotion());
        assertEquals("LEGACY_DISCOUNT", pricing.getPromotionType());
        assertEquals(20, pricing.getDiscountPercent());
        assertEquals(0, BigDecimal.valueOf(80_000).compareTo(pricing.getFinalPrice()));
        assertEquals(0, BigDecimal.valueOf(100_000).compareTo(pricing.getOriginalPrice()));
    }
}
