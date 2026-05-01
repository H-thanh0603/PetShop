package Model;

public class InventoryAgingSnapshot {
    private int productId;
    private String productName;
    private int freshQuantity;
    private int oneWeekQuantity;
    private int oneMonthQuantity;
    private int fourMonthQuantity;
    private int nearExpiryQuantity;
    private int expiredQuantity;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getFreshQuantity() {
        return freshQuantity;
    }

    public void setFreshQuantity(int freshQuantity) {
        this.freshQuantity = freshQuantity;
    }

    public int getOneWeekQuantity() {
        return oneWeekQuantity;
    }

    public void setOneWeekQuantity(int oneWeekQuantity) {
        this.oneWeekQuantity = oneWeekQuantity;
    }

    public int getOneMonthQuantity() {
        return oneMonthQuantity;
    }

    public void setOneMonthQuantity(int oneMonthQuantity) {
        this.oneMonthQuantity = oneMonthQuantity;
    }

    public int getFourMonthQuantity() {
        return fourMonthQuantity;
    }

    public void setFourMonthQuantity(int fourMonthQuantity) {
        this.fourMonthQuantity = fourMonthQuantity;
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
}
