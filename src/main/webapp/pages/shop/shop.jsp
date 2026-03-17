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
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    
    <style>
        * {
            font-family: 'Montserrat', sans-serif;
        }
        
        body {
            margin: 0;
            padding: 0;
            background: linear-gradient(135deg, #00bfa5 0%, #005f52 100%);
            min-height: 100vh;
            background-attachment: fixed;
        }
        
        /* Navbar override */
        .navbar {
            background-color: #fff !important;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .navbar .nav-link, .navbar .navbar-brand {
            color: #333 !important;
        }
        
        /* Shop Header */
        .shop-header {
            text-align: center;
            padding: 100px 0 30px;
            color: #fff;
        }
        .shop-header h1 {
            font-weight: 800;
            text-transform: uppercase;
            margin-bottom: 10px;
            font-size: 2.5rem;
        }
        .shop-header p {
            font-size: 1.1rem;
            opacity: 0.9;
        }
        
        /* Category Circles */
        .cat-circle {
            width: 100px;
            height: 100px;
            border-radius: 50%;
            background: #fff;
            margin: 0 auto 10px;
            overflow: hidden;
            padding: 5px;
            transition: transform 0.3s ease;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .cat-circle img {
            width: 90px;
            height: 90px;
            border-radius: 50%;
            object-fit: cover;
        }
        .cat-circle:hover {
            transform: scale(1.1);
            box-shadow: 0 0 15px rgba(0,255,204,0.6);
        }
        .cat-name {
            color: #fff;
            font-weight: 600;
            font-size: 0.95rem;
            margin-top: 8px;
        }
        
        /* Product Card */
        .product-card {
            background: #fff;
            border-radius: 15px;
            overflow: hidden;
            position: relative;
            transition: all 0.3s ease;
            height: 100%;
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        .product-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 25px rgba(0,0,0,0.3);
        }
        
        .badge-discount {
            position: absolute;
            top: 10px;
            left: 10px;
            background: #d63031;
            color: #fff;
            padding: 3px 8px;
            border-radius: 5px;
            font-size: 0.8rem;
            font-weight: 700;
            z-index: 10;
        }
        
        .product-img {
            width: 100%;
            height: 200px;
            object-fit: contain;
            padding: 20px;
            background: #fff;
            border-bottom: 1px solid #eee;
            image-orientation: from-image;
        }
        
        .product-img-wrapper {
            width: 100%;
            height: 200px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #fff;
            border-bottom: 1px solid #eee;
            overflow: hidden;
        }
        
        .product-img-wrapper img {
            max-width: 100%;
            max-height: 180px;
            object-fit: contain;
            image-orientation: from-image;
        }
        
        .product-info {
            padding: 15px;
            text-align: left;
        }
        
        .brand {
            font-size: 0.8rem;
            color: #008f7a;
            font-weight: 700;
            text-transform: uppercase;
        }
        
        .product-title {
            font-size: 1rem;
            font-weight: 600;
            color: #333;
            margin: 5px 0 10px;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            height: 48px;
        }
        .product-title a {
            color: #333;
            text-decoration: none;
        }
        
        .price-box {
            margin-bottom: 15px;
        }
        .new-price {
            color: #d63031;
            font-weight: 700;
            font-size: 1.1rem;
        }
        
        .btn-add-cart {
            width: 100%;
            border: none;
            background: #00bfa5;
            color: #fff;
            padding: 10px;
            border-radius: 8px;
            font-weight: 600;
            cursor: pointer;
            transition: 0.3s;
        }
        .btn-add-cart:hover {
            background: #008f7a;
        }
        
        /* Section titles */
        .section-title {
            color: #fff;
            font-weight: 700;
            font-size: 1.5rem;
        }
        
        /* Footer override */
        footer {
            background: #004d40 !important;
        }
    </style>
</head>
<body>

    <jsp:include page="/components/navbar.jsp" />

    <section class="shop-header">
        <div class="container">
            <h1>Siêu Thị Thú Cưng</h1>
            <p>Dinh dưỡng & Phụ kiện tốt nhất cho Boss của bạn</p>
        </div>
    </section>

<%--    <section class="container mt-4">--%>
<%--        <h3 class="section-title mb-4">Bộ Sưu Tập Cho Mèo Con</h3>--%>
<%--        --%>
<%--        <div class="row text-center g-4">--%>
<%--            <div class="col-4 col-md-2">--%>
<%--                <div class="cat-circle">--%>
<%--                    <img src="${pageContext.request.contextPath}/assets/images/shop_pic/cate_pate.jpg" alt="Pate">--%>
<%--                </div>--%>
<%--                <p class="cat-name">Pate Mèo Con</p>--%>
<%--            </div>--%>
<%--            <div class="col-4 col-md-2">--%>
<%--                <div class="cat-circle">--%>
<%--                    <img src="${pageContext.request.contextPath}/assets/images/shop_pic/cate_food.jpg" alt="Food">--%>
<%--                </div>--%>
<%--                <p class="cat-name">Thức Ăn Hạt</p>--%>
<%--            </div>--%>
<%--            <div class="col-4 col-md-2">--%>
<%--                <div class="cat-circle">--%>
<%--                    <img src="${pageContext.request.contextPath}/assets/images/shop_pic/cate_sand.jpg" alt="Sand">--%>
<%--                </div>--%>
<%--                <p class="cat-name">Cát Vệ Sinh</p>--%>
<%--            </div>--%>
<%--            <div class="col-4 col-md-2">--%>
<%--                <div class="cat-circle">--%>
<%--                    <img src="${pageContext.request.contextPath}/assets/images/shop_pic/cate_shampoo.jpg" alt="Shampoo">--%>
<%--                </div>--%>
<%--                <p class="cat-name">Sữa Tắm</p>--%>
<%--            </div>--%>
<%--            <div class="col-4 col-md-2">--%>
<%--                <div class="cat-circle">--%>
<%--                    <img src="${pageContext.request.contextPath}/assets/images/shop_pic/cate_toy.jpg" alt="Toy">--%>
<%--                </div>--%>
<%--                <p class="cat-name">Đồ Chơi</p>--%>
<%--            </div>--%>
<%--            <div class="col-4 col-md-2">--%>
<%--                <div class="cat-circle">--%>
<%--                    <img src="${pageContext.request.contextPath}/assets/images/shop_pic/cate_house.jpg" alt="House">--%>
<%--                </div>--%>
<%--                <p class="cat-name">Nhà Cho Mèo</p>--%>
<%--            </div>--%>
<%--        </div>--%>
<%--    </section>--%>

    <section class="container mt-5 mb-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="section-title">Được Boss Yêu Thích</h3>
            <a href="#" class="text-white text-decoration-none">Xem tất cả <i class='bx bx-right-arrow-alt'></i></a>
        </div>

        <div class="row g-4">
            <c:forEach items="${products}" var="p">
                <div class="col-6 col-md-3">
                    <div class="product-card">
                        <c:if test="${p.discount > 0}">
                            <div class="badge-discount">-${p.discount}%</div>
                        </c:if>

                        <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                            <div class="product-img-wrapper">
                                <img src="${pageContext.request.contextPath}/assets/images/shop_pic/${p.image}" 
                                     alt="${p.name}"
                                     onerror="this.src='https://placehold.co/300x300/e2e8f0/1e293b?text=PetShop'">
                            </div>
                        </a>
                        
                        <div class="product-info">
                            <span class="brand">PETSHOP SELECT</span>
                            <h5 class="product-title">
                                <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${p.name}</a>
                            </h5>
                            <div class="price-box">
                                <span class="new-price">${p.formattedPrice}</span>
                            </div>
                            <form action="${pageContext.request.contextPath}/add-to-cart" method="post">
                                <input type="hidden" name="id" value="${p.id}">
                                <input type="hidden" name="quantity" value="1">
                                <button type="submit" class="btn-add-cart"><i class='bx bxs-cart-add'></i> Thêm</button>
                            </form>
                        </div>
                    </div>
                </div>
            </c:forEach>
            
            <c:if test="${empty products}">
                <div class="col-12 text-center text-white">
                    <p>Đang cập nhật sản phẩm...</p>
                </div>
            </c:if>
        </div>
    </section>

    <jsp:include page="/components/footer.jsp" />
    <jsp:include page="/components/back-button.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
