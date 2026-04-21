package controller.shop;

import DAO.WishlistDAO;
import Model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/toggle-wishlist")
public class ToggleWishlistServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        String redirect = request.getParameter("redirect");
        String fallbackUrl = request.getContextPath() + "/shop";

        if (user == null) {
            String loginRedirect = redirect != null && !redirect.isBlank() ? redirect : fallbackUrl;
            response.sendRedirect(request.getContextPath() + "/login?redirect=" +
                    URLEncoder.encode(loginRedirect, StandardCharsets.UTF_8));
            return;
        }

        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            WishlistDAO wishlistDAO = new WishlistDAO();
            boolean wasInWishlist = wishlistDAO.isInWishlist(user.getId(), productId);
            wishlistDAO.toggleWishlist(user.getId(), productId);

            session.setAttribute("success", wasInWishlist
                    ? "Đã xóa sản phẩm khỏi danh sách yêu thích."
                    : "Đã thêm sản phẩm vào danh sách yêu thích.");
        } catch (Exception e) {
            session.setAttribute("error", "Không thể cập nhật danh sách yêu thích.");
        }

        response.sendRedirect((redirect != null && !redirect.isBlank()) ? redirect : fallbackUrl);
    }
}
