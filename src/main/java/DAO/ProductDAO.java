package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import Context.DBContext;
import Model.Product;
import Model.ProductFilterCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ProductPricingService;

public class ProductDAO {

    private static final Logger log = LoggerFactory.getLogger(ProductDAO.class);
    private static final String ACTIVE_PROMOTION_EXISTS_SQL =
            "EXISTS (SELECT 1 FROM promotions prm " +
            "JOIN promotion_products ppm ON ppm.promotion_id = prm.id " +
            "WHERE ppm.product_id = p.id " +
            "AND prm.status = 'ACTIVE' " +
            "AND NOW() BETWEEN prm.start_date AND prm.end_date " +
            "AND (prm.promotion_type <> 'FLASH_SALE' OR (ppm.sale_quantity IS NOT NULL AND COALESCE(ppm.sold_quantity, 0) < ppm.sale_quantity)))";

    private static final String PRODUCT_SELECT_WITH_REVIEWS =
            "SELECT p.*, COALESCE(AVG(r.rating), 0) AS average_rating, COUNT(r.id) AS review_count " +
            "FROM products p LEFT JOIN reviews r ON r.product_id = p.id ";
    private final ProductPricingService pricingService = new ProductPricingService();

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
        try {
            product.setBrand(rs.getString("brand"));
        } catch (Exception ignored) {
        }

        pricingService.applyPricing(product);

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

