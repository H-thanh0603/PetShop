package DAO;

import Context.DBContext;
import Util.AppConfig;
import Model.Order;
import Model.PaymentTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentTransactionDAO {
    private static final Logger log = LoggerFactory.getLogger(PaymentTransactionDAO.class);

    public int save(Connection conn, PaymentTransaction transaction) throws Exception {
        String sql = "INSERT INTO payment_transactions (" +
                "order_id, user_id, provider_key, provider_display_name, amount, currency, " +
                "transfer_reference, provider_transaction_id, status, verification_status, " +
                "verification_message, provider_metadata, created_at, updated_at, verified_at, expires_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, transaction.getOrderId());
            ps.setInt(2, transaction.getUserId());
            ps.setString(3, transaction.getProviderKey());
            ps.setString(4, transaction.getProviderDisplayName());
            ps.setBigDecimal(5, transaction.getAmount());
            ps.setString(6, transaction.getCurrency());
            ps.setString(7, transaction.getTransferReference());
            ps.setString(8, transaction.getProviderTransactionId());
            ps.setString(9, transaction.getStatus());
            ps.setString(10, transaction.getVerificationStatus());
            ps.setString(11, transaction.getVerificationMessage());
            ps.setString(12, transaction.getProviderMetadata());
            ps.setTimestamp(13, transaction.getCreatedAt());
            ps.setTimestamp(14, transaction.getUpdatedAt());
            ps.setTimestamp(15, transaction.getVerifiedAt());
            ps.setTimestamp(16, transaction.getExpiresAt());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public PaymentTransaction getLatestByOrderId(Connection conn, int orderId) throws Exception {
        String sql = "SELECT * FROM payment_transactions WHERE order_id = ? ORDER BY created_at DESC, id DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public PaymentTransaction getLatestByOrderIdForUpdate(Connection conn, int orderId) throws Exception {
        String sql = "SELECT * FROM payment_transactions WHERE order_id = ? ORDER BY created_at DESC, id DESC LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public boolean updateVerificationStatus(Connection conn, int transactionId, String transactionStatus,
                                            String verificationStatus, String verificationMessage,
                                            Timestamp updatedAt, Timestamp verifiedAt) throws Exception {
        String sql = "UPDATE payment_transactions " +
                "SET status = ?, verification_status = ?, verification_message = ?, updated_at = ?, verified_at = ? " +
                "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionStatus);
            ps.setString(2, verificationStatus);
            ps.setString(3, verificationMessage);
            ps.setTimestamp(4, updatedAt);
            ps.setTimestamp(5, verifiedAt);
            ps.setInt(6, transactionId);
            return ps.executeUpdate() > 0;
        }
    }

    public int expirePendingTransactions() {
        int pendingHours = AppConfig.getInt("payment.bank.pending-hours", 2);
        String sql = "UPDATE payment_transactions " +
                "SET status = 'EXPIRED', verification_status = 'EXPIRED', " +
                "verification_message = COALESCE(NULLIF(verification_message, ''), 'Quá thời gian chờ thanh toán chuyển khoản.'), " +
                "updated_at = CURRENT_TIMESTAMP " +
                "WHERE status = 'PENDING_VERIFICATION' " +
                "AND created_at <= DATE_SUB(NOW(), INTERVAL ? HOUR)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pendingHours);
            return ps.executeUpdate();
        } catch (Exception e) {
            log.error("Failed to expire pending payment transactions", e);
            return 0;
        }
    }

    public void attachLatestToOrders(Connection conn, List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
        }

        String sql = "SELECT pt.* FROM payment_transactions pt " +
                "JOIN (" +
                "  SELECT order_id, MAX(id) AS latest_id " +
                "  FROM payment_transactions " +
                "  WHERE order_id IN (" + placeholders + ")" +
                "  GROUP BY order_id" +
                ") latest ON latest.latest_id = pt.id";

        Map<Integer, Order> orderMap = new HashMap<>();
        for (Order order : orders) {
            orderMap.put(order.getId(), order);
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < orders.size(); i++) {
                ps.setInt(i + 1, orders.get(i).getId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PaymentTransaction transaction = map(rs);
                    Order order = orderMap.get(transaction.getOrderId());
                    if (order != null) {
                        applyTransaction(order, transaction);
                    }
                }
            }
        } catch (Exception e) {
            log.error("DB error", e);
        }
    }

    public void applyTransaction(Order order, PaymentTransaction transaction) {
        if (order == null || transaction == null) {
            return;
        }

        order.setPaymentTransactionStatus(transaction.getStatus());
        order.setPaymentVerificationStatus(transaction.getVerificationStatus());
        order.setPaymentReference(transaction.getTransferReference());
        order.setPaymentVerificationMessage(transaction.getVerificationMessage());
        order.setPaymentVerifiedAt(transaction.getVerifiedAt());
    }

    private PaymentTransaction map(ResultSet rs) throws Exception {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(rs.getInt("id"));
        transaction.setOrderId(rs.getInt("order_id"));
        transaction.setUserId(rs.getInt("user_id"));
        transaction.setProviderKey(rs.getString("provider_key"));
        transaction.setProviderDisplayName(rs.getString("provider_display_name"));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setCurrency(rs.getString("currency"));
        transaction.setTransferReference(rs.getString("transfer_reference"));
        transaction.setProviderTransactionId(rs.getString("provider_transaction_id"));
        transaction.setStatus(rs.getString("status"));
        transaction.setVerificationStatus(rs.getString("verification_status"));
        transaction.setVerificationMessage(rs.getString("verification_message"));
        transaction.setProviderMetadata(rs.getString("provider_metadata"));
        transaction.setCreatedAt(rs.getTimestamp("created_at"));
        transaction.setUpdatedAt(rs.getTimestamp("updated_at"));
        transaction.setVerifiedAt(rs.getTimestamp("verified_at"));
        try {
            transaction.setExpiresAt(rs.getTimestamp("expires_at"));
        } catch (Exception ignored) {
        }
        return transaction;
    }
}
