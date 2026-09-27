package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.Review;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository repository;

    @Autowired
    private javax.sql.DataSource dataSource;

    private int[] seedProductAndUser() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            long stamp = System.nanoTime();
            stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('reviewuser_" + stamp + "', 'x')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            int userId;
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                userId = keys.getInt(1);
            }
            stmt.executeUpdate("INSERT INTO products (name, price, stock, weight, is_active, reserved_quantity) VALUES ('ReviewProd_" + stamp + "', 100, 10, 0, 1, 0)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            int productId;
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                productId = keys.getInt(1);
            }
            return new int[]{userId, productId};
        }
    }

    private Review review(int userId, int productId, int rating, String comment) {
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setRating(rating);
        review.setComment(comment);
        return review;
    }

    @Test
    void addThenListByProduct() throws Exception {
        int[] ids = seedProductAndUser();
        assertTrue(repository.addReview(review(ids[0], ids[1], 5, "Great product")));
        assertEquals(1, repository.getReviewsByProductId(ids[1]).size());
    }

    @Test
    void hasUserReviewedProductAfterAdd() throws Exception {
        int[] ids = seedProductAndUser();
        assertFalse(repository.hasUserReviewedProduct(ids[0], ids[1]));
        repository.addReview(review(ids[0], ids[1], 4, "Good"));
        assertTrue(repository.hasUserReviewedProduct(ids[0], ids[1]));
    }

    @Test
    void duplicateCommentDetectedWithin24h() throws Exception {
        int[] ids = seedProductAndUser();
        repository.addReview(review(ids[0], ids[1], 5, "Same comment here"));
        assertTrue(repository.hasDuplicateRecentComment(ids[0], "Same comment here"));
    }

    @Test
    void addReviewWithNullCommentReturnsFalseOnConstraintViolation() throws Exception {
        // Contract pin: nullable comment is fine, but null product FK is not.
        // Use a nonexistent product id to trigger FK violation -> false.
        int[] ids = seedProductAndUser();
        Review bad = review(ids[0], -999, 5, "bad fk");
        assertEquals(false, repository.addReview(bad));
    }
}
