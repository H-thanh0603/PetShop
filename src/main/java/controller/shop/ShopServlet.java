package controller.shop;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.ProductDAO;
import DAO.PetTypeDAO;
import Model.Product;
import Model.PetType;

@WebServlet("/shop")
public class ShopServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        ProductDAO productDao = new ProductDAO();
        PetTypeDAO petTypeDao = new PetTypeDAO();

        // Lấy params
        String category = request.getParameter("category");
        String search = request.getParameter("search");
        String sort = request.getParameter("sort");
        String priceRange = request.getParameter("priceRange");
        String discountOnly = request.getParameter("discountOnly");
        String pet = request.getParameter("pet"); // dog, cat, fish, bird, ...

        // Lấy danh sách loại thú cưng active để hiển thị trên navbar/menu
        List<PetType> activePetTypes = petTypeDao.getActivePetTypes();
        request.setAttribute("petTypes", activePetTypes);

        // Lấy thông tin loại thú cưng đang chọn
        PetType selectedPetType = null;
        if (pet != null && !pet.trim().isEmpty()) {
            selectedPetType = petTypeDao.getPetTypeByCode(pet.trim());
        }

        // Lấy sản phẩm
        List<Product> products;
        List<String> categories;

        if (search != null && !search.trim().isEmpty()) {
            // Tìm kiếm
            products = productDao.searchProducts(search.trim());
            categories = productDao.getAllCategories();
        } else if (category != null && !category.trim().isEmpty()) {
            // Lọc theo category
            products = productDao.getProductsByCategory(category.trim());
            categories = productDao.getAllCategories();
        } else if (pet != null && !pet.trim().isEmpty()) {
            // Lọc theo loại thú cưng (dog, cat, ...)
            products = productDao.getProductsByPetType(pet.trim());
            // Nếu không có kết quả (chưa có pet_type_id), fallback về cách cũ
            if (products.isEmpty() && selectedPetType != null) {
                products = productDao.getProductsByPetTypeFallback(selectedPetType.getName());
            }
            // Lấy categories chỉ của loại thú cưng này
            categories = productDao.getCategoriesByPetType(pet.trim());
            if (categories.isEmpty() && selectedPetType != null) {
                // Fallback: lọc categories có chứa tên thú cưng
                categories = productDao.getAllCategories();
                String petName = selectedPetType.getName();
                categories.removeIf(c -> !c.contains(petName));
            }
        } else {
            // Tất cả sản phẩm
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
                case "under100":
                    products.removeIf(p -> p.getPrice() >= 100000);
                    break;
                case "100to300":
                    products.removeIf(p -> p.getPrice() < 100000 || p.getPrice() > 300000);
                    break;
                case "300to500":
                    products.removeIf(p -> p.getPrice() < 300000 || p.getPrice() > 500000);
                    break;
                case "above500":
                    products.removeIf(p -> p.getPrice() <= 500000);
                    break;
            }
        }

        // Sắp xếp
        if (sort != null) {
            switch (sort) {
                case "price-asc":
                    products.sort(Comparator.comparingDouble(Product::getPrice));
                    break;
                case "price-desc":
                    products.sort(Comparator.comparingDouble(Product::getPrice).reversed());
                    break;
                case "discount":
                    products.sort(Comparator.comparingInt(Product::getDiscount).reversed());
                    break;
                case "name":
                    products.sort(Comparator.comparing(Product::getName));
                    break;
            }
        }

        // Lấy sản phẩm giảm giá
        List<Product> discountProducts = productDao.getDiscountedProductsList();

        // Đẩy dữ liệu sang JSP
        request.setAttribute("products", products);
        request.setAttribute("categories", categories);
        request.setAttribute("discountProducts", discountProducts);
        request.setAttribute("selectedCategory", category);
        request.setAttribute("searchKeyword", search);
        request.setAttribute("selectedSort", sort);
        request.setAttribute("selectedPriceRange", priceRange);
        request.setAttribute("selectedDiscountOnly", discountOnly);
        request.setAttribute("selectedPet", pet);
        request.setAttribute("selectedPetType", selectedPetType);
        request.setAttribute("totalProducts", products.size());

        // Chọn JSP phù hợp
        if (pet == null && category == null && search == null && priceRange == null && discountOnly == null) {
            // Trang chính: sản phẩm nổi bật + danh mục
            request.getRequestDispatcher("/pages/shop/shop.jsp").forward(request, response);
        } else {
            // Trang lọc theo pet/category/search
            request.getRequestDispatcher("/pages/shop/shop-pet.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
