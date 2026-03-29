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
            <c:when test="${not empty selectedPetType}">Sản Phẩm Cho ${selectedPetType.name} - PetShop</c:when>
            <c:when test="${not empty searchKeyword}">Tìm kiếm: ${searchKeyword} - PetShop</c:when>
            <c:when test="${not empty selectedCategory}">${selectedCategory} - PetShop</c:when>
            <c:otherwise>Sản Phẩm - PetShop</c:otherwise>
        </c:choose>
    </title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Be+Vietnam+Pro:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #00bfa5;
            --primary-dark: #008f7a;
            --primary-light: #e0f7f3;
            --accent: #ff6b35;
            --surface: #f5f6f7;
            --surface-low: #eff1f2;
            --surface-white: #ffffff;
            --text: #1a2e35;
            --text-secondary: #5a6a70;
            --text-muted: #8a9a9e;
        }
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Be Vietnam Pro', sans-serif; background: var(--surface); color: var(--text); }
        h1,h2,h3,h4,h5,h6 { font-family: 'Plus Jakarta Sans', sans-serif; }

        .breadcrumb-bar { padding: 20px 0 0; }
        .breadcrumb-bar a { color: var(--text-secondary); text-decoration: none; font-size: 0.85rem; }
        .breadcrumb-bar a:hover { color: var(--primary); }
        .breadcrumb-bar span { color: var(--text); font-size: 0.85rem; font-weight: 600; }

        .page-header { padding: 2rem 0; }
        .page-header h1 { font-size: 2.4rem; font-weight: 800; letter-spacing: -0.02em; line-height: 1.2; }
        .page-header h1 .accent { color: var(--primary); }
        .page-header p { color: var(--text-secondary); font-size: 1rem; max-width: 550px; line-height: 1.7; }

        .pet-nav {
            display: inline-flex; gap: 0;
            margin-bottom: 2rem;
            background: var(--surface-white);
            border-radius: 9999px; padding: 4px;
        }
        .pet-nav a {
            padding: 10px 24px; font-weight: 600; font-size: 0.9rem;
            color: var(--text-secondary); text-decoration: none;
            border-radius: 9999px; transition: all 0.25s;
            font-family: 'Plus Jakarta Sans', sans-serif;
        }
        .pet-nav a:hover { color: var(--text); background: var(--surface-low); }
        .pet-nav a.active {
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: #fff;
        }

        .sidebar-block {
            background: var(--surface-white); border-radius: 1.5rem;
            padding: 1.5rem; margin-bottom: 1rem;
        }
        .sidebar-title {
            font-weight: 700; font-size: 0.8rem; text-transform: uppercase;
            color: var(--text); letter-spacing: 0.5px; margin-bottom: 1rem;
            font-family: 'Plus Jakarta Sans', sans-serif;
        }
        .filter-link {
            display: block; padding: 8px 12px;
            color: var(--text-secondary); text-decoration: none;
            font-size: 0.88rem; transition: all 0.2s;
            border-radius: 0.75rem; margin-bottom: 2px;
        }
        .filter-link:hover { color: var(--primary); background: var(--surface-low); }
        .filter-link.active { color: var(--primary-dark); font-weight: 600; background: var(--primary-light); }

        .member-cta {
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            border-radius: 1.5rem; padding: 1.5rem; color: #fff;
        }
        .member-cta h6 { font-weight: 700; font-family: 'Plus Jakarta Sans', sans-serif; }
        .member-cta p { font-size: 0.82rem; opacity: 0.9; margin-bottom: 12px; }
        .btn-member {
            background: var(--surface-white); color: var(--primary-dark);
            padding: 8px 20px; border-radius: 9999px;
            font-weight: 700; font-size: 0.82rem; border: none;
            transition: all 0.25s; text-decoration: none;
            font-family: 'Plus Jakarta Sans', sans-serif;
        }
        .btn-member:hover { transform: scale(1.02); box-shadow: 0 8px 20px rgba(0,191,165,0.15); color: var(--primary-dark); }

        .toolbar { margin-bottom: 1.5rem; }
        .toolbar .count { color: var(--text-secondary); font-size: 0.9rem; }
        .toolbar .count strong { color: var(--text); }
        .toolbar select {
            background: var(--surface-white); border: none;
            border-radius: 9999px; padding: 8px 20px;
            font-size: 0.88rem; font-family: 'Be Vietnam Pro', sans-serif;
            color: var(--text); outline: none; cursor: pointer;
            box-shadow: 0 2px 8px rgba(0,0,0,0.04);
        }
        .toolbar select:focus { box-shadow: 0 0 0 2px rgba(0,191,165,0.15); }

        .product-card {
            background: var(--surface-white); border-radius: 1.5rem;
            overflow: hidden; transition: all 0.3s; height: 100%;
        }
        .product-card:hover { transform: translateY(-4px); box-shadow: 0 20px 40px rgba(0,191,165,0.06); }
        .product-card .img-wrap {
            height: 220px; display: flex; align-items: center; justify-content: center;
            background: var(--surface-low); padding: 20px; position: relative;
        }
        .product-card .img-wrap img { max-height: 190px; max-width: 100%; object-fit: contain; }
        .product-card .badge-sale {
            position: absolute; top: 14px; left: 14px;
            background: var(--accent); color: #fff;
            padding: 5px 12px; border-radius: 9999px;
            font-size: 0.75rem; font-weight: 700; font-family: 'Plus Jakarta Sans', sans-serif;
        }
        .product-card .info { padding: 1rem; }
        .product-card .cat-label {
            font-size: 0.7rem; font-weight: 700; text-transform: uppercase;
            color: var(--primary-dark); letter-spacing: 0.5px;
            font-family: 'Plus Jakarta Sans', sans-serif;
        }
        .product-card .name {
            font-size: 0.9rem; font-weight: 600; color: var(--text);
            margin: 6px 0 8px; height: 42px; overflow: hidden;
            display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical;
            font-family: 'Plus Jakarta Sans', sans-serif;
        }
        .product-card .name a { color: var(--text); text-decoration: none; }
        .product-card .name a:hover { color: var(--primary); }
        .product-card .price { font-weight: 800; color: var(--text); font-size: 1.05rem; font-family: 'Plus Jakarta Sans', sans-serif; }
        .product-card .old-price { text-decoration: line-through; color: var(--text-muted); font-size: 0.85rem; margin-left: 6px; }
        .product-card .btn-cart {
            width: 40px; height: 40px; border-radius: 9999px;
            background: var(--primary); color: #fff; border: none;
            display: flex; align-items: center; justify-content: center;
            font-size: 1.1rem; transition: all 0.25s; flex-shrink: 0;
        }
        .product-card .btn-cart:hover { background: var(--primary-dark); transform: scale(1.08); }

        .empty-state i { font-size: 64px; color: var(--text-muted); }
        .btn-back-shop {
            background: var(--primary); color: #fff;
            padding: 10px 28px; border-radius: 9999px;
            font-weight: 600; font-size: 0.9rem; border: none;
            text-decoration: none; display: inline-flex; align-items: center; gap: 6px;
            transition: all 0.25s;
        }
        .btn-back-shop:hover { background: var(--primary-dark); color: #fff; transform: scale(1.02); }
    </style>
</head>
<body>
    <jsp:include page="/components/navbar.jsp" />

    <div class="container">
        <div class="breadcrumb-bar">
            <a href="${pageContext.request.contextPath}/home">Trang chủ</a> &rsaquo;
            <a href="${pageContext.request.contextPath}/shop">Sản phẩm</a> &rsaquo;
            <span>
                <c:choose>
                    <c:when test="${not empty selectedPetType}">Dành cho ${selectedPetType.name}</c:when>
                    <c:when test="${not empty selectedCategory}">${selectedCategory}</c:when>
                    <c:when test="${not empty searchKeyword}">Tìm kiếm: ${searchKeyword}</c:when>
                    <c:when test="${selectedDiscountOnly == 'true'}">Khuyến mãi</c:when>
                    <c:otherwise>Tất cả</c:otherwise>
                </c:choose>
            </span>
        </div>

        <!-- Page Header -->
        <div class="page-header">
            <div class="row align-items-center">
                <div class="col-lg-8">
                    <c:choose>
                        <c:when test="${not empty selectedPetType}">
                            <h1>Sản Phẩm Cho <span class="accent">${selectedPetType.name}</span> 
                                <c:choose>
                                    <c:when test="${selectedPetType.code == 'dog'}">🐕</c:when>
                                    <c:when test="${selectedPetType.code == 'cat'}">🐈</c:when>
                                    <c:when test="${selectedPetType.code == 'fish'}">🐟</c:when>
                                    <c:when test="${selectedPetType.code == 'bird'}">🐦</c:when>
                                    <c:when test="${selectedPetType.code == 'hamster'}">🐹</c:when>
                                    <c:when test="${selectedPetType.code == 'rabbit'}">🐰</c:when>
                                    <c:otherwise>🐾</c:otherwise>
                                </c:choose>
                            </h1>
                            <p>Thức ăn, đồ chơi, phụ kiện và chăm sóc sức khỏe dành riêng cho ${selectedPetType.name} của bạn.</p>
                        </c:when>
                        <c:when test="${not empty searchKeyword}">
                            <h1>Kết quả: <span class="accent">${searchKeyword}</span></h1>
                            <p>Tìm thấy ${totalProducts} sản phẩm phù hợp.</p>
                        </c:when>
                        <c:when test="${not empty selectedCategory}">
                            <h1><span class="accent">${selectedCategory}</span></h1>
                        </c:when>
                        <c:when test="${selectedDiscountOnly == 'true'}">
                            <h1>Sản Phẩm <span class="accent">Khuyến Mãi</span> 🔥</h1>
                            <p>Những ưu đãi tốt nhất dành cho thú cưng của bạn.</p>
                        </c:when>
                        <c:otherwise>
                            <h1>Tất Cả <span class="accent">Sản Phẩm</span></h1>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <!-- Pet Nav Tabs - Hiển thị động từ database -->
        <div class="pet-nav">
            <c:choose>
                <c:when test="${not empty petTypes}">
                    <c:forEach var="pt" items="${petTypes}">
                        <a href="${pageContext.request.contextPath}/shop?pet=${pt.code}"
                           class="${selectedPet == pt.code ? 'active' : ''}">
                            <i class='bx ${pt.icon}'></i> ${pt.name}
                        </a>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/shop?pet=dog"
                       class="${selectedPet == 'dog' ? 'active' : ''}">
                        <i class='bx bxs-dog'></i> Chó
                    </a>
                    <a href="${pageContext.request.contextPath}/shop?pet=cat"
                       class="${selectedPet == 'cat' ? 'active' : ''}">
                        <i class='bx bxs-cat'></i> Mèo
                    </a>
                </c:otherwise>
            </c:choose>
            <a href="${pageContext.request.contextPath}/shop"
               class="${empty selectedPet && empty selectedCategory && empty searchKeyword ? 'active' : ''}">
                Tất cả
            </a>
        </div>

        <div class="row">
            <!-- SIDEBAR -->
            <div class="col-lg-3 mb-4">
                <!-- Danh mục -->
                <div class="sidebar-block">
                    <div class="sidebar-title">Danh mục</div>
                    <c:forEach var="cat" items="${categories}">
                        <a href="${pageContext.request.contextPath}/shop?category=${cat}"
                           class="filter-link ${selectedCategory == cat ? 'active' : ''}">${cat}</a>
                    </c:forEach>
                </div>

                <!-- Khoảng giá -->
                <div class="sidebar-block">
                    <div class="sidebar-title">Khoảng giá</div>
                    <a href="${pageContext.request.contextPath}/shop?${not empty selectedPet ? 'pet='.concat(selectedPet).concat('&') : ''}priceRange=under100"
                       class="filter-link ${selectedPriceRange == 'under100' ? 'active' : ''}">Dưới 100.000đ</a>
                    <a href="${pageContext.request.contextPath}/shop?${not empty selectedPet ? 'pet='.concat(selectedPet).concat('&') : ''}priceRange=100to300"
                       class="filter-link ${selectedPriceRange == '100to300' ? 'active' : ''}">100.000đ - 300.000đ</a>
                    <a href="${pageContext.request.contextPath}/shop?${not empty selectedPet ? 'pet='.concat(selectedPet).concat('&') : ''}priceRange=300to500"
                       class="filter-link ${selectedPriceRange == '300to500' ? 'active' : ''}">300.000đ - 500.000đ</a>
                    <a href="${pageContext.request.contextPath}/shop?${not empty selectedPet ? 'pet='.concat(selectedPet).concat('&') : ''}priceRange=above500"
                       class="filter-link ${selectedPriceRange == 'above500' ? 'active' : ''}">Trên 500.000đ</a>
                </div>

                <!-- Khuyến mãi -->
                <div class="sidebar-block">
                    <div class="sidebar-title">Ưu đãi</div>
                    <a href="${pageContext.request.contextPath}/shop?discountOnly=true"
                       class="filter-link ${selectedDiscountOnly == 'true' ? 'active' : ''}">
                        <i class='bx bxs-discount' style="color: var(--accent); margin-right: 6px;"></i> Đang giảm giá
                    </a>
                </div>

                <!-- Member CTA -->
                <div class="member-cta">
                    <h6>🎁 Ưu đãi thành viên</h6>
                    <p>Giảm thêm 10% cho đơn hàng đầu tiên khi đăng ký.</p>
                    <a href="${pageContext.request.contextPath}/register" class="btn-member">Đăng ký ngay</a>
                </div>
            </div>

            <!-- MAIN CONTENT -->
            <div class="col-lg-9">
                <!-- Toolbar -->
                <div class="toolbar d-flex justify-content-between align-items-center flex-wrap gap-2">
                    <div class="count">Hiển thị <strong>${totalProducts}</strong> sản phẩm</div>
                    <form id="sortForm" method="get" action="${pageContext.request.contextPath}/shop" class="d-flex align-items-center gap-2">
                        <c:if test="${not empty selectedPet}"><input type="hidden" name="pet" value="${selectedPet}"></c:if>
                        <c:if test="${not empty selectedCategory}"><input type="hidden" name="category" value="${selectedCategory}"></c:if>
                        <c:if test="${not empty searchKeyword}"><input type="hidden" name="search" value="${searchKeyword}"></c:if>
                        <c:if test="${not empty selectedPriceRange}"><input type="hidden" name="priceRange" value="${selectedPriceRange}"></c:if>
                        <c:if test="${selectedDiscountOnly == 'true'}"><input type="hidden" name="discountOnly" value="true"></c:if>
                        <select name="sort" onchange="document.getElementById('sortForm').submit();">
                            <option value="">Sắp xếp</option>
                            <option value="price-asc" ${selectedSort == 'price-asc' ? 'selected' : ''}>Giá tăng dần</option>
                            <option value="price-desc" ${selectedSort == 'price-desc' ? 'selected' : ''}>Giá giảm dần</option>
                            <option value="discount" ${selectedSort == 'discount' ? 'selected' : ''}>Giảm giá nhiều</option>
                            <option value="name" ${selectedSort == 'name' ? 'selected' : ''}>Tên A-Z</option>
                        </select>
                    </form>
                </div>

                <!-- Product Grid -->
                <div class="row g-3">
                    <c:choose>
                        <c:when test="${not empty products}">
                            <c:forEach var="p" items="${products}">
                                <div class="col-6 col-md-4">
                                    <div class="product-card">
                                        <div class="img-wrap">
                                            <c:if test="${p.discount > 0}">
                                                <span class="badge-sale">-${p.discount}%</span>
                                            </c:if>
                                            <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                                <c:choose>
                                                    <c:when test="${p.image.startsWith('http')}">
                                                        <img src="${p.image}" alt="${p.name}"
                                                             onerror="this.src='https://placehold.co/200x200/e0f7f3/00bfa5?text=PetShop'">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <img src="${pageContext.request.contextPath}/${p.image}" alt="${p.name}"
                                                             onerror="this.src='https://placehold.co/200x200/e0f7f3/00bfa5?text=PetShop'">
                                                    </c:otherwise>
                                                </c:choose>
                                            </a>
                                        </div>
                                        <div class="info">
                                            <div class="cat-label">${p.category}</div>
                                            <div class="name">
                                                <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${p.name}</a>
                                            </div>
                                            <div class="d-flex justify-content-between align-items-center">
                                                <div>
                                                    <span class="price">${p.formattedPrice}</span>
                                                    <c:if test="${p.discount > 0}">
                                                        <span class="old-price">${p.formattedOldPrice}</span>
                                                    </c:if>
                                                </div>
                                                <form action="${pageContext.request.contextPath}/add-to-cart" method="post" style="margin:0;">
                                                    <input type="hidden" name="productId" value="${p.id}">
                                                    <input type="hidden" name="quantity" value="1">
                                                    <button type="submit" class="btn-cart" title="Thêm vào giỏ">
                                                        <i class='bx bx-cart-add'></i>
                                                    </button>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="col-12 text-center py-5">
                                <div class="empty-state">
                                    <i class='bx bx-search-alt'></i>
                                    <h5 class="mt-3">Không tìm thấy sản phẩm</h5>
                                    <p style="color: var(--text-secondary);">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm.</p>
                                    <a href="${pageContext.request.contextPath}/shop" class="btn-back-shop mt-2">
                                        <i class='bx bx-arrow-back'></i> Quay lại Shop
                                    </a>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>

    <div style="margin-top: 40px;">
        <jsp:include page="/components/footer.jsp" />
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
