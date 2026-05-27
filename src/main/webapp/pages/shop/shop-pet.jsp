<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <jsp:include page="/components/favicon.jsp" />
    <title>
        <c:choose>
            <c:when test="${selectedPet == 'dog'}">Sản Phẩm Cho Chó</c:when>
            <c:when test="${selectedPet == 'cat'}">Sản Phẩm Cho Mèo</c:when>
            <c:otherwise>Sản Phẩm - PetShop</c:otherwise>
        </c:choose>
    </title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        * { font-family: 'Montserrat', sans-serif; }
        body { background: #fafafa; }

        .breadcrumb-section { padding: 20px 0 0; }
        .breadcrumb-section a { color: #999; text-decoration: none; font-size: 0.85rem; }
        .breadcrumb-section a:hover { color: #00bfa5; }
        .breadcrumb-section span { color: #333; font-size: 0.85rem; font-weight: 600; }

        .page-header { padding: 30px 0 20px; }
        .page-header h1 { font-size: 2.2rem; font-weight: 800; color: #1a1a1a; }
        .page-header h1 span { color: #00bfa5; }
        .page-header p { color: #666; max-width: 600px; }
        .header-img { max-height: 200px; object-fit: contain; border-radius: 20px; }

        .pet-nav { border-bottom: 2px solid #eee; margin-bottom: 30px; }
        .pet-nav a { display: inline-block; padding: 12px 24px; font-weight: 600; font-size: 0.95rem; color: #999; text-decoration: none; border-bottom: 3px solid transparent; margin-bottom: -2px; transition: all 0.2s; }
        .pet-nav a:hover { color: #333; }
        .pet-nav a.active { color: #00bfa5; border-bottom-color: #00bfa5; }

        .sidebar-title { font-weight: 700; font-size: 0.85rem; text-transform: uppercase; color: #1a1a1a; letter-spacing: 0.5px; margin-bottom: 15px; }
        .sidebar-section { margin-bottom: 30px; }
        .filter-link { display: block; padding: 8px 0; color: #555; text-decoration: none; font-size: 0.9rem; transition: all 0.2s; border-left: 3px solid transparent; padding-left: 12px; }
        .filter-link:hover { color: #00bfa5; border-left-color: #00bfa5; padding-left: 16px; }
        .filter-link.active { color: #00bfa5; font-weight: 600; border-left-color: #00bfa5; }

        .product-card { background: #fff; border-radius: 16px; overflow: hidden; transition: all 0.3s; height: 100%; border: 1px solid #f0f0f0; }
        .product-card:hover { transform: translateY(-4px); box-shadow: 0 12px 30px rgba(0,0,0,0.08); }
        .product-card .img-wrap { height: 220px; display: flex; align-items: center; justify-content: center; background: #f9f9f9; padding: 20px; position: relative; }
        .product-card .img-wrap img { max-height: 190px; max-width: 100%; object-fit: contain; }
        .product-card .badge-sale { position: absolute; top: 10px; left: 10px; background: #00bfa5; color: #fff; padding: 4px 10px; border-radius: 8px; font-size: 0.75rem; font-weight: 700; }
        .product-card .info { padding: 16px; }
        .product-card .cat-label { font-size: 0.7rem; font-weight: 700; text-transform: uppercase; color: #00bfa5; letter-spacing: 0.5px; }
        .product-card .name { font-size: 0.9rem; font-weight: 600; color: #1a1a1a; margin: 6px 0 8px; height: 40px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; }
        .product-card .name a { color: #1a1a1a; text-decoration: none; }
        .product-card .name a:hover { color: #00bfa5; }
        .product-card .rating { font-size: 0.8rem; color: #f5a623; margin-bottom: 8px; }
        .product-card .price { font-weight: 700; color: #1a1a1a; font-size: 1.05rem; }
        .product-card .old-price { text-decoration: line-through; color: #aaa; font-size: 0.85rem; margin-left: 6px; }
        .product-card .btn-cart { width: 38px; height: 38px; border-radius: 50%; background: #00bfa5; color: #fff; border: none; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; transition: all 0.2s; flex-shrink: 0; }
        .product-card .btn-cart:hover { background: #009688; transform: scale(1.1); }
        .product-card .btn-cart:disabled { background: #cbd5e1; cursor: not-allowed; transform: none; }
        .stock-pill { display: inline-flex; align-items: center; gap: 6px; margin-top: 8px; padding: 5px 10px; border-radius: 999px; font-size: 0.78rem; font-weight: 700; }
        .stock-pill.stock-out { background: #fee2e2; color: #b91c1c; }
        .stock-pill.stock-low { background: #fef3c7; color: #b45309; }
        .stock-pill.stock-ok { background: #dcfce7; color: #15803d; }

        .toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 10px; }
        .toolbar .count { color: #999; font-size: 0.9rem; }
        .toolbar .count strong { color: #1a1a1a; }
        .toolbar select { border: 1px solid #ddd; border-radius: 8px; padding: 8px 16px; font-size: 0.9rem; font-family: 'Montserrat', sans-serif; outline: none; }
        .toolbar select:focus { border-color: #00bfa5; }

        .member-banner { background: linear-gradient(135deg, #00bfa5, #26c6da); border-radius: 16px; padding: 25px; color: #fff; }
        .member-banner h5 { font-weight: 700; }
        .member-banner p { font-size: 0.85rem; opacity: 0.9; }
        .btn-member { background: #fff; color: #00bfa5; padding: 8px 20px; border-radius: 50px; font-weight: 700; font-size: 0.85rem; border: none; transition: all 0.3s; text-decoration: none; }
        .btn-member:hover { background: #0b1a33; color: #fff; }

        /* Pagination */
        .shop-pagination { display: flex; justify-content: center; align-items: center; gap: 6px; margin-top: 30px; flex-wrap: wrap; }
        .shop-pagination a, .shop-pagination span { display: inline-flex; align-items: center; justify-content: center; min-width: 40px; height: 40px; padding: 0 12px; border-radius: 10px; font-weight: 600; font-size: 0.9rem; text-decoration: none; transition: all 0.2s; border: 1px solid #e0e0e0; color: #555; background: #fff; }
        .shop-pagination a:hover { border-color: #00bfa5; color: #00bfa5; background: #e0f7f5; }
        .shop-pagination .active { background: #00bfa5; color: #fff; border-color: #00bfa5; }
        .shop-pagination .disabled { opacity: 0.4; pointer-events: none; }

    /*    filter*/
        .brand-list {
            display: flex;
            flex-direction: column;
            gap: 6px;
            max-height: 220px;
            overflow-y: auto;
        }

        .brand-item {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 6px 8px;
            border-radius: 6px;
            cursor: pointer;
            transition: background 0.15s;
        }

        .brand-item:hover {
            background: #f5f5f5;
        }

        .brand-item input[type="checkbox"] {
            width: 16px;
            height: 16px;
            accent-color: #00bfa5;
            cursor: pointer;
            flex-shrink: 0;
        }

        .brand-name {
            font-size: 14px;
            color: #333;
            user-select: none;
        }

        /* Scrollbar đẹp hơn */
        .brand-list::-webkit-scrollbar {
            width: 4px;
        }
        .brand-list::-webkit-scrollbar-track {
            background: #f0f0f0;
            border-radius: 4px;
        }
        .brand-list::-webkit-scrollbar-thumb {
            background: #ccc;
            border-radius: 4px;
        }
    </style>
</head>
<body>
    <jsp:include page="/components/navbar.jsp" />
    <jsp:include page="/components/toast.jsp" />

    <div class="container">
        <div class="breadcrumb-section">
            <a href="${pageContext.request.contextPath}/home">Trang chủ</a> &gt;
            <a href="${pageContext.request.contextPath}/shop">Sản phẩm</a> &gt;
            <span>
                <c:choose>
                    <c:when test="${selectedPet == 'dog'}">Dành cho Chó</c:when>
                    <c:when test="${selectedPet == 'cat'}">Dành cho Mèo</c:when>
                    <c:when test="${not empty selectedCategory}">${selectedCategory}</c:when>
                    <c:when test="${not empty searchKeyword}">Tìm kiếm: ${searchKeyword}</c:when>
                    <c:otherwise>Tất cả</c:otherwise>
                </c:choose>
            </span>
        </div>

        <div class="page-header">
            <div class="row align-items-center">
                <div class="col-lg-8">
                    <h1>
                        <c:choose>
                            <c:when test="${selectedPet == 'dog'}">Dinh dưỡng <span>&</span> Phụ kiện cho Chó</c:when>
                            <c:when test="${selectedPet == 'cat'}">Dinh dưỡng <span>&</span> Phụ kiện cho Mèo</c:when>
                            <c:when test="${not empty searchKeyword}">Kết quả tìm kiếm</c:when>
                            <c:otherwise>Tất cả sản phẩm</c:otherwise>
                        </c:choose>
                    </h1>
                    <p>Khám phá bộ sưu tập được chọn lọc kỹ lưỡng cho sức khoẻ và phong cách của thú cưng của bạn.</p>
                </div>
                <div class="col-lg-4 text-end d-none d-lg-block">
                    <c:choose>
                        <c:when test="${selectedPet == 'dog'}"><img src="https://placehold.co/300x180/e0f7f5/00bfa5?text=🐕" class="header-img" alt="Dog"></c:when>
                        <c:when test="${selectedPet == 'cat'}"><img src="https://placehold.co/300x180/e8f5e9/4caf50?text=🐈" class="header-img" alt="Cat"></c:when>
                    </c:choose>
                </div>
            </div>
        </div>

        <div class="row">
            <!-- SIDEBAR -->
            <div class="col-lg-3 mb-4">
                <div class="sidebar-section"><div class="sidebar-title"><i class='bx bx-filter-alt'></i> Bộ lọc tìm kiếm</div></div>
                <div class="sidebar-section">
                    <div class="sidebar-title">Loại thú cưng</div>
                    <a href="${pageContext.request.contextPath}/shop"
                       class="filter-link ${empty selectedPet && empty selectedCategory ? 'active' : ''}">Tất cả thú cưng</a>
                    <a href="javascript:void(0)" onclick="navigateWithFilters({pet: 'dog', category: null})"
                       class="filter-link ${selectedPet == 'dog' ? 'active' : ''}">🐶 Chó</a>
                    <a href="javascript:void(0)" onclick="navigateWithFilters({pet: 'cat', category: null})"
                       class="filter-link ${selectedPet == 'cat' ? 'active' : ''}">🐱 Mèo</a>
                    <a href="javascript:void(0)" onclick="navigateWithFilters({discountOnly: 'true'})"
                       class="filter-link ${selectedDiscountOnly == 'true' ? ' active' : ''}">🔥 Khuyến mãi</a>
                </div>
                <div class="sidebar-section">
                    <div class="sidebar-title">Thương hiệu</div>
                    <div class="brand-list">
                        <c:forEach var="b" items="${brand}">
                            <label class="brand-item">
                                <input type="checkbox" name="brand" value="${b}" onchange="applyBrand()"
                                <c:forEach var="selected" items="${paramValues.brand}">
                                       <c:if test="${selected == b}">checked</c:if>
                                </c:forEach>
                                >
                                <span class="brand-name">${b}</span>
                            </label>
                        </c:forEach>
                    </div>
                </div>
                <div class="sidebar-section">
                    <div class="sidebar-title">Tình trạng</div>

                </div>
                <div class="sidebar-section">
                    <div class="sidebar-title">Đánh giá</div>

                </div>
                <div class="sidebar-section">
                    <div class="sidebar-title">Danh mục</div>
                    <c:forEach items="${categories}" var="cat">
                        <c:if test="${(selectedPet == 'dog' && cat.contains('Chó')) || (selectedPet == 'cat' && cat.contains('Mèo')) || (empty selectedPet)}">
                            <a href="javascript:void(0)"
                               onclick="navigateWithFilters({category: '${cat}'})"
                               class="filter-link ${selectedCategory == cat ? 'active' : ''}">${cat}</a>
                        </c:if>
                    </c:forEach>
                </div>
                <div class="sidebar-section">
                    <div class="sidebar-title">Khoảng giá</div>
                    <a href="javascript:void(0)" onclick="navigateWithFilters({priceRange: 'under100'})"
                       class="filter-link ${selectedPriceRange == 'under100' ? 'active' : ''}">Dưới 100.000đ</a>
                    <a href="javascript:void(0)" onclick="navigateWithFilters({priceRange: '100to300'})"
                       class="filter-link ${selectedPriceRange == '100to300' ? 'active' : ''}">100.000đ - 300.000đ</a>
                    <a href="javascript:void(0)" onclick="navigateWithFilters({priceRange: '300to500'})"
                       class="filter-link ${selectedPriceRange == '300to500' ? 'active' : ''}">300.000đ - 500.000đ</a>
                    <a href="javascript:void(0)" onclick="navigateWithFilters({priceRange: 'above500'})"
                       class="filter-link ${selectedPriceRange == 'above500' ? 'active' : ''}">Trên 500.000đ</a>
                </div>
                <div class="member-banner mt-3">
                    <h5>🎁 Ưu đãi Thành viên</h5>
                    <p>Giảm thêm 10% cho tất cả các loại thức ăn hạt</p>
                    <a href="${pageContext.request.contextPath}/register" class="btn-member">Đăng ký ngay</a>
                </div>
            </div>

            <!-- MAIN CONTENT -->
            <div class="col-lg-9">
                <div class="toolbar">
                    <div class="count">Hiển thị <strong>${products.size()}</strong> / <strong>${totalProducts}</strong> sản phẩm</div>
                    <div class="d-flex align-items-center gap-2">
                        <span class="small text-muted">Sắp xếp:</span>
                        <select onchange="applySort(this.value)">
                            <option value="">Phổ biến nhất</option>
                            <option value="price-asc" ${selectedSort == 'price-asc' ? 'selected' : ''}>Giá tăng dần</option>
                            <option value="price-desc" ${selectedSort == 'price-desc' ? 'selected' : ''}>Giá giảm dần</option>
                            <option value="discount" ${selectedSort == 'discount' ? 'selected' : ''}>Giảm giá nhiều</option>
                            <option value="name" ${selectedSort == 'name' ? 'selected' : ''}>Tên A-Z</option>
                        </select>
                    </div>
                </div>

                <div class="row g-3">
                    <c:forEach items="${products}" var="p">
                        <div class="col-6 col-md-4">
                            <div class="product-card">
                                <div class="img-wrap">
                                    <c:if test="${p.discount > 0}"><span class="badge-sale">-${p.discount}%</span></c:if>
                                    <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                        <img src="${pageContext.request.contextPath}/assets/images/shop_pic/${p.image}" alt="${p.name}" onerror="this.src='https://placehold.co/200x200/f9f9f9/999?text=PetShop'">
                                    </a>
                                </div>
                                <div class="info">
                                    <div class="cat-label">${p.category}</div>
                                    <div class="name"><a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${p.name}</a></div>
                                    <div class="rating"><i class='bx bxs-star'></i><i class='bx bxs-star'></i><i class='bx bxs-star'></i><i class='bx bxs-star'></i><i class='bx bxs-star-half'></i></div>
                                    <span>Thương hiệu: ${p.brand}</span>
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <span class="price">${p.formattedPrice}</span>
                                            <c:if test="${p.discount > 0}"><span class="old-price">${p.formattedOldPrice}</span></c:if>
                                            <c:choose>
                                                <c:when test="${p.stock <= 0}">
                                                    <div class="stock-pill stock-out">Hết hàng</div>
                                                </c:when>
                                                <c:when test="${p.stock < 10}">
                                                    <div class="stock-pill stock-low">Sắp hết: ${p.stock}</div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="stock-pill stock-ok">Còn hàng: ${p.stock}</div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <form action="${pageContext.request.contextPath}/add-to-cart" method="post">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <input type="hidden" name="quantity" value="1">
                                            <button type="submit" class="btn-cart" <c:if test="${p.stock <= 0}">disabled="disabled"</c:if>><i class='bx bx-cart-add'></i></button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty products}">
                        <div class="col-12 text-center py-5">
                            <i class='bx bx-search-alt' style="font-size: 60px; color: #ddd;"></i>
                            <p class="text-muted mt-3 mb-3">Không tìm thấy sản phẩm nào</p>
                            <a href="${pageContext.request.contextPath}/shop" style="background: #00bfa5; color: #fff; padding: 10px 25px; border-radius: 50px; font-weight: 600; text-decoration: none;">Xem tất cả sản phẩm</a>
                        </div>
                    </c:if>
                </div>

                <!-- PHÂN TRANG -->
                <c:if test="${totalPages > 1}">
                    <div class="shop-pagination">
                        <c:set var="baseUrl" value="${pageContext.request.contextPath}/shop?" />
                        <c:set var="params" value="" />
                        <c:if test="${not empty selectedPet}"><c:set var="params" value="${params}pet=${selectedPet}&" /></c:if>
                        <c:if test="${not empty selectedCategory}"><c:set var="params" value="${params}category=${selectedCategory}&" /></c:if>
                        <c:if test="${not empty searchKeyword}"><c:set var="params" value="${params}search=${searchKeyword}&" /></c:if>
                        <c:if test="${not empty selectedSort}"><c:set var="params" value="${params}sort=${selectedSort}&" /></c:if>
                        <c:if test="${not empty selectedPriceRange}"><c:set var="params" value="${params}priceRange=${selectedPriceRange}&" /></c:if>
                        <c:if test="${not empty selectedDiscountOnly}"><c:set var="params" value="${params}discountOnly=${selectedDiscountOnly}&" /></c:if>

                        <a href="${baseUrl}${params}page=${currentPage - 1}" class="${currentPage <= 1 ? 'disabled' : ''}"><i class='bx bx-chevron-left'></i></a>
                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <c:if test="${i == 1 || i == totalPages || (i >= currentPage - 2 && i <= currentPage + 2)}">
                                <a href="${baseUrl}${params}page=${i}" class="${currentPage == i ? 'active' : ''}">${i}</a>
                            </c:if>
                            <c:if test="${(i == currentPage - 3 && i > 1) || (i == currentPage + 3 && i < totalPages)}">
                                <span style="border: none; background: none;">...</span>
                            </c:if>
                        </c:forEach>
                        <a href="${baseUrl}${params}page=${currentPage + 1}" class="${currentPage >= totalPages ? 'disabled' : ''}"><i class='bx bx-chevron-right'></i></a>
                    </div>
                </c:if>
            </div>
        </div>
    </div>

    <jsp:include page="/components/footer.jsp" />
    <jsp:include page="/components/back-button.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function navigateWithFilters(overrides) {
            const url = new URL(window.location.href);
            url.searchParams.delete('page');
            for (const [key, value] of Object.entries(overrides)) {
                if (value === null || value === '') {
                    url.searchParams.delete(key);
                } else {
                    url.searchParams.set(key, value);
                }
            }
            window.location.href = url.toString();
        }

        function applySort(value) {
            navigateWithFilters({ sort: value || null });
        }
        function applyBrand() {
            const url = new URL(window.location.href);
            url.searchParams.delete('brand');
            url.searchParams.delete('page');

            document.querySelectorAll('input[name="brand"]:checked').forEach(cb => {
                url.searchParams.append('brand', cb.value);
            });
            window.location.href = url.toString();
        }
    </script>
</body>
</html>
