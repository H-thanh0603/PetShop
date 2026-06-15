package controller.pages;

import java.io.IOException;
import java.util.List;

import DAO.ProductDAO;
import Model.Product;
import Model.User; // Bổ sung import để nhận diện thực thể User
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // Bổ sung import để làm việc với Session

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Giữ nguyên toàn bộ logic lấy sản phẩm bán chạy cũ của bạn
        List<Product> popularProducts = productDAO.getPopularProductsPage(1, 5);
        int totalProducts = productDAO.getTotalPopularProductsCount();

        request.setAttribute("popularProducts", popularProducts);
        request.setAttribute("totalProducts", totalProducts);

        // 2. CHÈN TÍNH NĂNG 3: Kiểm tra đăng nhập và lấy gợi ý mua lại sản phẩm tiêu dùng nhanh
        HttpSession session = request.getSession(false);
        if (session != null) {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser != null) {
                // Gọi hàm lấy gợi ý từ ProductDAO đã viết ở bước trước
                List<Product> repurchaseProducts = productDAO.getRepurchaseSuggestionsByUserId(currentUser.getId());

                // Nếu người dùng có sản phẩm gợi ý, đẩy data ra request attribute
                if (repurchaseProducts != null && !repurchaseProducts.isEmpty()) {
                    request.setAttribute("repurchaseProducts", repurchaseProducts);
                }
            }
        }

        // 3. Giữ nguyên file đích điều hướng gốc của hệ thống là /pages/main/home.jsp
        request.getRequestDispatcher("/pages/main/home.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}