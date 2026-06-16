package services;

import Model.AiChatMessage;
import Model.User;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DeepSeekServiceTest {

    @Test
    public void testGetChatResponseFAQ() {
        DeepSeekService service = new DeepSeekService();
        List<AiChatMessage> history = new ArrayList<>();
        
        DeepSeekService.AiResponse response = service.getChatResponse("Shop ở đâu?", history, null);
        
        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertNotNull(response.getIntent());
        
        System.out.println("FAQ Answer: " + response.getAnswer());
        System.out.println("FAQ Intent: " + response.getIntent());
        System.out.println("Need Admin Support: " + response.isNeedAdminSupport());
    }

    @Test
    public void testGuestOrderCheckingDenied() {
        DeepSeekService service = new DeepSeekService();
        List<AiChatMessage> history = new ArrayList<>();
        
        DeepSeekService.AiResponse response = service.getChatResponse("Đơn hàng của tôi đang ở đâu?", history, null);
        
        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertTrue(response.getAnswer().contains("đăng nhập") || response.getAnswer().contains("Đăng nhập"), 
            "Should request guest login. Actual answer: " + response.getAnswer());
    }

    @Test
    public void testProductAdvice() {
        DeepSeekService service = new DeepSeekService();
        List<AiChatMessage> history = new ArrayList<>();
        
        DeepSeekService.AiResponse response = service.getChatResponse("Mèo con nên ăn gì?", history, null);
        
        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertNotNull(response.getIntent());
        
        System.out.println("Advice Answer: " + response.getAnswer());
        System.out.println("Advice Intent: " + response.getIntent());
        System.out.println("Related Product IDs: " + response.getRelatedProductIds());
    }

    @Test
    public void testEscalationLogicForComplaint() {
        DeepSeekService service = new DeepSeekService();
        List<AiChatMessage> history = new ArrayList<>();
        
        DeepSeekService.AiResponse response = service.getChatResponse("Tôi muốn khiếu nại về sản phẩm lỗi, nó bị hỏng.", history, null);
        
        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertTrue(response.isNeedAdminSupport(), "Complaints must trigger admin support");
    }

    @Test
    public void testCheckDbProducts() {
        try (java.sql.Connection conn = Context.DBContext.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            java.sql.ResultSet rs = stmt.executeQuery("SELECT id, name, price, stock, is_active FROM products LIMIT 10");
            System.out.println("=== PRODUCTS IN DATABASE ===");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") + ", Price: " + rs.getDouble("price") + ", Stock: " + rs.getInt("stock") + ", Active: " + rs.getInt("is_active"));
            }
            if (!found) {
                System.out.println("No products found in the database!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchProductsForAdvice() {
        DAO.ProductDAO dao = new DAO.ProductDAO();
        List<Model.Product> products = dao.searchProductsForAdvice("Mèo con nên ăn gì?", 5);
        System.out.println("=== SEARCH RESULTS FOR 'Mèo con nên ăn gì?' ===");
        for (Model.Product p : products) {
            System.out.println("ID: " + p.getId() + ", Name: " + p.getName() + ", Price: " + p.getPrice() + ", Stock: " + p.getStock() + ", Active: " + p.isActive());
        }
    }
}

