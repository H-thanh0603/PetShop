package controller.shop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import DAO.ProductDAO;
import Model.Product;

/**
 * API endpoint cho Search Autocomplete
 * Trả về danh sách sản phẩm gợi ý dựa trên keyword
 */
@WebServlet("/api/search-autocomplete")
public class SearchAutocompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProductDAO productDAO = new ProductDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String keyword = request.getParameter("q");
        
        if (keyword == null || keyword.trim().isEmpty()) {
            response.getWriter().write("[]");
            return;
        }
        
        keyword = keyword.trim();
        keyword = keyword.substring(0, Math.min(keyword.length(), 100));
        
        // Giới hạn 8 kết quả cho autocomplete
        List<Product> products = productDAO.searchProductsLimit(keyword, 8);
        
        // Chuyển đổi sang JSON đơn giản (chỉ lấy id, name, image, price)
        List<Map<String, Object>> results = new ArrayList<>();
        for (Product p : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("image", p.getImage() != null ? p.getImage() : "");
            item.put("price", p.getPrice());
            results.add(item);
        }
        
        response.getWriter().write(gson.toJson(results));
    }
}
