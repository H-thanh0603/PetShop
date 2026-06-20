package DAO;

import Context.DBContext;
import Model.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class CertificateDAO {

    private static final Logger log =
            LoggerFactory.getLogger(CertificateDAO.class);

    private Certificate mapCertificate(ResultSet rs) throws Exception {

        return new Certificate(
                rs.getInt("id"),
                rs.getInt("order_id"),
                rs.getInt("user_id"),
                rs.getString("order_code"),
                rs.getString("certificate_data"),
                rs.getString("cert_subject"),
                rs.getTimestamp("expires_at"),
                rs.getTimestamp("created_at")
        );
    }

    /**
     * ISSUE #5
     */
    public boolean save(int orderId,
                        int userId,
                        String orderCode,
                        String certificatePem,
                        String subject,
                        Timestamp expiresAt) {

        try (Connection conn = DBContext.getConnection()) {
            return save(conn,
                    orderId,
                    userId,
                    orderCode,
                    certificatePem,
                    subject,
                    expiresAt);
        } catch (Exception e) {
            log.error("DB error", e);
        }

        return false;
    }

    public boolean save(Connection conn,
                        int orderId,
                        int userId,
                        String orderCode,
                        String certificatePem,
                        String subject,
                        Timestamp expiresAt) throws Exception {

        String query =
                "INSERT INTO certificates " +
                "(order_id, user_id, order_code, certificate_data, cert_subject, expires_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            ps.setString(3, orderCode);
            ps.setString(4, certificatePem);
            ps.setString(5, subject);
            ps.setTimestamp(6, expiresAt);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Find certificate by order
     */
    public Certificate findByOrderId(int orderId) {

        String query =
                "SELECT * " +
                "FROM certificates " +
                "WHERE order_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCertificate(rs);
                }
            }

        } catch (Exception e) {
            log.error("DB error", e);
        }

        return null;
    }
}
