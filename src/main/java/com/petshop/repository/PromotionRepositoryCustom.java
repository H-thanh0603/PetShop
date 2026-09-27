package com.petshop.repository;

import com.petshop.model.Promotion;
import com.petshop.model.PromotionCandidate;
import com.petshop.model.Product;
import java.sql.Timestamp;
import java.util.List;

public interface PromotionRepositoryCustom {

    List<PromotionCandidate> findActivePromotionCandidates(int productId, Timestamp now);

    List<Product> getFlashSaleProducts(int limit);

    List<Promotion> getAllPromotions();

    Promotion getPromotionById(int id);

    int savePromotion(Promotion promotion);

    boolean deletePromotion(int id);

    boolean canDeletePromotion(int id);
}
