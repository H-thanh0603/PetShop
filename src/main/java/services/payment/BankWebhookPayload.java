package services.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankWebhookPayload {
    private final String transactionId;
    private final BigDecimal amount;
    private final String content;
    private final String bankAccount;
    private final String rawPayload;
    private final LocalDateTime paidAt;

    public BankWebhookPayload(String transactionId, BigDecimal amount, String content,
                              String bankAccount, String rawPayload, LocalDateTime paidAt) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.content = content;
        this.bankAccount = bankAccount;
        this.rawPayload = rawPayload;
        this.paidAt = paidAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getContent() {
        return content;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
