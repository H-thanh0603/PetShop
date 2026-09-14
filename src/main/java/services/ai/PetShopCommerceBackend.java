package services.ai;

import DAO.CustomerSupportKnowledgeDAO;
import DAO.OrderDAO;
import DAO.ProductDAO;
import Model.CustomerSupportKnowledge;
import Model.Order;
import Model.Product;
import Model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * PetShop's {@code StorefrontBackend} (Commerce Agents concept, ported to Java).
 * Every method runs server-side with the host's session identity; the model only
 * ever sees the returned data (fenced context), never credentials or other
 * users' rows. Guest order reads are refused here — defense in depth behind the
 * agent's own guest guard.
 */
public class PetShopCommerceBackend {
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final CustomerSupportKnowledgeDAO knowledgeDAO = new CustomerSupportKnowledgeDAO();

    public record SessionContext(Integer userId, boolean guest) {
        public static SessionContext of(User user) {
            return user == null ? new SessionContext(null, true)
                    : new SessionContext(user.getId(), false);
        }
    }

    public List<Product> searchProducts(String query, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 10));
        if (query == null || query.isBlank()) return List.of();
        try {
            return productDAO.searchProductsForAdvice(query.trim(), safeLimit);
        } catch (Exception e) {
            return List.of();
        }
    }

    public Product getProductDetails(int productId) {
        try {
            Product p = productDAO.getProductById(productId);
            return (p != null && p.isActive()) ? p : null;
        } catch (Exception e) {
            return null;
        }
    }

    public List<Product> recommendProducts(String query, int limit) {
        // Recommendations reuse proven search ranking; the agent explains the "why".
        return searchProducts(query, limit);
    }

    public List<Order> getOrders(SessionContext session, int limit) {
        if (session.guest() || session.userId() == null) return List.of();
        try {
            List<Order> all = orderDAO.getOrdersByUserId(session.userId());
            return all.subList(0, Math.min(all.size(), Math.max(1, Math.min(limit, 5))));
        } catch (Exception e) {
            return List.of();
        }
    }

    public Order getOrder(SessionContext session, int orderId) {
        if (session.guest() || session.userId() == null) return null;
        try {
            Order o = orderDAO.getOrderById(orderId);
            return (o != null && o.getUserId() == session.userId()) ? o : null;
        } catch (Exception e) {
            return null;
        }
    }

    public List<CustomerSupportKnowledge> searchPolicies(String query) {
        try {
            List<CustomerSupportKnowledge> all = knowledgeDAO.getAllActive();
            if (query == null || query.isBlank()) return all.subList(0, Math.min(all.size(), 5));
            String q = query.toLowerCase();
            List<CustomerSupportKnowledge> hits = new ArrayList<>();
            for (CustomerSupportKnowledge k : all) {
                String hay = ((k.getTitle() == null ? "" : k.getTitle()) + " "
                        + (k.getContent() == null ? "" : k.getContent()) + " "
                        + (k.getCategory() == null ? "" : k.getCategory())).toLowerCase();
                if (hay.contains(q) || q.length() < 3) hits.add(k);
                if (hits.size() >= 5) break;
            }
            return hits;
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<CustomerSupportKnowledge> allPolicies() {
        try {
            return knowledgeDAO.getAllActive();
        } catch (Exception e) {
            return List.of();
        }
    }
}
