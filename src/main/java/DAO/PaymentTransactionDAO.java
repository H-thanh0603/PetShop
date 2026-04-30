package DAO;

import Context.DBContext;
import Model.PaymentTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PaymentTransactionDAO {

    private PaymentTransaction map(ResultSet rs) throws Exception {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setId(rs.getInt("id"));
        tx.setOrderId(rs.getInt("order_id"));
        tx.setUserId(rs.getInt("user_id"));
        tx.setProvider(rs.getString("provider"));
        tx.setProviderOrderId(rs.getString("provider_order_id"));
        tx.setRequestId(rs.getString("request_id"));
        tx.setAmount(rs.getDouble("amount"));
        int couponId = rs.getInt("coupon_id");
        tx.setCouponId(rs.wasNull() ? null : couponId);
        tx.setDiscountReserved(rs.getBoolean("discount_reserved"));
        tx.setStatus(rs.getString("status"));
        tx.setPaymentToken(rs.getString("payment_token"));
        tx.setProviderTransactionId(rs.getString("provider_transaction_id"));
        tx.setResponseCode(rs.getString("response_code"));
        tx.setProviderMessage(rs.getString("provider_message"));
        tx.setRedirectUrl(rs.getString("redirect_url"));
        tx.setRawPayload(rs.getString("raw_payload"));
        tx.setCreatedAt(rs.getTimestamp("created_at"));
        tx.setUpdatedAt(rs.getTimestamp("updated_at"));
        tx.setCompletedAt(rs.getTimestamp("completed_at"));
        return tx;
    }

    public int create(Connection conn, PaymentTransaction tx) throws Exception {
        String sql = """
            INSERT INTO payment_transactions (
                order_id, user_id, provider, provider_order_id, request_id,
                amount, coupon_id, discount_reserved, status, payment_token,
                provider_transaction_id, response_code, provider_message,
                redirect_url, raw_payload, completed_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tx.getOrderId());
            ps.setInt(2, tx.getUserId());
            ps.setString(3, tx.getProvider());
            ps.setString(4, tx.getProviderOrderId());
            ps.setString(5, tx.getRequestId());
            ps.setDouble(6, tx.getAmount());
            if (tx.getCouponId() == null) {
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(7, tx.getCouponId());
            }
            ps.setBoolean(8, tx.isDiscountReserved());
            ps.setString(9, tx.getStatus());
            ps.setString(10, tx.getPaymentToken());
            ps.setString(11, tx.getProviderTransactionId());
            ps.setString(12, tx.getResponseCode());
            ps.setString(13, tx.getProviderMessage());
            ps.setString(14, tx.getRedirectUrl());
            ps.setString(15, tx.getRawPayload());
            ps.setTimestamp(16, tx.getCompletedAt());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    public PaymentTransaction getByProviderOrderId(String providerOrderId) {
        try (Connection conn = DBContext.getConnection()) {
            return getByProviderOrderId(conn, providerOrderId, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public PaymentTransaction getByProviderOrderIdForUpdate(Connection conn, String providerOrderId) throws Exception {
        return getByProviderOrderId(conn, providerOrderId, true);
    }

    private PaymentTransaction getByProviderOrderId(Connection conn, String providerOrderId, boolean forUpdate) throws Exception {
        String sql = "SELECT * FROM payment_transactions WHERE provider_order_id = ?"
                + (forUpdate ? " FOR UPDATE" : "");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, providerOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }

        return null;
    }

    public List<PaymentTransaction> getExpiredPendingTransactions(Connection conn, int userId, Timestamp before) throws Exception {
        List<PaymentTransaction> result = new ArrayList<>();
        String sql = """
            SELECT *
            FROM payment_transactions
            WHERE user_id = ?
              AND status = 'PENDING'
              AND created_at < ?
            FOR UPDATE
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, before);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }

        return result;
    }

    public boolean updateGatewayInit(Connection conn, int transactionId, String requestId,
                                     String redirectUrl, String responseCode, String providerMessage,
                                     String rawPayload) throws Exception {
        String sql = """
            UPDATE payment_transactions
            SET request_id = ?, redirect_url = ?, response_code = ?, provider_message = ?, raw_payload = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, requestId);
            ps.setString(2, redirectUrl);
            ps.setString(3, responseCode);
            ps.setString(4, providerMessage);
            ps.setString(5, rawPayload);
            ps.setInt(6, transactionId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(Connection conn, int transactionId, String status, String paymentToken,
                                String providerTransactionId, String responseCode, String providerMessage,
                                String redirectUrl, String rawPayload, Timestamp completedAt) throws Exception {
        String sql = """
            UPDATE payment_transactions
            SET status = ?,
                payment_token = ?,
                provider_transaction_id = ?,
                response_code = ?,
                provider_message = ?,
                redirect_url = ?,
                raw_payload = ?,
                completed_at = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, paymentToken);
            ps.setString(3, providerTransactionId);
            ps.setString(4, responseCode);
            ps.setString(5, providerMessage);
            ps.setString(6, redirectUrl);
            ps.setString(7, rawPayload);
            ps.setTimestamp(8, completedAt);
            ps.setInt(9, transactionId);
            return ps.executeUpdate() > 0;
        }
    }
}
