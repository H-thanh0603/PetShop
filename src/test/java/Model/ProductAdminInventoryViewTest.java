package Model;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProductAdminInventoryViewTest {

    @Test
    void marksProductAsExpiredWhenExpiredQuantityExists() {
        ProductAdminInventoryView view = new ProductAdminInventoryView();
        view.setExpiredQuantity(3);
        view.setNearExpiryQuantity(5);

        assertEquals("expired", view.getExpiryStatus());
        assertEquals("Có hàng hết hạn", view.getExpiryStatusLabel());
    }

    @Test
    void marksProductAsNearExpiryWhenEarliestBatchIsWithinThirtyDays() {
        ProductAdminInventoryView view = new ProductAdminInventoryView();
        view.setNearExpiryQuantity(7);
        view.setEarliestExpiryDate(Timestamp.valueOf(LocalDate.now().plusDays(12).atStartOfDay()));

        assertEquals("near-expiry", view.getExpiryStatus());
        assertEquals("Sắp hết hạn", view.getExpiryStatusLabel());
    }

    @Test
    void marksProductAsNoBatchWhenThereAreNoTrackedBatches() {
        ProductAdminInventoryView view = new ProductAdminInventoryView();
        view.setActiveBatchCount(0);

        assertEquals("no-batch", view.getExpiryStatus());
        assertEquals("Chưa có lô", view.getExpiryStatusLabel());
    }
}
