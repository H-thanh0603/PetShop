package controller.shop;

import DAO.ProductDAO;
import DAO.ReviewDAO;
import DAO.WishlistDAO;
import Model.Product;
import Model.Review;
import Model.User;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/product-detail")
public class ProductDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ProductDetailServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String idRaw = request.getParameter("id");
            if (idRaw == null || idRaw.isEmpty()) {
                response.sendRedirect("shop");
                return;
            }

            int id = Integer.parseInt(idRaw);
            
            ProductDAO pDao = new ProductDAO();
            Product p = pDao.getProductById(id);
            
            if (p == null) {
                request.getSession().setAttribute("error", "Sản phẩm không tồn tại hoặc đã bị xóa.");
                response.sendRedirect("shop");
                return;
            }

            ReviewDAO rDao = new ReviewDAO();
            List<Review> listReviews = rDao.getReviewsByProductId(id);

            User user = (User) request.getSession().getAttribute("user");
            boolean hasReviewed = user != null && rDao.hasUserReviewedProduct(user.getId(), id);
            boolean hasPurchased = user != null && new ReviewDAO().hasUserPurchasedProduct(user.getId(), id);
            Set<Integer> wishlistIds = java.util.Collections.emptySet();
            if (user != null) {
                WishlistDAO wishlistDAO = new WishlistDAO();
                wishlistIds = wishlistDAO.getWishlistProductIdsByUserId(user.getId());
                p.setWishlisted(wishlistIds.contains(p.getId()));
            }
            
            request.setAttribute("detail", p);
            request.setAttribute("listReviews", listReviews);
            request.setAttribute("hasReviewed", hasReviewed);
            request.setAttribute("hasPurchased", hasPurchased);
            request.setAttribute("wishlistProductIds", wishlistIds);
            
            List<Product> listRelated = pDao.getRelatedProducts(id); 
            if (!wishlistIds.isEmpty()) {
                for (Product related : listRelated) {
                    related.setWishlisted(wishlistIds.contains(related.getId()));
                }
            }
            request.setAttribute("relatedProducts", listRelated);
            
            request.getRequestDispatcher("/pages/shop/product.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            logger.warn("Invalid product id parameter: {}", request.getParameter("id"));
            request.getSession().setAttribute("error", "Mã sản phẩm không hợp lệ.");
            response.sendRedirect("shop");
        } catch (Exception e) {
            logger.error("Error loading product detail for id={}", request.getParameter("id"), e);
            request.getSession().setAttribute("error", "Không thể tải thông tin sản phẩm. Vui lòng thử lại.");
            response.sendRedirect("shop");
        }
    }
}
