<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <jsp:include page="/components/favicon.jsp" />
    <title>Siêu Thị Thú Cưng - PetShop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        * { font-family: 'Montserrat', sans-serif; }
        body { background: #fafafa; }
        .shop-hero { background: linear-gradient(135deg, #e0f7f5 0%, #f0fffe 100%); padding: 60px 0 50px; position: relative; overflow: hidden; }
        .shop-hero h1 { font-size: 2.8rem; font-weight: 800; color: #1a1a1a; line-height: 1.2; }
        .shop-hero h1 span { color: #00bfa5; }
        .shop-hero p { color: #666; font-size: 1.05rem; max-width: 500px; }
        .hero-stats { background: #00bfa5; color: #fff; border-radius: 15px; padding: 20px 25px; display: inline-block; }
        .hero-stats .num { font-size: 1.8rem; font-weight: 800; }
        .hero-stats .label { font-size: 0.8rem; opacity: 0.9; }
        .hero-img { max-height: 350px; object-fit: contain; }
        .btn-hero { background: #0b1a33; color: #fff; padding: 14px 30px; border-radius: 50px; font-weight: 600; border: none; transition: all 0.3s; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; }
        .btn-hero:hover { background: #333; color: #fff; transform: translateY(-2px); }
        .btn-hero-outline { background: transparent; color: #1a1a1a; padding: 14px 30px; border-radius: 50px; font-weight: 600; border: 2px solid #ddd; transition: all 0.3s; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; }
        .btn-hero-outline:hover { border-color: #00bfa5; color: #00bfa5; }
        .section-header { margin-bottom: 30px; }
        .section-header h2 { font-weight: 700; font-size: 1.6rem; color: #1a1a1a; }
        .section-header a { color: #00bfa5; text-decoration: none; font-weight: 600; font-size: 0.95rem; }
        .section-header a:hover { text-decoration: underline; }
        .product-card { background: #fff; border-radius: 16px; overflow: hidden; transition: all 0.3s; height: 100%; border: 1px solid #f0f0f0; }
        .product-card:hover { transform: translateY(-4px); box-shadow: 0 12px 30px rgba(0,0,0,0.08); }
        .product-card .img-wrap { height: 200px; display: flex; align-items: center; justify-content: center; background: #f9f9f9; padding: 15px; position: relative; }
        .product-card .img-wrap img { max-height: 170px; max-width: 100%; object-fit: contain; }
        .product-card .badge-sale { position: absolute; top: 10px; left: 10px; background: #00bfa5; color: #fff; padding: 4px 10px; border-radius: 8px; font-size: 0.75rem; font-weight: 700; }
        .product-card .badge-top { position: absolute; top: 10px; right: 10px; background: #0b1a33; color: #fff; padding: 4px 10px; border-radius: 999px; font-size: 0.72rem; font-weight: 700; }
        .product-card .info { padding: 16px; }
        .product-card .cat-label { font-size: 0.7rem; font-weight: 700; text-transform: uppercase; color: #00bfa5; letter-spacing: 0.5px; }
        .product-card .name { font-size: 0.9rem; font-weight: 600; color: #1a1a1a; margin: 6px 0 10px; height: 40px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; }
        .product-card .name a { color: #1a1a1a; text-decoration: none; }
        .product-card .name a:hover { color: #00bfa5; }
        .product-card .rating-line { font-size: 0.82rem; color: #f59e0b; margin-bottom: 10px; display: flex; align-items: center; gap: 6px; }
        .product-card .rating-line .muted { color: #94a3b8; }
        .product-card .price { font-weight: 700; color: #1a1a1a; font-size: 1.05rem; }
        .product-card .old-price { text-decoration: line-through; color: #aaa; font-size: 0.85rem; margin-left: 6px; }
        .product-actions { display: flex; align-items: center; gap: 8px; }
        .product-card .btn-cart, .product-card .btn-wishlist { width: 38px; height: 38px; border-radius: 50%; border: none; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; transition: all 0.2s; flex-shrink: 0; }
        .product-card .btn-cart { background: #00bfa5; color: #fff; }
        .product-card .btn-cart:hover { background: #009688; transform: scale(1.1); }
        .product-card .btn-cart:disabled { background: #cbd5e1; cursor: not-allowed; transform: none; }
        .product-card .btn-wishlist { background: #fee2e2; color: #dc2626; border: 1px solid #fecaca; }
        .product-card .btn-wishlist.active { background: #dc2626; color: #fff; border-color: #dc2626; }
        .product-card .btn-wishlist:hover { transform: scale(1.08); }
        .stock-pill { display: inline-flex; align-items: center; gap: 6px; margin-top: 8px; padding: 5px 10px; border-radius: 999px; font-size: 0.78rem; font-weight: 700; }
        .stock-pill.stock-out { background: #fee2e2; color: #b91c1c; }
        .stock-pill.stock-low { background: #fef3c7; color: #b45309; }
        .stock-pill.stock-ok { background: #dcfce7; color: #15803d; }
        .cat-card { background: #fff; border-radius: 16px; padding: 25px 20px; text-align: center; transition: all 0.3s; border: 1px solid #f0f0f0; height: 100%; }
        .cat-card:hover { transform: translateY(-4px); box-shadow: 0 12px 30px rgba(0,0,0,0.08); border-color: #00bfa5; }
        .cat-card .icon { font-size: 2.5rem; margin-bottom: 12px; }
        .cat-card h6 { font-weight: 700; font-size: 0.85rem; color: #1a1a1a; margin-bottom: 4px; }
        .cat-card p { font-size: 0.75rem; color: #999; margin: 0; }
        .cat-card a { text-decoration: none; color: inherit; display: block; }
        .pet-tab { display: inline-flex; align-items: center; gap: 8px; padding: 12px 28px; border-radius: 50px; font-weight: 700; font-size: 1rem; text-decoration: none; transition: all 0.3s; border: 2px solid #eee; color: #666; }
        .pet-tab:hover { border-color: #00bfa5; color: #00bfa5; }
        .pet-tab.active-dog { background: #e0f7f5; border-color: #00bfa5; color: #00bfa5; }
        .pet-tab.active-cat { background: #e8f5e9; border-color: #4caf50; color: #4caf50; }
        .pet-tab i { font-size: 1.3rem; }
        .promo-banner { background: linear-gradient(135deg, #00bfa5, #26c6da); border-radius: 20px; padding: 35px 40px; color: #fff; position: relative; overflow: hidden; }
        .promo-banner::after { content: '🐾'; position: absolute; right: 30px; top: -10px; font-size: 100px; opacity: 0.15; }
        .promo-banner h3 { font-weight: 800; font-size: 1.5rem; }
        .promo-banner p { opacity: 0.9; margin-bottom: 0; }
        .btn-promo { background: #fff; color: #00bfa5; padding: 10px 25px; border-radius: 50px; font-weight: 700; border: none; transition: all 0.3s; text-decoration: none; }
        .btn-promo:hover { background: #0b1a33; color: #fff; }
        .featured-large { background: #e0f7f5; border-radius: 20px; padding: 30px; height: 100%; display: flex; flex-direction: column; justify-content: space-between; }
        .featured-large h4 { font-weight: 700; color: #1a1a1a; }
        .featured-large .price { font-size: 1.3rem; font-weight: 800; color: #00bfa5; }
        .featured-large img { max-height: 180px; object-fit: contain; }
        .featured-large .meta { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin: 10px 0; font-size: 0.85rem; }
        .featured-large .meta .rating { color: #f59e0b; font-weight: 600; }
        .shop-pagination { display: flex; justify-content: center; align-items: center; gap: 6px; margin-top: 30px; flex-wrap: wrap; }
        .shop-pagination a, .shop-pagination span { display: inline-flex; align-items: center; justify-content: center; min-width: 40px; height: 40px; padding: 0 12px; border-radius: 10px; font-weight: 600; font-size: 0.9rem; text-decoration: none; transition: all 0.2s; border: 1px solid #e0e0e0; color: #555; background: #fff; }
        .shop-pagination a:hover { border-color: #00bfa5; color: #00bfa5; background: #e0f7f5; }
        .shop-pagination .active { background: #00bfa5; color: #fff; border-color: #00bfa5; }
        .shop-pagination .disabled { opacity: 0.4; pointer-events: none; }
        .sidebar-title { font-weight: 700; font-size: 0.85rem; text-transform: uppercase; color: #1a1a1a; letter-spacing: 0.5px; margin-bottom: 15px; }
        .sidebar-section { margin-bottom: 30px; }
        .filter-link { display: block; padding: 8px 0; color: #555; text-decoration: none; font-size: 0.9rem; transition: all 0.2s; border-left: 3px solid transparent; padding-left: 12px; }
        .filter-link:hover { color: #00bfa5; border-left-color: #00bfa5; padding-left: 16px; }
        .filter-link.active { color: #00bfa5; font-weight: 600; border-left-color: #00bfa5; }
    </style>
</head>
<body>
    <jsp:include page="/components/navbar.jsp" />
    <jsp:include page="/components/toast.jsp" />
    <c:set var="currentPageUrl" value="${pageContext.request.requestURI}${empty pageContext.request.queryString ? '' : '?'}${pageContext.request.queryString}" />

    <section class="shop-hero">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-lg-6 mb-4 mb-lg-0">
                    <p class="text-uppercase fw-bold small" style="color: #00bfa5; letter-spacing: 2px;">SIÊU THỊ THÚ CƯNG</p>
                    <h1>Nơi Lan Toả<br><span>Hạnh Phúc</span><br>Cho Thú Cưng.</h1>
                    <p class="mt-3 mb-4">Hơn hàng ngàn sản phẩm chất lượng cao, chúng tôi cung cấp thức ăn dinh dưỡng và phụ kiện cao cấp nhất cho thú cưng của bạn.</p>
                    <div class="d-flex gap-3 flex-wrap">
                        <a href="${pageContext.request.contextPath}/shop?pet=dog" class="btn-hero">Mua sắm ngay <i class='bx bx-right-arrow-alt'></i></a>
                        <a href="#categories" class="btn-hero-outline"><i class='bx bx-category'></i> Xem danh mục</a>
                    </div>
                </div>
                <div class="col-lg-6 text-center position-relative">
                    <img src="https://placehold.co/500x350/e0f7f5/00bfa5?text=🐕+🐈+PetShop" alt="PetShop Banner" class="hero-img">
                    <div class="hero-stats position-absolute" style="bottom: 20px; right: 10%;">
                        <div class="num">${totalProducts}+</div>
                        <div class="label">sản phẩm đa dạng</div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div class="container mt-5">
        <div class="text-center mb-5" id="categories">
            <h2 class="fw-bold mb-4">Bạn đang tìm sản phẩm cho?</h2>
            <div class="d-flex justify-content-center gap-3 flex-wrap">
                <a href="${pageContext.request.contextPath}/shop?pet=dog" class="pet-tab active-dog"><i class='bx bxs-dog'></i> Chó</a>
                <a href="${pageContext.request.contextPath}/shop?pet=cat" class="pet-tab active-cat"><i class='bx bxs-cat'></i> Mèo</a>
            </div>
        </div>

        <div class="section-header d-flex justify-content-between align-items-center">
            <h2><i class='bx bx-category' style="color: #00bfa5;"></i> Danh Mục Được Mua Nhiều</h2>
        </div>
        <div class="row g-3 mb-5">
            <div class="col-6 col-md-4 col-lg-2"><div class="cat-card"><a href="${pageContext.request.contextPath}/shop?category=Thức Ăn Cho Chó"><div class="icon">🦴</div><h6>Thức Ăn Cho Chó</h6><p>Dinh dưỡng</p></a></div></div>
            <div class="col-6 col-md-4 col-lg-2"><div class="cat-card"><a href="${pageContext.request.contextPath}/shop?category=Thức Ăn Cho Mèo"><div class="icon">🐟</div><h6>Thức Ăn Cho Mèo</h6><p>Dinh dưỡng</p></a></div></div>
            <div class="col-6 col-md-4 col-lg-2"><div class="cat-card"><a href="${pageContext.request.contextPath}/shop?category=Đồ Chơi - Huấn Luyện Cho Chó"><div class="icon">🎾</div><h6>Đồ Chơi Cho Chó</h6><p>Giải trí</p></a></div></div>
            <div class="col-6 col-md-4 col-lg-2"><div class="cat-card"><a href="${pageContext.request.contextPath}/shop?category=Cát Vệ Sinh Cho Mèo"><div class="icon">🧹</div><h6>Cát Vệ Sinh Mèo</h6><p>Vệ sinh</p></a></div></div>
            <div class="col-6 col-md-4 col-lg-2"><div class="cat-card"><a href="${pageContext.request.contextPath}/shop?category=Chăm Sóc Sức Khoẻ Cho Chó"><div class="icon">💊</div><h6>Sức Khoẻ Chó</h6><p>Chăm sóc</p></a></div></div>
            <div class="col-6 col-md-4 col-lg-2"><div class="cat-card"><a href="${pageContext.request.contextPath}/shop?category=Dụng Cụ Ăn Uống Cho Mèo"><div class="icon">🍽️</div><h6>Dụng Cụ Cho Mèo</h6><p>Phụ kiện</p></a></div></div>
        </div>

        <div class="promo-banner mb-5">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <h3>🎉 Ưu Đãi Thành Viên - Giảm đến 22%</h3>
                    <p>Đăng ký tài khoản để nhận ngay ưu đãi cho tất cả sản phẩm thức ăn hạt cao cấp.</p>
                </div>
                <div class="col-md-4 text-md-end mt-3 mt-md-0">
                    <a href="${pageContext.request.contextPath}/shop?discountOnly=true" class="btn-promo">Xem ưu đãi <i class='bx bx-right-arrow-alt'></i></a>
                </div>
            </div>
        </div>

        <div class="section-header d-flex justify-content-between align-items-center">
            <h2><i class='bx bxs-star' style="color: #00bfa5;"></i> Sản Phẩm Nổi Bật</h2>
            <a href="${pageContext.request.contextPath}/shop?pet=dog">Xem tất cả <i class='bx bx-right-arrow-alt'></i></a>
        </div>
        <div class="row g-3 mb-4">
            <c:if test="${not empty popularProducts}">
                <div class="col-lg-4 col-md-6">
                    <div class="featured-large">
                        <div>
                            <span class="cat-label">${fn:escapeXml(popularProducts[0].category)}</span>
                            <h4 class="mt-2">${fn:escapeXml(popularProducts[0].name)}</h4>
                            <p class="text-muted small">${fn:escapeXml(popularProducts[0].description)}</p>
                            <div class="meta">
                                <span class="rating"><i class='bx bxs-star'></i> ${popularProducts[0].formattedAverageRating}</span>
                                <span class="text-muted">${popularProducts[0].reviewCount} đánh giá</span>
                            </div>
                            <div class="price mb-3">${fn:escapeXml(popularProducts[0].formattedPrice)}</div>
                            <c:choose>
                                <c:when test="${popularProducts[0].stock <= 0}">
                                    <div class="stock-pill stock-out">Hết hàng</div>
                                </c:when>
                                <c:when test="${popularProducts[0].stock < 10}">
                                    <div class="stock-pill stock-low">Sắp hết: ${popularProducts[0].stock}</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="stock-pill stock-ok">Còn hàng: ${popularProducts[0].stock}</div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="text-center">
                            <a href="${pageContext.request.contextPath}/product-detail?id=${popularProducts[0].id}">
                                <img src="${pageContext.request.contextPath}/assets/images/shop_pic/${fn:escapeXml(popularProducts[0].image)}" alt="${fn:escapeXml(popularProducts[0].name)}" style="max-height: 160px; object-fit: contain;" onerror="this.src='https://placehold.co/300x200/e0f7f5/00bfa5?text=PetShop'">
                            </a>
                        </div>
                    </div>
                </div>
            </c:if>
            <div class="col-lg-8">
                <div class="row g-3">
                    <c:forEach items="${popularProducts}" var="p" begin="1" end="4">
                        <div class="col-6 col-md-6 col-lg-3">
                            <div class="product-card">
                                <div class="img-wrap">
                                    <c:if test="${p.discount > 0}"><span class="badge-sale">-${p.discount}%</span></c:if>
                                    <span class="badge-top">Hot</span>
                                    <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                        <img src="${pageContext.request.contextPath}/assets/images/shop_pic/${fn:escapeXml(p.image)}" alt="${fn:escapeXml(p.name)}" onerror="this.src='https://placehold.co/200x200/f9f9f9/999?text=PetShop'">
                                    </a>
                                </div>
                                <div class="info">
                                    <div class="cat-label">${fn:escapeXml(p.category)}</div>
                                    <div class="name"><a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${fn:escapeXml(p.name)}</a></div>
                                    <div class="rating-line"><i class='bx bxs-star'></i> ${p.formattedAverageRating} <span class="muted">(${p.reviewCount} đánh giá)</span></div>
                                    <div class="d-flex justify-content-between align-items-start">
                                        <div>
                                            <span class="price">${fn:escapeXml(p.formattedPrice)}</span>
                                            <c:if test="${p.discount > 0}"><span class="old-price">${fn:escapeXml(p.formattedOldPrice)}</span></c:if>
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
                                        <div class="product-actions">
                                            <form action="${pageContext.request.contextPath}/toggle-wishlist" method="post">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="productId" value="${p.id}">
                                                <input type="hidden" name="redirect" value="${currentPageUrl}">
                                                <button type="submit" class="btn-wishlist ${p.wishlisted ? 'active' : ''}" title="Yêu thích">
                                                    <i class='bx ${p.wishlisted ? 'bxs-heart' : 'bx-heart'}'></i>
                                                </button>
                                            </form>
                                            <form action="${pageContext.request.contextPath}/add-to-cart" method="post">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <input type="hidden" name="id" value="${p.id}">
                                                <input type="hidden" name="quantity" value="1">
                                                <button type="submit" class="btn-cart" <c:if test="${p.stock <= 0}">disabled="disabled"</c:if>><i class='bx bx-cart-add'></i></button>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>

        <c:if test="${not empty discountProducts}">
            <div class="section-header d-flex justify-content-between align-items-center mt-5">
                <h2><i class='bx bxs-discount' style="color: #00bfa5;"></i> Đang Giảm Giá</h2>
                <a href="${pageContext.request.contextPath}/shop?discountOnly=true">Xem tất cả <i class='bx bx-right-arrow-alt'></i></a>
            </div>
            <div class="row g-3 mb-3">
                <c:forEach items="${discountProducts}" var="p">
                    <div class="col-6 col-md-4 col-lg-3">
                        <div class="product-card">
                            <div class="img-wrap">
                                <span class="badge-sale">-${p.discount}%</span>
                                <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                    <img src="${pageContext.request.contextPath}/assets/images/shop_pic/${fn:escapeXml(p.image)}" alt="${fn:escapeXml(p.name)}" onerror="this.src='https://placehold.co/200x200/f9f9f9/999?text=PetShop'">
                                </a>
                            </div>
                            <div class="info">
                                <div class="cat-label">${fn:escapeXml(p.category)}</div>
                                <div class="name"><a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${fn:escapeXml(p.name)}</a></div>
                                <div class="rating-line"><i class='bx bxs-star'></i> ${p.formattedAverageRating} <span class="muted">(${p.reviewCount} đánh giá)</span></div>
                                <div class="d-flex justify-content-between align-items-start">
                                    <div>
                                        <span class="price">${fn:escapeXml(p.formattedPrice)}</span>
                                        <span class="old-price">${fn:escapeXml(p.formattedOldPrice)}</span>
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
                                    <div class="product-actions">
                                        <form action="${pageContext.request.contextPath}/toggle-wishlist" method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="productId" value="${p.id}">
                                            <input type="hidden" name="redirect" value="${currentPageUrl}">
                                            <button type="submit" class="btn-wishlist ${p.wishlisted ? 'active' : ''}" title="Yêu thích">
                                                <i class='bx ${p.wishlisted ? 'bxs-heart' : 'bx-heart'}'></i>
                                            </button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/add-to-cart" method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <input type="hidden" name="quantity" value="1">
                                            <button type="submit" class="btn-cart" <c:if test="${p.stock <= 0}">disabled="disabled"</c:if>><i class='bx bx-cart-add'></i></button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
            <c:if test="${saleTotalPages > 1}">
                <div class="shop-pagination mb-5">
                    <a href="${pageContext.request.contextPath}/shop?salePage=${salePage - 1}&catalogPage=${catalogPage}" class="${salePage <= 1 ? 'disabled' : ''}"><i class='bx bx-chevron-left'></i></a>
                    <c:forEach begin="1" end="${saleTotalPages}" var="i">
                        <a href="${pageContext.request.contextPath}/shop?salePage=${i}&catalogPage=${catalogPage}" class="${salePage == i ? 'active' : ''}">${i}</a>
                    </c:forEach>
                    <a href="${pageContext.request.contextPath}/shop?salePage=${salePage + 1}&catalogPage=${catalogPage}" class="${salePage >= saleTotalPages ? 'disabled' : ''}"><i class='bx bx-chevron-right'></i></a>
                </div>
            </c:if>
        </c:if>

        <c:if test="${not empty catalogProducts}">
            <div class="section-header d-flex justify-content-between align-items-center mt-4">
                <h2><i class='bx bx-grid-alt' style="color: #00bfa5;"></i> Tất Cả Sản Phẩm</h2>
            </div>
            <div class="row">
                <div class="col-lg-3 mb-4">
                    <div class="sidebar-section"><div class="sidebar-title"><i class='bx bx-filter-alt'></i> Bộ lọc tìm kiếm</div></div>
                    <div class="sidebar-section">
                        <div class="sidebar-title">Loại thú cưng</div>
                        <a href="${pageContext.request.contextPath}/shop" class="filter-link active">Tất cả thú cưng</a>
                        <c:forEach items="${petTypes}" var="pt">
                            <a href="${pageContext.request.contextPath}/shop?pet=${pt.code}" class="filter-link"><i class='bx ${pt.icon}'></i> ${pt.name}</a>
                        </c:forEach>
                    </div>
                    <div class="sidebar-section">
                        <div class="sidebar-title">Danh mục</div>
                        <c:forEach items="${categories}" var="cat">
                            <a href="${pageContext.request.contextPath}/shop?category=${cat}" class="filter-link">${cat}</a>
                        </c:forEach>
                    </div>
                    <div class="sidebar-section">
                        <div class="sidebar-title">Khoảng giá</div>
                        <a href="${pageContext.request.contextPath}/shop?priceRange=under100" class="filter-link">Dưới 100.000đ</a>
                        <a href="${pageContext.request.contextPath}/shop?priceRange=100to300" class="filter-link">100.000đ - 300.000đ</a>
                        <a href="${pageContext.request.contextPath}/shop?priceRange=300to500" class="filter-link">300.000đ - 500.000đ</a>
                        <a href="${pageContext.request.contextPath}/shop?priceRange=above500" class="filter-link">Trên 500.000đ</a>
                    </div>
                </div>
                <div class="col-lg-9">
                    <div class="row g-3 mb-3">
                        <c:forEach items="${catalogProducts}" var="p">
                            <div class="col-6 col-md-4 col-lg-4">
                        <div class="product-card">
                            <div class="img-wrap">
                                <c:if test="${p.discount > 0}"><span class="badge-sale">-${p.discount}%</span></c:if>
                                <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                    <img src="${pageContext.request.contextPath}/assets/images/shop_pic/${fn:escapeXml(p.image)}" alt="${fn:escapeXml(p.name)}" onerror="this.src='https://placehold.co/200x200/f9f9f9/999?text=PetShop'">
                                </a>
                            </div>
                            <div class="info">
                                <div class="cat-label">${fn:escapeXml(p.category)}</div>
                                <div class="name"><a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${fn:escapeXml(p.name)}</a></div>
                                <div class="rating-line"><i class='bx bxs-star'></i> ${p.formattedAverageRating} <span class="muted">(${p.reviewCount} đánh giá)</span></div>
                                <div class="d-flex justify-content-between align-items-start">
                                    <div>
                                        <span class="price">${fn:escapeXml(p.formattedPrice)}</span>
                                        <c:if test="${p.discount > 0}"><span class="old-price">${fn:escapeXml(p.formattedOldPrice)}</span></c:if>
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
                                    <div class="product-actions">
                                        <form action="${pageContext.request.contextPath}/toggle-wishlist" method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="productId" value="${p.id}">
                                            <input type="hidden" name="redirect" value="${currentPageUrl}">
                                            <button type="submit" class="btn-wishlist ${p.wishlisted ? 'active' : ''}" title="Yêu thích">
                                                <i class='bx ${p.wishlisted ? 'bxs-heart' : 'bx-heart'}'></i>
                                            </button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/add-to-cart" method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <input type="hidden" name="quantity" value="1">
                                            <button type="submit" class="btn-cart" <c:if test="${p.stock <= 0}">disabled="disabled"</c:if>><i class='bx bx-cart-add'></i></button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
            <c:if test="${catalogTotalPages > 1}">
                <div class="shop-pagination mb-5">
                    <a href="${pageContext.request.contextPath}/shop?catalogPage=${catalogPage - 1}&salePage=${salePage}" class="${catalogPage <= 1 ? 'disabled' : ''}"><i class='bx bx-chevron-left'></i></a>
                    <c:forEach begin="1" end="${catalogTotalPages}" var="i">
                        <a href="${pageContext.request.contextPath}/shop?catalogPage=${i}&salePage=${salePage}" class="${catalogPage == i ? 'active' : ''}">${i}</a>
                    </c:forEach>
                    <a href="${pageContext.request.contextPath}/shop?catalogPage=${catalogPage + 1}&salePage=${salePage}" class="${catalogPage >= catalogTotalPages ? 'disabled' : ''}"><i class='bx bx-chevron-right'></i></a>
                </div>
                </div>
            </div>
        </c:if>
    </div>

    <jsp:include page="/components/footer.jsp" />
    <jsp:include page="/components/back-button.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
