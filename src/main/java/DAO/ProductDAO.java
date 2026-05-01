package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import Context.DBContext;
import Model.Product;
import Model.ProductFilterCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductDAO {

    private static final Logger log = LoggerFactory.getLogger(ProductDAO.class);

    private static final String PRODUCT_SELECT_WITH_REVIEWS =
            "SELECT p.*, COALESCE(AVG(r.rating), 0) AS average_rating, COUNT(r.id) AS review_count " +
            "FROM products p LEFT JOIN reviews r ON r.product_id = p.id ";

    private Product mapProduct(ResultSet rs) throws Exception {
        String desc = rs.getString("description");
        if (desc == null) desc = "";

        String cat = "";
        try {
            cat = rs.getString("category");
            if (cat == null) {
                cat = "";
            }
        } catch (Exception e) {
            cat = "";
        }

        Product product = new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("image"),
                rs.getBigDecimal("price"),
                rs.getInt("discount"),
                desc,
                cat
        );

        // Đồng bộ đầy đủ stock/weight/pet_type_id từ DB để mọi view và thao tác cart dùng đúng tồn kho hiện tại.
        try {
            product.setWeight(rs.getInt("weight"));
        } catch (Exception ignored) {
        }
        try {
            product.setStock(rs.getInt("stock"));
        } catch (Exception ignored) {
        }
        try {
            product.setPet_type_id(rs.getInt("pet_type_id"));
        } catch (Exception ignored) {
        }
        try {
            product.setAverageRating(rs.getDouble("average_rating"));
        } catch (Exception ignored) {
            product.setAverageRating(0);
        }
        try {
            product.setReviewCount(rs.getInt("review_count"));
        } catch (Exception ignored) {
            product.setReviewCount(0);
        }
        try {
            product.setActive(rs.getBoolean("is_active"));
        } catch (Exception ignored) {
            product.setActive(true);
        }

        return product;
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.is_active = 1 GROUP BY p.id ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error fetching all products", e); }
        return list;
    }

    public List<Product> getProductsByPetType(String petTypeCode) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT p.*, COALESCE(AVG(r.rating), 0) AS average_rating, COUNT(r.id) AS review_count " +
                "FROM products p " +
                "INNER JOIN pet_types pt ON p.pet_type_id = pt.id " +
                "LEFT JOIN reviews r ON r.product_id = p.id " +
                "WHERE pt.code = ? AND pt.is_active = 1 AND p.is_active = 1 GROUP BY p.id ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, petTypeCode); ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error fetching products by pet type code={}", petTypeCode, e); }
        return list;
    }

    public List<Product> getProductsByPetTypeFallback(String petTypeName) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.category LIKE ? AND p.is_active = 1 GROUP BY p.id ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, "%" + petTypeName + "%"); ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error fetching products by pet type name={}", petTypeName, e); }
        return list;
    }

    public List<String> getCategoriesByPetType(String petTypeCode) {
        List<String> list = new ArrayList<>();
        String query = "SELECT DISTINCT p.category FROM products p INNER JOIN pet_types pt ON p.pet_type_id = pt.id WHERE pt.code = ? AND p.category IS NOT NULL AND p.category != '' AND p.is_active = 1 ORDER BY p.category";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, petTypeCode); ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(rs.getString("category")); }
        } catch (Exception e) { log.error("Error fetching categories by pet type code={}", petTypeCode, e); }
        return list;
    }

    public List<String> getAllCategories() {
        List<String> list = new ArrayList<>();
        String query = "SELECT DISTINCT category FROM products WHERE category IS NOT NULL AND category != '' AND is_active = 1 ORDER BY category";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(rs.getString("category")); }
        } catch (Exception e) { log.error("Error fetching all categories", e); }
        return list;
    }

    public List<String> getPopularCategories(int limit) {
        List<String> list = new ArrayList<>();
        String query = "SELECT p.category, SUM(oi.quantity) AS total_sold FROM order_items oi JOIN products p ON oi.product_id = p.id JOIN orders o ON oi.order_id = o.id WHERE o.status != 'Cancelled' AND p.category IS NOT NULL AND p.category != '' AND p.is_active = 1 GROUP BY p.category ORDER BY total_sold DESC LIMIT ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { list.add(rs.getString("category")); } }
        } catch (Exception e) { log.error("Error fetching popular categories limit={}", limit, e); }
        return list;
    }

    public List<Product> getProductsByCategory(String category) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.category = ? AND p.is_active = 1 GROUP BY p.id ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, category); ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error fetching products by category={}", category, e); }
        return list;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS +
                "WHERE (p.name LIKE ? OR p.description LIKE ?) AND p.is_active = 1 GROUP BY p.id ORDER BY p.name ASC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            String p = "%" + keyword + "%"; ps.setString(1, p); ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error searching products keyword={}", keyword, e); }
        return list;
    }

    public List<Product> searchProductsLimit(String keyword, int limit) {
        List<Product> list = new ArrayList<>();
        // ORDER BY: tên bắt đầu bằng keyword lên trước, sau đó mới đến chứa keyword ở giữa
        String query = PRODUCT_SELECT_WITH_REVIEWS +
                       "WHERE (p.name LIKE ? OR p.description LIKE ?) AND p.is_active = 1 " +
                       "GROUP BY p.id " +
                       "ORDER BY CASE WHEN p.name LIKE ? THEN 0 ELSE 1 END, p.name ASC LIMIT ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            String contains = "%" + keyword + "%";
            String startsWith = keyword + "%";
            ps.setString(1, contains);
            ps.setString(2, contains);
            ps.setString(3, startsWith);
            ps.setInt(4, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error searching products with limit keyword={}", keyword, e); }
        return list;
    }

    public List<Product> getFilteredProductsPage(ProductFilterCriteria criteria) {
        List<Product> list = new ArrayList<>();
        FilterQueryParts parts = buildFilteredQuery(criteria, false);
        String sql = "SELECT p.*, COALESCE(rv.average_rating, 0) AS average_rating, " +
                "COALESCE(rv.review_count, 0) AS review_count, COALESCE(bs.total_sold, 0) AS total_sold " +
                "FROM products p " +
                "LEFT JOIN pet_types pt ON p.pet_type_id = pt.id " +
                "LEFT JOIN (" +
                "  SELECT product_id, AVG(rating) AS average_rating, COUNT(id) AS review_count " +
                "  FROM reviews GROUP BY product_id" +
                ") rv ON rv.product_id = p.id " +
                "LEFT JOIN (" +
                "  SELECT oi.product_id, SUM(oi.quantity) AS total_sold " +
                "  FROM order_items oi " +
                "  JOIN orders o ON o.id = oi.order_id " +
                "  WHERE o.status != 'Cancelled' " +
                "  GROUP BY oi.product_id" +
                ") bs ON bs.product_id = p.id " +
                parts.whereClause +
                " ORDER BY " + parts.orderByClause +
                " LIMIT ? OFFSET ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = bindFilterParameters(ps, criteria, 1);
            ps.setInt(idx++, criteria.getPageSize());
            ps.setInt(idx, Math.max(0, (criteria.getPage() - 1) * criteria.getPageSize()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        } catch (Exception e) {
            log.error("Error fetching filtered products", e);
        }
        return list;
    }

    public int countFilteredProducts(ProductFilterCriteria criteria) {
        FilterQueryParts parts = buildFilteredQuery(criteria, true);
        String sql = "SELECT COUNT(*) FROM products p " +
                "LEFT JOIN pet_types pt ON p.pet_type_id = pt.id " +
                parts.whereClause;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindFilterParameters(ps, criteria, 1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            log.error("Error counting filtered products", e);
        }
        return 0;
    }

    public List<Product> getDiscountedProductsList() {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.discount > 0 AND p.is_active = 1 GROUP BY p.id ORDER BY p.discount DESC, p.id DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error fetching discounted products list", e); }
        return list;
    }

    public List<Product> getDiscountedProductsPage(int page, int size) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.discount > 0 AND p.is_active = 1 GROUP BY p.id ORDER BY p.discount DESC, p.id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, size); ps.setInt(2, Math.max(0, (page - 1) * size));
            ResultSet rs = ps.executeQuery(); while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error fetching discounted products page={}", page, e); }
        return list;
    }

    public int getTotalDiscountedProductsCount() {
        String query = "SELECT COUNT(*) FROM products WHERE discount > 0 AND is_active = 1";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { log.error("Error counting discounted products", e); }
        return 0;
    }

    public List<Product> getAllProductsPage(int page, int size) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.is_active = 1 GROUP BY p.id ORDER BY p.id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, size); ps.setInt(2, Math.max(0, (page - 1) * size));
            ResultSet rs = ps.executeQuery(); while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error fetching all products page={}", page, e); }
        return list;
    }

    public int getTotalProductsCount() {
        String query = "SELECT COUNT(*) FROM products WHERE is_active = 1";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { log.error("Error counting total products", e); }
        return 0;
    }

    public List<Product> getPopularProductsPage(int page, int size) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT p.*, COALESCE(s.total_sold, 0) AS total_sold, " +
                "COALESCE(rv.average_rating, 0) AS average_rating, COALESCE(rv.review_count, 0) AS review_count " +
                "FROM products p " +
                "LEFT JOIN (" +
                "  SELECT oi.product_id, SUM(oi.quantity) AS total_sold " +
                "  FROM order_items oi " +
                "  JOIN orders o ON o.id = oi.order_id " +
                "  WHERE o.status != 'Cancelled' " +
                "  GROUP BY oi.product_id " +
                ") s ON s.product_id = p.id " +
                "LEFT JOIN (" +
                "  SELECT product_id, AVG(rating) AS average_rating, COUNT(id) AS review_count " +
                "  FROM reviews GROUP BY product_id " +
                ") rv ON rv.product_id = p.id " +
                "WHERE p.is_active = 1 " +
                "ORDER BY total_sold DESC, p.discount DESC, p.id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, size); ps.setInt(2, Math.max(0, (page - 1) * size));
            ResultSet rs = ps.executeQuery(); while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error fetching popular products page={}", page, e); }
        return list;
    }

    public int getTotalPopularProductsCount() {
        String query = "SELECT COUNT(*) FROM products WHERE is_active = 1";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { log.error("Error counting popular products", e); }
        return 0;
    }

    private int bindFilterParameters(PreparedStatement ps, ProductFilterCriteria criteria, int startIndex) throws Exception {
        int idx = startIndex;
        if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
            ps.setString(idx++, criteria.getCategory().trim());
        }
        if (criteria.getPetTypeCode() != null && !criteria.getPetTypeCode().isBlank()) {
            ps.setString(idx++, criteria.getPetTypeCode().trim());
        }
        if (criteria.getSearchKeyword() != null && !criteria.getSearchKeyword().isBlank()) {
            String pattern = "%" + criteria.getSearchKeyword().trim() + "%";
            ps.setString(idx++, pattern);
            ps.setString(idx++, pattern);
        }
        if (criteria.isDiscountOnly()) {
            // no bind needed
        }
        if (criteria.getPriceRange() != null && !criteria.getPriceRange().isBlank()) {
            switch (criteria.getPriceRange()) {
                case "under100":
                    ps.setBigDecimal(idx++, BigDecimal.valueOf(100000));
                    break;
                case "100to300":
                    ps.setBigDecimal(idx++, BigDecimal.valueOf(100000));
                    ps.setBigDecimal(idx++, BigDecimal.valueOf(300000));
                    break;
                case "300to500":
                    ps.setBigDecimal(idx++, BigDecimal.valueOf(300000));
                    ps.setBigDecimal(idx++, BigDecimal.valueOf(500000));
                    break;
                case "above500":
                    ps.setBigDecimal(idx++, BigDecimal.valueOf(500000));
                    break;
                default:
                    break;
            }
        }
        return idx;
    }

    private FilterQueryParts buildFilteredQuery(ProductFilterCriteria criteria, boolean countOnly) {
        StringBuilder where = new StringBuilder("WHERE p.is_active = 1");

        if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
            where.append(" AND p.category = ?");
        }
        if (criteria.getPetTypeCode() != null && !criteria.getPetTypeCode().isBlank()) {
            where.append(" AND pt.code = ?");
        }
        if (criteria.getSearchKeyword() != null && !criteria.getSearchKeyword().isBlank()) {
            where.append(" AND (p.name LIKE ? OR p.description LIKE ?)");
        }
        if (criteria.isDiscountOnly()) {
            where.append(" AND p.discount > 0");
        }
        if (criteria.getPriceRange() != null && !criteria.getPriceRange().isBlank()) {
            switch (criteria.getPriceRange()) {
                case "under100":
                    where.append(" AND p.price < ?");
                    break;
                case "100to300":
                    where.append(" AND p.price >= ? AND p.price <= ?");
                    break;
                case "300to500":
                    where.append(" AND p.price >= ? AND p.price <= ?");
                    break;
                case "above500":
                    where.append(" AND p.price > ?");
                    break;
                default:
                    break;
            }
        }

        String orderBy = countOnly ? "" : resolveOrderBy(criteria.getSort());
        return new FilterQueryParts(where.toString(), orderBy);
    }

    private String resolveOrderBy(String sort) {
        if (sort == null || sort.isBlank()) {
            return "p.id DESC";
        }
        switch (sort) {
            case "price-asc":
                return "p.price ASC, p.id DESC";
            case "price-desc":
                return "p.price DESC, p.id DESC";
            case "discount":
                return "p.discount DESC, p.id DESC";
            case "name":
                return "p.name ASC, p.id DESC";
            case "rating":
                return "average_rating DESC, review_count DESC, p.id DESC";
            case "best-selling":
                return "total_sold DESC, p.id DESC";
            case "newest":
            default:
                return "p.id DESC";
        }
    }

    private static final class FilterQueryParts {
        private final String whereClause;
        private final String orderByClause;

        private FilterQueryParts(String whereClause, String orderByClause) {
            this.whereClause = whereClause;
            this.orderByClause = orderByClause;
        }
    }

    public Product getProductById(int id) {
        try (Connection conn = DBContext.getConnection()) {
            return getProductById(conn, id);
        } catch (Exception e) {
            log.error("Error fetching product by id={}", id, e);
        }
        return null;
    }

    public Product getProductById(Connection conn, int id) {
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.id = ? GROUP BY p.id";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapProduct(rs);
            }
        } catch (Exception e) { log.error("Error fetching product by id={} with connection", id, e); }
        return null;
    }

    public Product getProductByIdForUpdate(Connection conn, int id) {
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.id = ? GROUP BY p.id FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapProduct(rs);
            }
        } catch (Exception e) { log.error("Error fetching product for update id={}", id, e); }
        return null;
    }

    public boolean addProduct(String name, String image, BigDecimal price, int discount, String description) {
        String query = "INSERT INTO products (name, image, price, discount, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name); ps.setString(2, image); ps.setBigDecimal(3, price);
            ps.setInt(4, discount); ps.setString(5, description);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("Error adding product name={}", name, e); }
        return false;
    }

    public boolean addProduct(String name, String image, BigDecimal price, int discount, String description, int stock, int weight, String category, int petTypeId) {
        String query = "INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setString(2, image);
            ps.setBigDecimal(3, price);
            ps.setInt(4, discount);
            ps.setString(5, description);
            ps.setInt(6, stock);
            ps.setInt(7, weight);
            ps.setString(8, category);
            ps.setInt(9, petTypeId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("Error adding product with stock name={}", name, e); }
        return false;
    }

    public boolean updateProduct(int id, String name, String image, BigDecimal price, int discount, String description) {
        String query = "UPDATE products SET name = ?, image = ?, price = ?, discount = ?, description = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name); ps.setString(2, image); ps.setBigDecimal(3, price);
            ps.setInt(4, discount); ps.setString(5, description); ps.setInt(6, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("Error updating product id={}", id, e); }
        return false;
    }

    public boolean updateProduct(int id, String name, String image, BigDecimal price, int discount, String description, int stock, int weight, String category, int petTypeId) {
        String query = "UPDATE products SET name=?, image=?, price=?, discount=?, description=?, stock=?, weight=?, category=?, pet_type_id=? WHERE id=?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setString(2, image);
            ps.setBigDecimal(3, price);
            ps.setInt(4, discount);
            ps.setString(5, description);
            ps.setInt(6, stock);
            ps.setInt(7, weight);
            ps.setString(8, category);
            ps.setInt(9, petTypeId);
            ps.setInt(10, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("Error updating product with stock id={}", id, e); }
        return false;
    }

    public boolean deleteProduct(int id) {
        String query = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("Error deleting product id={}", id, e); }
        return false;
    }

    public boolean softDeleteProduct(int id) {
        String query = "UPDATE products SET is_active = 0 WHERE id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("Error soft-deleting product id={}", id, e); }
        return false;
    }

    public int getTotalProducts() {
        String query = "SELECT COUNT(*) FROM products WHERE is_active = 1";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { log.error("Error counting total products", e); }
        return 0;
    }

    public int getDiscountedProducts() {
        String query = "SELECT COUNT(*) FROM products WHERE discount > 0 AND is_active = 1";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { log.error("Error counting discounted products", e); }
        return 0;
    }

    public List<Product> getRelatedProducts(int excludeId) {
        List<Product> list = new ArrayList<>();
        final int limit = 4;

        // Step 1: resolve the category of the excluded product
        String catQuery = "SELECT category FROM products WHERE id = ?";
        String category = null;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(catQuery)) {
            ps.setInt(1, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { category = rs.getString("category"); }
            }
        } catch (Exception e) { log.error("Error fetching related products for id={}", excludeId, e); }

        // Step 2: fetch same-category products using a random OFFSET on the primary-key index
        // instead of ORDER BY RAND() which performs a full-table sort.
        if (category != null && !category.isEmpty()) {
            String countQuery = "SELECT COUNT(*) FROM products p WHERE p.id != ? AND p.category = ? AND p.is_active = 1";
            int count = 0;
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(countQuery)) {
                ps.setInt(1, excludeId);
                ps.setString(2, category);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) { count = rs.getInt(1); }
                }
            } catch (Exception e) { log.error("Error fetching related products for id={}", excludeId, e); }

            int offset = (count > limit) ? ThreadLocalRandom.current().nextInt(count - limit + 1) : 0;
            String query = PRODUCT_SELECT_WITH_REVIEWS +
                    "WHERE p.id != ? AND p.category = ? AND p.is_active = 1 GROUP BY p.id ORDER BY p.id LIMIT ? OFFSET ?";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, excludeId);
                ps.setString(2, category);
                ps.setInt(3, limit);
                ps.setInt(4, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) { list.add(mapProduct(rs)); }
                }
            } catch (Exception e) { log.error("Error fetching related products for id={}", excludeId, e); }
        }

        // Step 3: if we still need more products, fill from the rest of the catalogue
        // using the same indexed approach.
        if (list.size() < limit) {
            int needed = limit - list.size();
            List<Integer> excludeIds = new ArrayList<>();
            excludeIds.add(excludeId);
            for (Product p : list) { excludeIds.add(p.getId()); }

            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < excludeIds.size(); i++) {
                if (i > 0) placeholders.append(",");
                placeholders.append("?");
            }

            String countQuery = "SELECT COUNT(*) FROM products p WHERE p.id NOT IN (" + placeholders + ") AND p.is_active = 1";
            int count = 0;
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(countQuery)) {
                int paramIdx = 1;
                for (int id : excludeIds) { ps.setInt(paramIdx++, id); }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) { count = rs.getInt(1); }
                }
            } catch (Exception e) { log.error("Error fetching related products for id={}", excludeId, e); }

            int offset = (count > needed) ? ThreadLocalRandom.current().nextInt(count - needed + 1) : 0;
            String query = PRODUCT_SELECT_WITH_REVIEWS +
                    "WHERE p.id NOT IN (" + placeholders + ") AND p.is_active = 1 GROUP BY p.id ORDER BY p.id LIMIT ? OFFSET ?";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                int paramIdx = 1;
                for (int id : excludeIds) { ps.setInt(paramIdx++, id); }
                ps.setInt(paramIdx++, needed);
                ps.setInt(paramIdx, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) { list.add(mapProduct(rs)); }
                }
            } catch (Exception e) { log.error("Error fetching related products for id={}", excludeId, e); }
        }
        return list;
    }

    public List<Product> getProductsByPage(int index, int size) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.is_active = 1 GROUP BY p.id ORDER BY p.id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, size); ps.setInt(2, (index - 1) * size);
            ResultSet rs = ps.executeQuery(); while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error fetching products by page index={}", index, e); }
        return list;
    }

    // ========== STOCK MANAGEMENT ==========
    public boolean decreaseStock(int productId, int quantity) {
        try (Connection conn = DBContext.getConnection()) {
            return decreaseStock(conn, productId, quantity);
        } catch (Exception e) {
            log.error("Error decreasing stock for product id={}", productId, e);
        }
        return false;
    }

    public boolean decreaseStock(Connection conn, int productId, int quantity) {
        String query = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error decreasing stock for product id={}", productId, e);
        }
        return false;
    }

    public boolean increaseStock(int productId, int quantity) {
        try (Connection conn = DBContext.getConnection()) {
            return increaseStock(conn, productId, quantity);
        } catch (Exception e) {
            log.error("Error increasing stock for product id={}", productId, e);
        }
        return false;
    }

    public boolean increaseStock(Connection conn, int productId, int quantity) {
        String query = "UPDATE products SET stock = stock + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error increasing stock for product id={}", productId, e);
        }
        return false;
    }
    public int getStock(int productId) {
        String query = "SELECT stock FROM products WHERE id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, productId); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("stock");
        } catch (Exception e) { log.error("Error fetching stock for product id={}", productId, e); } return 0;
    }
    public boolean updateStock(int productId, int newStock) {
        String query = "UPDATE products SET stock = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, newStock); ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("Error updating stock for product id={}", productId, e); } return false;
    }
    public List<Product> getLowStockProducts(int threshold) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.stock < ? AND p.stock > 0 GROUP BY p.id ORDER BY p.stock ASC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, threshold); ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error fetching low stock products threshold={}", threshold, e); } return list;
    }
    public List<Product> getOutOfStockProducts() {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.stock <= 0 GROUP BY p.id ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error fetching out-of-stock products", e); } return list;
    }
}
