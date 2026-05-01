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
    private String note;
    private BigDecimal totalAmount;
    private String status; // Pending, Confirmed, Shipping, Completed, Cancelled
    private String payment_method;
    private boolean payment_status;
    private String paymentTransactionStatus;
    private String paymentVerificationStatus;
    private String paymentReference;
    private String paymentVerificationMessage;
    private Timestamp paymentVerifiedAt;
    private Timestamp createdAt;
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

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

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
            case "Pending":
                return "Chờ xử lý";
            case "Confirmed":
                return "Đã xác nhận";
            case "Shipping":
                return "Đang giao";
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
            case "Pending":
                return "Đơn hàng đang chờ cửa hàng xác nhận.";
            case "Confirmed":
                return "Đơn hàng đã được xác nhận và đang chuẩn bị.";
            case "Shipping":
                return "Đơn hàng đang trên đường giao đến bạn.";
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
            case "Pending":
                return "status-pending";
            case "Confirmed":
                return "status-confirmed";
            case "Shipping":
                return "status-shipping";
            case "Completed":
                return "status-completed";
            case "Cancelled":
                return "status-cancelled";
            default:
                return "status-pending";
        }
    }

    public boolean isCancelableByUser() {
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
            case "momo":
                return "Ví MoMo";
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

    public String getPaymentFlowLabel() {
        if ("PENDING_VERIFICATION".equalsIgnoreCase(paymentTransactionStatus)) {
            return "Chờ đối soát chuyển khoản";
        }
        if ("FAILED".equalsIgnoreCase(paymentTransactionStatus)) {
            return "Đối soát thất bại";
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
            case "NOT_REQUIRED":
                return "payment-neutral";
            default:
                return "payment-unpaid";
        }
    }
}
