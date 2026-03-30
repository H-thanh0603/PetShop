package controller.shop;

import java.io.IOException;
import java.util.List;
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
    private Gson gson = new Gson();

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
        
        // Giới hạn 8 kết quả cho autocomplete
        List<Product> products = productDAO.searchProductsLimit(keyword, 8);
        
        // Chuyển đổi sang JSON đơn giản (chỉ lấy id, name, image, price)
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            if (i > 0) json.append(",");
            json.append("{");
            json.append("\"id\":").append(p.getId()).append(",");
            json.append("\"name\":\"").append(escapeJson(p.getName())).append("\",");
            json.append("\"image\":\"").append(escapeJson(p.getImage() != null ? p.getImage() : "")).append("\",");
            json.append("\"price\":").append(p.getPrice());
            json.append("}");
        }
        json.append("]");
        
        response.getWriter().write(json.toString());
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
