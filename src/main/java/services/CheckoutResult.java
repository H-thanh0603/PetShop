package services;

import Model.PaymentTransaction;

import java.math.BigDecimal;

public class CheckoutResult {
    private boolean success;
    private String message;
    private String paymentMethodDb;
    private PaymentTransaction paymentTransaction;
    private Integer orderId;

    private BigDecimal totalAmount;
    private int shippingFee;
    private BigDecimal discount;
    private BigDecimal finalTotal;

    private String privateKeyBase64;
    private String orderHash;
    private String toolUrl;

    public CheckoutResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPaymentMethodDb() {
        return paymentMethodDb;
    }

    public void setPaymentMethodDb(String paymentMethodDb) {
        this.paymentMethodDb = paymentMethodDb;
    }

    public PaymentTransaction getPaymentTransaction() {
        return paymentTransaction;
    }

    public void setPaymentTransaction(PaymentTransaction paymentTransaction) {
        this.paymentTransaction = paymentTransaction;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public int getShippingFee() { return shippingFee; }
    public void setShippingFee(int shippingFee) { this.shippingFee = shippingFee; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getFinalTotal() { return finalTotal; }
    public void setFinalTotal(BigDecimal finalTotal) { this.finalTotal = finalTotal; }

    public String getPrivateKeyBase64() { return privateKeyBase64; }
    public void setPrivateKeyBase64(String privateKeyBase64) { this.privateKeyBase64 = privateKeyBase64; }

    public String getOrderHash() { return orderHash; }
    public void setOrderHash(String orderHash) { this.orderHash = orderHash; }

    public String getToolUrl() { return toolUrl; }
    public void setToolUrl(String toolUrl) { this.toolUrl = toolUrl; }
}
