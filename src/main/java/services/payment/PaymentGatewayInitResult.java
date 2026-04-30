package services.payment;

public class PaymentGatewayInitResult {
    private final boolean success;
    private final String redirectUrl;
    private final String requestId;
    private final String responseCode;
    private final String message;
    private final String rawResponse;

    private PaymentGatewayInitResult(boolean success, String redirectUrl, String requestId,
                                     String responseCode, String message, String rawResponse) {
        this.success = success;
        this.redirectUrl = redirectUrl;
        this.requestId = requestId;
        this.responseCode = responseCode;
        this.message = message;
        this.rawResponse = rawResponse;
    }

    public static PaymentGatewayInitResult success(String redirectUrl, String requestId,
                                                   String responseCode, String message,
                                                   String rawResponse) {
        return new PaymentGatewayInitResult(true, redirectUrl, requestId, responseCode, message, rawResponse);
    }

    public static PaymentGatewayInitResult failure(String responseCode, String message, String rawResponse) {
        return new PaymentGatewayInitResult(false, null, null, responseCode, message, rawResponse);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getMessage() {
        return message;
    }

    public String getRawResponse() {
        return rawResponse;
    }
}
