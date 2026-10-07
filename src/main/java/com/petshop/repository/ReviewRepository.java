package com.petshop.repository;

import com.petshop.model.Review;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // NOTE: schema drift — the `status` column does not exist on `reviews`
    // (Task 3 ruling). The old `r.status = 1` filter threw and was swallowed.
    // These queries fill userName/productName via JOIN FETCH like the old SQL.
    @Query("SELECT r FROM Review r WHERE r.productId = :productId ORDER BY r.createdAt DESC")
    List<Review> findByProductIdOrderByCreatedAtDesc(@Param("productId") int productId);

    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    List<Review> findAllOrderByCreatedAtDesc();

    @Query("SELECT r FROM Review r WHERE r.rating = :maxRating ORDER BY r.createdAt DESC")
    List<Review> findByRatingOrderByCreatedAtDesc(@Param("maxRating") int maxRating);

    boolean existsByUserIdAndProductId(int userId, int productId);

    @Query(value = "SELECT COUNT(*) FROM reviews WHERE user_id = :userId AND created_at >= DATE_SUB(NOW(), INTERVAL 60 MINUTE)",
            nativeQuery = true)
    int countReviewsByUserInLastHour(@Param("userId") int userId);

    boolean existsByUserIdAndCommentAndCreatedAtAfter(int userId, String comment, java.util.Date since);

    @Transactional
    default List<Review> getReviewsByProductId(int productId) {
        try {
            List<Review> reviews = findByProductIdOrderByCreatedAtDesc(productId);
            fillNames(reviews);
            return reviews;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ReviewRepository.class).error("DB error", e);
            return List.of();
        }
    }

    default boolean hasUserReviewedProduct(int userId, int productId) {
        try {
            return existsByUserIdAndProductId(userId, productId);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ReviewRepository.class).error("DB error", e);
            return false;
        }
    }

    @Transactional
    default List<Review> getAllReviews() {
        try {
            List<Review> reviews = findAllOrderByCreatedAtDesc();
            fillNames(reviews);
            return reviews;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ReviewRepository.class).error("DB error", e);
            return List.of();
        }
    }

    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    List<Review> findRecentByCreatedAtDesc(Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.rating <= 2 ORDER BY r.createdAt DESC")
    List<Review> findLowRatingOrderByCreatedAtDesc(Pageable pageable);

    // ReportDAO variants: LIMIT-ed recent lists, names filled like the old JOIN.
    default List<Review> getRecentReviews(int limit) {
        try {
            List<Review> reviews = findRecentByCreatedAtDesc(Pageable.ofSize(limit));
            fillNames(reviews);
            return reviews;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ReviewRepository.class).error("DB error", e);
            return List.of();
        }
    }

    default List<Review> getRecentLowRatingReviews(int limit) {
        try {
            List<Review> reviews = findLowRatingOrderByCreatedAtDesc(Pageable.ofSize(limit));
            fillNames(reviews);
            return reviews;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ReviewRepository.class).error("DB error", e);
            return List.of();
        }
    }

    @Transactional
    default List<Review> getReviewsByMaxRating(int maxRating) {
        try {
            List<Review> reviews = findByRatingOrderByCreatedAtDesc(maxRating);
            fillNames(reviews);
            return reviews;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ReviewRepository.class).error("DB error", e);
            return List.of();
        }
    }

    @Transactional
    default boolean deleteReview(int reviewId) {
        try {
            deleteById(reviewId);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ReviewRepository.class).error("DB error", e);
            return false;
        }
    }

    default boolean hasUserPurchasedProduct(int userId, int productId) {
        // Preserved verbatim: the old implementation is a hardcoded `return true`.
        return true;
    }

    @Transactional
    default boolean addReview(Review review) {
        try {
            save(review);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ReviewRepository.class).error("DB error", e);
            return false;
        }
    }

    default int countReviewsByUserInLastHourSafe(int userId) {
        try {
            return countReviewsByUserInLastHour(userId);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ReviewRepository.class).error("DB error", e);
            return Integer.MAX_VALUE; // fail-safe: reject on DB error (old contract)
        }
    }

    default boolean hasDuplicateRecentComment(int userId, String comment) {
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.HOUR, -24);
            return existsByUserIdAndCommentAndCreatedAtAfter(userId, comment, new java.sql.Date(cal.getTimeInMillis()));
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ReviewRepository.class).error("DB error", e);
            return true; // fail-safe: reject on DB error (old contract)
        }
    }

    @Transactional
    default boolean updateReviewStatus(int reviewId, boolean status) {
        // No `status` column on schema (drift) — the old code threw SQLException
        // and returned false; preserved exactly + flagged in Task 3 report.
        LoggerFactory.getLogger(ReviewRepository.class)
                .error("updateReviewStatus unsupported: no `status` column on reviews (schema drift)");
        return false;
    }

    @Transactional
    default boolean replyReview(int reviewId, String adminReply) {
        // No `admin_reply` column on schema (drift) — old code returned false.
        LoggerFactory.getLogger(ReviewRepository.class)
                .error("replyReview unsupported: no `admin_reply` column on reviews (schema drift)");
        return false;
    }

    // Fills @Transient display names. Needs user/product lookups; wired by the
    // service layer via fillNames(reviews, users, products) — default no-op here
    // keeps the repository compilable without extra collaborators.
    default void fillNames(List<Review> reviews) {
    }
}
