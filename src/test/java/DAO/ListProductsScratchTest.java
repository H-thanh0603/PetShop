package DAO;

import org.junit.jupiter.api.Test;
import Context.DBContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ListProductsScratchTest {
    @Test
    public void testList() throws Exception {
        System.out.println("=== LISTING PRODUCTS ===");
        try (Connection conn = DBContext.getConnection();
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT id, name, price, stock, is_active, category FROM products")) {
                while (rs.next()) {
                    System.out.println(String.format("Product: ID=%d, Name=%s, Price=%s, Stock=%d, Active=%b, Cat=%s",
                        rs.getInt("id"), rs.getString("name"), rs.getBigDecimal("price").toPlainString(),
                        rs.getInt("stock"), rs.getBoolean("is_active"), rs.getString("category")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
