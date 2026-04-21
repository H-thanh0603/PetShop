<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Yêu thích | PetShop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <style>
        .wishlist-card { border: none; border-radius: 18px; box-shadow: 0 6px 24px rgba(0,0,0,0.06); }
        .wishlist-image { width: 120px; height: 120px; object-fit: contain; background: #f8fafc; border-radius: 16px; padding: 12px; }
        .wishlist-empty { border: 2px dashed #cbd5e1; border-radius: 20px; padding: 48px 24px; text-align: center; background: #fff; }
    </style>
</head>
<body class="bg-light">
    <jsp:include page="/components/navbar.jsp" />
    <jsp:include page="/components/toast.jsp" />

    <div class="container py-5">
        <nav aria-label="breadcrumb" class="mb-3">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/home">Trang chủ</a></li>
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/shop">Cửa hàng</a></li>
                <li class="breadcrumb-item active" aria-current="page">Yêu thích</li>
            </ol>
        </nav>
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="fw-bold mb-1"><i class='bx bx-heart text-danger'></i> Danh sách yêu thích</h2>
                <p class="text-muted mb-0">Lưu lại các sản phẩm bạn muốn mua sau.</p>
            </div>
            <a href="${pageContext.request.contextPath}/shop" class="btn btn-outline-primary">
                <i class='bx bx-store'></i> Tiếp tục mua sắm
            </a>
        </div>

        <c:choose>
            <c:when test="${empty wishlistProducts}">
                <div class="wishlist-empty">
                    <i class='bx bx-heart-circle' style="font-size: 64px; color: #cbd5e1;"></i>
                    <h4 class="mt-3">Chưa có sản phẩm yêu thích</h4>
                    <p class="text-muted">Hãy thêm các sản phẩm bạn quan tâm để quay lại mua nhanh hơn.</p>
                    <a href="${pageContext.request.contextPath}/shop" class="btn btn-primary mt-2">Khám phá sản phẩm</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row g-4">
                    <c:forEach items="${wishlistProducts}" var="p">
                        <div class="col-12">
                            <div class="card wishlist-card">
                                <div class="card-body p-4">
                                    <div class="row align-items-center g-3">
                                        <div class="col-md-auto text-center">
                                            <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                                <img src="${pageContext.request.contextPath}/assets/images/shop_pic/${p.image}"
                                                     alt="${p.name}" class="wishlist-image"
                                                     onerror="this.src='https://placehold.co/140x140/e2e8f0/1e293b?text=PetShop'">
                                            </a>
                                        </div>
                                        <div class="col">
                                            <div class="d-flex flex-column flex-md-row justify-content-between gap-3">
                                                <div>
                                                    <div class="text-uppercase small text-success fw-semibold">${p.category}</div>
                                                    <h5 class="fw-bold mb-2">
                                                        <a class="text-dark text-decoration-none" href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                                                            ${p.name}
                                                        </a>
                                                    </h5>
                                                    <div class="text-warning small mb-2">
                                                        <i class='bx bxs-star'></i> ${p.formattedAverageRating} (${p.reviewCount} đánh giá)
                                                    </div>
                                                    <div class="fw-bold text-danger fs-5">${p.formattedPrice}</div>
                                                    <c:if test="${p.discount > 0}">
                                                        <div class="small text-muted">Tiết kiệm ${p.formattedDiscountAmount}</div>
                                                    </c:if>
                                                    <div class="small mt-2 ${p.stock > 0 ? 'text-success' : 'text-danger'}">
                                                        <i class='bx ${p.stock > 0 ? "bx-check-circle" : "bx-x-circle"}'></i>
                                                        ${p.stock > 0 ? 'Còn hàng' : 'Hết hàng'}
                                                    </div>
                                                </div>
                                                <div class="d-flex flex-column gap-2 justify-content-center">
                                                    <form action="${pageContext.request.contextPath}/add-to-cart" method="post">
                                                        <input type="hidden" name="id" value="${p.id}">
                                                        <input type="hidden" name="quantity" value="1">
                                                        <button type="submit" class="btn btn-primary" ${p.stock <= 0 ? 'disabled' : ''}>
                                                            <i class='bx bx-cart-add'></i> Thêm vào giỏ
                                                        </button>
                                                    </form>
                                                    <form action="${pageContext.request.contextPath}/toggle-wishlist" method="post">
                                                        <input type="hidden" name="productId" value="${p.id}">
                                                        <input type="hidden" name="redirect" value="${pageContext.request.contextPath}/wishlist">
                                                        <button type="submit" class="btn btn-outline-danger">
                                                            <i class='bx bx-trash'></i> Bỏ yêu thích
                                                        </button>
                                                    </form>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <jsp:include page="/components/footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
