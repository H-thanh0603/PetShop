package com.petshop.web;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import DAO.PetTypeDAO;
import DAO.ProductDAO;
import DAO.WishlistDAO;
import Model.PetType;
import Model.Product;
import Model.ProductFilterCriteria;
import Model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces ShopServlet (/shop) 1:1 — same params, same model attributes,
 * same two views (shop.jsp unfiltered, shop-pet.jsp filtered).
 */
@Controller
public class ShopController {

    private static final int PAGE_SIZE = 12;
    private static final int BEST_SELLER_SIZE = 6;

    private final ProductDAO productDao;
    private final PetTypeDAO petTypeDao;
    private final WishlistDAO wishlistDAO;

    public ShopController() {
        this(new ProductDAO(), new PetTypeDAO(), new WishlistDAO());
    }

    ShopController(ProductDAO productDao, PetTypeDAO petTypeDao, WishlistDAO wishlistDAO) {
        this.productDao = productDao;
        this.petTypeDao = petTypeDao;
        this.wishlistDAO = wishlistDAO;
    }

    @GetMapping("/shop")
    public String shop(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "priceRange", required = false) String priceRange,
            @RequestParam(value = "discountOnly", required = false) String discountOnly,
            @RequestParam(value = "pet", required = false) String pet,
            @RequestParam(value = "page", required = false) String pageRaw,
            @RequestParam(value = "salePage", required = false) String salePageRaw,
            @RequestParam(value = "catalogPage", required = false) String catalogPageRaw,
            HttpServletRequest request,
            HttpSession session,
            Model model) {
        search = trimAndClamp(search, 100);
        int page = parsePage(pageRaw);
        int salePage = parsePage(salePageRaw);
        int catalogPage = parsePage(catalogPageRaw);

        List<PetType> activePetTypes = petTypeDao.getActivePetTypes();
        model.addAttribute("petTypes", activePetTypes);

        User currentUser = (User) session.getAttribute("user");
        Set<Integer> wishlistProductIds = Collections.emptySet();
        if (currentUser != null) {
            wishlistProductIds = wishlistDAO.getWishlistProductIdsByUserId(currentUser.getId());
        }
        model.addAttribute("wishlistProductIds", wishlistProductIds);

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

        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchKeyword", search);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedPriceRange", priceRange);
        model.addAttribute("selectedDiscountOnly", discountOnly);
        model.addAttribute("selectedPet", pet);
        model.addAttribute("selectedPetType", selectedPetType);

        if (!isFiltered) {
            List<Product> popularProducts = productDao.getPopularProductsPage(1, BEST_SELLER_SIZE);
            List<Product> discountProducts = productDao.getDiscountedProductsPage(salePage, PAGE_SIZE);
            List<Product> catalogProducts = productDao.getAllProductsPage(catalogPage, PAGE_SIZE);
            markWishlisted(popularProducts, wishlistProductIds);
            markWishlisted(discountProducts, wishlistProductIds);
            markWishlisted(catalogProducts, wishlistProductIds);

            int discountTotal = productDao.getTotalDiscountedProductsCount();
            int catalogTotal = productDao.getTotalProductsCount();

            model.addAttribute("products", List.of());
            model.addAttribute("popularProducts", popularProducts);
            model.addAttribute("discountProducts", discountProducts);
            model.addAttribute("catalogProducts", catalogProducts);
            model.addAttribute("salePage", salePage);
            model.addAttribute("catalogPage", catalogPage);
            model.addAttribute("saleTotalPages", getTotalPages(discountTotal, PAGE_SIZE));
            model.addAttribute("catalogTotalPages", getTotalPages(catalogTotal, PAGE_SIZE));
            model.addAttribute("totalProducts", catalogTotal);
            return "pages/shop/shop";
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

        model.addAttribute("products", pagedProducts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalProducts", totalFiltered);
        List<Product> discountProducts = productDao.getDiscountedProductsList();
        markWishlisted(discountProducts, wishlistProductIds);
        model.addAttribute("discountProducts", discountProducts);
        List<String> allBrands = productDao.getAllBrands();
        model.addAttribute("brands", allBrands);
        return "pages/shop/shop-pet";
    }

    @PostMapping("/shop")
    public String shopPost(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "priceRange", required = false) String priceRange,
            @RequestParam(value = "discountOnly", required = false) String discountOnly,
            @RequestParam(value = "pet", required = false) String pet,
            @RequestParam(value = "page", required = false) String pageRaw,
            @RequestParam(value = "salePage", required = false) String salePageRaw,
            @RequestParam(value = "catalogPage", required = false) String catalogPageRaw,
            HttpServletRequest request,
            HttpSession session,
            Model model) {
        return shop(category, search, sort, priceRange, discountOnly, pet,
                pageRaw, salePageRaw, catalogPageRaw, request, session, model);
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
