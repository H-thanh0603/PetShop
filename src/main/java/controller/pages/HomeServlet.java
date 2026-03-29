package controller.pages;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import DAO.ProductDAO;
import Model.HomeCategoryCard;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProductDAO productDAO = new ProductDAO();

        List<String> popularCategories = productDAO.getPopularCategories(12);
        if (popularCategories.isEmpty()) {
            popularCategories = productDAO.getAllCategories();
        }

        List<HomeCategoryCard> homeCategories = new ArrayList<>();
        for (String category : popularCategories) {
            if (category == null || category.trim().isEmpty()) {
                continue;
            }
            String cleanCategory = category.trim();
            homeCategories.add(new HomeCategoryCard(
                cleanCategory,
                resolveIconKey(cleanCategory),
                request.getContextPath() + "/shop?category=" + URLEncoder.encode(cleanCategory, StandardCharsets.UTF_8)
            ));
        }

        request.setAttribute("homeCategories", homeCategories);
        request.getRequestDispatcher("/pages/main/home.jsp").forward(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    private String resolveIconKey(String category) {
        String normalized = normalize(category);

        if (normalized.contains("thuc an") || normalized.contains("pate") || normalized.contains("sua")) {
            return "food";
        }
        if (normalized.contains("ve sinh") || normalized.contains("tam") || normalized.contains("shampoo")) {
            return "hygiene";
        }
        if (normalized.contains("cat")) {
            return "litter";
        }
        if (normalized.contains("an uong") || normalized.contains("bat") || normalized.contains("nuoc")) {
            return "bowl";
        }
        if (normalized.contains("do choi") || normalized.contains("huan luyen")) {
            return "toy";
        }
        if (normalized.contains("suc khoe") || normalized.contains("thuoc") || normalized.contains("vitamin")) {
            return "health";
        }
        if (normalized.contains("van chuyen") || normalized.contains("balo") || normalized.contains("long")) {
            return "carrier";
        }
        if (normalized.contains("giam gia") || normalized.contains("sale") || normalized.contains("khuyen mai")) {
            return "sale";
        }
        if (normalized.contains("cho")) {
            return "food";
        }
        if (normalized.contains("meo")) {
            return "food";
        }
        return "food";
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value.toLowerCase(), Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        return normalized.replace('đ', 'd');
    }
}

