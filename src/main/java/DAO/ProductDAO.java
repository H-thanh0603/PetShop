package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import Context.DBContext;
import Model.Product;

public class ProductDAO {

    // 1. Lấy danh sách tất cả (Cho trang Shop)
    // Dùng logic của bạn (HEAD) để lấy description
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM Products"; 

        try {
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Xử lý nếu mô tả bị null
                String desc = rs.getString("description");
                if (desc == null) desc = ""; 

                list.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("image"),
                    rs.getDouble("price"),
                    rs.getInt("discount"),
                    desc 
                ));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Lấy 1 sản phẩm theo ID (Cho trang Chi tiết)
    // Dùng logic của bạn (HEAD) để lấy description
    public Product getProductById(int id) {
        String query = "SELECT * FROM Products WHERE id = ?";
        try {
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String desc = rs.getString("description");
                if (desc == null) desc = "Đang cập nhật thông tin sản phẩm...";

                return new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("image"),
                    rs.getDouble("price"),
                    rs.getInt("discount"),
                    desc 
                );
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    // 3. Thêm sản phẩm mới (Admin)
    public boolean addProduct(String name, String image, double price, int discount, String description) {
        String query = "INSERT INTO Products (name, image, price, discount, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, name);
            ps.setString(2, image);
            ps.setDouble(3, price);
            ps.setInt(4, discount);
            ps.setString(5, description); 
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 4. Cập nhật sản phẩm (Admin)

    public boolean updateProduct(int id, String name, String image, double price, int discount, String description) {
        String query = "UPDATE Products SET name = ?, image = ?, price = ?, discount = ?, description = ? WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, name);
            ps.setString(2, image);
            ps.setDouble(3, price);
            ps.setInt(4, discount);
            ps.setString(5, description);
            ps.setInt(6, id);
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 5. Xóa sản phẩm (Admin)
    public boolean deleteProduct(int id) {
        String query = "DELETE FROM Products WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 6. Đếm tổng sản phẩm (Thống kê)
    public int getTotalProducts() {
        String query = "SELECT COUNT(*) FROM Products";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // 7. Đếm sản phẩm đang giảm giá (Thống kê)
    public int getDiscountedProducts() {
        String query = "SELECT COUNT(*) FROM Products WHERE discount > 0";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
 // 8. Lấy 4 sản phẩm liên quan (trừ sản phẩm đang xem)
    public List<Product> getRelatedProducts(int excludeId) {
        List<Product> list = new ArrayList<>();

        String query = "SELECT * FROM Products WHERE id != ? LIMIT 4"; 

        try {
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, excludeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String desc = rs.getString("description");
                if (desc == null) desc = ""; 

            
                list.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("image"),
                    rs.getDouble("price"),
                    rs.getInt("discount"),
                    desc 
                ));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    
}