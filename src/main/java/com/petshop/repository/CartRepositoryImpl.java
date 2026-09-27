package com.petshop.repository;

import com.petshop.model.CartItem;
import com.petshop.model.CartRow;
import com.petshop.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CartRepositoryImpl implements CartRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(CartRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final ProductRepository productRepository;

    @Autowired
    public CartRepositoryImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private List<CartRow> findRows(int userId) {
        return entityManager.createQuery("SELECT c FROM CartRow c WHERE c.userId = :userId", CartRow.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    private CartRow findRow(int userId, int productId) {
        List<CartRow> rows = entityManager
                .createQuery("SELECT c FROM CartRow c WHERE c.userId = :userId AND c.productId = :productId",
                        CartRow.class)
                .setParameter("userId", userId)
                .setParameter("productId", productId)
                .getResultList();
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    @Transactional
    public void saveCartItem(int userId, int productId, int quantity) {
        try {
            entityManager.createNativeQuery(
                    "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE quantity = ?")
                    .setParameter(1, userId)
                    .setParameter(2, productId)
                    .setParameter(3, quantity)
                    .setParameter(4, quantity)
                    .executeUpdate();
        } catch (RuntimeException e) {
            log.error("DB error", e);
        }
    }

    @Override
    @Transactional
    public void addToCart(int userId, int productId, int quantityToAdd) {
        try {
            Product product = productRepository.getProductById(productId);
            if (product == null || product.getAvailablePurchaseQuantity() <= 0 || quantityToAdd <= 0) {
                return;
            }
            CartRow existing = findRow(userId, productId);
            if (existing != null) {
                int newQuantity = Math.min(existing.getQuantity() + quantityToAdd,
                        product.getAvailablePurchaseQuantity());
                existing.setQuantity(newQuantity);
                entityManager.merge(existing);
            } else {
                int quantityToSave = Math.min(quantityToAdd, product.getAvailablePurchaseQuantity());
                if (quantityToSave <= 0) {
                    return;
                }
                CartRow row = new CartRow();
                row.setUserId(userId);
                row.setProductId(productId);
                row.setQuantity(quantityToSave);
                entityManager.persist(row);
            }
        } catch (RuntimeException e) {
            log.error("DB error", e);
        }
    }

    @Override
    @Transactional
    public void removeFromCart(int userId, int productId) {
        try {
            entityManager.createQuery("DELETE FROM CartRow c WHERE c.userId = :userId AND c.productId = :productId")
                    .setParameter("userId", userId)
                    .setParameter("productId", productId)
                    .executeUpdate();
        } catch (RuntimeException e) {
            log.error("DB error", e);
        }
    }

    @Override
    @Transactional
    public boolean updateCartQuantity(int userId, int productId, int newQuantity) {
        try {
            Product product = productRepository.getProductById(productId);
            if (product == null || product.getAvailablePurchaseQuantity() <= 0) {
                return false;
            }
            if (newQuantity < 1) {
                newQuantity = 1;
            }
            if (newQuantity > product.getAvailablePurchaseQuantity()) {
                newQuantity = product.getAvailablePurchaseQuantity();
            }
            CartRow existing = findRow(userId, productId);
            if (existing == null) {
                return false;
            }
            existing.setQuantity(newQuantity);
            entityManager.merge(existing);
            return true;
        } catch (RuntimeException e) {
            log.error("DB error", e);
        }
        return false;
    }

    @Override
    @Transactional
    public void clearCart(int userId) {
        try {
            entityManager.createQuery("DELETE FROM CartRow c WHERE c.userId = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
        } catch (RuntimeException e) {
            log.error("DB error", e);
        }
    }

    @Override
    @Transactional
    public Map<Integer, CartItem> getCartByUserId(int userId) {
        Map<Integer, CartItem> cart = new HashMap<>();
        try {
            for (CartRow row : findRows(userId)) {
                int productId = row.getProductId();
                int quantity = row.getQuantity();
                Product product = productRepository.getProductById(productId);
                if (product == null || product.getAvailablePurchaseQuantity() <= 0) {
                    entityManager.remove(entityManager.contains(row) ? row : entityManager.merge(row));
                    continue;
                }
                int safeQuantity = Math.max(1, Math.min(quantity, product.getAvailablePurchaseQuantity()));
                if (safeQuantity != quantity) {
                    row.setQuantity(safeQuantity);
                    entityManager.merge(row);
                }
                cart.put(productId, new CartItem(product, safeQuantity));
            }
        } catch (RuntimeException e) {
            log.error("DB error", e);
        }
        return cart;
    }

    @Override
    @Transactional
    public int getTotalQuantity(int userId) {
        try {
            Object result = entityManager
                    .createQuery("SELECT COALESCE(SUM(c.quantity), 0) FROM CartRow c WHERE c.userId = :userId")
                    .setParameter("userId", userId)
                    .getSingleResult();
            return result == null ? 0 : ((Number) result).intValue();
        } catch (RuntimeException e) {
            log.error("DB error", e);
        }
        return 0;
    }

    @Override
    @Transactional
    public void syncCartFromSession(int userId, Map<Integer, CartItem> sessionCart) {
        if (sessionCart == null || sessionCart.isEmpty()) {
            return;
        }
        try {
            for (Map.Entry<Integer, CartItem> entry : sessionCart.entrySet()) {
                Product product = productRepository.getProductById(entry.getKey());
                if (product == null || product.getAvailablePurchaseQuantity() <= 0) {
                    continue;
                }
                int quantityToSync = Math.min(entry.getValue().getQuantity(),
                        product.getAvailablePurchaseQuantity());
                if (quantityToSync > 0) {
                    addToCart(userId, entry.getKey(), quantityToSync);
                }
            }
        } catch (RuntimeException e) {
            log.error("DB error", e);
        }
    }
}
