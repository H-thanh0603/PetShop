package com.petshop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

/**
 * Static holder for the JPA EntityManager, used by repository default methods
 * that build dynamic native queries (product filters, advice search, related
 * products). Set by Spring at startup; null outside a Spring context.
 */
@Component
public class ProductRepositoryHolder {

    private static volatile EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        ProductRepositoryHolder.entityManager = entityManager;
    }

    public static EntityManager entityManager() {
        EntityManager em = entityManager;
        if (em == null) {
            throw new IllegalStateException("EntityManager not initialized (no Spring context)");
        }
        return em;
    }
}
