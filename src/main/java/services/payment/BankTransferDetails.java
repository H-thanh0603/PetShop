package services.payment;

import Util.AppConfig;

public class BankTransferDetails {
    private final String bankId;
    private final String displayName;
    private final String accountNumber;
    private final String accountName;
    private final String transferPrefix;
    private final String currency;

    private BankTransferDetails(String bankId, String displayName, String accountNumber,
                                String accountName, String transferPrefix, String currency) {
        this.bankId = bankId;
        this.displayName = displayName;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.transferPrefix = transferPrefix;
        this.currency = currency;
    }

    public static BankTransferDetails fromConfig() {
        return new BankTransferDetails(
                AppConfig.getOrDefault("payment.bank.id", "VPB"),
                AppConfig.getOrDefault("payment.bank.display-name", "VP Bank"),
                AppConfig.getOrDefault("payment.bank.account-number", "0368600557"),
                AppConfig.getOrDefault("payment.bank.account-name", "NGUYEN HUU THANH"),
                AppConfig.getOrDefault("payment.bank.transfer-prefix", "PETSH"),
                AppConfig.getOrDefault("payment.bank.currency", "VND")
        );
    }

    public boolean isConfigured() {
        return hasText(bankId) && hasText(accountNumber) && hasText(accountName);
    }

    public String buildTransferReference(int orderId) {
        return normalizedTransferPrefix() + orderId + sixDigitSuffix();
    }

    public String buildReservedTransferReference(int userId) {
        return normalizedTransferPrefix() + userId + sixDigitSuffix();
    }

    public String getBankId() {
        return bankId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getTransferPrefix() {
        return transferPrefix;
    }

    public String getCurrency() {
        return currency;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizedTransferPrefix() {
        String normalized = transferPrefix == null ? "" : transferPrefix.toUpperCase().replaceAll("[^A-Z0-9]", "");
        return normalized.isEmpty() ? "PETSH" : normalized;
    }

    private String sixDigitSuffix() {
        long suffix = System.currentTimeMillis() % 1_000_000L;
        return String.format("%06d", suffix);
    }
}
