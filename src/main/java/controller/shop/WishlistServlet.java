package controller.shop;

import DAO.WishlistDAO;
import Model.Product;
import Model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/wishlist")
public class WishlistServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final WishlistDAO wishlistDAO = new WishlistDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        List<Product> wishlistProducts = wishlistDAO.getWishlistProductsByUserId(user.getId());

        request.setAttribute("wishlistProducts", wishlistProducts);
        request.getRequestDispatcher("/pages/shop/wishlist.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;

        // Nếu chưa đăng nhập, trả về lỗi hoặc chuyển hướng
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập.");
            return;
        }

        String action = request.getParameter("action");
        String productIdStr = request.getParameter("productId");

        if ("toggle".equals(action) && productIdStr != null) {
            try {
                int productId = Integer.parseInt(productIdStr);
                // Thực hiện đảo ngược trạng thái yêu thích trong DB
                boolean isNowWishlisted = wishlistDAO.toggleWishlistAndReturnState(user.getId(), productId);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": true, \"isWishlisted\": " + isNowWishlisted + "}");
                return;
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"success\": false, \"message\": \"Lỗi xử lý hệ thống.\"}");
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/wishlist");
    }
}
