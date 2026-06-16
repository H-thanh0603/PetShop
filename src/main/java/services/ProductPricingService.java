package services;

import DAO.PromotionDAO;
import Model.Product;
import Model.ProductPricing;
import Model.PromotionCandidate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class ProductPricingService {

    private final PromotionDAO promotionDAO;

    public ProductPricingService() {
        this(new PromotionDAO());
    }

    public ProductPricingService(PromotionDAO promotionDAO) {
        this.promotionDAO = promotionDAO;
    }

    public ProductPricing calculatePricing(Product product, Timestamp now) {
        if (product == null) {
            return new ProductPricing();
        }

        Timestamp effectiveNow = now != null ? now : Timestamp.valueOf(LocalDateTime.now());
        BigDecimal originalPrice = product.getLegacyOriginalPrice();
        ProductPricing bestPricing = buildFallbackPricing(product);
        bestPricing.setOriginalPrice(originalPrice);

        List<PromotionCandidate> candidates = promotionDAO.findActivePromotionCandidates(product.getId(), effectiveNow);
        for (PromotionCandidate candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            if ("FLASH_SALE".equalsIgnoreCase(candidate.getPromotionType())
                    && candidate.getRemainingFlashQuantity() <= 0) {
                continue;
            }

            BigDecimal candidateFinalPrice = calculateFinalPrice(originalPrice, candidate.getDiscountType(), candidate.getDiscountValue());
            if (candidateFinalPrice.compareTo(bestPricing.getFinalPrice()) < 0) {
                ProductPricing pricing = new ProductPricing();
                pricing.setOriginalPrice(originalPrice);
                pricing.setFinalPrice(candidateFinalPrice);
                pricing.setDiscountAmount(originalPrice.subtract(candidateFinalPrice).max(BigDecimal.ZERO));
                pricing.setDiscountPercent(calculateDiscountPercent(originalPrice, pricing.getDiscountAmount()));
                pricing.setPromotionId(candidate.getPromotionId());
                pricing.setPromotionName(candidate.getPromotionName());
                pricing.setPromotionType(candidate.getPromotionType());
                pricing.setPromotionEndTime(candidate.getEndDate());
                if ("FLASH_SALE".equalsIgnoreCase(candidate.getPromotionType())) {
                    pricing.setFlashSaleRemainingQuantity(candidate.getRemainingFlashQuantity());
                }
                bestPricing = pricing;
            }
        }
        return bestPricing;
    }

    public ProductPricing calculatePricing(java.sql.Connection conn, Product product, Timestamp now) {
        if (product == null) {
            return new ProductPricing();
        }

        Timestamp effectiveNow = now != null ? now : Timestamp.valueOf(LocalDateTime.now());
        BigDecimal originalPrice = product.getLegacyOriginalPrice();
        ProductPricing bestPricing = buildFallbackPricing(product);
        bestPricing.setOriginalPrice(originalPrice);

        List<PromotionCandidate> candidates = promotionDAO.findActivePromotionCandidates(conn, product.getId(), effectiveNow);
        for (PromotionCandidate candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            if ("FLASH_SALE".equalsIgnoreCase(candidate.getPromotionType())
                    && candidate.getRemainingFlashQuantity() <= 0) {
                continue;
            }

            BigDecimal candidateFinalPrice = calculateFinalPrice(originalPrice, candidate.getDiscountType(), candidate.getDiscountValue());
            if (candidateFinalPrice.compareTo(bestPricing.getFinalPrice()) < 0) {
                ProductPricing pricing = new ProductPricing();
                pricing.setOriginalPrice(originalPrice);
                pricing.setFinalPrice(candidateFinalPrice);
                pricing.setDiscountAmount(originalPrice.subtract(candidateFinalPrice).max(BigDecimal.ZERO));
                pricing.setDiscountPercent(calculateDiscountPercent(originalPrice, pricing.getDiscountAmount()));
                pricing.setPromotionId(candidate.getPromotionId());
                pricing.setPromotionName(candidate.getPromotionName());
                pricing.setPromotionType(candidate.getPromotionType());
                pricing.setPromotionEndTime(candidate.getEndDate());
                if ("FLASH_SALE".equalsIgnoreCase(candidate.getPromotionType())) {
                    pricing.setFlashSaleRemainingQuantity(candidate.getRemainingFlashQuantity());
                }
                bestPricing = pricing;
            }
        }
        return bestPricing;
    }

    public void applyPricing(Product product) {
        applyPricing(product, Timestamp.valueOf(LocalDateTime.now()));
    }

    public void applyPricing(Product product, Timestamp now) {
        ProductPricing pricing = calculatePricing(product, now);
        applyPricing(product, pricing);
    }

    public void applyPricing(java.sql.Connection conn, Product product, Timestamp now) {
        applyPricing(product, calculatePricing(conn, product, now));
    }

    public void applyPricing(Product product, ProductPricing pricing) {
        if (product == null) {
            return;
        }
        product.clearPromotionState();
        if (pricing == null) {
            return;
        }
        product.setPromotionOriginalPrice(pricing.getOriginalPrice());
        product.setPromotionFinalPrice(pricing.getFinalPrice());
        product.setPromotionDiscountAmount(pricing.getDiscountAmount());
        product.setPromotionDiscountPercent(pricing.getDiscountPercent());
        product.setActivePromotionId(pricing.getPromotionId());
        product.setActivePromotionName(pricing.getPromotionName());
        product.setActivePromotionType(pricing.getPromotionType());
        product.setFlashSaleRemainingQuantity(pricing.getFlashSaleRemainingQuantity());
        product.setPromotionEndTime(pricing.getPromotionEndTime());
    }

    private ProductPricing buildFallbackPricing(Product product) {
        ProductPricing pricing = new ProductPricing();
        BigDecimal originalPrice = product != null ? product.getLegacyOriginalPrice() : BigDecimal.ZERO;
        BigDecimal finalPrice = product != null && product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        pricing.setOriginalPrice(originalPrice);
        pricing.setFinalPrice(finalPrice);
        pricing.setDiscountAmount(BigDecimal.ZERO);
        pricing.setDiscountPercent(0);
        return pricing;
    }

    private BigDecimal calculateFinalPrice(BigDecimal originalPrice, String discountType, BigDecimal discountValue) {
        BigDecimal safeOriginal = originalPrice != null ? originalPrice : BigDecimal.ZERO;
        BigDecimal safeValue = discountValue != null ? discountValue : BigDecimal.ZERO;
        BigDecimal finalPrice;
        if ("PERCENT".equalsIgnoreCase(discountType)) {
            finalPrice = safeOriginal.subtract(
                    safeOriginal.multiply(safeValue).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
            );
        } else {
            finalPrice = safeOriginal.subtract(safeValue);
        }
        return finalPrice.max(BigDecimal.ZERO);
    }

    private int calculateDiscountPercent(BigDecimal originalPrice, BigDecimal discountAmount) {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0
                || discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return discountAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(originalPrice, 0, RoundingMode.HALF_UP)
                .intValue();
    }
}
