package com.petshop.web;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import DAO.ProductDAO;
import DAO.ReviewDAO;
import DAO.WishlistDAO;
import Model.Product;
import Model.Review;
import Model.User;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces ProductDetailServlet (/product-detail), WishlistServlet (/wishlist)
 * and ToggleWishlistServlet (/toggle-wishlist) 1:1 — same view names, same
 * session attributes, same JSON bodies, same redirect targets.
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

    @PostMapping("/toggle-wishlist")
    public Object toggleWishlist(
            @RequestParam(value = "productId", required = false) String productIdRaw,
            @RequestParam(value = "redirect", required = false) String redirect,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            @RequestHeader(value = "Accept", required = false) String accept,
            HttpSession session,
            jakarta.servlet.http.HttpServletRequest request) {
        String fallbackUrl = request.getContextPath() + "/shop";
        boolean ajaxRequest = isAjaxRequest(requestedWith, accept);

        User user = (User) session.getAttribute("user");
        if (user == null) {
            String loginRedirect = redirect != null && !redirect.isBlank() ? redirect : fallbackUrl;
            String loginUrl = request.getContextPath() + "/login?redirect="
                    + java.net.URLEncoder.encode(loginRedirect, java.nio.charset.StandardCharsets.UTF_8);

            if (ajaxRequest) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .body("{\"success\":false,\"authenticated\":false,\"loginUrl\":\"" + escapeJson(loginUrl) + "\"}");
            }
            return "redirect:" + loginUrl;
        }

        try {
            int productId = parseProductId(productIdRaw);
            boolean isWishlisted = wishlistDAO.toggleWishlistAndReturnState(user.getId(), productId);

            String message = isWishlisted
                    ? "Đã thêm sản phẩm vào danh sách yêu thích."
                    : "Đã xóa sản phẩm khỏi danh sách yêu thích.";

            if (ajaxRequest) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .body("{\"success\":true,\"authenticated\":true,\"wishlisted\":" + isWishlisted
                                + ",\"message\":\"" + escapeJson(message) + "\"}");
            }

            session.setAttribute("success", message);
        } catch (IllegalArgumentException e) {
            String message = "Sản phẩm không hợp lệ.";

            if (ajaxRequest) {
                return ResponseEntity.badRequest()
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .body("{\"success\":false,\"authenticated\":true,\"message\":\"" + escapeJson(message) + "\"}");
            }

            session.setAttribute("error", message);
        } catch (Exception e) {
            logger.error("Unable to toggle wishlist for user id={} productIdRaw={}",
                    user.getId(), productIdRaw, e);
            String message = "Không thể cập nhật danh sách yêu thích.";

            if (ajaxRequest) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .body("{\"success\":false,\"authenticated\":true,\"message\":\"" + escapeJson(message) + "\"}");
            }

            session.setAttribute("error", message);
        }

        String target = (redirect != null && !redirect.isBlank()) ? redirect : fallbackUrl;
        return "redirect:" + target;
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

    private boolean isAjaxRequest(String requestedWith, String accept) {
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
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
