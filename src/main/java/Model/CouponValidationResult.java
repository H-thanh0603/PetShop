package Model;

public class CouponValidationResult {
    private final boolean valid;
    private final Coupon coupon;
    private final String message;

    private CouponValidationResult(boolean valid, Coupon coupon, String message) {
        this.valid = valid;
        this.coupon = coupon;
        this.message = message;
    }

    public static CouponValidationResult valid(Coupon coupon) {
        return new CouponValidationResult(true, coupon, null);
    }

    public static CouponValidationResult invalid(String message) {
        return new CouponValidationResult(false, null, message);
    }

    public static CouponValidationResult empty() {
        return new CouponValidationResult(false, null, null);
    }

    public boolean isValid() {
        return valid && coupon != null;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public String getMessage() {
        return message;
    }
}
