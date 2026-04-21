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

@WebServlet("/product-detail")
public class ProductDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
                response.sendRedirect("shop");
                return;
            }

            ReviewDAO rDao = new ReviewDAO();
            List<Review> listReviews = rDao.getReviewsByProductId(id);

            User user = (User) request.getSession().getAttribute("user");
            boolean hasReviewed = user != null && rDao.hasUserReviewedProduct(user.getId(), id);
            Set<Integer> wishlistIds = java.util.Collections.emptySet();
            if (user != null) {
                WishlistDAO wishlistDAO = new WishlistDAO();
                wishlistIds = wishlistDAO.getWishlistProductIdsByUserId(user.getId());
                p.setWishlisted(wishlistIds.contains(p.getId()));
            }
            
            request.setAttribute("detail", p);
            request.setAttribute("listReviews", listReviews);
            request.setAttribute("hasReviewed", hasReviewed);
            request.setAttribute("wishlistProductIds", wishlistIds);
            
            List<Product> listRelated = pDao.getRelatedProducts(id); 
            if (!wishlistIds.isEmpty()) {
                for (Product related : listRelated) {
                    related.setWishlisted(wishlistIds.contains(related.getId()));
                }
            }
            request.setAttribute("relatedProducts", listRelated);
            
            request.getRequestDispatcher("/pages/shop/product.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("shop");
        }
    }
}
