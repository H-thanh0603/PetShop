package Model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ProductAdminInventoryView {
    private int productId;
    private int activeBatchCount;
    private int trackedQuantity;
    private int nearExpiryQuantity;
    private int expiredQuantity;
    private Timestamp earliestExpiryDate;
    private String earliestBatchCode;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getActiveBatchCount() {
        return activeBatchCount;
    }

    public void setActiveBatchCount(int activeBatchCount) {
        this.activeBatchCount = activeBatchCount;
    }

    public int getTrackedQuantity() {
        return trackedQuantity;
    }

    public void setTrackedQuantity(int trackedQuantity) {
        this.trackedQuantity = trackedQuantity;
    }

    public int getNearExpiryQuantity() {
        return nearExpiryQuantity;
    }

    public void setNearExpiryQuantity(int nearExpiryQuantity) {
        this.nearExpiryQuantity = nearExpiryQuantity;
    }

    public int getExpiredQuantity() {
        return expiredQuantity;
    }

    public void setExpiredQuantity(int expiredQuantity) {
        this.expiredQuantity = expiredQuantity;
    }

    public Timestamp getEarliestExpiryDate() {
        return earliestExpiryDate;
    }

    public void setEarliestExpiryDate(Timestamp earliestExpiryDate) {
        this.earliestExpiryDate = earliestExpiryDate;
    }

    public String getEarliestBatchCode() {
        return earliestBatchCode;
    }

    public void setEarliestBatchCode(String earliestBatchCode) {
        this.earliestBatchCode = earliestBatchCode;
    }

    public String getFormattedEarliestExpiryDate() {
        if (earliestExpiryDate == null) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy").format(earliestExpiryDate);
    }

    public long getDaysUntilEarliestExpiry() {
        if (earliestExpiryDate == null) {
            return Long.MAX_VALUE;
        }
        LocalDate expiry = earliestExpiryDate.toLocalDateTime().toLocalDate();
        return ChronoUnit.DAYS.between(LocalDate.now(), expiry);
    }

    public String getExpiryStatus() {
        if (expiredQuantity > 0) {
            return "expired";
        }
        if (nearExpiryQuantity > 0 || getDaysUntilEarliestExpiry() <= 30) {
            return "near-expiry";
        }
        if (activeBatchCount <= 0) {
            return "no-batch";
        }
        if (earliestExpiryDate == null) {
            return "no-expiry";
        }
        return "healthy";
    }

    public String getExpiryStatusLabel() {
        switch (getExpiryStatus()) {
            case "expired":
                return "Có hàng hết hạn";
            case "near-expiry":
                return "Sắp hết hạn";
            case "no-batch":
                return "Chưa có lô";
            case "no-expiry":
                return "Không có HSD";
            default:
                return "Ổn";
        }
    }
}
