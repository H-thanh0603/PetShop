package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Context.DBContext;
import Model.Product;

public class ProductDAO {

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
                rs.getDouble("price"),
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

        return product;
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "GROUP BY p.id ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> getProductsByPetType(String petTypeCode) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT p.*, COALESCE(AVG(r.rating), 0) AS average_rating, COUNT(r.id) AS review_count " +
                "FROM products p " +
                "INNER JOIN pet_types pt ON p.pet_type_id = pt.id " +
                "LEFT JOIN reviews r ON r.product_id = p.id " +
                "WHERE pt.code = ? AND pt.is_active = 1 GROUP BY p.id ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, petTypeCode); ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> getProductsByPetTypeFallback(String petTypeName) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.category LIKE ? GROUP BY p.id ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, "%" + petTypeName + "%"); ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<String> getCategoriesByPetType(String petTypeCode) {
        List<String> list = new ArrayList<>();
        String query = "SELECT DISTINCT p.category FROM products p INNER JOIN pet_types pt ON p.pet_type_id = pt.id WHERE pt.code = ? AND p.category IS NOT NULL AND p.category != '' ORDER BY p.category";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, petTypeCode); ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(rs.getString("category")); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<String> getAllCategories() {
        List<String> list = new ArrayList<>();
        String query = "SELECT DISTINCT category FROM products WHERE category IS NOT NULL AND category != '' ORDER BY category";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(rs.getString("category")); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<String> getPopularCategories(int limit) {
        List<String> list = new ArrayList<>();
        String query = "SELECT p.category, SUM(oi.quantity) AS total_sold FROM order_items oi JOIN products p ON oi.product_id = p.id JOIN orders o ON oi.order_id = o.id WHERE o.status != 'Cancelled' AND p.category IS NOT NULL AND p.category != '' GROUP BY p.category ORDER BY total_sold DESC LIMIT ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { list.add(rs.getString("category")); } }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> getProductsByCategory(String category) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.category = ? GROUP BY p.id ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, category); ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS +
                "WHERE p.name LIKE ? OR p.description LIKE ? GROUP BY p.id ORDER BY p.name ASC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            String p = "%" + keyword + "%"; ps.setString(1, p); ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> searchProductsLimit(String keyword, int limit) {
        List<Product> list = new ArrayList<>();
        // ORDER BY: tên bắt đầu bằng keyword lên trước, sau đó mới đến chứa keyword ở giữa
        String query = PRODUCT_SELECT_WITH_REVIEWS +
                       "WHERE p.name LIKE ? OR p.description LIKE ? " +
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
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> getDiscountedProductsList() {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.discount > 0 GROUP BY p.id ORDER BY p.discount DESC, p.id DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> getDiscountedProductsPage(int page, int size) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.discount > 0 GROUP BY p.id ORDER BY p.discount DESC, p.id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, size); ps.setInt(2, Math.max(0, (page - 1) * size));
            ResultSet rs = ps.executeQuery(); while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public int getTotalDiscountedProductsCount() {
        String query = "SELECT COUNT(*) FROM products WHERE discount > 0";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public List<Product> getAllProductsPage(int page, int size) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "GROUP BY p.id ORDER BY p.id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, size); ps.setInt(2, Math.max(0, (page - 1) * size));
            ResultSet rs = ps.executeQuery(); while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public int getTotalProductsCount() {
        String query = "SELECT COUNT(*) FROM products";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
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
                "ORDER BY total_sold DESC, p.discount DESC, p.id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, size); ps.setInt(2, Math.max(0, (page - 1) * size));
            ResultSet rs = ps.executeQuery(); while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public int getTotalPopularProductsCount() {
        String query = "SELECT COUNT(*) FROM products";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public Product getProductById(int id) {
        try (Connection conn = DBContext.getConnection()) {
            return getProductById(conn, id);
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean addProduct(String name, String image, double price, int discount, String description) {
        String query = "INSERT INTO products (name, image, price, discount, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name); ps.setString(2, image); ps.setDouble(3, price);
            ps.setInt(4, discount); ps.setString(5, description);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateProduct(int id, String name, String image, double price, int discount, String description) {
        String query = "UPDATE products SET name = ?, image = ?, price = ?, discount = ?, description = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name); ps.setString(2, image); ps.setDouble(3, price);
            ps.setInt(4, discount); ps.setString(5, description); ps.setInt(6, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteProduct(int id) {
        String query = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public int getTotalProducts() {
        String query = "SELECT COUNT(*) FROM products";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public int getDiscountedProducts() {
        String query = "SELECT COUNT(*) FROM products WHERE discount > 0";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public List<Product> getRelatedProducts(int excludeId) {
        List<Product> list = new ArrayList<>();
        String catQuery = "SELECT category FROM products WHERE id = ?";
        String category = null;
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(catQuery)) {
            ps.setInt(1, excludeId); ResultSet rs = ps.executeQuery();
            if (rs.next()) { category = rs.getString("category"); }
        } catch (Exception e) { e.printStackTrace(); }
        if (category != null && !category.isEmpty()) {
            String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.id != ? AND p.category = ? GROUP BY p.id ORDER BY RAND() LIMIT 4";
            try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, excludeId); ps.setString(2, category);
                ResultSet rs = ps.executeQuery(); while (rs.next()) { list.add(mapProduct(rs)); }
            } catch (Exception e) { e.printStackTrace(); }
        }
        if (list.size() < 4) {
            StringBuilder ids = new StringBuilder(String.valueOf(excludeId));
            for (Product p : list) { ids.append(",").append(p.getId()); }
            String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.id NOT IN (" + ids + ") GROUP BY p.id ORDER BY RAND() LIMIT ?";
            try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, 4 - list.size()); ResultSet rs = ps.executeQuery();
                while (rs.next()) { list.add(mapProduct(rs)); }
            } catch (Exception e) { e.printStackTrace(); }
        }
        return list;
    }

    public List<Product> getProductsByPage(int index, int size) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "GROUP BY p.id ORDER BY p.id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, size); ps.setInt(2, (index - 1) * size);
            ResultSet rs = ps.executeQuery(); while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ========== STOCK MANAGEMENT ==========
    public boolean decreaseStock(int productId, int quantity) {
        try (Connection conn = DBContext.getConnection()) {
            return decreaseStock(conn, productId, quantity);
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return false;
    }

    public boolean increaseStock(int productId, int quantity) {
        try (Connection conn = DBContext.getConnection()) {
            return increaseStock(conn, productId, quantity);
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return false;
    }
    public int getStock(int productId) {
        String query = "SELECT stock FROM products WHERE id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, productId); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("stock");
        } catch (Exception e) { e.printStackTrace(); } return 0;
    }
    public boolean updateStock(int productId, int newStock) {
        String query = "UPDATE products SET stock = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, newStock); ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); } return false;
    }
    public List<Product> getLowStockProducts(int threshold) {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.stock < ? AND p.stock > 0 GROUP BY p.id ORDER BY p.stock ASC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, threshold); ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); } return list;
    }
    public List<Product> getOutOfStockProducts() {
        List<Product> list = new ArrayList<>();
        String query = PRODUCT_SELECT_WITH_REVIEWS + "WHERE p.stock <= 0 GROUP BY p.id ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); } return list;
    }
    // lay tat ca cac brand dang hoat dong ra lam filter
        public List<String> getAllBrands() {
            List<String> brands = new ArrayList<>();
            String sql = "SELECT DISTINCT brand FROM products WHERE brand IS NOT NULL ORDER BY brand";

            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    brands.add(rs.getString("brand"));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return brands;
        }

}
