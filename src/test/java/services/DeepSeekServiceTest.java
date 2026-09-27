package services;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.AiChatMessage;
import com.petshop.model.User;
import com.petshop.repository.AiSupportSettingRepository;
import com.petshop.repository.CustomerSupportKnowledgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import services.ai.CommerceAgent;
import services.ai.PetShopCommerceBackend;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaTestConfig.class)
@ContextConfiguration(classes = com.petshop.PetShopApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DeepSeekServiceTest {

    @Autowired
    AiSupportSettingRepository settingRepository;
    @Autowired
    CustomerSupportKnowledgeRepository knowledgeRepository;

    DeepSeekService service;

    @BeforeEach
    void setUp() {
        PetShopCommerceBackend backend = new PetShopCommerceBackend(knowledgeRepository);
        CommerceAgent agent = new CommerceAgent(settingRepository, backend);
        service = new DeepSeekService(settingRepository, agent);
    }

    @Test
    public void testGetChatResponseFAQ() {
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
        List<AiChatMessage> history = new ArrayList<>();
        
        DeepSeekService.AiResponse response = service.getChatResponse("Đơn hàng của tôi đang ở đâu?", history, null);
        
        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertTrue(response.getAnswer().contains("đăng nhập") || response.getAnswer().contains("Đăng nhập"), 
            "Should request guest login. Actual answer: " + response.getAnswer());
    }

    @Test
    public void testProductAdvice() {
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
        List<AiChatMessage> history = new ArrayList<>();
        
        DeepSeekService.AiResponse response = service.getChatResponse("Tôi muốn khiếu nại về sản phẩm lỗi, nó bị hỏng.", history, null);
        
        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertTrue(response.isNeedAdminSupport(), "Complaints must trigger admin support");
    }

    @Test
    public void testCheckDbProducts() {
        try (java.sql.Connection conn = com.petshop.context.DBContext.getConnection();
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
        com.petshop.dao.ProductDAO dao = new com.petshop.dao.ProductDAO();
        List<com.petshop.model.Product> products = dao.searchProductsForAdvice("Mèo con nên ăn gì?", 5);
        System.out.println("=== SEARCH RESULTS FOR 'Mèo con nên ăn gì?' ===");
        for (com.petshop.model.Product p : products) {
            System.out.println("ID: " + p.getId() + ", Name: " + p.getName() + ", Price: " + p.getPrice() + ", Stock: " + p.getStock() + ", Active: " + p.isActive());
        }
    }
}