    public List<String> getAllBrands() {
        List<String> brands = new ArrayList<>();
        String sql = "SELECT DISTINCT brand FROM products WHERE brand IS NOT NULL AND brand != '' AND is_active = 1 ORDER BY brand";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                brands.add(rs.getString("brand"));
            }
        } catch (Exception e) { log.error("Error fetching all brands", e); }
        return brands;
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

    public List<Product> searchProductsForAdvice(String message, int limit) {
        List<Product> results = new ArrayList<>();
        String lowerMessage = message.toLowerCase();
        
        // Determine pet type if mentioned
        String petType = null;
        if (lowerMessage.contains("mèo") || lowerMessage.contains("cat")) {
            petType = "cat";
        } else if (lowerMessage.contains("chó") || lowerMessage.contains("dog") || lowerMessage.contains("poodle")) {
            petType = "dog";
        }
        
        // Look for specific product keywords
        String[] keywords = {"hạt", "pate", "cát", "sữa tắm", "shampoo", "đồ chơi", "bát", "nhà", "chuồng", "vòng cổ", "dây dắt", "sữa", "snack", "thức ăn", "xương"};
        List<String> matchedKeywords = new ArrayList<>();
        for (String kw : keywords) {
            if (lowerMessage.contains(kw)) {
                matchedKeywords.add(kw);
            }
        }
        
        // If no specific keywords, use the whole message words (excluding short words)
        if (matchedKeywords.isEmpty()) {
            String query = message.replaceAll("[^a-zA-Z0-9ăâđêôơưàảãáạằẳẵắặầẩẫấậèẻẽéẹềểễếệìỉĩíịòỏõóọồổỗốộờởỡớợùủũúụừửữứựỳỷỹýỵ\\s]", "");
            String[] words = query.split("\\s+");
            for (String word : words) {
                if (word.length() > 2 && !"cho".equals(word) && !"mèo".equals(word) && !"chó".equals(word) && !"bán".equals(word) && !"mua".equals(word) && !"shop".equals(word)) {
                    matchedKeywords.add(word);
                }
            }
        }
        
        // Build SQL query to search in DB
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, COALESCE(AVG(r.rating), 0) AS average_rating, COUNT(r.id) AS review_count " +
            "FROM products p " +
            "LEFT JOIN reviews r ON r.product_id = p.id " +
            "LEFT JOIN pet_types pt ON p.pet_type_id = pt.id " +
            "WHERE p.is_active = 1 AND p.stock > 0 AND p.price > 0 "
        );
        
        if (petType != null) {
            sql.append("AND pt.code = ? ");
        }
        
        if (!matchedKeywords.isEmpty()) {
            sql.append("AND (");
            for (int i = 0; i < matchedKeywords.size(); i++) {
                if (i > 0) sql.append(" OR ");
                sql.append("p.name LIKE ? OR p.description LIKE ? OR p.category LIKE ?");
            }
            sql.append(") ");
        }
        
        sql.append("GROUP BY p.id ORDER BY p.id DESC LIMIT ?");
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (petType != null) {
                ps.setString(paramIdx++, petType);
            }
            for (String kw : matchedKeywords) {
                String searchPattern = "%" + kw + "%";
                ps.setString(paramIdx++, searchPattern);
                ps.setString(paramIdx++, searchPattern);
                ps.setString(paramIdx++, searchPattern);
            }
            ps.setInt(paramIdx, limit);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapProduct(rs));
                }
            }
        } catch (Exception e) {
            log.error("Error searching products for advice", e);
        }
        
        // Fallback: if no products found, just return some active in-stock products
        if (results.isEmpty()) {
            String fallbackSql = "SELECT p.*, COALESCE(AVG(r.rating), 0) AS average_rating, COUNT(r.id) AS review_count " +
                    "FROM products p LEFT JOIN reviews r ON r.product_id = p.id " +
                    "WHERE p.is_active = 1 AND p.stock > 0 AND p.price > 0 " +
                    "GROUP BY p.id ORDER BY p.id DESC LIMIT ?";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(fallbackSql)) {
                ps.setInt(1, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapProduct(rs));
                    }
                }
            } catch (Exception e) {
                log.error("Error in advice fallback", e);
            }
        }
        return results;
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
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE " + ACTIVE_PROMOTION_EXISTS_SQL + " AND p.is_active = 1 GROUP BY p.id ORDER BY COALESCE((SELECT CASE WHEN prm.discount_type = 'PERCENT' THEN prm.discount_value ELSE ROUND(prm.discount_value * 100 / p.price, 0) END FROM promotions prm JOIN promotion_products ppm ON ppm.promotion_id = prm.id WHERE ppm.product_id = p.id AND prm.status = 'ACTIVE' AND NOW() BETWEEN prm.start_date AND prm.end_date LIMIT 1), 0) DESC, p.id DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error fetching discounted products list", e); }
        return list;
    }

    public List<Product> getDiscountedProductsPage(int page, int size) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE " + ACTIVE_PROMOTION_EXISTS_SQL + " AND p.is_active = 1 GROUP BY p.id ORDER BY COALESCE((SELECT CASE WHEN prm.discount_type = 'PERCENT' THEN prm.discount_value ELSE ROUND(prm.discount_value * 100 / p.price, 0) END FROM promotions prm JOIN promotion_products ppm ON ppm.promotion_id = prm.id WHERE ppm.product_id = p.id AND prm.status = 'ACTIVE' AND NOW() BETWEEN prm.start_date AND prm.end_date LIMIT 1), 0) DESC, p.id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, size); ps.setInt(2, Math.max(0, (page - 1) * size));
            ResultSet rs = ps.executeQuery(); while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { log.error("Error fetching discounted products page={}", page, e); }
        return list;
    }

    public int getTotalDiscountedProductsCount() {
        String query = "SELECT COUNT(*) FROM products p WHERE " + ACTIVE_PROMOTION_EXISTS_SQL + " AND p.is_active = 1";
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
                "ORDER BY total_sold DESC, COALESCE((SELECT CASE WHEN prm.discount_type = 'PERCENT' THEN prm.discount_value ELSE ROUND(prm.discount_value * 100 / p.price, 0) END FROM promotions prm JOIN promotion_products ppm ON ppm.promotion_id = prm.id WHERE ppm.product_id = p.id AND prm.status = 'ACTIVE' AND NOW() BETWEEN prm.start_date AND prm.end_date LIMIT 1), 0) DESC, p.id DESC LIMIT ? OFFSET ?";
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
        if (criteria.getBrands() != null && !criteria.getBrands().isEmpty()) {
            for (String brand : criteria.getBrands()) {
                ps.setString(idx++, brand);
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
            where.append(" AND ").append(ACTIVE_PROMOTION_EXISTS_SQL);
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
        if (criteria.getBrands() != null && !criteria.getBrands().isEmpty()) {
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < criteria.getBrands().size(); i++) {
                if (i > 0) placeholders.append(",");
                placeholders.append("?");
            }
            where.append(" AND p.brand IN (").append(placeholders).append(")");
        }
        if (criteria.isAvailabilityOnly()) {
            where.append(" AND p.stock > 0");
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
                return "COALESCE((SELECT CASE WHEN prm.discount_type = 'PERCENT' THEN prm.discount_value ELSE ROUND(prm.discount_value * 100 / p.price, 0) END FROM promotions prm JOIN promotion_products ppm ON ppm.promotion_id = prm.id WHERE ppm.product_id = p.id AND prm.status = 'ACTIVE' AND NOW() BETWEEN prm.start_date AND prm.end_date LIMIT 1), 0) DESC, p.id DESC";
            case "name":
                return "p.name ASC, p.id DESC";
            case "rating":
                return "average_rating DESC, review_count DESC, p.id DESC";
            case "best-selling":
                return "total_sold DESC, p.id DESC";
            case "availability":
                return "p.stock DESC, p.id DESC";
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

    public int addProductAndReturnId(String name, String image, BigDecimal price, int discount, String description, int weight, String category, int petTypeId) {
        String query = "INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active) VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, 1)";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, image); ps.setBigDecimal(3, price);
            ps.setInt(4, discount); ps.setString(5, description);
            ps.setInt(6, weight); ps.setString(7, category); ps.setInt(8, petTypeId);
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (Exception e) { log.error("Error adding product without stock", e); }
        return -1;
    }

    public int addProductAndReturnId(String name, String image, BigDecimal price, int discount, String description, int stock, int weight, String category, int petTypeId) {
        String query = "INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, image);
            ps.setBigDecimal(3, price);
            ps.setInt(4, discount);
            ps.setString(5, description);
            ps.setInt(6, stock);
            ps.setInt(7, weight);
            ps.setString(8, category);
            ps.setInt(9, petTypeId);
            if (ps.executeUpdate() == 0) {
                return 0;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (Exception e) { log.error("Error adding product and returning id name={}", name, e); }
        return 0;
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

    public boolean updateProduct(int id, String name, String image, BigDecimal price, int discount, String description, int weight, String category, int petTypeId) {
        String query = "UPDATE products SET name=?, image=?, price=?, discount=?, description=?, weight=?, category=?, pet_type_id=? WHERE id=?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setString(2, image);
            ps.setBigDecimal(3, price);
            ps.setInt(4, discount);
            ps.setString(5, description);
            ps.setInt(6, weight);
            ps.setString(7, category);
            ps.setInt(8, petTypeId);
            ps.setInt(9, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { log.error("Error updating product without stock id={}", id, e); }
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
        String query = "SELECT COUNT(*) FROM products p WHERE " + ACTIVE_PROMOTION_EXISTS_SQL + " AND p.is_active = 1";
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

    public boolean reserveStock(Connection conn, int productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        String query = "UPDATE products SET stock = stock - ?, reserved_quantity = reserved_quantity + ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, quantity);
            ps.setInt(2, quantity);
            ps.setInt(3, productId);
            ps.setInt(4, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLSyntaxErrorException e) {
            if (isUnknownColumn(e, "reserved_quantity")) {
                log.warn("products.reserved_quantity is missing; falling back to legacy stock decrement for product id={}", productId);
                return reserveStockLegacy(conn, productId, quantity);
            }
            log.error("Error reserving stock for product id={}", productId, e);
        } catch (Exception e) {
            log.error("Error reserving stock for product id={}", productId, e);
        }
        return false;
    }

    private boolean reserveStockLegacy(Connection conn, int productId, int quantity) {
        String query = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error reserving stock with legacy schema for product id={}", productId, e);
        }
        return false;
    }

    public boolean releaseReservedStock(Connection conn, int productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        String query = "UPDATE products SET stock = stock + ?, reserved_quantity = reserved_quantity - ? WHERE id = ? AND reserved_quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, quantity);
            ps.setInt(2, quantity);
            ps.setInt(3, productId);
            ps.setInt(4, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLSyntaxErrorException e) {
            if (isUnknownColumn(e, "reserved_quantity")) {
                log.warn("products.reserved_quantity is missing; falling back to legacy stock release for product id={}", productId);
                return releaseReservedStockLegacy(conn, productId, quantity);
            }
            log.error("Error releasing reserved stock for product id={}", productId, e);
        } catch (Exception e) {
            log.error("Error releasing reserved stock for product id={}", productId, e);
        }
        return false;
    }

    private boolean releaseReservedStockLegacy(Connection conn, int productId, int quantity) {
        String query = "UPDATE products SET stock = stock + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error releasing reserved stock with legacy schema for product id={}", productId, e);
        }
        return false;
    }

    public boolean finalizeReservedStock(Connection conn, int productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        String query = "UPDATE products " +
                "SET reserved_quantity = CASE WHEN reserved_quantity >= ? THEN reserved_quantity - ? ELSE 0 END, " +
                "sold_quantity = sold_quantity + ? " +
                "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, quantity);
            ps.setInt(2, quantity);
            ps.setInt(3, quantity);
            ps.setInt(4, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLSyntaxErrorException e) {
            if (isUnknownColumn(e, "reserved_quantity")) {
                log.warn("products.reserved_quantity is missing; legacy reserve already decremented stock for product id={}", productId);
                return true;
            }
            if (isUnknownColumn(e, "sold_quantity")) {
                return finalizeReservedStockWithoutSoldQuantity(conn, productId, quantity);
            }
            log.error("Error finalizing reserved stock for product id={}", productId, e);
        } catch (Exception e) {
            log.error("Error finalizing reserved stock for product id={}", productId, e);
        }
        return false;
    }

    private boolean finalizeReservedStockWithoutSoldQuantity(Connection conn, int productId, int quantity) {
        String query = "UPDATE products " +
                "SET reserved_quantity = CASE WHEN reserved_quantity >= ? THEN reserved_quantity - ? ELSE 0 END " +
                "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, quantity);
            ps.setInt(2, quantity);
            ps.setInt(3, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error finalizing reserved stock without sold quantity for product id={}", productId, e);
        }
        return false;
    }

    private boolean isUnknownColumn(SQLSyntaxErrorException e, String columnName) {
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        return ("42S22".equals(e.getSQLState()) || e.getErrorCode() == 1054)
                && message.contains(columnName.toLowerCase());
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
            ps.setInt(1, newStock);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error updating stock for product id={}", productId, e);
        }
        return false;
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
