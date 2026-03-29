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

        /* HERO */
        .shop-hero {
            background: linear-gradient(135deg, #004d40 0%, var(--primary) 100%);
            padding: 80px 0 60px;
            position: relative;
            overflow: hidden;
        }
        .shop-hero::before {
            content: '';
            position: absolute;
            width: 600px; height: 600px;
            background: rgba(255,255,255,0.04);
            border-radius: 50%;
            top: -200px; right: -100px;
        }
        .shop-hero h1 {
            font-size: 3rem; font-weight: 800; color: #fff;
            line-height: 1.15; letter-spacing: -0.02em;
        }
        .shop-hero h1 .highlight { color: var(--primary-light); }
        .shop-hero .subtitle { color: rgba(255,255,255,0.8); font-size: 1.05rem; max-width: 480px; line-height: 1.7; }
        .btn-primary-pill {
            background: #fff; color: var(--primary-dark);
            padding: 14px 32px; border-radius: 9999px;
            font-weight: 700; font-family: 'Plus Jakarta Sans', sans-serif;
            border: none; font-size: 0.95rem;
            transition: all 0.25s; text-decoration: none;
            display: inline-flex; align-items: center; gap: 8px;
        }
        .btn-primary-pill:hover { transform: scale(1.03); box-shadow: 0 12px 30px rgba(0,191,165,0.2); color: var(--primary-dark); }
        .btn-outline-pill {
            background: transparent; color: #fff;
            padding: 14px 32px; border-radius: 9999px;
            font-weight: 600; border: 2px solid rgba(255,255,255,0.3);
            font-size: 0.95rem; transition: all 0.25s; text-decoration: none;
            display: inline-flex; align-items: center; gap: 8px;
        }
        .btn-outline-pill:hover { background: rgba(255,255,255,0.1); border-color: rgba(255,255,255,0.6); color: #fff; }
        .hero-stat {
            background: var(--accent); color: #fff;
            border-radius: 1.5rem; padding: 18px 24px;
        }
        .hero-stat .num { font-size: 1.8rem; font-weight: 800; font-family: 'Plus Jakarta Sans', sans-serif; }
        .hero-stat .label { font-size: 0.78rem; opacity: 0.9; }

        /* SECTIONS */
        .section-pad { padding: 60px 0; }
        .section-header { margin-bottom: 2rem; }
        .section-header h2 { font-weight: 800; font-size: 1.5rem; color: var(--text); }
        .section-header .view-all {
            color: var(--primary-dark); text-decoration: none;
            font-weight: 600; font-size: 0.9rem;
            font-family: 'Plus Jakarta Sans', sans-serif;
            position: relative; z-index: 2;
        }
        .section-header .view-all:hover { text-decoration: underline; }

        /* PET TABS */
        .pet-tab {
            display: inline-flex; align-items: center; gap: 10px;
            padding: 16px 36px; border-radius: 9999px;
            font-weight: 700; font-size: 1rem;
            font-family: 'Plus Jakarta Sans', sans-serif;
            text-decoration: none; transition: all 0.3s;
            background: var(--surface-white); color: var(--text-secondary);
            box-shadow: 0 4px 15px rgba(0,0,0,0.04);
        }
        .pet-tab:hover { transform: scale(1.03); color: var(--text); }
        .pet-tab.dog:hover { background: var(--primary-light); color: var(--primary-dark); }
        .pet-tab.cat:hover { background: #fff3e0; color: #e65100; }
        .pet-tab i { font-size: 1.4rem; }

        /* CATEGORY CARDS */
        .cat-card {
            background: var(--surface-white); border-radius: 1.5rem;
            padding: 20px 16px; text-align: center;
            transition: all 0.3s; height: 100%; cursor: pointer;
            overflow: hidden;
        }
        .cat-card:hover { transform: translateY(-4px); box-shadow: 0 12px 30px rgba(0,191,165,0.08); }
        .cat-card .cat-img {
            width: 80px; height: 80px; border-radius: 1.2rem;
            object-fit: cover; margin: 0 auto 12px;
            display: block; background: var(--surface-low);
        }
        .cat-card .icon { font-size: 2.5rem; margin-bottom: 12px; }
        .cat-card h6 { font-weight: 700; font-size: 0.82rem; color: var(--text); margin-bottom: 2px; }
        .cat-card p { font-size: 0.72rem; color: var(--text-muted); margin: 0; }
        .cat-card a { text-decoration: none; color: inherit; display: block; }

        /* PRODUCT CARD */
        .product-card {
            background: var(--surface-white); border-radius: 1.5rem;
            overflow: hidden; transition: all 0.3s; height: 100%;
        }
        .product-card:hover { transform: translateY(-4px); box-shadow: 0 16px 40px rgba(0,0,0,0.06); }
        .product-card .img-wrap {
            height: 200px; display: flex; align-items: center; justify-content: center;
            background: var(--surface-low); padding: 16px; position: relative;
        }
        .product-card .img-wrap img { max-height: 170px; max-width: 100%; object-fit: contain; }
        .product-card .badge-sale {
            position: absolute; top: 12px; left: 12px;
            background: var(--accent); color: #fff;
            padding: 4px 12px; border-radius: 9999px;
            font-size: 0.75rem; font-weight: 700;
        }
        .product-card .info { padding: 1rem; }
        .product-card .cat-label {
            font-size: 0.7rem; font-weight: 700; text-transform: uppercase;
            color: var(--primary-dark); letter-spacing: 0.5px;
        }
        .product-card .name {
            font-size: 0.88rem; font-weight: 600; color: var(--text);
            margin: 6px 0 10px; height: 40px; overflow: hidden;
            display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical;
        }
        .product-card .name a { color: var(--text); text-decoration: none; }
        .product-card .name a:hover { color: var(--primary-dark); }
        .product-card .price { font-weight: 800; color: var(--text); font-size: 1rem; }
        .product-card .old-price { text-decoration: line-through; color: var(--text-muted); font-size: 0.82rem; margin-left: 6px; }
        .product-card .btn-cart {
            width: 38px; height: 38px; border-radius: 9999px;
            background: var(--primary); color: #fff; border: none;
            display: flex; align-items: center; justify-content: center;
            font-size: 1.1rem; transition: all 0.25s; flex-shrink: 0;
        }
        .product-card .btn-cart:hover { background: var(--primary-dark); transform: scale(1.08); }

        /* PROMO */
        .promo-banner {
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            border-radius: 1.5rem; padding: 36px 40px; color: #fff;
            position: relative; overflow: hidden;
        }
        .promo-banner h3 { font-weight: 800; font-size: 1.4rem; }
        .promo-banner p { opacity: 0.9; }
        .btn-promo {
            background: #fff; color: var(--primary-dark);
            padding: 12px 28px; border-radius: 9999px;
            font-weight: 700; border: none; transition: all 0.25s;
            text-decoration: none; display: inline-flex; align-items: center; gap: 6px;
            position: relative; z-index: 2;
        }
        .btn-promo:hover { transform: scale(1.03); box-shadow: 0 8px 20px rgba(0,0,0,0.1); color: var(--primary-dark); }

        /* FEATURED */
        .featured-large {
            background: var(--primary-light); border-radius: 1.5rem;
            padding: 2rem; height: 100%;
            display: flex; flex-direction: column; justify-content: space-between;
        }
        .featured-large h4 { font-weight: 800; color: var(--text); }
        .featured-large .f-price { font-size: 1.3rem; font-weight: 800; color: var(--primary-dark); }
        .featured-large img { max-height: 170px; object-fit: contain; }
        .btn-view {
            background: var(--primary); color: #fff;
            padding: 10px 24px; border-radius: 9999px;
            font-weight: 600; font-size: 0.85rem; border: none;
            text-decoration: none; display: inline-flex; align-items: center; gap: 6px;
            transition: all 0.25s;
        }
        .btn-view:hover { background: var(--primary-dark); color: #fff; transform: scale(1.03); }

        /* SERVICES */
        .services-bg { background: var(--surface-low); }
        .service-card {
            background: var(--surface-white); border-radius: 1.5rem;
            padding: 2rem; height: 100%; transition: all 0.3s;
        }
        .service-card:hover { transform: translateY(-4px); box-shadow: 0 12px 30px rgba(0,0,0,0.05); }
        .service-card .s-icon {
            width: 52px; height: 52px; border-radius: 1rem;
            display: flex; align-items: center; justify-content: center;
            font-size: 1.4rem; margin-bottom: 16px;
        }
        .service-card h5 { font-weight: 700; font-size: 1rem; }
        .service-card p { font-size: 0.85rem; color: var(--text-secondary); line-height: 1.6; }
        .service-card a { color: var(--primary-dark); text-decoration: none; font-weight: 600; font-size: 0.85rem; }
        .service-card a:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <jsp:include page="/components/navbar.jsp" />

    <!-- HERO SECTION -->
    <section class="shop-hero">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-lg-7">
                    <h1>Siêu Thị <span class="highlight">Thú Cưng</span><br>Hàng Đầu Việt Nam</h1>
                    <p class="subtitle mt-3">Hơn 5.000 sản phẩm chính hãng cho thú cưng của bạn. Giao hàng nhanh, giá tốt nhất thị trường.</p>
                    <div class="d-flex gap-3 mt-4 flex-wrap">
                        <c:choose>
                            <c:when test="${not empty petTypes}">
                                <c:forEach var="pt" items="${petTypes}" varStatus="st">
                                    <c:choose>
                                        <c:when test="${st.index == 0}">
                                            <a href="${pageContext.request.contextPath}/shop?pet=${pt.code}" class="btn-primary-pill">
                                                <i class='bx ${pt.icon}'></i> Sản phẩm cho ${pt.name}
                                            </a>
                                        </c:when>
                                        <c:when test="${st.index == 1}">
                                            <a href="${pageContext.request.contextPath}/shop?pet=${pt.code}" class="btn-outline-pill">
                                                <i class='bx ${pt.icon}'></i> Sản phẩm cho ${pt.name}
                                            </a>
                                        </c:when>
                                    </c:choose>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/shop?pet=dog" class="btn-primary-pill">
                                    <i class='bx bxs-dog'></i> Sản phẩm cho Chó
                                </a>
                                <a href="${pageContext.request.contextPath}/shop?pet=cat" class="btn-outline-pill">
                                    <i class='bx bxs-cat'></i> Sản phẩm cho Mèo
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="col-lg-5 d-none d-lg-flex justify-content-end gap-3 mt-4 mt-lg-0">
                    <div class="hero-stat text-center">
                        <div class="num">5K+</div>
                        <div class="label">Sản phẩm</div>
                    </div>
                    <div class="hero-stat text-center" style="background: rgba(255,255,255,0.12);">
                        <div class="num">50+</div>
                        <div class="label">Thương hiệu</div>
                    </div>
                    <div class="hero-stat text-center" style="background: rgba(255,255,255,0.12);">
                        <div class="num">10K+</div>
                        <div class="label">Khách hàng</div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- PET TABS - Hiển thị động từ database -->
    <section class="section-pad" style="padding-bottom: 0;">
        <div class="container">
            <div class="d-flex justify-content-center gap-4 flex-wrap">
                <c:choose>
                    <c:when test="${not empty petTypes}">
                        <c:forEach var="pt" items="${petTypes}" varStatus="st">
                            <a href="${pageContext.request.contextPath}/shop?pet=${pt.code}" 
                               class="pet-tab ${pt.code}">
                                <i class='bx ${pt.icon}'></i> Dành cho ${pt.name}
                            </a>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <%-- Fallback nếu chưa có dữ liệu pet_types --%>
                        <a href="${pageContext.request.contextPath}/shop?pet=dog" class="pet-tab dog">
                            <i class='bx bxs-dog'></i> Dành cho Chó
                        </a>
                        <a href="${pageContext.request.contextPath}/shop?pet=cat" class="pet-tab cat">
                            <i class='bx bxs-cat'></i> Dành cho Mèo
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>

    <!-- DANH MỤC PHỔ BIẾN -->
    <section class="section-pad">
        <div class="container">
            <div class="section-header d-flex justify-content-between align-items-center">
                <h2><i class='bx bx-category' style="color: var(--primary);"></i> Danh Mục Được Mua Nhiều</h2>
                <a href="${pageContext.request.contextPath}/shop?pet=dog" class="view-all">Xem tất cả <i class='bx bx-right-arrow-alt'></i></a>
            </div>
            <div class="row g-3">
                <c:forEach var="cat" items="${categories}" varStatus="st">
                        <div class="col-6 col-md-4 col-lg-3">
                            <div class="cat-card">
                                <a href="${pageContext.request.contextPath}/shop?category=${cat}">
                                    <c:choose>
                                        <c:when test="${cat.contains('Thức Ăn Cho Chó')}">
                                            <img class="cat-img" src="https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/t/h/thuc-an-cho-cho-truong-thanh-pedigree-vi-thit-bo-1.5kg.jpg" alt="${cat}">
                                        </c:when>
                                        <c:when test="${cat.contains('Sữa Cho Chó')}">
                                            <img class="cat-img" src="https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/s/u/sua-bot-bio-milk-cho-cho-con.jpg" alt="${cat}">
                                        </c:when>
                                        <c:when test="${cat.contains('Sức Khoẻ Cho Chó')}">
                                            <img class="cat-img" src="https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/f/r/frontline-plus-cho-cho.jpg" alt="${cat}">
                                        </c:when>
                                        <c:when test="${cat.contains('Ăn Uống Cho Chó')}">
                                            <img class="cat-img" src="https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/b/a/bat-an-inox-chong-lat.jpg" alt="${cat}">
                                        </c:when>
                                        <c:when test="${cat.contains('Huấn Luyện Cho Chó')}">
                                            <img class="cat-img" src="https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/b/o/bong-cao-su-kong-classic.jpg" alt="${cat}">
                                        </c:when>
                                        <c:when test="${cat.contains('Vệ Sinh Cho Chó')}">
                                            <img class="cat-img" src="https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/s/u/sua-tam-sos-cho-cho-long-trang.jpg" alt="${cat}">
                                        </c:when>
                                        <c:when test="${cat.contains('Thức Ăn Cho Mèo')}">
                                            <img class="cat-img" src="https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/w/h/whiskas-adult-ocean-fish.jpg" alt="${cat}">
                                        </c:when>
                                        <c:when test="${cat.contains('Sức Khoẻ Cho Mèo')}">
                                            <img class="cat-img" src="https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/l/y/lysine-cho-meo-vegebrand.jpg" alt="${cat}">
                                        </c:when>
                                        <c:when test="${cat.contains('Ăn Uống Cho Mèo')}">
                                            <img class="cat-img" src="https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/b/a/bat-an-doi-cho-meo-co-gia-do.jpg" alt="${cat}">
                                        </c:when>
                                        <c:when test="${cat.contains('Huấn Luyện Cho Mèo')}">
                                            <img class="cat-img" src="https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/c/a/can-cau-long-vu-cho-meo.jpg" alt="${cat}">
                                        </c:when>
                                        <c:when test="${cat.contains('Vệ Sinh Cho Mèo') && !cat.contains('Cát')}">
                                            <img class="cat-img" src="https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/s/u/sua-tam-sos-cho-meo-long-dai.jpg" alt="${cat}">
                                        </c:when>
                                        <c:when test="${cat.contains('Cát Vệ Sinh')}">
                                            <img class="cat-img" src="https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/c/a/cat-dau-nanh-cature-6l.jpg" alt="${cat}">
                                        </c:when>
                                        <c:when test="${cat.contains('Dụng Cụ Vệ Sinh Cho Mèo')}">
                                            <img class="cat-img" src="https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/n/h/nha-ve-sinh-cho-meo-co-nap.jpg" alt="${cat}">
                                        </c:when>
                                        <c:otherwise>
                                            <img class="cat-img" src="https://placehold.co/80x80/e0f7f3/00bfa5?text=🐾" alt="${cat}">
                                        </c:otherwise>
                                    </c:choose>
                                    <h6>${cat}</h6>
                                </a>
                            </div>
                        </div>
                </c:forEach>
            </div>
        </div>
    </section>

    <!-- PROMO BANNER -->
    <section style="padding: 0 0 40px;">
        <div class="container">
            <div class="promo-banner">
                <div class="row align-items-center">
                    <div class="col-lg-8">
                        <h3>🎉 Giảm giá lên đến 50% cho thành viên mới</h3>
                        <p class="mb-3">Đăng ký ngay để nhận ưu đãi độc quyền và tích điểm đổi quà.</p>
                        <a href="${pageContext.request.contextPath}/register" class="btn-promo">
                            Đăng ký ngay <i class='bx bx-right-arrow-alt'></i>
                        </a>
                    </div>
                    <div class="col-lg-4 text-end d-none d-lg-block">
                        <span style="font-size: 5rem; opacity: 0.15;">🐾</span>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- SẢN PHẨM NỔI BẬT -->
    <section class="section-pad" style="background: var(--surface-low); padding-top: 50px;">
        <div class="container">
            <div class="section-header d-flex justify-content-between align-items-center">
                <h2><i class='bx bx-star' style="color: var(--accent);"></i> Sản Phẩm Nổi Bật</h2>
                <a href="${pageContext.request.contextPath}/shop?discountOnly=true" class="view-all">Xem tất cả <i class='bx bx-right-arrow-alt'></i></a>
            </div>
            <div class="row g-3">
                <c:choose>
                    <c:when test="${not empty products}">
                        <c:forEach var="p" items="${products}" varStatus="st">
                            <c:if test="${st.index < 8}">
                                <div class="col-6 col-md-4 col-lg-3">
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
                            </c:if>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="col-12 text-center py-5">
                            <i class='bx bx-package' style="font-size: 4rem; color: var(--text-muted);"></i>
                            <p class="mt-3" style="color: var(--text-secondary);">Chưa có sản phẩm nào.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>

    <!-- SẢN PHẨM GIẢM GIÁ -->
    <c:if test="${not empty discountProducts}">
    <section class="section-pad">
        <div class="container">
            <div class="section-header d-flex justify-content-between align-items-center">
                <h2><i class='bx bxs-discount' style="color: var(--accent);"></i> Đang Giảm Giá</h2>
                <a href="${pageContext.request.contextPath}/shop?discountOnly=true" class="view-all">Xem tất cả <i class='bx bx-right-arrow-alt'></i></a>
            </div>
            <div class="row g-3">
                <c:forEach var="p" items="${discountProducts}" varStatus="st">
                    <c:if test="${st.index < 4}">
                        <div class="col-6 col-md-4 col-lg-3">
                            <div class="product-card">
                                <div class="img-wrap">
                                    <span class="badge-sale">-${p.discount}%</span>
                                    <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                        <c:choose>
                                            <c:when test="${p.image.startsWith('http')}">
                                                <img src="${p.image}" alt="${p.name}"
                                                     onerror="this.src='https://placehold.co/200x200/e0f7f3/00bfa5?text=Sale'">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${pageContext.request.contextPath}/${p.image}" alt="${p.name}"
                                                     onerror="this.src='https://placehold.co/200x200/e0f7f3/00bfa5?text=Sale'">
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
                                            <span class="old-price">${p.formattedOldPrice}</span>
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
                    </c:if>
                </c:forEach>
            </div>
        </div>
    </section>
    </c:if>

    <!-- DỊCH VỤ -->
    <section class="section-pad services-bg">
        <div class="container">
            <div class="section-header text-center mb-4">
                <h2>Dịch Vụ Của Chúng Tôi</h2>
                <p style="color: var(--text-secondary); max-width: 500px; margin: 8px auto 0;">Không chỉ mua sắm, PetShop còn cung cấp các dịch vụ chăm sóc toàn diện cho thú cưng.</p>
            </div>
            <div class="row g-4">
                <div class="col-md-6 col-lg-3">
                    <div class="service-card">
                        <div class="s-icon" style="background: var(--primary-light); color: var(--primary-dark);">
                            <i class='bx bx-package'></i>
                        </div>
                        <h5>Giao Hàng Nhanh</h5>
                        <p>Miễn phí giao hàng cho đơn từ 300K. Giao trong 2h nội thành.</p>
                    </div>
                </div>
                <div class="col-md-6 col-lg-3">
                    <div class="service-card">
                        <div class="s-icon" style="background: #fff3e0; color: #e65100;">
                            <i class='bx bx-shield-quarter'></i>
                        </div>
                        <h5>Hàng Chính Hãng</h5>
                        <p>100% sản phẩm nhập khẩu chính hãng, có tem kiểm định.</p>
                    </div>
                </div>
                <div class="col-md-6 col-lg-3">
                    <div class="service-card">
                        <div class="s-icon" style="background: #e8f5e9; color: #2e7d32;">
                            <i class='bx bx-refresh'></i>
                        </div>
                        <h5>Đổi Trả Dễ Dàng</h5>
                        <p>Đổi trả miễn phí trong 7 ngày nếu sản phẩm lỗi.</p>
                    </div>
                </div>
                <div class="col-md-6 col-lg-3">
                    <div class="service-card">
                        <div class="s-icon" style="background: #e3f2fd; color: #1565c0;">
                            <i class='bx bx-support'></i>
                        </div>
                        <h5>Tư Vấn 24/7</h5>
                        <p>Đội ngũ bác sĩ thú y tư vấn miễn phí mọi lúc.</p>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <jsp:include page="/components/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
