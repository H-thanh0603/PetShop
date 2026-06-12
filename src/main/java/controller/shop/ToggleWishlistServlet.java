package controller.shop;

import DAO.WishlistDAO;
import Model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/toggle-wishlist")
public class ToggleWishlistServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ToggleWishlistServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        String redirect = request.getParameter("redirect");
        String fallbackUrl = request.getContextPath() + "/shop";
        boolean ajaxRequest = isAjaxRequest(request);

        if (user == null) {
            String loginRedirect = redirect != null && !redirect.isBlank() ? redirect : fallbackUrl;
            String loginUrl = request.getContextPath() + "/login?redirect="
                    + URLEncoder.encode(loginRedirect, StandardCharsets.UTF_8);

            if (ajaxRequest) {
                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "{\"success\":false,\"authenticated\":false,\"loginUrl\":\"" + escapeJson(loginUrl) + "\"}");
                return;
            }

            response.sendRedirect(loginUrl);
            return;
        }

        try {
            int productId = parseProductId(request.getParameter("productId"));
            WishlistDAO wishlistDAO = new WishlistDAO();
            boolean isWishlisted = wishlistDAO.toggleWishlistAndReturnState(user.getId(), productId);

            String message = isWishlisted
                    ? "Đã thêm sản phẩm vào danh sách yêu thích."
                    : "Đã xóa sản phẩm khỏi danh sách yêu thích.";

            if (ajaxRequest) {
                writeJson(response, HttpServletResponse.SC_OK,
                        "{\"success\":true,\"authenticated\":true,\"wishlisted\":" + isWishlisted
                                + ",\"message\":\"" + escapeJson(message) + "\"}");
                return;
            }

            session.setAttribute("success", message);
        } catch (IllegalArgumentException e) {
            String message = "Sản phẩm không hợp lệ.";

            if (ajaxRequest) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        "{\"success\":false,\"authenticated\":true,\"message\":\"" + escapeJson(message) + "\"}");
                return;
            }

            session.setAttribute("error", message);
        } catch (Exception e) {
            logger.error("Unable to toggle wishlist for user id={} productIdRaw={}",
                    user.getId(), request.getParameter("productId"), e);
            String message = "Không thể cập nhật danh sách yêu thích.";

            if (ajaxRequest) {
                writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "{\"success\":false,\"authenticated\":true,\"message\":\"" + escapeJson(message) + "\"}");
                return;
            }

            session.setAttribute("error", message);
        }

        response.sendRedirect((redirect != null && !redirect.isBlank()) ? redirect : fallbackUrl);
    }

    private int parseProductId(String rawProductId) {
        if (rawProductId == null || rawProductId.isBlank()) {
            throw new IllegalArgumentException("Missing productId");
        }

        try {
            int productId = Integer.parseInt(rawProductId.trim());
            if (productId <= 0) {
                throw new IllegalArgumentException("Invalid productId");
            }
            return productId;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid productId", e);
        }
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }

    private void writeJson(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
