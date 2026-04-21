package Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProductCommerceTest {

    @Test
    void formatsAverageRatingWithSingleDecimal() {
        Product product = new Product();
        product.setAverageRating(4.25);

        assertEquals("4.3", product.getFormattedAverageRating());
    }

    @Test
    void returnsZeroFormattedAverageWhenThereAreNoReviews() {
        Product product = new Product();
        product.setAverageRating(0);
        product.setReviewCount(0);

        assertEquals("0.0", product.getFormattedAverageRating());
        assertEquals(0, product.getReviewCount());
    }

    @Test
    void calculatesDiscountAmountFromOldPrice() {
        Product product = new Product();
        product.setPrice(132000);
        product.setDiscount(20);

        assertEquals(33000, Math.round(product.getDiscountAmount()));
    }
}
