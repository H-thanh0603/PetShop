package DAO;

import Context.DBContext;
import Model.OrderSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class OrderSignatureDAO {

    private static final Logger log =
            LoggerFactory.getLogger(OrderSignatureDAO.class);

    private OrderSignature mapOrderSignature(ResultSet rs) throws Exception {

        OrderSignature signature = new OrderSignature();

        signature.setId(rs.getInt("id"));
        signature.setOrderId(rs.getInt("order_id"));
        signature.setUserId(rs.getInt("user_id"));
        signature.setSignature(rs.getString("signature"));

        signature.setVerifyStatus(
                OrderSignature.VerifyStatus.valueOf(
                        rs.getString("verify_status")
                )
        );

        signature.setVerifyMessage(
                rs.getString("verify_message")
        );

        signature.setVerifiedAt(
                rs.getTimestamp("verified_at")
        );

        signature.setCreatedAt(
                rs.getTimestamp("created_at")
        );

        return signature;
    }

    public boolean save(int orderId,
                        int userId,
                        String signatureBase64) {

        try (Connection conn = DBContext.getConnection()) {
            return save(conn, orderId, userId, signatureBase64);
        } catch (Exception e) {
            log.error("DB error", e);
        }

        return false;
    }

    public boolean save(Connection conn,
                        int orderId,
                        int userId,
                        String signatureBase64) throws Exception {

        String query =
                "INSERT INTO order_signatures " +
                "(order_id, user_id, signature, verify_status) " +
                "VALUES (?, ?, ?, 'pending')";

        try (PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            ps.setString(3, signatureBase64);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateVerifyStatus(
            int orderId,
            OrderSignature.VerifyStatus status,
            String message) {

        String query =
                "UPDATE order_signatures " +
                "SET verify_status = ?, " +
                "verify_message = ?, " +
                "verified_at = CURRENT_TIMESTAMP " +
                "WHERE order_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, status.name());
            ps.setString(2, message);
            ps.setInt(3, orderId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            log.error("DB error", e);
        }

        return false;
    }

    public OrderSignature findByOrderId(int orderId) {

        String query =
                "SELECT * " +
                "FROM order_signatures " +
                "WHERE order_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapOrderSignature(rs);
                }
            }

        } catch (Exception e) {
            log.error("DB error", e);
        }

        return null;
    }
    public List<OrderSignature> findByUserId(int userId) {
    List<OrderSignature> list = new ArrayList<>();

    String sql =
        "SELECT * FROM order_signatures " +
        "WHERE user_id=? " +
        "ORDER BY created_at DESC";
    }
}
