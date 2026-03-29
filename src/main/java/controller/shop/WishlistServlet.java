package controller.shop;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import DAO.WishlistDAO;
import Model.Product;
import Model.User;

/**
 * Servlet xử lý Wishlist - Sản phẩm yêu thích
 */
@WebServlet(urlPatterns = {"/wishlist", "/api/wishlist"})
public class WishlistServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private WishlistDAO wishlistDAO = new WishlistDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            if (request.getServletPath().equals("/api/wishlist")) {
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Chưa đăng nhập\"}");
            } else {
                session.setAttribute("redirectAfterLogin", request.getContextPath() + "/wishlist");
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("check".equals(action)) {
            // API kiểm tra sản phẩm có trong wishlist không
            response.setContentType("application/json");
            int productId = Integer.parseInt(request.getParameter("productId"));
            boolean inWishlist = wishlistDAO.isInWishlist(user.getId(), productId);
            response.getWriter().write("{\"inWishlist\":" + inWishlist + "}");
        } else if ("count".equals(action)) {
            // API đếm số sản phẩm trong wishlist
            response.setContentType("application/json");
            int count = wishlistDAO.countWishlist(user.getId());
            response.getWriter().write("{\"count\":" + count + "}");
        } else {
            // Hiển thị trang wishlist
            List<Product> wishlist = wishlistDAO.getWishlistByUserId(user.getId());
            request.setAttribute("wishlist", wishlist);
            request.getRequestDispatcher("/pages/shop/wishlist.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.getWriter().write("{\"success\":false,\"message\":\"Vui lòng đăng nhập\"}");
            return;
        }
        
        String action = request.getParameter("action");
        int productId = Integer.parseInt(request.getParameter("productId"));
        
        boolean success = false;
        String message = "";
        
        if ("add".equals(action)) {
            success = wishlistDAO.addToWishlist(user.getId(), productId);
            message = success ? "Đã thêm vào yêu thích" : "Sản phẩm đã có trong danh sách";
        } else if ("remove".equals(action)) {
            success = wishlistDAO.removeFromWishlist(user.getId(), productId);
            message = success ? "Đã xóa khỏi yêu thích" : "Không thể xóa";
        } else if ("toggle".equals(action)) {
            if (wishlistDAO.isInWishlist(user.getId(), productId)) {
                success = wishlistDAO.removeFromWishlist(user.getId(), productId);
                message = "Đã xóa khỏi yêu thích";
            } else {
                success = wishlistDAO.addToWishlist(user.getId(), productId);
                message = "Đã thêm vào yêu thích";
            }
        }
        
        int count = wishlistDAO.countWishlist(user.getId());
        boolean inWishlist = wishlistDAO.isInWishlist(user.getId(), productId);
        
        response.getWriter().write("{\"success\":" + success + 
                                   ",\"message\":\"" + message + "\"" +
                                   ",\"count\":" + count +
                                   ",\"inWishlist\":" + inWishlist + "}");
    }
}
