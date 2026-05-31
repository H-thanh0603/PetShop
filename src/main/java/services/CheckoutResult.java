package services;

import Model.PaymentTransaction;

public class CheckoutResult {
    private boolean success;
    private String message;
    private String paymentMethodDb;
    private PaymentTransaction paymentTransaction;
    private Integer orderId;

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
}
