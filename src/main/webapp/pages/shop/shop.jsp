<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
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

        /* Hero Banner */
        .shop-hero {
            background: linear-gradient(135deg, #f8f4f0 0%, #fef9f4 100%);
            padding: 60px 0 50px;
            position: relative;
            overflow: hidden;
        }
        .shop-hero h1 {
            font-size: 2.8rem;
            font-weight: 800;
            color: #1a1a1a;
            line-height: 1.2;
        }
        .shop-hero h1 span { color: #e8613c; }
        .shop-hero p { color: #666; font-size: 1.05rem; max-width: 500px; }
        .hero-stats {
            background: #e8613c;
            color: #fff;
            border-radius: 15px;
            padding: 20px 25px;
            display: inline-block;
        }
        .hero-stats .num { font-size: 1.8rem; font-weight: 800; }
        .hero-stats .label { font-size: 0.8rem; opacity: 0.9; }
        .hero-img { max-height: 350px; object-fit: contain; }
        .btn-hero {
            background: #1a1a1a;
            color: #fff;
            padding: 14px 30px;
            border-radius: 50px;
            font-weight: 600;
            border: none;
            transition: all 0.3s;
        }
        .btn-hero:hover { background: #333; color: #fff; transform: translateY(-2px); }
        .btn-hero-outline {
            background: transparent;
            color: #1a1a1a;
            padding: 14px 30px;
            border-radius: 50px;
            font-weight: 600;
            border: 2px solid #ddd;
            transition: all 0.3s;
        }
        .btn-hero-outline:hover { border-color: #e8613c; color: #e8613c; }

        /* Section Title */
        .section-header { margin-bottom: 30px; }
        .section-header h2 { font-weight: 700; font-size: 1.6rem; color: #1a1a1a; }
        .section-header a { color: #e8613c; text-decoration: none; font-weight: 600; font-size: 0.95rem; }
        .section-header a:hover { text-decoration: underline; }

        /* Product Card */
        .product-card {
            background: #fff;
            border-radius: 16px;
            overflow: hidden;
            transition: all 0.3s;
            height: 100%;
            border: 1px solid #f0f0f0;
        }
        .product-card:hover { transform: translateY(-4px); box-shadow: 0 12px 30px rgba(0,0,0,0.08); }
        .product-card .img-wrap {
            height: 200px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #f9f9f9;
            padding: 15px;
            position: relative;
        }
        .product-card .img-wrap img { max-height: 170px; max-width: 100%; object-fit: contain; }
        .product-card .badge-sale {
            position: absolute;
            top: 10px;
            left: 10px;
            background: #e8613c;
            color: #fff;
            padding: 4px 10px;
            border-radius: 8px;
            font-size: 0.75rem;
            font-weight: 700;
        }
        .product-card .info { padding: 16px; }
        .product-card .cat-label { font-size: 0.7rem; font-weight: 700; text-transform: uppercase; color: #e8613c; letter-spacing: 0.5px; }
        .product-card .name {
            font-size: 0.9rem;
            font-weight: 600;
            color: #1a1a1a;
            margin: 6px 0 10px;
            height: 40px;
            overflow: hidden;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
        }
        .product-card .name a { color: #1a1a1a; text-decoration: none; }
        .product-card .name a:hover { color: #e8613c; }
        .product-card .price { font-weight: 700; color: #1a1a1a; font-size: 1.05rem; }
        .product-card .old-price { text-decoration: line-through; color: #aaa; font-size: 0.85rem; margin-left: 6px; }
        .product-card .btn-cart {
            width: 38px;
            height: 38px;
            border-radius: 50%;
            background: #e8613c;
            color: #fff;
            border: none;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.1rem;
            transition: all 0.2s;
            flex-shrink: 0;
        }
        .product-card .btn-cart:hover { background: #d4532e; transform: scale(1.1); }

        /* Category Card */
        .cat-card {
            background: #fff;
            border-radius: 16px;
            padding: 25px 20px;
            text-align: center;
            transition: all 0.3s;
            border: 1px solid #f0f0f0;
            height: 100%;
        }
        .cat-card:hover { transform: translateY(-4px); box-shadow: 0 12px 30px rgba(0,0,0,0.08); border-color: #e8613c; }
        .cat-card .icon { font-size: 2.5rem; margin-bottom: 12px; }
        .cat-card h6 { font-weight: 700; font-size: 0.85rem; color: #1a1a1a; margin-bottom: 4px; }
        .cat-card p { font-size: 0.75rem; color: #999; margin: 0; }
        .cat-card a { text-decoration: none; color: inherit; display: block; }

        /* Pet Section Tabs */
        .pet-tab {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 12px 28px;
            border-radius: 50px;
            font-weight: 700;
            font-size: 1rem;
            text-decoration: none;
            transition: all 0.3s;
            border: 2px solid #eee;
            color: #666;
        }
        .pet-tab:hover { border-color: #e8613c; color: #e8613c; }
        .pet-tab.active-dog { background: #fff3e0; border-color: #e8613c; color: #e8613c; }
        .pet-tab.active-cat { background: #e8f5e9; border-color: #4caf50; color: #4caf50; }
        .pet-tab i { font-size: 1.3rem; }

        /* Promo Banner */
        .promo-banner {
            background: linear-gradient(135deg, #e8613c, #ff8a65);
            border-radius: 20px;
            padding: 35px 40px;
            color: #fff;
            position: relative;
            overflow: hidden;
        }
        .promo-banner::after {
            content: '🐾';
            position: absolute;
            right: 30px;
            top: -10px;
            font-size: 100px;
            opacity: 0.15;
        }
        .promo-banner h3 { font-weight: 800; font-size: 1.5rem; }
        .promo-banner p { opacity: 0.9; margin-bottom: 0; }
        .btn-promo {
            background: #fff;
            color: #e8613c;
            padding: 10px 25px;
            border-radius: 50px;
            font-weight: 700;
            border: none;
            transition: all 0.3s;
        }
        .btn-promo:hover { background: #1a1a1a; color: #fff; }

        /* Featured large card */
        .featured-large {
            background: #f0ebe4;
            border-radius: 20px;
            padding: 30px;
            height: 100%;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }
        .featured-large h4 { font-weight: 700; color: #1a1a1a; }
        .featured-large .price { font-size: 1.3rem; font-weight: 800; color: #e8613c; }
        .featured-large img { max-height: 180px; object-fit: contain; }
    </style>
</head>
<body>
    <jsp:include page="/components/navbar.jsp" />

    <!-- HERO BANNER -->
    <section class="shop-hero">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-lg-6 mb-4 mb-lg-0">
                    <p class="text-uppercase fw-bold small" style="color: #e8613c; letter-spacing: 2px;">SIÊU THỊ THÚ CƯNG</p>
                    <h1>Nơi Lan Toả<br><span>Hạnh Phúc</span><br>Cho Thú Cưng.</h1>
                    <p class="mt-3 mb-4">Hơn hàng ngàn sản phẩm chất lượng cao, chúng tôi cung cấp thức ăn dinh dưỡng và phụ kiện cao cấp nhất cho thú cưng của bạn.</p>
                    <div class="d-flex gap-3 flex-wrap">
                        <a href="${pageContext.request.contextPath}/shop?pet=dog" class="btn-hero">
                            Mua sắm ngay <i class='bx bx-right-arrow-alt'></i>
                        </a>
                        <a href="#categories" class="btn-hero-outline">
                            <i class='bx bx-category'></i> Xem danh mục
                        </a>
                    </div>
                </div>
                <div class="col-lg-6 text-center position-relative">
                    <img src="https://placehold.co/500x350/f8f4f0/e8613c?text=🐕+🐈+PetShop" 
                         alt="PetShop Banner" class="hero-img">
                    <div class="hero-stats position-absolute" style="bottom: 20px; right: 10%;">
                        <div class="num">${totalProducts}+</div>
                        <div class="label">sản phẩm đa dạng</div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div class="container mt-5">
        <!-- PET TYPE TABS -->
        <div class="text-center mb-5" id="categories">
            <h2 class="fw-bold mb-4">Bạn đang tìm sản phẩm cho?</h2>
            <div class="d-flex justify-content-center gap-3 flex-wrap">
                <a href="${pageContext.request.contextPath}/shop?pet=dog" class="pet-tab active-dog">
                    <i class='bx bxs-dog'></i> Chó
                </a>
                <a href="${pageContext.request.contextPath}/shop?pet=cat" class="pet-tab active-cat">
                    <i class='bx bxs-cat'></i> Mèo
                </a>
            </div>
        </div>

        <!-- DANH MỤC ĐƯỢC MUA NHIỀU -->
        <div class="section-header d-flex justify-content-between align-items-center">
            <h2><i class='bx bx-category' style="color: #e8613c;"></i> Danh Mục Được Mua Nhiều</h2>
        </div>
        <div class="row g-3 mb-5">
            <div class="col-6 col-md-4 col-lg-2">
                <div class="cat-card">
                    <a href="${pageContext.request.contextPath}/shop?category=Thức Ăn Cho Chó">
                        <div class="icon">🦴</div>
                        <h6>Thức Ăn Cho Chó</h6>
                        <p>Dinh dưỡng</p>
                    </a>
                </div>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <div class="cat-card">
                    <a href="${pageContext.request.contextPath}/shop?category=Thức Ăn Cho Mèo">
                        <div class="icon">🐟</div>
                        <h6>Thức Ăn Cho Mèo</h6>
                        <p>Dinh dưỡng</p>
                    </a>
                </div>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <div class="cat-card">
                    <a href="${pageContext.request.contextPath}/shop?category=Đồ Chơi - Huấn Luyện Cho Chó">
                        <div class="icon">🎾</div>
                        <h6>Đồ Chơi Cho Chó</h6>
                        <p>Giải trí</p>
                    </a>
                </div>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <div class="cat-card">
                    <a href="${pageContext.request.contextPath}/shop?category=Cát Vệ Sinh Cho Mèo">
                        <div class="icon">🧹</div>
                        <h6>Cát Vệ Sinh Mèo</h6>
                        <p>Vệ sinh</p>
                    </a>
                </div>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <div class="cat-card">
                    <a href="${pageContext.request.contextPath}/shop?category=Chăm Sóc Sức Khoẻ Cho Chó">
                        <div class="icon">💊</div>
                        <h6>Sức Khoẻ Chó</h6>
                        <p>Chăm sóc</p>
                    </a>
                </div>
            </div>
            <div class="col-6 col-md-4 col-lg-2">
                <div class="cat-card">
                    <a href="${pageContext.request.contextPath}/shop?category=Dụng Cụ Ăn Uống Cho Mèo">
                        <div class="icon">🍽️</div>
                        <h6>Dụng Cụ Cho Mèo</h6>
                        <p>Phụ kiện</p>
                    </a>
                </div>
            </div>
        </div>

        <!-- PROMO BANNER -->
        <div class="promo-banner mb-5">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <h3>🎉 Ưu Đãi Thành Viên - Giảm đến 22%</h3>
                    <p>Đăng ký tài khoản để nhận ngay ưu đãi cho tất cả sản phẩm thức ăn hạt cao cấp.</p>
                </div>
                <div class="col-md-4 text-md-end mt-3 mt-md-0">
                    <a href="${pageContext.request.contextPath}/shop?discountOnly=true" class="btn-promo">
                        Xem ưu đãi <i class='bx bx-right-arrow-alt'></i>
                    </a>
                </div>
            </div>
        </div>

        <!-- SẢN PHẨM NỔI BẬT -->
        <div class="section-header d-flex justify-content-between align-items-center">
            <h2><i class='bx bxs-star' style="color: #e8613c;"></i> Sản Phẩm Nổi Bật</h2>
            <a href="${pageContext.request.contextPath}/shop?pet=dog">Xem tất cả <i class='bx bx-right-arrow-alt'></i></a>
        </div>

        <div class="row g-3 mb-4">
            <!-- Featured Large Card -->
            <c:if test="${not empty products}">
                <div class="col-lg-4 col-md-6">
                    <div class="featured-large">
                        <div>
                            <span class="cat-label">${products[0].category}</span>
                            <h4 class="mt-2">${products[0].name}</h4>
                            <p class="text-muted small">${products[0].description}</p>
                            <div class="price mb-3">${products[0].formattedPrice}</div>
                        </div>
                        <div class="text-center">
                            <a href="${pageContext.request.contextPath}/product-detail?id=${products[0].id}">
                                <img src="${pageContext.request.contextPath}/assets/images/shop_pic/${products[0].image}" 
                                     alt="${products[0].name}" style="max-height: 160px; object-fit: contain;"
                                     onerror="this.src='https://placehold.co/300x200/f0ebe4/e8613c?text=PetShop'">
                            </a>
                        </div>
                    </div>
                </div>
            </c:if>

            <!-- Product Grid -->
            <div class="col-lg-8">
                <div class="row g-3">
                    <c:forEach items="${products}" var="p" begin="1" end="4">
                        <div class="col-6 col-md-6 col-lg-3">
                            <div class="product-card">
                                <c:if test="${p.discount > 0}">
                                    <div class="img-wrap">
                                        <span class="badge-sale">-${p.discount}%</span>
                                </c:if>
                                <c:if test="${p.discount <= 0}">
                                    <div class="img-wrap">
                                </c:if>
                                    <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                        <img src="${pageContext.request.contextPath}/assets/images/shop_pic/${p.image}" 
                                             alt="${p.name}"
                                             onerror="this.src='https://placehold.co/200x200/f9f9f9/999?text=PetShop'">
                                    </a>
                                </div>
                                <div class="info">
                                    <div class="cat-label">${p.category}</div>
                                    <div class="name"><a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${p.name}</a></div>
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <span class="price">${p.formattedPrice}</span>
                                            <c:if test="${p.discount > 0}">
                                                <span class="old-price">${p.formattedOldPrice}</span>
                                            </c:if>
                                        </div>
                                        <form action="${pageContext.request.contextPath}/add-to-cart" method="post">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <input type="hidden" name="quantity" value="1">
                                            <button type="submit" class="btn-cart"><i class='bx bx-cart-add'></i></button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>

        <!-- SẢN PHẨM ĐANG GIẢM GIÁ -->
        <c:if test="${not empty discountProducts}">
            <div class="section-header d-flex justify-content-between align-items-center mt-5">
                <h2><i class='bx bxs-discount' style="color: #e8613c;"></i> Đang Giảm Giá</h2>
                <a href="${pageContext.request.contextPath}/shop?discountOnly=true">Xem tất cả <i class='bx bx-right-arrow-alt'></i></a>
            </div>
            <div class="row g-3 mb-5">
                <c:forEach items="${discountProducts}" var="p" begin="0" end="3">
                    <div class="col-6 col-md-3">
                        <div class="product-card">
                            <div class="img-wrap">
                                <span class="badge-sale">-${p.discount}%</span>
                                <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                    <img src="${pageContext.request.contextPath}/assets/images/shop_pic/${p.image}" 
                                         alt="${p.name}"
                                         onerror="this.src='https://placehold.co/200x200/f9f9f9/999?text=PetShop'">
                                </a>
                            </div>
                            <div class="info">
                                <div class="cat-label">${p.category}</div>
                                <div class="name"><a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${p.name}</a></div>
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <span class="price">${p.formattedPrice}</span>
                                        <span class="old-price">${p.formattedOldPrice}</span>
                                    </div>
                                    <form action="${pageContext.request.contextPath}/add-to-cart" method="post">
                                        <input type="hidden" name="id" value="${p.id}">
                                        <input type="hidden" name="quantity" value="1">
                                        <button type="submit" class="btn-cart"><i class='bx bx-cart-add'></i></button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:if>
    </div>

    <jsp:include page="/components/footer.jsp" />
    <jsp:include page="/components/back-button.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
