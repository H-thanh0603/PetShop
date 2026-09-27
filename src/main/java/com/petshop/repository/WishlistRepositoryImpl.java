package com.petshop.repository;

import com.petshop.model.Product;
import com.petshop.model.WishlistEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WishlistRepositoryImpl implements WishlistRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(WishlistRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final ProductRepository productRepository;

    @Autowired
    public WishlistRepositoryImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private List<WishlistEntry> findEntries(int userId) {
        return entityManager
                .createQuery("SELECT w FROM WishlistEntry w WHERE w.userId = :userId", WishlistEntry.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    @Transactional
    public List<Product> getWishlistProductsByUserId(int userId) {
        List<Product> products = new ArrayList<>();
        try {
            for (Integer productId : getWishlistProductIdsByUserId(userId)) {
                Product product = productRepository.getProductById(productId);
                if (product != null) {
                    product.setWishlisted(true);
                    products.add(product);
                }
            }
        } catch (DataAccessException e) {
            log.error("DB error", e);
        }
        return products;
    }

    @Override
    @Transactional
    public Set<Integer> getWishlistProductIdsByUserId(int userId) {
        Set<Integer> ids = new HashSet<>();
        try {
            for (WishlistEntry entry : findEntries(userId)) {
                ids.add(entry.getProductId());
            }
        } catch (RuntimeException e) {
            log.error("Error fetching wishlist product ids for user id={}", userId, e);
        }
        return ids;
    }

    @Override
    @Transactional
    public boolean isInWishlist(int userId, int productId) {
        try {
            return !entityManager
                    .createQuery("SELECT w FROM WishlistEntry w WHERE w.userId = :userId AND w.productId = :productId",
                            WishlistEntry.class)
                    .setParameter("userId", userId)
                    .setParameter("productId", productId)
                    .setMaxResults(1)
                    .getResultList()
                    .isEmpty();
        } catch (RuntimeException e) {
            log.error("Error checking wishlist for user id={} product id={}", userId, productId, e);
        }
        return false;
    }

    @Override
    @Transactional
    public boolean addToWishlist(int userId, int productId) {
        try {
            WishlistEntry entry = new WishlistEntry();
            entry.setUserId(userId);
            entry.setProductId(productId);
            entityManager.persist(entry);
            entityManager.flush();
            return true;
        } catch (RuntimeException e) {
            log.error("Error adding to wishlist user id={} product id={}", userId, productId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean removeFromWishlist(int userId, int productId) {
        try {
            return entityManager
                    .createQuery("DELETE FROM WishlistEntry w WHERE w.userId = :userId AND w.productId = :productId")
                    .setParameter("userId", userId)
                    .setParameter("productId", productId)
                    .executeUpdate() > 0;
        } catch (RuntimeException e) {
            log.error("Error removing from wishlist user id={} product id={}", userId, productId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean toggleWishlist(int userId, int productId) {
        if (isInWishlist(userId, productId)) {
            return removeFromWishlist(userId, productId);
        }
        return addToWishlist(userId, productId);
    }

    @Override
    @Transactional
    public boolean toggleWishlistAndReturnState(int userId, int productId) throws SQLException {
        // Single-statement tx (was manual-tx in the DAO): check-then-flip atomically.
        boolean wasInWishlist = isInWishlist(userId, productId);
        int affected;
        try {
            if (wasInWishlist) {
                affected = entityManager
                        .createQuery("DELETE FROM WishlistEntry w WHERE w.userId = :userId AND w.productId = :productId")
                        .setParameter("userId", userId)
                        .setParameter("productId", productId)
                        .executeUpdate();
            } else {
                WishlistEntry entry = new WishlistEntry();
                entry.setUserId(userId);
                entry.setProductId(productId);
                entityManager.persist(entry);
                entityManager.flush();
                affected = 1;
            }
        } catch (RuntimeException e) {
            throw new SQLException("Wishlist update failed", e);
        }
        if (affected != 1) {
            throw new SQLException("Wishlist update affected " + affected + " rows.");
        }
        return !wasInWishlist;
    }
}
