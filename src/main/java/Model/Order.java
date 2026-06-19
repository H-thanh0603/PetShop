package Model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.List;

public class Order {
    private int id;
    private int userId;
    private String fullname;
    private String phone;
    private String address;
    private String recipientFullname;
    private String recipientPhone;
    private String shippingAddress;
    private String customerFullname;
    private String customerPhone;
    private String note;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal shippingFee = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount;
    private String status; // Pending, Confirmed, Shipping, Delivered, Completed, Cancelled
    private String payment_method;
    private boolean payment_status;
    private String paymentTransactionStatus;
    private String paymentVerificationStatus;
    private String paymentReference;
    private String paymentVerificationMessage;
    private Timestamp paymentVerifiedAt;
    private Timestamp createdAt;
    private Timestamp statusUpdatedAt;
    private String ghnOrderId;
    private String ghnTrackingCode;
    private String ghnStatus;
    private Timestamp ghnPushedAt;
    private Timestamp ghnLastSyncAt;
    private String ghnErrorMessage;
    private List<OrderItem> items;

    public Order() {
        this.totalAmount = BigDecimal.ZERO;
    }

    public Order(int id, int userId, String fullname, String phone, String address, String note, BigDecimal totalAmount, String status, Timestamp createdAt,  String payment_method, boolean payment_status) {
        this.id = id;
        this.userId = userId;
        this.fullname = fullname;
        this.phone = phone;
        this.address = address;
        this.recipientFullname = fullname;
        this.recipientPhone = phone;
        this.shippingAddress = address;
        this.note = note;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.status = status;
        this.createdAt = createdAt;
        this.payment_method = payment_method;
        this.payment_status = payment_status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullname() { return getRecipientFullname(); }
    public void setFullname(String fullname) {
        this.fullname = fullname;
        this.recipientFullname = fullname;
    }

    public String getPhone() { return getRecipientPhone(); }
    public void setPhone(String phone) {
        this.phone = phone;
        this.recipientPhone = phone;
    }

    public String getAddress() { return getShippingAddress(); }
    public void setAddress(String address) {
        this.address = address;
        this.shippingAddress = address;
    }

    public String getRecipientFullname() {
        return hasText(recipientFullname) ? recipientFullname : fullname;
    }

    public void setRecipientFullname(String recipientFullname) {
        this.recipientFullname = recipientFullname;
        this.fullname = recipientFullname;
    }

    public String getRecipientPhone() {
        return hasText(recipientPhone) ? recipientPhone : phone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
        this.phone = recipientPhone;
    }

    public String getShippingAddress() {
        return hasText(shippingAddress) ? shippingAddress : address;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
        this.address = shippingAddress;
    }

    public String getCustomerFullname() {
        return customerFullname;
    }

    public void setCustomerFullname(String customerFullname) {
        this.customerFullname = customerFullname;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO; }
    public BigDecimal getSubtotal() { return subtotal != null ? subtotal : BigDecimal.ZERO; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO; }
    public BigDecimal getShippingFee() { return shippingFee != null ? shippingFee : BigDecimal.ZERO; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee != null ? shippingFee : BigDecimal.ZERO; }
    public BigDecimal getDiscountAmount() { return discountAmount != null ? discountAmount : BigDecimal.ZERO; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getStatusUpdatedAt() { return statusUpdatedAt; }
    public void setStatusUpdatedAt(Timestamp statusUpdatedAt) { this.statusUpdatedAt = statusUpdatedAt; }

    public String getFormattedTotalAmount() {
        DecimalFormat formatter = new DecimalFormat("###,###");
        return formatter.format(totalAmount) + "đ";
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public boolean getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(boolean payment_status) {
        this.payment_status = payment_status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getStatusLabel() {
        if (status == null) {
            return "Không xác định";
        }
        switch (status) {
            case "Awaiting Payment":
                return "Đang chờ thanh toán";
            case "Pending":
                return "Chờ xử lý";
            case "Confirmed":
                return "Đã xác nhận";
            case "Paid":
                return "Đã thanh toán";
            case "Shipping":
                return "Đang giao";
            case "Delivered":
                return "Đã giao hàng";
            case "Completed":
                return "Hoàn thành";
            case "Cancelled":
                return "Đã hủy";
            default:
                return status;
        }
    }

    public String getStatusDescription() {
        if (status == null) {
            return "Đơn hàng chưa có trạng thái.";
        }
        switch (status) {
            case "Awaiting Payment":
                return "Đang chờ thanh toán";
            case "Pending":
                return "Đơn hàng đang chờ cửa hàng xác nhận.";
            case "Confirmed":
                return "Đơn hàng đã được xác nhận và đang chuẩn bị.";
                case "Paid":
                    return "Đơn hàng đã được thanh toán";
            case "Shipping":
                return "Đơn hàng đang trên đường giao đến bạn.";
            case "Delivered":
                return "Đơn hàng đã được giao tới nơi. Vui lòng xác nhận đã nhận hàng.";
            case "Completed":
                return "Đơn hàng đã được giao thành công.";
            case "Cancelled":
                return "Đơn hàng đã bị hủy.";
            default:
                return status;
        }
    }

    public String getStatusCssClass() {
        if (status == null) {
            return "status-pending";
        }
        switch (status) {
            case "Awaiting Payment":
                return "status-awaiting-payment";
            case "Pending":
                return "status-pending";
            case "Confirmed":
                return "status-confirmed";
                case "Paid":
                    return "status-paid";
            case "Shipping":
                return "status-shipping";
            case "Delivered":
                return "status-delivered"; /* You will need to add this CSS class */
            case "Completed":
                return "status-completed";
            case "Cancelled":
                return "status-cancelled";
            default:
                return "status-pending";
        }
    }

    public boolean isCancelableByUser() {
        // Unpaid orders awaiting payment can always be cancelled (gives an exit
        // for abandoned VNPAY checkouts so they don't stay stuck forever).
        if ("Awaiting Payment".equalsIgnoreCase(status) && !payment_status) {
            return true;
        }
        if (!"Pending".equalsIgnoreCase(status) && !"Confirmed".equalsIgnoreCase(status)) {
            return false;
        }
        // Enforce 1-hour cancellation window
        if (createdAt != null) {
            long elapsedSeconds = (System.currentTimeMillis() - createdAt.getTime()) / 1000;
            return elapsedSeconds <= 3600;
        }
        return true; // if no timestamp, allow cancel
    }

    /**
     * Whether the user may re-pay this order via VNPAY without creating a
     * duplicate. Only unpaid "Awaiting Payment" VNPAY orders within a 30-minute
     * window from creation are eligible.
     */
    public boolean isRepayable() {
        if (!"Awaiting Payment".equalsIgnoreCase(status) || payment_status) {
            return false;
        }
        if (payment_method != null && !"VNPAY".equalsIgnoreCase(payment_method)) {
            return false;
        }
        if (createdAt == null) {
            return true;
        }
        long elapsedSeconds = (System.currentTimeMillis() - createdAt.getTime()) / 1000;
        return elapsedSeconds <= 1800;
    }

    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        int count = 0;
        for (OrderItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    public String getPaymentMethodLabel() {
        if (payment_method == null || payment_method.isBlank()) {
            return "Chưa xác định";
        }
        switch (payment_method.toLowerCase()) {
            case "cod":
                return "Thanh toán khi nhận hàng";
            case "vnpay":
                return "Thanh toán qua Vnpay";
            case "bank":
            case "bank_transfer":
                return "Chuyển khoản ngân hàng";
            default:
                return payment_method;
        }
    }

    public String getPaymentStatusLabel() {
        return payment_status ? "Đã thanh toán" : "Chưa thanh toán";
    }

    public String getPaymentTransactionStatus() {
        return paymentTransactionStatus;
    }

    public void setPaymentTransactionStatus(String paymentTransactionStatus) {
        this.paymentTransactionStatus = paymentTransactionStatus;
    }

    public String getPaymentVerificationStatus() {
        return paymentVerificationStatus;
    }

    public void setPaymentVerificationStatus(String paymentVerificationStatus) {
        this.paymentVerificationStatus = paymentVerificationStatus;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getPaymentVerificationMessage() {
        return paymentVerificationMessage;
    }

    public void setPaymentVerificationMessage(String paymentVerificationMessage) {
        this.paymentVerificationMessage = paymentVerificationMessage;
    }

    public Timestamp getPaymentVerifiedAt() {
        return paymentVerifiedAt;
    }

    public void setPaymentVerifiedAt(Timestamp paymentVerifiedAt) {
        this.paymentVerifiedAt = paymentVerifiedAt;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public String getPaymentFlowLabel() {
        if ("PENDING_VERIFICATION".equalsIgnoreCase(paymentTransactionStatus)) {
            return "Chờ đối soát chuyển khoản";
        }
        if ("FAILED".equalsIgnoreCase(paymentTransactionStatus)) {
            return "Đối soát thất bại";
        }
        if ("EXPIRED".equalsIgnoreCase(paymentTransactionStatus)) {
            return "Quá hạn thanh toán";
        }
        if ("VERIFIED".equalsIgnoreCase(paymentTransactionStatus)) {
            return "Đã xác nhận thanh toán";
        }
        if (payment_status) {
            return "Đã thanh toán";
        }
        return "Chưa thanh toán";
    }

    public boolean isBankTransferPayment() {
        return "bank_transfer".equalsIgnoreCase(payment_method) || "bank".equalsIgnoreCase(payment_method);
    }

    public boolean isAwaitingPaymentReview() {
        return isBankTransferPayment() && "PENDING".equalsIgnoreCase(paymentVerificationStatus);
    }

    public String getPaymentVerificationLabel() {
        if (paymentVerificationStatus == null || paymentVerificationStatus.isBlank()) {
            return payment_status ? "Đã thanh toán" : "Chưa thanh toán";
        }
        switch (paymentVerificationStatus.toUpperCase()) {
            case "PENDING":
                return "Chờ đối soát";
            case "VERIFIED":
                return "Đã xác nhận";
            case "FAILED":
                return "Đối soát lỗi";
            case "EXPIRED":
                return "Quá hạn";
            case "NOT_REQUIRED":
                return "Không yêu cầu";
            default:
                return paymentVerificationStatus;
        }
    }

    public String getPaymentVerificationCssClass() {
        if (paymentVerificationStatus == null || paymentVerificationStatus.isBlank()) {
            return payment_status ? "payment-verified" : "payment-unpaid";
        }
        switch (paymentVerificationStatus.toUpperCase()) {
            case "PENDING":
                return "payment-pending";
            case "VERIFIED":
                return "payment-verified";
            case "FAILED":
                return "payment-failed";
            case "EXPIRED":
                return "payment-expired";
            case "NOT_REQUIRED":
                return "payment-neutral";
            default:
                return "payment-unpaid";
        }
    }

    // GHN shipping fields
    public String getGhnOrderId() { return ghnOrderId; }
    public void setGhnOrderId(String ghnOrderId) { this.ghnOrderId = ghnOrderId; }

    public String getGhnTrackingCode() { return ghnTrackingCode; }
    public void setGhnTrackingCode(String ghnTrackingCode) { this.ghnTrackingCode = ghnTrackingCode; }

    public String getGhnStatus() { return ghnStatus; }
    public void setGhnStatus(String ghnStatus) { this.ghnStatus = ghnStatus; }

    public Timestamp getGhnPushedAt() { return ghnPushedAt; }
    public void setGhnPushedAt(Timestamp ghnPushedAt) { this.ghnPushedAt = ghnPushedAt; }

    public Timestamp getGhnLastSyncAt() { return ghnLastSyncAt; }
    public void setGhnLastSyncAt(Timestamp ghnLastSyncAt) { this.ghnLastSyncAt = ghnLastSyncAt; }

    public String getGhnErrorMessage() { return ghnErrorMessage; }
    public void setGhnErrorMessage(String ghnErrorMessage) { this.ghnErrorMessage = ghnErrorMessage; }
}
