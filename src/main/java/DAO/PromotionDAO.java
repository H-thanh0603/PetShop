package DAO;

import Context.DBContext;
import Model.Product;
import Model.Promotion;
import Model.PromotionCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PromotionDAO {

    private static final Logger log = LoggerFactory.getLogger(PromotionDAO.class);

    private static final String ACTIVE_PROMOTION_QUERY =
            "SELECT p.id AS promotion_id, p.name AS promotion_name, p.discount_type, p.discount_value, " +
            "p.promotion_type, p.end_date, pp.sale_quantity, pp.sold_quantity " +
            "FROM promotions p " +
            "JOIN promotion_products pp ON pp.promotion_id = p.id " +
            "WHERE pp.product_id = ? " +
            "AND p.status = 'ACTIVE' " +
            "AND ? >= p.start_date " +
            "AND ? <= p.end_date";

    public List<PromotionCandidate> findActivePromotionCandidates(Connection conn, int productId, Timestamp now) {
        List<PromotionCandidate> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(ACTIVE_PROMOTION_QUERY)) {
            ps.setInt(1, productId);
            ps.setTimestamp(2, now);
            ps.setTimestamp(3, now);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PromotionCandidate candidate = new PromotionCandidate();
                    candidate.setPromotionId(rs.getInt("promotion_id"));
                    candidate.setPromotionName(rs.getString("promotion_name"));
                    candidate.setDiscountType(rs.getString("discount_type"));
                    candidate.setDiscountValue(rs.getBigDecimal("discount_value"));
                    candidate.setPromotionType(rs.getString("promotion_type"));
                    candidate.setEndDate(rs.getTimestamp("end_date"));
                    candidate.setSaleQuantity((Integer) rs.getObject("sale_quantity"));
                    candidate.setSoldQuantity((Integer) rs.getObject("sold_quantity"));
                    result.add(candidate);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching active promotion candidates for product id={}", productId, e);
        }
        return result;
    }

    public List<PromotionCandidate> findActivePromotionCandidates(int productId, Timestamp now) {
        try (Connection conn = DBContext.getConnection()) {
            return findActivePromotionCandidates(conn, productId, now);
        } catch (Exception e) {
            log.error("Error fetching active promotion candidates for product id={}", productId, e);
        }
        return Collections.emptyList();
    }

    public boolean reserveFlashSaleQuantity(Connection conn, int promotionId, int productId, int quantity) throws Exception {
        String sql = "UPDATE promotion_products " +
                "SET sold_quantity = COALESCE(sold_quantity, 0) + ? " +
                "WHERE promotion_id = ? AND product_id = ? " +
                "AND sale_quantity IS NOT NULL " +
                "AND COALESCE(sold_quantity, 0) + ? <= sale_quantity";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, promotionId);
            ps.setInt(3, productId);
            ps.setInt(4, quantity);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean releaseFlashSaleQuantity(Connection conn, int promotionId, int productId, int quantity) throws Exception {
        String sql = "UPDATE promotion_products " +
                "SET sold_quantity = GREATEST(COALESCE(sold_quantity, 0) - ?, 0) " +
                "WHERE promotion_id = ? AND product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, promotionId);
            ps.setInt(3, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Product> getFlashSaleProducts(int limit) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT pr.id AS pid, MIN(p.end_date) AS earliest_end " +
                "FROM products pr " +
                "JOIN promotion_products pp ON pp.product_id = pr.id " +
                "JOIN promotions p ON p.id = pp.promotion_id " +
                "WHERE pr.is_active = 1 " +
                "AND p.status = 'ACTIVE' " +
                "AND p.promotion_type = 'FLASH_SALE' " +
                "AND NOW() BETWEEN p.start_date AND p.end_date " +
                "AND pp.sale_quantity IS NOT NULL " +
                "AND COALESCE(pp.sold_quantity, 0) < pp.sale_quantity " +
                "GROUP BY pr.id " +
                "ORDER BY earliest_end ASC, pr.id DESC " +
                "LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                ProductDAO productDAO = new ProductDAO();
                while (rs.next()) {
                    Product product = productDAO.getProductById(conn, rs.getInt("pid"));
                    if (product != null) {
                        products.add(product);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching flash sale products", e);
        }
        return products;
    }

    public List<Promotion> getAllPromotions() {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT p.*, COUNT(pp.id) AS product_count, COALESCE(SUM(pp.sale_quantity), 0) AS total_sale_quantity, " +
                "COALESCE(SUM(pp.sold_quantity), 0) AS total_sold_quantity " +
                "FROM promotions p LEFT JOIN promotion_products pp ON pp.promotion_id = p.id " +
                "GROUP BY p.id ORDER BY p.created_at DESC, p.id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Promotion promotion = mapPromotion(rs);
                // SUM() trong MySQL trả về DECIMAL — phải đọc qua Number để tránh ClassCastException.
                promotion.setSaleQuantity(toInteger(rs.getObject("total_sale_quantity")));
                promotion.setSoldQuantity(toInteger(rs.getObject("total_sold_quantity")));
                list.add(promotion);
            }
        } catch (Exception e) {
            log.error("Error fetching all promotions", e);
        }
        return list;
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Promotion getPromotionById(int id) {
        String sql = "SELECT * FROM promotions WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Promotion promotion = mapPromotion(rs);
                    promotion.setProductIds(getPromotionProductIds(conn, id));
                    Integer saleQuantity = getPromotionSaleQuantity(conn, id);
                    promotion.setSaleQuantity(saleQuantity);
                    return promotion;
                }
            }
        } catch (Exception e) {
            log.error("Error fetching promotion id={}", id, e);
        }
        return null;
    }

    public int savePromotion(Promotion promotion) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int promotionId = promotion.getId() > 0 ? updatePromotion(conn, promotion) : insertPromotion(conn, promotion);
                if (promotionId <= 0) {
                    conn.rollback();
                    return 0;
                }
                replacePromotionProducts(conn, promotionId, promotion.getProductIds(), promotion.getPromotionType(), promotion.getSaleQuantity());
                conn.commit();
                return promotionId;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            log.error("Error saving promotion name={}", promotion.getName(), e);
        }
        return 0;
    }

    public boolean updatePromotionStatus(int id, String status) {
        String sql = "UPDATE promotions SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error updating promotion status id={}", id, e);
        }
        return false;
    }

    public boolean deletePromotion(int id) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!canDeletePromotion(conn, id)) {
                    conn.rollback();
                    return false;
                }
                try (PreparedStatement deleteMappings = conn.prepareStatement("DELETE FROM promotion_products WHERE promotion_id = ?");
                     PreparedStatement deletePromotion = conn.prepareStatement("DELETE FROM promotions WHERE id = ?")) {
                    deleteMappings.setInt(1, id);
                    deleteMappings.executeUpdate();
                    deletePromotion.setInt(1, id);
                    boolean deleted = deletePromotion.executeUpdate() > 0;
                    if (deleted) {
                        conn.commit();
                    } else {
                        conn.rollback();
                    }
                    return deleted;
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            log.error("Error deleting promotion id={}", id, e);
        }
        return false;
    }

    public boolean canDeletePromotion(int id) {
        try (Connection conn = DBContext.getConnection()) {
            return canDeletePromotion(conn, id);
        } catch (Exception e) {
            log.error("Error checking delete permission for promotion id={}", id, e);
        }
        return false;
    }

    private boolean canDeletePromotion(Connection conn, int id) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM order_items WHERE promotion_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    private int insertPromotion(Connection conn, Promotion promotion) throws Exception {
        String sql = "INSERT INTO promotions (name, description, discount_type, discount_value, start_date, end_date, status, promotion_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindPromotion(ps, promotion);
            if (ps.executeUpdate() == 0) {
                return 0;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    private int updatePromotion(Connection conn, Promotion promotion) throws Exception {
        String sql = "UPDATE promotions SET name = ?, description = ?, discount_type = ?, discount_value = ?, " +
                "start_date = ?, end_date = ?, status = ?, promotion_type = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindPromotion(ps, promotion);
            ps.setInt(9, promotion.getId());
            return ps.executeUpdate() > 0 ? promotion.getId() : 0;
        }
    }

    private void bindPromotion(PreparedStatement ps, Promotion promotion) throws Exception {
        ps.setString(1, promotion.getName());
        ps.setString(2, promotion.getDescription());
        ps.setString(3, promotion.getDiscountType());
        ps.setBigDecimal(4, promotion.getDiscountValue() != null ? promotion.getDiscountValue() : BigDecimal.ZERO);
        ps.setTimestamp(5, promotion.getStartDate());
        ps.setTimestamp(6, promotion.getEndDate());
        ps.setString(7, promotion.getStatus());
        ps.setString(8, promotion.getPromotionType());
    }

    private void replacePromotionProducts(Connection conn, int promotionId, List<Integer> productIds,
                                          String promotionType, Integer saleQuantity) throws Exception {
        try (PreparedStatement deletePs = conn.prepareStatement("DELETE FROM promotion_products WHERE promotion_id = ?")) {
            deletePs.setInt(1, promotionId);
            deletePs.executeUpdate();
        }
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO promotion_products (promotion_id, product_id, sale_quantity, sold_quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer productId : productIds) {
                if (productId == null) {
                    continue;
                }
                ps.setInt(1, promotionId);
                ps.setInt(2, productId);
                if ("FLASH_SALE".equalsIgnoreCase(promotionType)) {
                    ps.setObject(3, saleQuantity);
                    ps.setInt(4, 0);
                } else {
                    ps.setObject(3, null);
                    ps.setObject(4, null);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<Integer> getPromotionProductIds(Connection conn, int promotionId) throws Exception {
        List<Integer> productIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT product_id FROM promotion_products WHERE promotion_id = ? ORDER BY product_id")) {
            ps.setInt(1, promotionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productIds.add(rs.getInt("product_id"));
                }
            }
        }
        return productIds;
    }

    private Integer getPromotionSaleQuantity(Connection conn, int promotionId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT sale_quantity FROM promotion_products WHERE promotion_id = ? LIMIT 1")) {
            ps.setInt(1, promotionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return (Integer) rs.getObject("sale_quantity");
                }
            }
        }
        return null;
    }

    private Promotion mapPromotion(ResultSet rs) throws Exception {
        Promotion promotion = new Promotion();
        promotion.setId(rs.getInt("id"));
        promotion.setName(rs.getString("name"));
        promotion.setDescription(rs.getString("description"));
        promotion.setDiscountType(rs.getString("discount_type"));
        promotion.setDiscountValue(rs.getBigDecimal("discount_value"));
        promotion.setStartDate(rs.getTimestamp("start_date"));
        promotion.setEndDate(rs.getTimestamp("end_date"));
        promotion.setStatus(rs.getString("status"));
        promotion.setPromotionType(rs.getString("promotion_type"));
        promotion.setCreatedAt(rs.getTimestamp("created_at"));
        promotion.setUpdatedAt(rs.getTimestamp("updated_at"));
        return promotion;
    }
}
