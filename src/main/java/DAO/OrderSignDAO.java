package DAO;

import Context.DBContext;
import Model.OrderSign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OrderSignDAO {

    private static final Logger log =
            LoggerFactory.getLogger(OrderSignDAO.class);

    private OrderSign mapOrderSign(ResultSet rs) throws Exception {
        OrderSign os = new OrderSign(
                rs.getInt("id"),
                rs.getInt("order_id"),
                rs.getInt("user_id"),
                rs.getString("order_data"),
                rs.getString("order_hash"),
                rs.getString("public_key"),
                rs.getTimestamp("created_at")
        );
        try { os.setPrivateKey(rs.getString("private_key")); } catch (Exception ignored) {}
        return os;
    }

    public boolean save(int orderId,
                        int userId,
                        String orderData,
                        String orderHash,
                        String publicKey,
                        String privateKey) {

        try (Connection conn = DBContext.getConnection()) {
            return save(conn, orderId, userId,
                    orderData, orderHash, publicKey, privateKey);
        } catch (Exception e) {
            log.error("DB error", e);
        }

        return false;
    }

    public boolean save(Connection conn,
                        int orderId,
                        int userId,
                        String orderData,
                        String orderHash,
                        String publicKey,
                        String privateKey) throws Exception {

        String query =
                "INSERT INTO order_signs " +
                "(order_id, user_id, order_data, order_hash, public_key, private_key) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            ps.setString(3, orderData);
            ps.setString(4, orderHash);
            ps.setString(5, publicKey);
            ps.setString(6, privateKey);

            return ps.executeUpdate() > 0;
        }
    }

    public OrderSign findByOrderId(int orderId) {

        String query =
                "SELECT * FROM order_signs WHERE order_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOrderSign(rs);
                }
            }

        } catch (Exception e) {
            log.error("DB error", e);
        }

        return null;
    }

    public List<OrderSign> findByUserId(int userId) {

        List<OrderSign> list = new ArrayList<>();

        String query =
                "SELECT * FROM order_signs " +
                "WHERE user_id = ? " +
                "ORDER BY created_at DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrderSign(rs));
                }
            }

        } catch (Exception e) {
            log.error("DB error", e);
        }

        return list;
    }

    public List<OrderSign> findPendingByUserId(int userId) {

        List<OrderSign> list = new ArrayList<>();

        String query =
                "SELECT os.* FROM order_signs os " +
                "LEFT JOIN order_signatures osig " +
                "ON os.order_id = osig.order_id " +
                "WHERE os.user_id = ? " +
                "AND (osig.verify_status IS NULL " +
                "OR osig.verify_status != 'verified') " +
                "ORDER BY os.created_at DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrderSign(rs));
                }
            }

        } catch (Exception e) {
            log.error("DB error", e);
        }

        return list;
    }
}
