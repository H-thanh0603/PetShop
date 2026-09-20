package com.petshop.web;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import DAO.ProductDAO;
import DAO.ReviewDAO;
import DAO.WishlistDAO;
import Model.Product;
import Model.Review;
import Model.User;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces ProductDetailServlet (/product-detail) and WishlistServlet
 * (/wishlist) 1:1 — same view names, same session attributes, same
 * redirect targets.
 */
@Controller
public class CatalogController {

    private static final Logger logger = LoggerFactory.getLogger(CatalogController.class);

    private final ProductDAO productDAO;
    private final ReviewDAO reviewDAO;
    private final WishlistDAO wishlistDAO;

    public CatalogController() {
        this(new ProductDAO(), new ReviewDAO(), new WishlistDAO());
    }

    CatalogController(ProductDAO productDAO, ReviewDAO reviewDAO, WishlistDAO wishlistDAO) {
        this.productDAO = productDAO;
        this.reviewDAO = reviewDAO;
        this.wishlistDAO = wishlistDAO;
    }

    @GetMapping("/product-detail")
    public String productDetail(
            @RequestParam(value = "id", required = false) String idRaw,
            Model model,
            HttpSession session) {
        try {
            if (idRaw == null || idRaw.isEmpty()) {
                return "redirect:/shop";
            }

            int id = Integer.parseInt(idRaw);
            Product p = productDAO.getProductById(id);

            if (p == null) {
                session.setAttribute("error", "Sản phẩm không tồn tại hoặc đã bị xóa.");
                return "redirect:/shop";
            }

            List<Review> listReviews = reviewDAO.getReviewsByProductId(id);

            User user = (User) session.getAttribute("user");
            boolean hasReviewed = user != null && reviewDAO.hasUserReviewedProduct(user.getId(), id);
            boolean hasPurchased = user != null && reviewDAO.hasUserPurchasedProduct(user.getId(), id);
            Set<Integer> wishlistIds = Collections.emptySet();
            if (user != null) {
                wishlistIds = wishlistDAO.getWishlistProductIdsByUserId(user.getId());
                p.setWishlisted(wishlistIds.contains(p.getId()));
            }

            model.addAttribute("detail", p);
            model.addAttribute("listReviews", listReviews);
            model.addAttribute("lengthReviews", listReviews.size());
            model.addAttribute("hasReviewed", hasReviewed);
            model.addAttribute("hasPurchased", hasPurchased);
            model.addAttribute("wishlistProductIds", wishlistIds);

            List<Product> listRelated = productDAO.getRelatedProducts(id);
            if (!wishlistIds.isEmpty()) {
                for (Product related : listRelated) {
                    related.setWishlisted(wishlistIds.contains(related.getId()));
                }
            }
            model.addAttribute("relatedProducts", listRelated);

            return "pages/shop/product";
        } catch (NumberFormatException e) {
            logger.warn("Invalid product id parameter: {}", idRaw);
            session.setAttribute("error", "Mã sản phẩm không hợp lệ.");
            return "redirect:/shop";
        } catch (Exception e) {
            logger.error("Error loading product detail for id={}", idRaw, e);
            session.setAttribute("error", "Không thể tải thông tin sản phẩm. Vui lòng thử lại.");
            return "redirect:/shop";
        }
    }

    @GetMapping("/wishlist")
    public String wishlist(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<Product> wishlistProducts = wishlistDAO.getWishlistProductsByUserId(user.getId());
        model.addAttribute("wishlistProducts", wishlistProducts);
        return "pages/shop/wishlist";
    }
}
