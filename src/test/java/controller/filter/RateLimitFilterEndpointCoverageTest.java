package controller.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitFilterEndpointCoverageTest {

    @Test
    @DisplayName("sensitive endpoints should have explicit rate limits")
    void sensitiveEndpointsShouldHaveExplicitRateLimits() {
        RateLimitFilter filter = new RateLimitFilter();

        assertEquals(10, filter.getLimitForEndpoint("/login"));
        assertEquals(6, filter.getLimitForEndpoint("/register"));
        assertEquals(6, filter.getLimitForEndpoint("/forgot-password"));
        assertEquals(8, filter.getLimitForEndpoint("/checkout"));
        assertEquals(8, filter.getLimitForEndpoint("/api/search-autocomplete"));
        assertEquals(5, filter.getLimitForEndpoint("/add-review"));
    }
}
