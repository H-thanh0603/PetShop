package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import Context.DBContext;
import Model.Product;

public class ProductDAO {

    private Product mapProduct(ResultSet rs) throws Exception {
        String desc = rs.getString("description");
        if (desc == null) desc = "";
        String cat = "";
        try { cat = rs.getString("category"); if (cat == null) cat = ""; } catch (Exception e) {}

        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("image"),
            rs.getDouble("price"),
            rs.getInt("discount"),
            desc,
            cat
        );
    }

    // 1. Lấy danh sách tất cả
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy sản phẩm theo loại thú cưng
    public List<Product> getProductsByPetType(String petTypeCode) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT p.* FROM products p " +
                       "INNER JOIN pet_types pt ON p.pet_type_id = pt.id " +
                       "WHERE pt.code = ? AND pt.is_active = 1";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, petTypeCode);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Lấy sản phẩm theo loại thú cưng (fallback)
    public List<Product> getProductsByPetTypeFallback(String petTypeName) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products WHERE category LIKE ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, "%" + petTypeName + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Lấy danh sách categories theo loại thú cưng
    public List<String> getCategoriesByPetType(String petTypeCode) {
        List<String> list = new ArrayList<>();
        String query = "SELECT DISTINCT p.category FROM products p " +
                       "INNER JOIN pet_types pt ON p.pet_type_id = pt.id " +
                       "WHERE pt.code = ? AND p.category IS NOT NULL AND p.category != '' ORDER BY p.category";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, petTypeCode);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(rs.getString("category")); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Lấy danh sách categories
    public List<String> getAllCategories() {
        List<String> list = new ArrayList<>();
        String query = "SELECT DISTINCT category FROM products WHERE category IS NOT NULL AND category != '' ORDER BY category";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(rs.getString("category")); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Lấy danh mục được mua nhiều
    public List<String> getPopularCategories(int limit) {
        List<String> list = new ArrayList<>();
        String query = "SELECT p.category, SUM(oi.quantity) AS total_sold " +
                       "FROM order_items oi JOIN products p ON oi.product_id = p.id " +
                       "JOIN orders o ON oi.order_id = o.id " +
                       "WHERE o.status != 'Cancelled' AND p.category IS NOT NULL AND p.category != '' " +
                       "GROUP BY p.category ORDER BY total_sold DESC, p.category ASC LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { list.add(rs.getString("category")); }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Lấy sản phẩm theo category
    public List<Product> getProductsByCategory(String category) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products WHERE category = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Tìm kiếm sản phẩm
    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products WHERE name LIKE ? OR description LIKE ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Tìm kiếm sản phẩm với giới hạn (cho autocomplete)
    public List<Product> searchProductsLimit(String keyword, int limit) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products WHERE name LIKE ? OR description LIKE ? LIMIT ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setInt(3, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Lấy sản phẩm giảm giá
    public List<Product> getDiscountedProductsList() {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products WHERE discount > 0 ORDER BY discount DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> getDiscountedProductsPage(int page, int size) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products WHERE discount > 0 ORDER BY discount DESC, id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, size);
            ps.setInt(2, Math.max(0, (page - 1) * size));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public int getTotalDiscountedProductsCount() {
        String query = "SELECT COUNT(*) FROM products WHERE discount > 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public List<Product> getAllProductsPage(int page, int size) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products ORDER BY id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, size);
            ps.setInt(2, Math.max(0, (page - 1) * size));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public int getTotalProductsCount() {
        String query = "SELECT COUNT(*) FROM products";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public List<Product> getPopularProductsPage(int page, int size) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT p.*, COALESCE(SUM(oi.quantity), 0) AS total_sold " +
                       "FROM products p LEFT JOIN order_items oi ON oi.product_id = p.id " +
                       "LEFT JOIN orders o ON o.id = oi.order_id AND o.status != 'Cancelled' " +
                       "GROUP BY p.id ORDER BY total_sold DESC, p.discount DESC, p.id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, size);
            ps.setInt(2, Math.max(0, (page - 1) * size));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public int getTotalPopularProductsCount() {
        String query = "SELECT COUNT(*) FROM products";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // 2. Lấy 1 sản phẩm theo ID
    public Product getProductById(int id) {
        String query = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String desc = rs.getString("description");
                if (desc == null) desc = "Đang cập nhật thông tin sản phẩm...";
                return new Product(
                    rs.getInt("id"), rs.getString("name"), rs.getString("image"),
                    rs.getDouble("price"), rs.getInt("discount"), desc
                );
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // 3. Thêm sản phẩm mới (Admin)
    public boolean addProduct(String name, String image, double price, int discount, String description) {
        String query = "INSERT INTO products (name, image, price, discount, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name); ps.setString(2, image); ps.setDouble(3, price);
            ps.setInt(4, discount); ps.setString(5, description);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // 4. Cập nhật sản phẩm (Admin)
    public boolean updateProduct(int id, String name, String image, double price, int discount, String description) {
        String query = "UPDATE products SET name = ?, image = ?, price = ?, discount = ?, description = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name); ps.setString(2, image); ps.setDouble(3, price);
            ps.setInt(4, discount); ps.setString(5, description); ps.setInt(6, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // 5. Xóa sản phẩm (Admin)
    public boolean deleteProduct(int id) {
        String query = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // 6. Đếm tổng sản phẩm
    public int getTotalProducts() {
        String query = "SELECT COUNT(*) FROM products";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // 7. Đếm sản phẩm đang giảm giá
    public int getDiscountedProducts() {
        String query = "SELECT COUNT(*) FROM products WHERE discount > 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // 8. Lấy sản phẩm liên quan (cùng category, trừ sản phẩm đang xem)
    public List<Product> getRelatedProducts(int excludeId) {
        List<Product> list = new ArrayList<>();
        // Lấy category của sản phẩm hiện tại
        String catQuery = "SELECT category FROM products WHERE id = ?";
        String category = null;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(catQuery)) {
            ps.setInt(1, excludeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { category = rs.getString("category"); }
        } catch (Exception e) { e.printStackTrace(); }

        // Lấy sản phẩm cùng category trước
        if (category != null && !category.isEmpty()) {
            String query = "SELECT * FROM products WHERE id != ? AND category = ? ORDER BY RAND() LIMIT 4";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, excludeId);
                ps.setString(2, category);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) { list.add(mapProduct(rs)); }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // Nếu chưa đủ 4, lấy thêm sản phẩm khác
        if (list.size() < 4) {
            StringBuilder excludeIds = new StringBuilder(String.valueOf(excludeId));
            for (Product p : list) { excludeIds.append(",").append(p.getId()); }
            String query = "SELECT * FROM products WHERE id NOT IN (" + excludeIds + ") ORDER BY RAND() LIMIT ?";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, 4 - list.size());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) { list.add(mapProduct(rs)); }
            } catch (Exception e) { e.printStackTrace(); }
        }
        return list;
    }

    // 9. Phân trang danh sách sản phẩm
    public List<Product> getProductsByPage(int index, int size) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products LIMIT ? OFFSET ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, size);
            ps.setInt(2, (index - 1) * size);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ========== STOCK MANAGEMENT ==========

    // Giảm tồn kho khi đặt hàng
    public boolean decreaseStock(int productId, int quantity) {
        String query = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, quantity); ps.setInt(2, productId); ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Tăng tồn kho (khi hủy đơn)
    public boolean increaseStock(int productId, int quantity) {
        String query = "UPDATE products SET stock = stock + ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, quantity); ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Lấy tồn kho
    public int getStock(int productId) {
        String query = "SELECT stock FROM products WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("stock");
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // Cập nhật tồn kho
    public boolean updateStock(int productId, int newStock) {
        String query = "UPDATE products SET stock = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, newStock); ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Lấy sản phẩm sắp hết hàng
    public List<Product> getLowStockProducts(int threshold) {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products WHERE stock < ? AND stock > 0 ORDER BY stock ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, threshold);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Lấy sản phẩm hết hàng
    public List<Product> getOutOfStockProducts() {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products WHERE stock <= 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapProduct(rs)); }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}
