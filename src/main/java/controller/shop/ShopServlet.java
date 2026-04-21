package controller.shop;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.ProductDAO;
import DAO.PetTypeDAO;
import DAO.WishlistDAO;
import Model.Product;
import Model.PetType;
import Model.User;

@WebServlet("/shop")
public class ShopServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 12;
    private static final int BEST_SELLER_SIZE = 6;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        ProductDAO productDao = new ProductDAO();
        PetTypeDAO petTypeDao = new PetTypeDAO();

        // Lấy params
        String category = request.getParameter("category");
        String search = request.getParameter("search");
        if (search != null) {
            search = search.substring(0, Math.min(search.length(), 100));
        }
        String sort = request.getParameter("sort");
        String priceRange = request.getParameter("priceRange");
        String discountOnly = request.getParameter("discountOnly");
        String pet = request.getParameter("pet");
        int page = parsePage(request.getParameter("page"));
        int salePage = parsePage(request.getParameter("salePage"));
        int catalogPage = parsePage(request.getParameter("catalogPage"));

        // Lấy danh sách loại thú cưng active
        List<PetType> activePetTypes = petTypeDao.getActivePetTypes();
        request.setAttribute("petTypes", activePetTypes);
        
        User currentUser = (User) request.getSession().getAttribute("user");
        Set<Integer> wishlistProductIds = java.util.Collections.emptySet();
        if (currentUser != null) {
            WishlistDAO wishlistDAO = new WishlistDAO();
            wishlistProductIds = wishlistDAO.getWishlistProductIdsByUserId(currentUser.getId());
        }
        request.setAttribute("wishlistProductIds", wishlistProductIds);

        // Lấy thông tin loại thú cưng đang chọn
        PetType selectedPetType = null;
        if (pet != null && !pet.trim().isEmpty()) {
            selectedPetType = petTypeDao.getPetTypeByCode(pet.trim());
        }

        // Lấy sản phẩm
        List<Product> products;
        List<String> categories;

        if (search != null && !search.trim().isEmpty()) {
            products = productDao.searchProducts(search.trim());
            categories = productDao.getAllCategories();
        } else if (category != null && !category.trim().isEmpty()) {
            products = productDao.getProductsByCategory(category.trim());
            categories = productDao.getAllCategories();
        } else if (pet != null && !pet.trim().isEmpty()) {
            products = productDao.getProductsByPetType(pet.trim());
            if (products.isEmpty() && selectedPetType != null) {
                products = productDao.getProductsByPetTypeFallback(selectedPetType.getName());
            }
            categories = productDao.getCategoriesByPetType(pet.trim());
            if (categories.isEmpty() && selectedPetType != null) {
                categories = productDao.getAllCategories();
                String petName = selectedPetType.getName();
                categories.removeIf(c -> !c.contains(petName));
            }
        } else {
            products = productDao.getAllProducts();
            categories = productDao.getAllCategories();
        }

        // Lọc giảm giá
        if ("true".equals(discountOnly)) {
            products.removeIf(p -> p.getDiscount() <= 0);
        }

        // Lọc theo khoảng giá
        if (priceRange != null && !priceRange.isEmpty()) {
            switch (priceRange) {
                case "under100": products.removeIf(p -> p.getPrice() >= 100000); break;
                case "100to300": products.removeIf(p -> p.getPrice() < 100000 || p.getPrice() > 300000); break;
                case "300to500": products.removeIf(p -> p.getPrice() < 300000 || p.getPrice() > 500000); break;
                case "above500": products.removeIf(p -> p.getPrice() <= 500000); break;
            }
        }

        // Sắp xếp
        if (sort != null) {
            switch (sort) {
                case "price-asc": products.sort(Comparator.comparingDouble(Product::getPrice)); break;
                case "price-desc": products.sort(Comparator.comparingDouble(Product::getPrice).reversed()); break;
                case "discount": products.sort(Comparator.comparingInt(Product::getDiscount).reversed()); break;
                case "name": products.sort(Comparator.comparing(Product::getName)); break;
            }
        }

        // Đẩy dữ liệu chung
        request.setAttribute("categories", categories);
        request.setAttribute("selectedCategory", category);
        request.setAttribute("searchKeyword", search);
        request.setAttribute("selectedSort", sort);
        request.setAttribute("selectedPriceRange", priceRange);
        request.setAttribute("selectedDiscountOnly", discountOnly);
        request.setAttribute("selectedPet", pet);
        request.setAttribute("selectedPetType", selectedPetType);
        request.setAttribute("totalProducts", products.size());

        boolean isFiltered = (pet != null || category != null || search != null || priceRange != null || discountOnly != null);

        if (!isFiltered) {
            // Trang shop chính - có sections riêng
            List<Product> popularProducts = productDao.getPopularProductsPage(1, BEST_SELLER_SIZE);
            List<Product> discountProducts = productDao.getDiscountedProductsPage(salePage, PAGE_SIZE);
            List<Product> catalogProducts = productDao.getAllProductsPage(catalogPage, PAGE_SIZE);
            markWishlisted(products, wishlistProductIds);
            markWishlisted(popularProducts, wishlistProductIds);
            markWishlisted(discountProducts, wishlistProductIds);
            markWishlisted(catalogProducts, wishlistProductIds);

            int discountTotal = productDao.getTotalDiscountedProductsCount();
            int catalogTotal = productDao.getTotalProductsCount();

            request.setAttribute("products", products);
            request.setAttribute("popularProducts", popularProducts);
            request.setAttribute("discountProducts", discountProducts);
            request.setAttribute("catalogProducts", catalogProducts);
            request.setAttribute("salePage", salePage);
            request.setAttribute("catalogPage", catalogPage);
            request.setAttribute("saleTotalPages", getTotalPages(discountTotal, PAGE_SIZE));
            request.setAttribute("catalogTotalPages", getTotalPages(catalogTotal, PAGE_SIZE));
            request.setAttribute("totalProducts", catalogTotal);
            request.getRequestDispatcher("/pages/shop/shop.jsp").forward(request, response);
        } else {
            // Trang lọc/tìm kiếm - phân trang
            int totalFiltered = products.size();
            int totalPages = getTotalPages(totalFiltered, PAGE_SIZE);
            if (page > totalPages) page = totalPages;

            int fromIndex = (page - 1) * PAGE_SIZE;
            int toIndex = Math.min(fromIndex + PAGE_SIZE, totalFiltered);

            List<Product> pagedProducts = (fromIndex < totalFiltered) 
                ? products.subList(fromIndex, toIndex) 
                : List.of();
            markWishlisted(pagedProducts, wishlistProductIds);

            request.setAttribute("products", pagedProducts);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalProducts", totalFiltered);
            List<Product> discountProducts = productDao.getDiscountedProductsList();
            markWishlisted(discountProducts, wishlistProductIds);
            request.setAttribute("discountProducts", discountProducts);
            request.getRequestDispatcher("/pages/shop/shop-pet.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private int parsePage(String value) {
        try {
            int page = Integer.parseInt(value);
            return Math.max(page, 1);
        } catch (Exception e) {
            return 1;
        }
    }

    private int getTotalPages(int totalItems, int pageSize) {
        if (totalItems <= 0) return 1;
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    private void markWishlisted(List<Product> products, Set<Integer> wishlistProductIds) {
        if (products == null || wishlistProductIds == null || wishlistProductIds.isEmpty()) {
            return;
        }
        for (Product product : products) {
            product.setWishlisted(wishlistProductIds.contains(product.getId()));
        }
    }
}
