package controller.shop;

import DAO.PetTypeDAO;
import DAO.ProductDAO;
import DAO.WishlistDAO;
import Model.PetType;
import Model.Product;
import Model.ProductFilterCriteria;
import Model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@WebServlet("/shop")
public class ShopServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 12;
    private static final int BEST_SELLER_SIZE = 6;

    private ProductDAO productDao = new ProductDAO();
    private PetTypeDAO petTypeDao = new PetTypeDAO();
    private WishlistDAO wishlistDAO = new WishlistDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String category = request.getParameter("category");
        String search = trimAndClamp(request.getParameter("search"), 100);
        String sort = request.getParameter("sort");
        String priceRange = request.getParameter("priceRange");
        String discountOnly = request.getParameter("discountOnly");
        String pet = request.getParameter("pet");
        int page = parsePage(request.getParameter("page"));
        int salePage = parsePage(request.getParameter("salePage"));
        int catalogPage = parsePage(request.getParameter("catalogPage"));

        List<PetType> activePetTypes = petTypeDao.getActivePetTypes();
        request.setAttribute("petTypes", activePetTypes);

        User currentUser = (User) request.getSession().getAttribute("user");
        Set<Integer> wishlistProductIds = java.util.Collections.emptySet();
        if (currentUser != null) {
            wishlistProductIds = wishlistDAO.getWishlistProductIdsByUserId(currentUser.getId());
        }
        request.setAttribute("wishlistProductIds", wishlistProductIds);

        PetType selectedPetType = null;
        if (pet != null && !pet.trim().isEmpty()) {
            selectedPetType = petTypeDao.getPetTypeByCode(pet.trim());
        }

        boolean isFiltered = hasText(pet) || hasText(category) || hasText(search)
                || hasText(priceRange) || hasText(discountOnly) || hasText(sort)
                || request.getParameterValues("brand") != null;

        List<String> categories = hasText(pet)
                ? productDao.getCategoriesByPetType(pet.trim())
                : productDao.getAllCategories();
        if (categories.isEmpty()) {
            categories = productDao.getAllCategories();
        }

        request.setAttribute("categories", categories);
        request.setAttribute("selectedCategory", category);
        request.setAttribute("searchKeyword", search);
        request.setAttribute("selectedSort", sort);
        request.setAttribute("selectedPriceRange", priceRange);
        request.setAttribute("selectedDiscountOnly", discountOnly);
        request.setAttribute("selectedPet", pet);
        request.setAttribute("selectedPetType", selectedPetType);

        if (!isFiltered) {
            List<Product> popularProducts = productDao.getPopularProductsPage(1, BEST_SELLER_SIZE);
            List<Product> discountProducts = productDao.getDiscountedProductsPage(salePage, PAGE_SIZE);
            List<Product> catalogProducts = productDao.getAllProductsPage(catalogPage, PAGE_SIZE);
            markWishlisted(popularProducts, wishlistProductIds);
            markWishlisted(discountProducts, wishlistProductIds);
            markWishlisted(catalogProducts, wishlistProductIds);

            int discountTotal = productDao.getTotalDiscountedProductsCount();
            int catalogTotal = productDao.getTotalProductsCount();

            request.setAttribute("products", List.of());
            request.setAttribute("popularProducts", popularProducts);
            request.setAttribute("discountProducts", discountProducts);
            request.setAttribute("catalogProducts", catalogProducts);
            request.setAttribute("salePage", salePage);
            request.setAttribute("catalogPage", catalogPage);
            request.setAttribute("saleTotalPages", getTotalPages(discountTotal, PAGE_SIZE));
            request.setAttribute("catalogTotalPages", getTotalPages(catalogTotal, PAGE_SIZE));
            request.setAttribute("totalProducts", catalogTotal);
            request.getRequestDispatcher("/pages/shop/shop.jsp").forward(request, response);
            return;
        }

        ProductFilterCriteria criteria = buildFilterCriteria(category, search, sort, priceRange, discountOnly, pet, page);
        String[] selectedBrands = request.getParameterValues("brand");
        if (selectedBrands != null && selectedBrands.length > 0) {
            criteria.setBrands(java.util.Arrays.asList(selectedBrands));
        }
        if ("availability".equals(sort)) {
            criteria.setAvailabilityOnly(true);
        }
        int totalFiltered = productDao.countFilteredProducts(criteria);
        int totalPages = getTotalPages(totalFiltered, PAGE_SIZE);
        if (page > totalPages) {
            page = totalPages;
            criteria.setPage(page);
        }

        List<Product> pagedProducts = productDao.getFilteredProductsPage(criteria);
        markWishlisted(pagedProducts, wishlistProductIds);

        request.setAttribute("products", pagedProducts);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalProducts", totalFiltered);
        List<Product> discountProducts = productDao.getDiscountedProductsList();
        markWishlisted(discountProducts, wishlistProductIds);
        request.setAttribute("discountProducts", discountProducts);
        List<String> allBrands = productDao.getAllBrands();
        request.setAttribute("brands", allBrands);
        request.getRequestDispatcher("/pages/shop/shop-pet.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private ProductFilterCriteria buildFilterCriteria(String category, String search, String sort,
                                                      String priceRange, String discountOnly,
                                                      String pet, int page) {
        ProductFilterCriteria criteria = new ProductFilterCriteria();
        criteria.setCategory(trimToNull(category));
        criteria.setSearchKeyword(trimToNull(search));
        criteria.setSort(trimToNull(sort));
        criteria.setPriceRange(trimToNull(priceRange));
        criteria.setDiscountOnly("true".equals(discountOnly));
        criteria.setPetTypeCode(trimToNull(pet));
        criteria.setPage(page);
        criteria.setPageSize(PAGE_SIZE);
        return criteria;
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
        if (totalItems <= 0) {
            return 1;
        }
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String trimAndClamp(String value, int maxLength) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        return trimmed.substring(0, Math.min(trimmed.length(), maxLength));
    }
}
