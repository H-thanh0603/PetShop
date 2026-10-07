package com.petshop.repository;

import com.petshop.model.Product;
import com.petshop.model.ProductFilterCriteria;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // ---- shared fragments (kept as constants for the native list queries) ----

    // NOTE: native list queries reuse the DAO's proven SQL verbatim (review
    // aggregates, promo joins) and hydrate via a row mapper in default methods.

    @Query(value = "SELECT p.* FROM products p WHERE p.is_active = 1 ORDER BY p.id DESC",
            nativeQuery = true)
    List<Product> findAllActiveOrderByIdDesc();

    @Query(value = "SELECT p.* FROM products p "
            + "INNER JOIN pet_types pt ON p.pet_type_id = pt.id "
            + "WHERE pt.code = :code AND pt.is_active = 1 AND p.is_active = 1 ORDER BY p.id DESC",
            nativeQuery = true)
    List<Product> findByPetTypeCode(@Param("code") String petTypeCode);

    @Query(value = "SELECT p.* FROM products p WHERE p.category LIKE :pattern AND p.is_active = 1 ORDER BY p.id DESC",
            nativeQuery = true)
    List<Product> findByPetTypeFallback(@Param("pattern") String pattern);

    @Query(value = "SELECT DISTINCT p.category FROM products p "
            + "INNER JOIN pet_types pt ON p.pet_type_id = pt.id "
            + "WHERE pt.code = :code AND p.category IS NOT NULL AND p.category != '' AND p.is_active = 1 ORDER BY p.category",
            nativeQuery = true)
    List<String> findCategoriesByPetType(@Param("code") String petTypeCode);

    @Query(value = "SELECT DISTINCT category FROM products WHERE category IS NOT NULL AND category != '' AND is_active = 1 ORDER BY category",
            nativeQuery = true)
    List<String> findAllCategories();

    @Query(value = "SELECT DISTINCT brand FROM products WHERE brand IS NOT NULL AND brand != '' AND is_active = 1 ORDER BY brand",
            nativeQuery = true)
    List<String> findAllBrands();

    @Query(value = "SELECT p.category FROM order_items oi JOIN products p ON oi.product_id = p.id "
            + "JOIN orders o ON oi.order_id = o.id WHERE o.status != 'Cancelled' "
            + "AND p.category IS NOT NULL AND p.category != '' AND p.is_active = 1 "
            + "GROUP BY p.category ORDER BY SUM(oi.quantity) DESC LIMIT :limit",
            nativeQuery = true)
    List<String> findPopularCategories(@Param("limit") int limit);

    @Query(value = "SELECT p.* FROM products p WHERE p.category = :category AND p.is_active = 1 ORDER BY p.id DESC",
            nativeQuery = true)
    List<Product> findByCategory(@Param("category") String category);

    @Query(value = "SELECT p.* FROM products p WHERE (p.name LIKE :pattern OR p.description LIKE :pattern) AND p.is_active = 1 ORDER BY p.name ASC",
            nativeQuery = true)
    List<Product> searchProductsNative(@Param("pattern") String pattern);

    @Query(value = "SELECT p.* FROM products p WHERE (p.name LIKE :contains OR p.description LIKE :contains) AND p.is_active = 1 "
            + "GROUP BY p.id ORDER BY CASE WHEN p.name LIKE :startsWith THEN 0 ELSE 1 END, p.name ASC LIMIT :limit",
            nativeQuery = true)
    List<Product> searchProductsLimitNative(@Param("contains") String contains, @Param("startsWith") String startsWith,
                                            @Param("limit") int limit);

    @Query(value = "SELECT p.* FROM products p WHERE p.is_active = 1 ORDER BY p.id DESC LIMIT :size OFFSET :offset",
            nativeQuery = true)
    List<Product> findAllActivePage(@Param("size") int size, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM products WHERE is_active = 1", nativeQuery = true)
    int countActive();

    // ---- locking read ----

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Product findForUpdateById(@Param("id") int id);

    // ---- stock writes (ambient-tx; callers in manual-tx join it once @Transactional) ----

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE products SET stock = stock - :qty WHERE id = :id AND stock >= :qty", nativeQuery = true)
    int decreaseStockNative(@Param("id") int productId, @Param("qty") int quantity);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE products SET stock = stock - :qty, reserved_quantity = reserved_quantity + :qty WHERE id = :id AND stock >= :qty",
            nativeQuery = true)
    int reserveStockNative(@Param("id") int productId, @Param("qty") int quantity);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE products SET stock = stock + :qty, reserved_quantity = reserved_quantity - :qty WHERE id = :id AND reserved_quantity >= :qty",
            nativeQuery = true)
    int releaseReservedStockNative(@Param("id") int productId, @Param("qty") int quantity);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE products SET reserved_quantity = CASE WHEN reserved_quantity >= :qty THEN reserved_quantity - :qty ELSE 0 END WHERE id = :id",
            nativeQuery = true)
    int finalizeReservedStockNative(@Param("id") int productId, @Param("qty") int quantity);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE products SET stock = stock + :qty WHERE id = :id", nativeQuery = true)
    int increaseStockNative(@Param("id") int productId, @Param("qty") int quantity);

    @Query(value = "SELECT stock FROM products WHERE id = :id", nativeQuery = true)
    Integer findStockById(@Param("id") int id);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE products SET stock = :stock WHERE id = :id", nativeQuery = true)
    int updateStockNative(@Param("id") int productId, @Param("stock") int newStock);

    @Query(value = "SELECT p.* FROM products p WHERE p.is_active = 1 AND p.stock <= :threshold AND p.stock > 0 ORDER BY p.stock ASC",
            nativeQuery = true)
    List<Product> findLowStock(@Param("threshold") int threshold);

    // ReportDAO variant: no is_active filter, stock-ASC then id-DESC order, LIMIT.
    @Query(value = "SELECT p.* FROM products p WHERE p.stock > 0 AND p.stock <= :threshold "
            + "ORDER BY p.stock ASC, p.id DESC LIMIT :lim",
            nativeQuery = true)
    List<Product> findLowStockProducts(@Param("threshold") int threshold, @Param("lim") int lim);

    @Query(value = "SELECT p.* FROM products p WHERE p.is_active = 1 AND p.stock <= 0 ORDER BY p.id DESC",
            nativeQuery = true)
    List<Product> findOutOfStock();

    // ---- review aggregates (replaces the DAO's per-query LEFT JOIN subquery) ----

    @Query(value = "SELECT product_id, AVG(rating), COUNT(id) FROM reviews WHERE product_id IN :ids GROUP BY product_id",
            nativeQuery = true)
    List<Object[]> findReviewAggregates(@Param("ids") List<Integer> ids);

    @Transactional
    default List<Product> fillReviewAggregates(List<Product> products) {
        try {
            if (products.isEmpty()) {
                return products;
            }
            List<Integer> ids = products.stream().map(Product::getId).toList();
            for (Object[] row : findReviewAggregates(ids)) {
                int pid = ((Number) row[0]).intValue();
                double avg = row[1] == null ? 0 : ((Number) row[1]).doubleValue();
                int count = row[2] == null ? 0 : ((Number) row[2]).intValue();
                for (Product product : products) {
                    if (product.getId() == pid) {
                        product.setAverageRating(avg);
                        product.setReviewCount(count);
                        break;
                    }
                }
            }
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error filling review aggregates", e);
        }
        return products;
    }

    @Transactional
    default Product fillReviewAggregatesOne(Product product) {
        if (product == null) {
            return null;
        }
        fillReviewAggregates(java.util.List.of(product));
        return product;
    }

    // ---- DAO contract surface (same names/signatures; conn overloads delegate, ambient-tx) ----

    @Transactional
    default List<Product> getAllProducts() {
        try {
            return fillReviewAggregates(findAllActiveOrderByIdDesc());
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching all products", e);
            return List.of();
        }
    }

    @Transactional
    default List<Product> getProductsByPetType(String petTypeCode) {
        try {
            return fillReviewAggregates(findByPetTypeCode(petTypeCode));
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching products by pet type code={}", petTypeCode, e);
            return List.of();
        }
    }

    @Transactional
    default List<Product> getProductsByPetTypeFallback(String petTypeName) {
        return getProductsByCategory("%" + petTypeName + "%");
    }

    @Transactional
    default List<String> getCategoriesByPetType(String petTypeCode) {
        try {
            return findCategoriesByPetType(petTypeCode);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching categories by pet type code={}", petTypeCode, e);
            return List.of();
        }
    }

    @Transactional
    default List<String> getAllCategories() {
        try {
            return findAllCategories();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching all categories", e);
            return List.of();
        }
    }

    @Transactional
    default List<String> getAllBrands() {
        try {
            return findAllBrands();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching all brands", e);
            return List.of();
        }
    }

    @Transactional
    default List<String> getPopularCategories(int limit) {
        try {
            return findPopularCategories(limit);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching popular categories limit={}", limit, e);
            return List.of();
        }
    }

    @Transactional
    default List<Product> getProductsByCategory(String category) {
        try {
            if (category != null && category.startsWith("%") && category.endsWith("%")) {
                return fillReviewAggregates(findByCategoryLike(category));
            }
            return fillReviewAggregates(findByCategory(category));
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching products by category={}", category, e);
            return List.of();
        }
    }

    @Query(value = "SELECT p.* FROM products p WHERE p.category LIKE :pattern AND p.is_active = 1 ORDER BY p.id DESC",
            nativeQuery = true)
    List<Product> findByCategoryLike(@Param("pattern") String pattern);

    @Transactional
    default List<Product> searchProducts(String keyword) {
        try {
            String pattern = "%" + keyword + "%";
            return fillReviewAggregates(searchProductsNative(pattern));
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error searching products keyword={}", keyword, e);
            return List.of();
        }
    }

    @Transactional
    default List<Product> searchProductsLimit(String keyword, int limit) {
        try {
            String contains = "%" + keyword + "%";
            return fillReviewAggregates(searchProductsLimitNative(contains, keyword + "%", limit));
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error searching products with limit keyword={}", keyword, e);
            return List.of();
        }
    }

    @Transactional
    default List<Product> searchProductsForAdvice(String message, int limit) {
        // Java-side keyword extraction preserved verbatim from the DAO; only the
        // JDBC execution becomes repository native queries below.
        List<Product> results = new java.util.ArrayList<>();
        String lowerMessage = message.toLowerCase();
        String petType = null;
        if (lowerMessage.contains("mèo") || lowerMessage.contains("cat")) {
            petType = "cat";
        } else if (lowerMessage.contains("chó") || lowerMessage.contains("dog") || lowerMessage.contains("poodle")) {
            petType = "dog";
        }
        String[] keywords = {"hạt", "pate", "cát", "sữa tắm", "shampoo", "đồ chơi", "bát", "nhà", "chuồng", "vòng cổ", "dây dắt", "sữa", "snack", "thức ăn", "xương"};
        List<String> matchedKeywords = new java.util.ArrayList<>();
        for (String kw : keywords) {
            if (lowerMessage.contains(kw)) {
                matchedKeywords.add(kw);
            }
        }
        if (matchedKeywords.isEmpty()) {
            String query = message.replaceAll("[^a-zA-Z0-9ăâđêôơưàảãáạằẳẵắặầẩẫấậèẻẽéẹềểễếệìỉĩíịòỏõóọồổỗốộờởỡớợùủũúụừửữứựỳỷỹýỵ\\s]", "");
            for (String word : query.split("\\s+")) {
                if (word.length() > 2 && !"cho".equals(word) && !"mèo".equals(word) && !"chó".equals(word) && !"bán".equals(word) && !"mua".equals(word) && !"shop".equals(word)) {
                    matchedKeywords.add(word);
                }
            }
        }
        try {
            results.addAll(findAdviceProducts(petType, matchedKeywords.isEmpty() ? null : matchedKeywords, limit));
            if (results.isEmpty()) {
                results.addAll(findAdviceFallback(limit));
            }
            return fillReviewAggregates(results);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error searching products for advice", e);
            return List.of();
        }
    }

    default List<Product> findAdviceProducts(String petType, List<String> keywords, int limit) {
        // Built dynamically like the DAO; executed as a single native query.
        StringBuilder sql = new StringBuilder("SELECT p.* FROM products p "
                + "LEFT JOIN pet_types pt ON p.pet_type_id = pt.id "
                + "WHERE p.is_active = 1 AND p.stock > 0 AND p.price > 0 ");
        List<Object> params = new java.util.ArrayList<>();
        if (petType != null) {
            sql.append("AND pt.code = ? ");
            params.add(petType);
        }
        if (keywords != null && !keywords.isEmpty()) {
            sql.append("AND (");
            for (int i = 0; i < keywords.size(); i++) {
                if (i > 0) {
                    sql.append(" OR ");
                }
                sql.append("p.name LIKE ? OR p.description LIKE ? OR p.category LIKE ?");
                String pattern = "%" + keywords.get(i) + "%";
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            }
            sql.append(") ");
        }
        sql.append("GROUP BY p.id ORDER BY p.id DESC LIMIT ").append(limit);
        jakarta.persistence.EntityManager entityManager = ProductRepositoryHolder.entityManager();
        jakarta.persistence.Query query = entityManager.createNativeQuery(sql.toString(), Product.class);
        int idx = 1;
        for (Object param : params) {
            query.setParameter(idx++, param);
        }
        @SuppressWarnings("unchecked")
        List<Product> list = query.getResultList();
        return list;
    }

    @Query(value = "SELECT p.* FROM products p WHERE p.is_active = 1 AND p.stock > 0 AND p.price > 0 "
            + "GROUP BY p.id ORDER BY p.id DESC LIMIT :limit",
            nativeQuery = true)
    List<Product> findAdviceFallback(@Param("limit") int limit);

    @Transactional
    default List<Product> getFilteredProductsPage(ProductFilterCriteria criteria) {
        // Filter SQL preserved verbatim from the DAO builder (WHERE/ORDER/LIMIT),
        // executed as native query with the same parameter order.
        try {
            String[] built = buildFilteredQuery(criteria, false);
            jakarta.persistence.EntityManager entityManager = ProductRepositoryHolder.entityManager();
            jakarta.persistence.Query query = entityManager.createNativeQuery(built[0], Product.class);
            bindFilterParameters(query, criteria, 1);
            int idx = bindCount(query, criteria);
            query.setParameter(idx++, criteria.getPageSize());
            query.setParameter(idx, Math.max(0, (criteria.getPage() - 1) * criteria.getPageSize()));
            @SuppressWarnings("unchecked")
            List<Product> list = query.getResultList();
            return fillReviewAggregates(list);
        } catch (RuntimeException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching filtered products", e);
            return List.of();
        }
    }

    @Transactional
    default int countFilteredProducts(ProductFilterCriteria criteria) {
        try {
            String[] built = buildFilteredQuery(criteria, true);
            jakarta.persistence.EntityManager entityManager = ProductRepositoryHolder.entityManager();
            jakarta.persistence.Query query = entityManager.createNativeQuery(built[0]);
            bindFilterParameters(query, criteria, 1);
            Object result = query.getSingleResult();
            return result == null ? 0 : ((Number) result).intValue();
        } catch (RuntimeException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error counting filtered products", e);
            return 0;
        }
    }

    default String[] buildFilteredQuery(ProductFilterCriteria criteria, boolean countOnly) {
        StringBuilder where = new StringBuilder("WHERE p.is_active = 1");
        if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
            where.append(" AND p.category = ?");
        }
        if (criteria.getPetTypeCode() != null && !criteria.getPetTypeCode().isBlank()) {
            where.append(" AND pt.code = ?");
        }
        if (criteria.getSearchKeyword() != null && !criteria.getSearchKeyword().isBlank()) {
            where.append(" AND (p.name LIKE ? OR p.description LIKE ?)");
        }
        if (criteria.isDiscountOnly()) {
            where.append(" AND (p.discount > 0)");
        }
        if (criteria.getPriceRange() != null && !criteria.getPriceRange().isBlank()) {
            switch (criteria.getPriceRange()) {
                case "under100": where.append(" AND p.price < ?"); break;
                case "100to300": where.append(" AND p.price >= ? AND p.price <= ?"); break;
                case "300to500": where.append(" AND p.price >= ? AND p.price <= ?"); break;
                case "above500": where.append(" AND p.price > ?"); break;
                default: break;
            }
        }
        if (criteria.getBrands() != null && !criteria.getBrands().isEmpty()) {
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < criteria.getBrands().size(); i++) {
                if (i > 0) {
                    placeholders.append(",");
                }
                placeholders.append("?");
            }
            where.append(" AND p.brand IN (").append(placeholders).append(")");
        }
        if (criteria.isAvailabilityOnly()) {
            where.append(" AND p.stock > 0");
        }
        String select;
        if (countOnly) {
            select = "SELECT COUNT(*) FROM products p LEFT JOIN pet_types pt ON p.pet_type_id = pt.id " + where;
        } else {
            select = "SELECT p.* FROM products p LEFT JOIN pet_types pt ON p.pet_type_id = pt.id " + where
                    + " ORDER BY " + resolveFilterOrderBy(criteria.getSort()) + " LIMIT ? OFFSET ?";
        }
        return new String[]{select.toString(), ""};
    }

    default int bindFilterParameters(jakarta.persistence.Query query, ProductFilterCriteria criteria, int startIndex) {
        int idx = startIndex;
        if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
            query.setParameter(idx++, criteria.getCategory().trim());
        }
        if (criteria.getPetTypeCode() != null && !criteria.getPetTypeCode().isBlank()) {
            query.setParameter(idx++, criteria.getPetTypeCode().trim());
        }
        if (criteria.getSearchKeyword() != null && !criteria.getSearchKeyword().isBlank()) {
            String pattern = "%" + criteria.getSearchKeyword().trim() + "%";
            query.setParameter(idx++, pattern);
            query.setParameter(idx++, pattern);
        }
        if (criteria.getPriceRange() != null && !criteria.getPriceRange().isBlank()) {
            switch (criteria.getPriceRange()) {
                case "under100":
                    query.setParameter(idx++, BigDecimal.valueOf(100000));
                    break;
                case "100to300":
                    query.setParameter(idx++, BigDecimal.valueOf(100000));
                    query.setParameter(idx++, BigDecimal.valueOf(300000));
                    break;
                case "300to500":
                    query.setParameter(idx++, BigDecimal.valueOf(300000));
                    query.setParameter(idx++, BigDecimal.valueOf(500000));
                    break;
                case "above500":
                    query.setParameter(idx++, BigDecimal.valueOf(500000));
                    break;
                default:
                    break;
            }
        }
        if (criteria.getBrands() != null && !criteria.getBrands().isEmpty()) {
            for (String brand : criteria.getBrands()) {
                query.setParameter(idx++, brand);
            }
        }
        return idx;
    }

    default int bindCount(jakarta.persistence.Query query, ProductFilterCriteria criteria) {
        return bindFilterParameters(query, criteria, 1);
    }

    default String resolveFilterOrderBy(String sort) {
        if (sort == null || sort.isBlank()) {
            return "p.id DESC";
        }
        switch (sort) {
            case "price-asc": return "p.price ASC, p.id DESC";
            case "price-desc": return "p.price DESC, p.id DESC";
            case "discount": return "p.discount DESC, p.id DESC";
            case "name": return "p.name ASC, p.id DESC";
            case "rating": return "p.id DESC";
            case "best-selling": return "p.id DESC";
            case "availability": return "p.stock DESC, p.id DESC";
            case "newest":
            default: return "p.id DESC";
        }
    }

    @Transactional
    default List<Product> getDiscountedProductsList() {
        try {
            return fillReviewAggregates(findDiscountedProductsListNative());
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching discounted products list", e);
            return List.of();
        }
    }

    @Query(value = "SELECT p.* FROM products p WHERE p.discount > 0 AND p.is_active = 1 ORDER BY p.discount DESC, p.id DESC",
            nativeQuery = true)
    List<Product> findDiscountedProductsListNative();

    @Transactional
    default List<Product> getDiscountedProductsPage(int page, int size) {
        try {
            return fillReviewAggregates(findDiscountedProductsPageNative(size, Math.max(0, (page - 1) * size)));
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching discounted products page={}", page, e);
            return List.of();
        }
    }

    @Query(value = "SELECT p.* FROM products p WHERE p.discount > 0 AND p.is_active = 1 ORDER BY p.discount DESC, p.id DESC LIMIT :size OFFSET :offset",
            nativeQuery = true)
    List<Product> findDiscountedProductsPageNative(@Param("size") int size, @Param("offset") int offset);

    @Transactional
    default int getTotalDiscountedProductsCount() {
        try {
            return countDiscountedProductsNative();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error counting discounted products", e);
            return 0;
        }
    }

    @Query(value = "SELECT COUNT(*) FROM products p WHERE p.discount > 0 AND p.is_active = 1", nativeQuery = true)
    int countDiscountedProductsNative();

    @Transactional
    default List<Product> getAllProductsPage(int page, int size) {
        try {
            return fillReviewAggregates(findAllActivePage(size, Math.max(0, (page - 1) * size)));
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching all products page={}", page, e);
            return List.of();
        }
    }

    @Transactional
    default int getTotalProductsCount() {
        try {
            return countActive();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error counting total products", e);
            return 0;
        }
    }

    @Transactional
    default List<Product> getPopularProductsPage(int page, int size) {
        // Popularity排序 requires order_items aggregation; preserved via native query.
        try {
            return fillReviewAggregates(findPopularProductsPageNative(size, Math.max(0, (page - 1) * size)));
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching popular products page={}", page, e);
            return List.of();
        }
    }

    @Query(value = "SELECT p.* FROM products p LEFT JOIN (SELECT oi.product_id, SUM(oi.quantity) AS total_sold "
            + "FROM order_items oi JOIN orders o ON o.id = oi.order_id WHERE o.status != 'Cancelled' GROUP BY oi.product_id) s "
            + "ON s.product_id = p.id WHERE p.is_active = 1 ORDER BY total_sold DESC, p.id DESC LIMIT :size OFFSET :offset",
            nativeQuery = true)
    List<Product> findPopularProductsPageNative(@Param("size") int size, @Param("offset") int offset);

    @Transactional
    default int getTotalPopularProductsCount() {
        return getTotalProductsCount();
    }

    @Transactional
    default Product getProductById(int id) {
        try {
            return fillReviewAggregatesOne(findById(id).orElse(null));
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching product by id={}", id, e);
            return null;
        }
    }

    @Deprecated(forRemoval = true)
    default Product getProductById(java.sql.Connection conn, int id) {
        // All caller waves (Cart/C6, Report/E, Promotion/D1, Order/D3) migrated — no Connection overloads remain.
        return getProductById(id);
    }

    @Deprecated(forRemoval = true)
    default Product getProductByIdForUpdate(java.sql.Connection conn, int id) {
        // TODO(Task 9): OrderDAO/D3 replaces with findForUpdateById inside @Transactional.
        try {
            return fillReviewAggregatesOne(findForUpdateById(id));
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching product for update id={}", id, e);
            return null;
        }
    }

    @Transactional
    default boolean addProduct(String name, String image, BigDecimal price, int discount, String description) {
        return addProductFull(name, image, price, discount, description, 0, 0, null, 0);
    }

    @Transactional
    default boolean addProduct(String name, String image, BigDecimal price, int discount, String description,
                               int stock, int weight, String category, int petTypeId) {
        return addProductFull(name, image, price, discount, description, stock, weight, category, petTypeId);
    }

    @Transactional
    default boolean addProductFull(String name, String image, BigDecimal price, int discount, String description,
                                   int stock, int weight, String category, int petTypeId) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setImage(image);
            product.setPrice(price);
            product.setDiscount(discount);
            product.setDescription(description);
            product.setStock(stock);
            product.setWeight(weight);
            product.setCategory(category);
            product.setPet_type_id(petTypeId);
            product.setActive(true);
            save(product);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error adding product name={}", name, e);
            return false;
        }
    }

    @Transactional
    default int addProductAndReturnId(String name, String image, BigDecimal price, int discount, String description,
                                     int weight, String category, int petTypeId) {
        return addProductAndReturnIdFull(name, image, price, discount, description, 0, weight, category, petTypeId, true);
    }

    @Transactional
    default int addProductAndReturnId(String name, String image, BigDecimal price, int discount, String description,
                                     int stock, int weight, String category, int petTypeId) {
        return addProductAndReturnIdFull(name, image, price, discount, description, stock, weight, category, petTypeId, false);
    }

    @Transactional
    default int addProductAndReturnIdFull(String name, String image, BigDecimal price, int discount, String description,
                                         int stock, int weight, String category, int petTypeId, boolean zeroStockActive) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setImage(image);
            product.setPrice(price);
            product.setDiscount(discount);
            product.setDescription(description);
            product.setStock(stock);
            product.setWeight(weight);
            product.setCategory(category);
            product.setPet_type_id(petTypeId);
            product.setActive(true);
            return save(product).getId();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error adding product and returning id name={}", name, e);
            return zeroStockActive ? -1 : 0;
        }
    }

    @Transactional
    default boolean updateProduct(int id, String name, String image, BigDecimal price, int discount, String description) {
        return updateProductFull(id, name, image, price, discount, description, null, null, null, null);
    }

    @Transactional
    default boolean updateProduct(int id, String name, String image, BigDecimal price, int discount, String description,
                                  int weight, String category, int petTypeId) {
        return updateProductFull(id, name, image, price, discount, description, null, weight, category, petTypeId);
    }

    @Transactional
    default boolean updateProduct(int id, String name, String image, BigDecimal price, int discount, String description,
                                  int stock, int weight, String category, int petTypeId) {
        return updateProductFull(id, name, image, price, discount, description, stock, weight, category, petTypeId);
    }

    @Transactional
    default boolean updateProductFull(int id, String name, String image, BigDecimal price, int discount, String description,
                                      Integer stock, Integer weight, String category, Integer petTypeId) {
        try {
            Product product = findById(id).orElse(null);
            if (product == null) {
                return false;
            }
            product.setName(name);
            product.setImage(image);
            product.setPrice(price);
            product.setDiscount(discount);
            product.setDescription(description);
            if (stock != null) {
                product.setStock(stock);
            }
            if (weight != null) {
                product.setWeight(weight);
            }
            if (category != null) {
                product.setCategory(category);
            }
            if (petTypeId != null) {
                product.setPet_type_id(petTypeId);
            }
            save(product);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error updating product id={}", id, e);
            return false;
        }
    }

    @Transactional
    default boolean deleteProduct(int id) {
        try {
            deleteById(id);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error deleting product id={}", id, e);
            return false;
        }
    }

    @Transactional
    default boolean softDeleteProduct(int id) {
        try {
            return setActive(id, false) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error soft-deleting product id={}", id, e);
            return false;
        }
    }

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Product p SET p.isActive = :active WHERE p.id = :id")
    int setActive(@Param("id") int id, @Param("active") boolean active);

    @Transactional
    default int getTotalProducts() {
        return getTotalProductsCount();
    }

    @Transactional
    default int getDiscountedProducts() {
        return getTotalDiscountedProductsCount();
    }

    @Transactional
    default List<Product> getRelatedProducts(int excludeId) {
        // Random-offset algorithm preserved verbatim (ThreadLocalRandom), reads via repository.
        try {
            Product excluded = findById(excludeId).orElse(null);
            if (excluded == null) {
                return List.of();
            }
            List<Product> list = new java.util.ArrayList<>();
            final int limit = 4;
            String category = excluded.getCategory();
            if (category != null && !category.isEmpty()) {
                int count = countByCategoryExcluding(category, excludeId);
                int offset = (count > limit) ? java.util.concurrent.ThreadLocalRandom.current().nextInt(count - limit + 1) : 0;
                list.addAll(findRelatedByCategory(category, excludeId, limit, offset));
            }
            if (list.size() < limit) {
                int needed = limit - list.size();
                List<Integer> excludeIds = new java.util.ArrayList<>();
                excludeIds.add(excludeId);
                for (Product p : list) {
                    excludeIds.add(p.getId());
                }
                int count = countExcludingIds(excludeIds);
                int offset = (count > needed) ? java.util.concurrent.ThreadLocalRandom.current().nextInt(count - needed + 1) : 0;
                list.addAll(findCatalogExcludingIds(excludeIds, needed, offset));
            }
            return fillReviewAggregates(list);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching related products for id={}", excludeId, e);
            return List.of();
        }
    }

    @Query(value = "SELECT COUNT(*) FROM products p WHERE p.id != :excludeId AND p.category = :category AND p.is_active = 1",
            nativeQuery = true)
    int countByCategoryExcluding(@Param("category") String category, @Param("excludeId") int excludeId);

    @Query(value = "SELECT p.* FROM products p WHERE p.id != :excludeId AND p.category = :category AND p.is_active = 1 ORDER BY p.id LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Product> findRelatedByCategory(@Param("category") String category, @Param("excludeId") int excludeId,
                                        @Param("limit") int limit, @Param("offset") int offset);

    default int countExcludingIds(List<Integer> excludeIds) {
        jakarta.persistence.EntityManager entityManager = ProductRepositoryHolder.entityManager();
        String placeholders = excludeIds.stream().map(i -> "?").collect(java.util.stream.Collectors.joining(","));
        jakarta.persistence.Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM products p WHERE p.id NOT IN (" + placeholders + ") AND p.is_active = 1");
        int idx = 1;
        for (int id : excludeIds) {
            query.setParameter(idx++, id);
        }
        Object result = query.getSingleResult();
        return result == null ? 0 : ((Number) result).intValue();
    }

    default List<Product> findCatalogExcludingIds(List<Integer> excludeIds, int needed, int offset) {
        jakarta.persistence.EntityManager entityManager = ProductRepositoryHolder.entityManager();
        String placeholders = excludeIds.stream().map(i -> "?").collect(java.util.stream.Collectors.joining(","));
        jakarta.persistence.Query query = entityManager.createNativeQuery(
                "SELECT p.* FROM products p WHERE p.id NOT IN (" + placeholders + ") AND p.is_active = 1 ORDER BY p.id LIMIT ? OFFSET ?", Product.class);
        int idx = 1;
        for (int id : excludeIds) {
            query.setParameter(idx++, id);
        }
        query.setParameter(idx++, needed);
        query.setParameter(idx, offset);
        @SuppressWarnings("unchecked")
        List<Product> list = query.getResultList();
        return list;
    }

    @Transactional
    default List<Product> getProductsByPage(int index, int size) {
        return getAllProductsPage(index, size);
    }

    @Transactional
    default boolean decreaseStock(int productId, int quantity) {
        try {
            return decreaseStockNative(productId, quantity) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error decreasing stock for product id={}", productId, e);
            return false;
        }
    }

    @Transactional
    default boolean decreaseStockBool(int productId, int quantity) {
        return decreaseStock(productId, quantity);
    }

    @Deprecated(forRemoval = true)
    default boolean decreaseStock(java.sql.Connection conn, int productId, int quantity) {
        // TODO(Task 9): OrderDAO/D3 replaces with ambient decreaseStock inside @Transactional.
        return decreaseStock(productId, quantity);
    }

    @Deprecated(forRemoval = true)
    default boolean reserveStock(java.sql.Connection conn, int productId, int quantity) {
        // TODO(Task 9/D1/C6): caller waves remove conn (OrderDAO/D3, PromotionDAO/D1, CheckoutService/D3, CartDAO/C6).
        return reserveStockAmbient(productId, quantity);
    }

    @Transactional
    default boolean reserveStockAmbient(int productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        try {
            return reserveStockNative(productId, quantity) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error reserving stock for product id={}", productId, e);
            return false;
        }
    }

    @Deprecated(forRemoval = true)
    default boolean releaseReservedStock(java.sql.Connection conn, int productId, int quantity) {
        // TODO(Task 9): OrderDAO/D3 replaces with ambient call inside @Transactional.
        return releaseReservedStockAmbient(productId, quantity);
    }

    @Transactional
    default boolean releaseReservedStockAmbient(int productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        try {
            return releaseReservedStockNative(productId, quantity) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error releasing reserved stock for product id={}", productId, e);
            return false;
        }
    }

    @Deprecated(forRemoval = true)
    default boolean finalizeReservedStock(java.sql.Connection conn, int productId, int quantity) {
        // TODO(Task 9): OrderDAO/D3 replaces with ambient call inside @Transactional.
        return finalizeReservedStockAmbient(productId, quantity);
    }

    @Transactional
    default boolean finalizeReservedStockAmbient(int productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        try {
            return finalizeReservedStockNative(productId, quantity) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error finalizing reserved stock for product id={}", productId, e);
            return false;
        }
    }

    @Transactional
    default boolean increaseStock(int productId, int quantity) {
        try {
            return increaseStockNative(productId, quantity) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error increasing stock for product id={}", productId, e);
            return false;
        }
    }

    @Deprecated(forRemoval = true)
    default boolean increaseStock(java.sql.Connection conn, int productId, int quantity) {
        // TODO(Task 9): caller waves remove conn.
        return increaseStock(productId, quantity);
    }

    @Transactional
    default int getStock(int productId) {
        try {
            Integer stock = findStockById(productId);
            return stock == null ? 0 : stock;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error getting stock for product id={}", productId, e);
            return 0;
        }
    }

    @Transactional
    default boolean updateStock(int productId, int newStock) {
        try {
            return updateStockNative(productId, newStock) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error updating stock for product id={}", productId, e);
            return false;
        }
    }

    @Transactional
    default List<Product> getLowStockProducts(int threshold) {
        try {
            return fillReviewAggregates(findLowStock(threshold));
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching low stock products", e);
            return List.of();
        }
    }

    @Transactional
    default List<Product> getOutOfStockProducts() {
        try {
            return fillReviewAggregates(findOutOfStock());
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(ProductRepository.class).error("Error fetching out of stock products", e);
            return List.of();
        }
    }
}
