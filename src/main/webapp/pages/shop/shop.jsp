<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <jsp:include page="/components/favicon.jsp" />
    <title>Siêu Thị Thú Cưng - PetShop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Be+Vietnam+Pro:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #00bfa5;
            --primary-dark: #008f7a;
            --primary-soft: #e8fbf7;
            --accent: #ff7a45;
            --ink: #122033;
            --ink-soft: #1a2e5a;
            --text-secondary: #60707d;
            --text-muted: #90a2af;
            --surface: #f5f8f9;
            --white: #ffffff;
            --border: rgba(18, 32, 51, 0.08);
            --shadow-soft: 0 16px 36px rgba(18, 32, 51, 0.08);
        }
        * { box-sizing: border-box; }
        body {
            margin: 0;
            font-family: 'Be Vietnam Pro', sans-serif;
            background: linear-gradient(180deg, #f8fbfb 0%, #f3f7f8 100%);
            color: var(--ink);
        }
        h1,h2,h3,h4,h5,h6 { font-family: 'Plus Jakarta Sans', sans-serif; }
        .section-pad { padding: 56px 0; }
        .shop-hero {
            padding: 76px 0 42px;
            background: linear-gradient(135deg, #0a3447 0%, #0b6b66 48%, var(--primary) 100%);
            color: #fff;
        }
        .shop-hero h1 {
            margin: 0 0 12px;
            font-size: clamp(2.4rem, 5vw, 4rem);
            font-weight: 800;
            letter-spacing: -0.04em;
        }
        .shop-hero p {
            margin: 0;
            max-width: 650px;
            color: rgba(255,255,255,0.84);
            line-height: 1.8;
        }
        .hero-stats {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 14px;
            margin-top: 26px;
        }
        .hero-stat {
            background: rgba(255,255,255,0.12);
            border: 1px solid rgba(255,255,255,0.14);
            border-radius: 22px;
            padding: 18px 16px;
            text-align: center;
        }
        .hero-stat strong {
            display: block;
            font-size: 1.45rem;
            font-weight: 800;
        }
        .hero-stat span {
            display: block;
            margin-top: 4px;
            font-size: 0.84rem;
            color: rgba(255,255,255,0.84);
        }
        .shop-shell {
            margin-top: -22px;
            position: relative;
            z-index: 2;
        }
        .filter-bar {
            background: rgba(255,255,255,0.96);
            border-radius: 28px;
            padding: 20px;
            box-shadow: var(--shadow-soft);
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 14px;
        }
        .filter-field {
            display: flex;
            flex-direction: column;
            gap: 8px;
        }
        .filter-label {
            font-size: 0.82rem;
            font-weight: 700;
            color: var(--ink-soft);
        }
        .filter-control {
            width: 100%;
            min-height: 48px;
            border-radius: 16px;
            border: 1px solid var(--border);
            background: var(--white);
            padding: 0 14px;
            color: var(--ink);
            font-weight: 500;
        }
        .filter-actions {
            display: flex;
            align-items: center;
            gap: 10px;
            flex-wrap: wrap;
        }
        .btn-filter,
        .btn-filter-secondary {
            min-height: 48px;
            border-radius: 16px;
            padding: 0 18px;
            font-weight: 700;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            border: none;
        }
        .btn-filter {
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: #fff;
        }
        .btn-filter:hover { color: #fff; }
        .btn-filter-secondary {
            background: var(--primary-soft);
            color: var(--primary-dark);
        }
        .btn-filter-secondary:hover { color: var(--primary-dark); }
        .category-row {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
        }
        .category-chip {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 16px;
            border-radius: 999px;
            background: var(--white);
            border: 1px solid var(--border);
            color: var(--ink-soft);
            text-decoration: none;
            box-shadow: 0 8px 18px rgba(18, 32, 51, 0.05);
        }
        .category-chip:hover { color: var(--primary-dark); border-color: rgba(0, 191, 165, 0.24); }
        .section-head {
            display: flex;
            align-items: end;
            justify-content: space-between;
            gap: 16px;
            margin-bottom: 24px;
        }
        .section-head h2 {
            margin: 0 0 8px;
            font-size: 1.9rem;
            font-weight: 800;
            color: var(--ink);
        }
        .section-head p {
            margin: 0;
            color: var(--text-secondary);
            max-width: 620px;
            line-height: 1.7;
        }
        .section-link {
            color: var(--primary-dark);
            text-decoration: none;
            font-weight: 700;
            white-space: nowrap;
        }
        .section-link:hover { color: var(--primary-dark); text-decoration: underline; }
        .product-grid {
            display: grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 18px;
        }
        .best-seller-grid {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 18px;
        }
        .product-card {
            background: var(--white);
            border-radius: 26px;
            overflow: hidden;
            border: 1px solid rgba(18, 32, 51, 0.06);
            box-shadow: var(--shadow-soft);
            height: 100%;
            transition: transform 0.25s ease, box-shadow 0.25s ease;
        }
        .product-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 18px 40px rgba(18, 32, 51, 0.10);
        }
        .product-media {
            min-height: 220px;
            padding: 22px;
            background: linear-gradient(180deg, #f9fcfc 0%, #edf6f6 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
        }
        .product-media img {
            max-width: 100%;
            max-height: 170px;
            object-fit: contain;
        }
        .product-badge {
            position: absolute;
            top: 16px;
            left: 16px;
            padding: 7px 12px;
            border-radius: 999px;
            background: var(--accent);
            color: #fff;
            font-size: 0.75rem;
            font-weight: 800;
        }
        .product-info { padding: 18px; }
        .product-category {
            color: var(--primary-dark);
            font-size: 0.72rem;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            margin-bottom: 8px;
        }
        .product-name {
            min-height: 48px;
            margin-bottom: 14px;
            font-weight: 700;
            font-size: 0.96rem;
            line-height: 1.45;
        }
        .product-name a {
            text-decoration: none;
            color: var(--ink);
        }
        .product-name a:hover { color: var(--primary-dark); }
        .product-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
        }
        .price-wrap strong {
            display: block;
            font-size: 1.02rem;
            font-weight: 800;
        }
        .price-wrap span {
            display: block;
            margin-top: 2px;
            color: var(--text-muted);
            text-decoration: line-through;
            font-size: 0.82rem;
        }
        .cart-btn {
            width: 42px;
            height: 42px;
            border-radius: 50%;
            border: none;
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: #fff;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 1.15rem;
        }
        .section-box {
            background: rgba(255,255,255,0.9);
            border-radius: 30px;
            padding: 28px;
            box-shadow: var(--shadow-soft);
        }
        .section-box.best-seller-box {
            background: linear-gradient(180deg, #ffffff 0%, #f9fefd 100%);
        }
        .pagination-bar {
            display: flex;
            justify-content: center;
            flex-wrap: wrap;
            gap: 10px;
            margin-top: 24px;
        }
        .page-btn {
            min-width: 42px;
            height: 42px;
            padding: 0 14px;
            border-radius: 999px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            text-decoration: none;
            color: var(--ink-soft);
            background: var(--white);
            border: 1px solid var(--border);
            font-weight: 700;
        }
        .page-btn.active {
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: #fff;
            border-color: transparent;
        }
        .empty-state {
            text-align: center;
            padding: 36px 20px;
            color: var(--text-secondary);
        }
        @media (max-width: 1199px) {
            .filter-bar,
            .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
            .best-seller-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
        }
        @media (max-width: 767px) {
            .section-pad { padding: 44px 0; }
            .hero-stats,
            .filter-bar,
            .product-grid { grid-template-columns: 1fr; }
            .best-seller-grid { grid-template-columns: 1fr; }
            .section-head { flex-direction: column; align-items: start; }
        }
    </style>
</head>
<body>
    <jsp:include page="/components/navbar.jsp" />

    <section class="shop-hero">
        <div class="container">
            <h1>Trang Shop</h1>
            <p>Nơi tập trung toàn bộ sản phẩm đang bán tại PetShop. Bạn có thể xem nhóm bán chạy, hàng đang giảm giá và toàn bộ catalog sản phẩm với phân trang rõ ràng để dễ duyệt khi số lượng mặt hàng tăng lên sau này.</p>
            <div class="hero-stats">
                <div class="hero-stat"><strong>${totalProducts}</strong><span>Tổng sản phẩm</span></div>
                <div class="hero-stat"><strong>${not empty categories ? categories.size() : 0}</strong><span>Danh mục</span></div>
                <div class="hero-stat"><strong>12</strong><span>Sản phẩm mỗi trang</span></div>
            </div>
        </div>
    </section>

    <div class="shop-shell">
        <div class="container">
            <form action="${pageContext.request.contextPath}/shop" method="get" class="filter-bar">
                <div class="filter-field">
                    <label class="filter-label" for="shopSearch">Tìm kiếm</label>
                    <input id="shopSearch" class="filter-control" type="text" name="search" placeholder="Tên sản phẩm..." />
                </div>
                <div class="filter-field">
                    <label class="filter-label" for="shopPet">Thú cưng</label>
                    <select id="shopPet" class="filter-control" name="pet">
                        <option value="">Tất cả</option>
                        <c:forEach var="pt" items="${petTypes}">
                            <option value="${pt.code}">${pt.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="filter-field">
                    <label class="filter-label" for="shopCategory">Danh mục</label>
                    <select id="shopCategory" class="filter-control" name="category">
                        <option value="">Tất cả</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat}">${cat}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="filter-field">
                    <label class="filter-label" for="shopSort">Sắp xếp</label>
                    <select id="shopSort" class="filter-control" name="sort">
                        <option value="">Mặc định</option>
                        <option value="price-asc">Giá tăng dần</option>
                        <option value="price-desc">Giá giảm dần</option>
                        <option value="discount">Giảm giá nhiều nhất</option>
                        <option value="name">Tên A-Z</option>
                    </select>
                </div>
                <div class="filter-actions" style="grid-column: 1 / -1;">
                    <button type="submit" class="btn-filter"><i class='bx bx-search-alt-2'></i> Lọc sản phẩm</button>
                    <a href="${pageContext.request.contextPath}/shop" class="btn-filter-secondary"><i class='bx bx-reset'></i> Đặt lại</a>
                    <a href="${pageContext.request.contextPath}/shop?discountOnly=true" class="btn-filter-secondary"><i class='bx bxs-discount'></i> Chỉ xem hàng giảm giá</a>
                </div>
            </form>
        </div>
    </div>

    <section class="section-pad" style="padding-bottom: 20px;">
        <div class="container">
            <div class="section-head">
                <div>
                    <h2>Danh Mục Sản Phẩm</h2>
                    <p>Các danh mục hiện có trong cửa hàng để bạn đi nhanh đến đúng nhóm sản phẩm đang cần.</p>
                </div>
            </div>
            <div class="category-row">
                <c:forEach var="cat" items="${categories}">
                    <a href="${pageContext.request.contextPath}/shop?category=${cat}" class="category-chip">
                        <c:choose>
                            <c:when test="${cat.contains('Thức Ăn')}"><i class='bx bx-bowl-hot'></i></c:when>
                            <c:when test="${cat.contains('Sữa')}"><i class='bx bx-coffee-togo'></i></c:when>
                            <c:when test="${cat.contains('Vệ Sinh') || cat.contains('Tắm')}"><i class='bx bx-spray-can'></i></c:when>
                            <c:when test="${cat.contains('Cát')}"><i class='bx bx-archive'></i></c:when>
                            <c:when test="${cat.contains('Ăn Uống')}"><i class='bx bx-dish'></i></c:when>
                            <c:when test="${cat.contains('Sức Khoẻ')}"><i class='bx bx-plus-medical'></i></c:when>
                            <c:when test="${cat.contains('Huấn Luyện') || cat.contains('Đồ Chơi')}"><i class='bx bx-bone'></i></c:when>
                            <c:when test="${cat.contains('Dụng Cụ Vệ Sinh')}"><i class='bx bx-brush'></i></c:when>
                            <c:otherwise><i class='bx bx-paw'></i></c:otherwise>
                        </c:choose>
                        ${cat}
                    </a>
                </c:forEach>
            </div>
        </div>
    </section>

    <section class="section-pad" style="padding-top: 12px;">
        <div class="container">
            <div class="section-box best-seller-box">
                <div class="section-head">
                    <div>
                        <h2>Bán Chạy Nổi Bật</h2>
                        <p>Một khu ngắn để nhấn vào sản phẩm đang được quan tâm nhiều nhất, tránh lặp với catalog chính.</p>
                    </div>
                </div>
                <c:choose>
                    <c:when test="${not empty popularProducts}">
                        <div class="best-seller-grid">
                            <c:forEach var="p" items="${popularProducts}">
                                <div class="product-card">
                                    <div class="product-media">
                                        <c:if test="${p.discount > 0}"><span class="product-badge">-${p.discount}%</span></c:if>
                                        <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                            <c:choose>
                                                <c:when test="${not empty p.image && p.image.startsWith('http')}">
                                                    <img src="${p.image}" alt="${p.name}" onerror="this.src='https://placehold.co/260x220/e8fbf7/008f7a?text=PetShop'">
                                                </c:when>
                                                <c:when test="${not empty p.image}">
                                                    <img src="${pageContext.request.contextPath}/${p.image}" alt="${p.name}" onerror="this.src='https://placehold.co/260x220/e8fbf7/008f7a?text=PetShop'">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="https://placehold.co/260x220/e8fbf7/008f7a?text=PetShop" alt="${p.name}">
                                                </c:otherwise>
                                            </c:choose>
                                        </a>
                                    </div>
                                    <div class="product-info">
                                        <div class="product-category">${p.category}</div>
                                        <div class="product-name"><a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${p.name}</a></div>
                                        <div class="product-meta">
                                            <div class="price-wrap">
                                                <strong>${p.formattedPrice}</strong>
                                                <c:if test="${p.discount > 0}"><span>${p.formattedOldPrice}</span></c:if>
                                            </div>
                                            <form action="${pageContext.request.contextPath}/add-to-cart" method="post" style="margin:0;">
                                                <input type="hidden" name="id" value="${p.id}">
                                                <input type="hidden" name="quantity" value="1">
                                                <button type="submit" class="cart-btn"><i class='bx bx-cart-add'></i></button>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise><div class="empty-state">Chưa có sản phẩm để hiển thị.</div></c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>

    <section class="section-pad" style="padding-top: 0;">
        <div class="container">
            <div class="section-box">
                <div class="section-head">
                    <div>
                        <h2>Hàng Đang Được Giảm Giá</h2>
                        <p>Các sản phẩm đang có ưu đãi, mỗi trang hiển thị 12 sản phẩm để dễ theo dõi và chốt đơn.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/shop?discountOnly=true" class="section-link">Xem trang lọc giảm giá</a>
                </div>
                <c:choose>
                    <c:when test="${not empty discountProducts}">
                        <div class="product-grid">
                            <c:forEach var="p" items="${discountProducts}">
                                <div class="product-card">
                                    <div class="product-media">
                                        <span class="product-badge">-${p.discount}%</span>
                                        <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                            <c:choose>
                                                <c:when test="${not empty p.image && p.image.startsWith('http')}">
                                                    <img src="${p.image}" alt="${p.name}" onerror="this.src='https://placehold.co/260x220/e8fbf7/008f7a?text=Sale'">
                                                </c:when>
                                                <c:when test="${not empty p.image}">
                                                    <img src="${pageContext.request.contextPath}/${p.image}" alt="${p.name}" onerror="this.src='https://placehold.co/260x220/e8fbf7/008f7a?text=Sale'">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="https://placehold.co/260x220/e8fbf7/008f7a?text=Sale" alt="${p.name}">
                                                </c:otherwise>
                                            </c:choose>
                                        </a>
                                    </div>
                                    <div class="product-info">
                                        <div class="product-category">${p.category}</div>
                                        <div class="product-name"><a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${p.name}</a></div>
                                        <div class="product-meta">
                                            <div class="price-wrap">
                                                <strong>${p.formattedPrice}</strong>
                                                <span>${p.formattedOldPrice}</span>
                                            </div>
                                            <form action="${pageContext.request.contextPath}/add-to-cart" method="post" style="margin:0;">
                                                <input type="hidden" name="id" value="${p.id}">
                                                <input type="hidden" name="quantity" value="1">
                                                <button type="submit" class="cart-btn"><i class='bx bx-cart-add'></i></button>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise><div class="empty-state">Hiện chưa có sản phẩm giảm giá.</div></c:otherwise>
                </c:choose>
                <c:if test="${saleTotalPages > 1}">
                    <div class="pagination-bar">
                        <c:forEach begin="1" end="${saleTotalPages}" var="i">
                            <a href="${pageContext.request.contextPath}/shop?salePage=${i}&catalogPage=${catalogPage}" class="page-btn ${i == salePage ? 'active' : ''}">${i}</a>
                        </c:forEach>
                    </div>
                </c:if>
            </div>
        </div>
    </section>

    <section class="section-pad" style="padding-top: 0;">
        <div class="container">
            <div class="section-box">
                <div class="section-head">
                    <div>
                        <h2>Tất Cả Sản Phẩm</h2>
                        <p>Toàn bộ sản phẩm trong database được đưa vào khu catalog chính. Mỗi trang hiển thị 12 sản phẩm để sau này bạn thêm nhiều mặt hàng vẫn duyệt được rõ ràng.</p>
                    </div>
                </div>
                <c:choose>
                    <c:when test="${not empty catalogProducts}">
                        <div class="product-grid">
                            <c:forEach var="p" items="${catalogProducts}">
                                <div class="product-card">
                                    <div class="product-media">
                                        <c:if test="${p.discount > 0}"><span class="product-badge">-${p.discount}%</span></c:if>
                                        <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                            <c:choose>
                                                <c:when test="${not empty p.image && p.image.startsWith('http')}">
                                                    <img src="${p.image}" alt="${p.name}" onerror="this.src='https://placehold.co/260x220/e8fbf7/008f7a?text=PetShop'">
                                                </c:when>
                                                <c:when test="${not empty p.image}">
                                                    <img src="${pageContext.request.contextPath}/${p.image}" alt="${p.name}" onerror="this.src='https://placehold.co/260x220/e8fbf7/008f7a?text=PetShop'">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="https://placehold.co/260x220/e8fbf7/008f7a?text=PetShop" alt="${p.name}">
                                                </c:otherwise>
                                            </c:choose>
                                        </a>
                                    </div>
                                    <div class="product-info">
                                        <div class="product-category">${p.category}</div>
                                        <div class="product-name"><a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${p.name}</a></div>
                                        <div class="product-meta">
                                            <div class="price-wrap">
                                                <strong>${p.formattedPrice}</strong>
                                                <c:if test="${p.discount > 0}"><span>${p.formattedOldPrice}</span></c:if>
                                            </div>
                                            <form action="${pageContext.request.contextPath}/add-to-cart" method="post" style="margin:0;">
                                                <input type="hidden" name="id" value="${p.id}">
                                                <input type="hidden" name="quantity" value="1">
                                                <button type="submit" class="cart-btn"><i class='bx bx-cart-add'></i></button>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise><div class="empty-state">Chưa có sản phẩm trong catalog.</div></c:otherwise>
                </c:choose>
                <c:if test="${catalogTotalPages > 1}">
                    <div class="pagination-bar">
                        <c:forEach begin="1" end="${catalogTotalPages}" var="i">
                            <a href="${pageContext.request.contextPath}/shop?salePage=${salePage}&catalogPage=${i}" class="page-btn ${i == catalogPage ? 'active' : ''}">${i}</a>
                        </c:forEach>
                    </div>
                </c:if>
            </div>
        </div>
    </section>

    <jsp:include page="/components/footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
