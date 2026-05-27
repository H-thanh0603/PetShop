package DAO;

import Model.BankWebhookEvent;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class BankWebhookEventDAO {

    public BankWebhookEvent findByProviderTransactionId(Connection conn, String providerTransactionId) throws Exception {
        String sql = "SELECT * FROM bank_webhook_events WHERE provider_transaction_id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, providerTransactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public int save(Connection conn, BankWebhookEvent.Status status, String providerTransactionId,
                    BigDecimal amount, String bankContent, String bankAccount,
                    Integer paymentTransactionId, String rawPayload) throws Exception {
        String sql = "INSERT INTO bank_webhook_events (" +
                "provider_transaction_id, amount, bank_content, bank_account, " +
                "payment_transaction_id, status, raw_payload, received_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, providerTransactionId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, bankContent);
            ps.setString(4, bankAccount);
            if (paymentTransactionId == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, paymentTransactionId);
            }
            ps.setString(6, status.name());
            ps.setString(7, rawPayload);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    private BankWebhookEvent map(ResultSet rs) throws Exception {
        BankWebhookEvent event = new BankWebhookEvent();
        event.setId(rs.getInt("id"));
        event.setProviderTransactionId(rs.getString("provider_transaction_id"));
        event.setAmount(rs.getBigDecimal("amount"));
        event.setBankContent(rs.getString("bank_content"));
        event.setBankAccount(rs.getString("bank_account"));
        int paymentTransactionId = rs.getInt("payment_transaction_id");
        event.setPaymentTransactionId(rs.wasNull() ? null : paymentTransactionId);
        event.setStatus(rs.getString("status"));
        event.setRawPayload(rs.getString("raw_payload"));
        event.setReceivedAt(rs.getTimestamp("received_at"));
        return event;
    }
}
