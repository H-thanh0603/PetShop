<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>

<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>${fn:escapeXml(detail.name)} | PetShop</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link
    href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
    rel="stylesheet">
<link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css'
    rel='stylesheet'>
<link rel="stylesheet"
    href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">

<style>
.product-img-main {
    border: 1px solid #e0e0e0;
    border-radius: 10px;
    padding: 20px;
    background: #fff;
    transition: transform 0.3s;
}

.product-img-main:hover {
    transform: scale(1.02);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.thumb-img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border: 1px solid #ddd;
    border-radius: 5px;
    cursor: pointer;
    opacity: 0.6;
    transition: 0.3s;
}

.thumb-img:hover, .thumb-img.active {
    opacity: 1;
    border-color: #0d6efd;
}

.price-tag {
    font-size: 2rem;
    font-weight: 700;
    color: #dc3545;
}

.nav-tabs .nav-link {
    color: #555;
    font-weight: 600;
}

.nav-tabs .nav-link.active {
    color: #0d6efd;
    border-top: 3px solid #0d6efd;
}

.related-card {
    transition: 0.3s;
    border: none;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.related-card:hover {
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    transform: translateY(-5px);
}

.rating-css input {
    display: none;
}

.rating-css input+label {
    font-size: 24px;
    color: #ccc;
    cursor: pointer;
}

.rating-css input:checked+label ~ label {
    color: #ccc;
}

.star-icon {
    display: flex;
    flex-direction: row-reverse;
    justify-content: start;
    gap: 5px;
}
.star-icon input[type="radio"] {
    display: none;
}

.star-icon label {
    color: #ffc107;
}

.star-icon input:checked ~ label {
    color: #ffc107;
}

.star-icon label:hover, .star-icon label:hover ~ label {
    color: #ffdb58;
}

.rating-summary {
    background: #fff7ed;
    border: 1px solid #fed7aa;
    border-radius: 14px;
    padding: 16px 18px;
}

.rating-stars i {
    color: #f59e0b;
}

.wishlist-form {
    margin: 0;
}

.wishlist-btn {
    min-width: 56px;
}

.spec-badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    background: #eff6ff;
    color: #1d4ed8;
    border-radius: 999px;
    padding: 6px 12px;
    font-size: 0.85rem;
    font-weight: 600;
    margin-right: 10px;
    margin-bottom: 10px;
}

.review-empty {
    border: 1px dashed #d1d5db;
    border-radius: 16px;
    padding: 24px;
    text-align: center;
    color: #6b7280;
    background: #f9fafb;
}
.star-icon:has(label:hover) label {
    color: #ddd;
}

.star-icon label:hover,
.star-icon label:hover ~ label {
    color: #ffc107;
}
</style>
<link href="${pageContext.request.contextPath}/assets/css/storefront-polish.css" rel="stylesheet">
</head>
<body>

    <jsp:include page="/components/navbar.jsp" />
    <jsp:include page="/components/toast.jsp" />
    <div class="bg-light py-3 mb-4 product-breadcrumb-band">
        <div class="container">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a
                        href="${pageContext.request.contextPath}/home"
                        class="text-decoration-none">Trang chủ</a></li>
                    <li class="breadcrumb-item"><a
                        href="${pageContext.request.contextPath}/shop"
                        class="text-decoration-none">Cửa hàng</a></li>
                    <li class="breadcrumb-item active" aria-current="page">${fn:escapeXml(detail.name)}</li>
                </ol>
            </nav>
        </div>
    </div>

    <div class="container mb-5 product-detail-page">
        <c:set var="productRedirect"
            value="${pageContext.request.contextPath}/product-detail?id=${detail.id}" />

        <div class="row g-5 product-top-grid">
            <div class="col-lg-5">
                <div class="product-media-panel">
                <div class="product-img-main text-center mb-3">
                    <img id="mainImage"
                        src="${fn:startsWith(detail.image, 'http') ? fn:escapeXml(detail.image) : pageContext.request.contextPath += '/assets/images/shop_pic/' += fn:escapeXml(detail.image)}"
                        class="img-fluid" alt="${fn:escapeXml(detail.name)}" style="max-height: 400px;"
                        onerror="this.src='https://placehold.co/400x400/e2e8f0/1e293b?text=PetShop'">
                </div>

                <div class="d-flex justify-content-center gap-2">
                    <img
                        src="${fn:startsWith(detail.image, 'http') ? detail.image : pageContext.request.contextPath += '/assets/images/shop_pic/' += detail.image}"
                        class="thumb-img active" onclick="changeImage(this)"> <img
                        src="${fn:startsWith(detail.image, 'http') ? detail.image : pageContext.request.contextPath += '/assets/images/shop_pic/' += detail.image}"
                        class="thumb-img" onclick="changeImage(this)"> <img
                        src="${fn:startsWith(detail.image, 'http') ? detail.image : pageContext.request.contextPath += '/assets/images/shop_pic/' += detail.image}"
                        class="thumb-img" onclick="changeImage(this)">
                </div>
                </div>
            </div>

            <div class="col-lg-7">
                <div class="product-purchase-panel">
                <div class="d-flex flex-wrap gap-2 mb-2">
                    <span class="badge product-badge">Chính hãng</span>
                    <c:if test="${detail.discount > 0}">
                        <span class="badge product-badge product-badge-sale">Giảm ${detail.discount}%</span>
                    </c:if>
                    <c:if test="${detail.stock > 0 && detail.stock < 10}">
                        <span class="badge product-badge product-badge-warn">Sắp hết hàng</span>
                    </c:if>
                </div>
                <h2 class="fw-bold text-dark mb-2">${fn:escapeXml(detail.name)}</h2>

                <div class="d-flex flex-wrap align-items-center gap-3 mb-3 small">
                    <div class="rating-stars">
                        <i class="fas fa-star"></i>
                        <span class="fw-semibold text-dark ms-1">${detail.formattedAverageRating}/5</span>
                        <span class="text-muted ms-1">(${detail.reviewCount} đánh giá)</span>
                    </div>
                    <span class="text-muted border-start ps-3">Mã SP: <strong>SP00${detail.id}</strong></span>
                    <span class="text-muted border-start ps-3">Danh mục: <strong>${fn:escapeXml(detail.category)}</strong></span>
                </div>

                <div class="price-panel mb-4">
                    <div class="d-flex flex-wrap align-items-end gap-3">
                        <span class="price-tag"> <fmt:formatNumber
                                value="${detail.price}" type="currency" currencySymbol="₫" />
                        </span>
                        <c:if test="${detail.discount > 0}">
                            <div class="pb-2">
                                <div class="text-muted text-decoration-line-through">${fn:escapeXml(detail.formattedOldPrice)}</div>
                                <div class="text-success fw-semibold">Tiết kiệm ${fn:escapeXml(detail.formattedDiscountAmount)}</div>
                            </div>
                        </c:if>
                    </div>
                    <div class="mt-2">
                        <c:choose>
                            <c:when test="${detail.stock <= 0}">
                                <div class="text-danger fw-semibold">
                                    <i class='bx bxs-error-circle'></i> Hết hàng
                                </div>
                            </c:when>
                            <c:when test="${detail.stock < 10}">
                                <div class="text-warning fw-semibold">
                                    <i class='bx bxs-package'></i> Chỉ còn ${detail.stock} sản phẩm
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="text-success">
                                    <i class='bx bxs-check-circle'></i> Còn ${detail.stock} sản phẩm - Sẵn sàng giao ngay
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="rating-summary product-rating-summary mb-4">
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3">
                        <div>
                            <div class="text-uppercase text-muted small fw-semibold">Đánh giá khách hàng</div>
                            <div class="fs-4 fw-bold text-dark">${detail.formattedAverageRating}/5</div>
                            <div class="text-muted">Từ ${detail.reviewCount} nhận xét thực tế</div>
                        </div>
                        <div class="text-muted small">
                            Sản phẩm đang được khách hàng quan tâm nhờ giá tốt, nguồn gốc rõ ràng và phù hợp cho thú cưng.
                        </div>
                    </div>
                </div>

                <p class="text-muted mb-4">
                    <c:choose>
                        <c:when test="${not empty detail.description}">
                            ${fn:escapeXml(detail.description)}
                        </c:when>
                        <c:otherwise>
                            Sản phẩm chất lượng cao dành cho thú cưng, phù hợp cho nhu cầu chăm sóc hằng ngày và hỗ trợ sức khoẻ toàn diện.
                        </c:otherwise>
                    </c:choose>
                </p>

                <form id="addToCartForm"
                    action="${pageContext.request.contextPath}/add-to-cart"
                    method="post" class="product-buy-box">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="id" value="${detail.id}"> <input
                        type="hidden" name="actionType" id="actionType" value="add">

                    <div class="row align-items-end">
                        <div class="col-md-3 mb-3">
                            <label class="form-label fw-bold">Số lượng</label>
                            <div class="input-group">
                                <input type="number" id="qtyInput" name="quantity"
                                    class="form-control text-center" value="1"
                                    inputmode="numeric" autocomplete="off"
                                    aria-describedby="qtyError">
                            </div>
                            <div id="qtyError" class="text-danger small mt-2 d-none"
                                aria-live="polite"></div>
                        </div>

                        <div class="col-md-9 mb-3 d-flex gap-2 flex-wrap">
                            <button type="button" onclick="submitForm('add')"
                                class="btn btn-primary btn-lg flex-grow-1 product-action-btn shadow-sm">
                                <i class='bx bx-cart-add'></i> Thêm vào giỏ
                            </button>

                            <button type="button" onclick="submitForm('buy')"
                                class="btn btn-danger btn-lg flex-grow-1 product-action-btn shadow-sm">
                                <i class='bx bx-bolt-circle'></i> Mua ngay</button>
                        </div>
                    </div>
                    <div class="purchase-note">
                        Bạn có thể kiểm tra phí vận chuyển và mã giảm giá ở bước thanh toán.
                    </div>
                </form>

                <form action="${pageContext.request.contextPath}/toggle-wishlist" method="post"
                    class="wishlist-form mt-3 product-wishlist-row">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="productId" value="${detail.id}">
                    <input type="hidden" name="redirect" value="${productRedirect}">
                    <button type="submit"
                        class="btn ${detail.wishlisted ? 'btn-danger' : 'btn-outline-danger'} wishlist-btn"
                        title="${detail.wishlisted ? 'Xóa khỏi yêu thích' : 'Thêm vào yêu thích'}">
                        <i class='bx ${detail.wishlisted ? 'bxs-heart' : 'bx-heart'} fs-4'></i>
                        ${detail.wishlisted ? 'Đã yêu thích' : 'Thêm vào yêu thích'}
                    </button>
                </form>

                <div class="product-assurance small">
                    <div class="product-assurance-item">
                        <i class='bx bx-shield-quarter text-primary me-2'></i> Hàng chính hãng
                    </div>
                    <div class="product-assurance-item">
                        <i class='bx bx-refresh text-primary me-2'></i> Đổi trả linh hoạt nếu lỗi
                    </div>
                    <div class="product-assurance-item">
                        <i class='bx bxs-truck text-primary me-2'></i> Freeship đơn từ
                        500k
                    </div>
                    <div class="product-assurance-item">
                        <i class='bx bx-support text-primary me-2'></i> Tư vấn chọn sản phẩm
                        phù hợp
                    </div>
                </div>
                </div>
            </div>
        </div>

        <div class="row mt-5">
            <div class="col-12">

                <ul class="nav nav-tabs" id="myTab" role="tablist">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link active" id="desc-tab" data-bs-toggle="tab"
                            data-bs-target="#desc" type="button">Mô tả chi tiết</button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="specs-tab" data-bs-toggle="tab"
                            data-bs-target="#specs" type="button">Thông tin mua hàng</button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="reviews-tab" data-bs-toggle="tab"
                            data-bs-target="#reviews" type="button">Đánh giá (${detail.reviewCount})</button>
                    </li>
                </ul>

                <div
                    class="tab-content border border-top-0 p-4 bg-white rounded-bottom"
                    id="myTabContent">

                    <div class="tab-pane fade show active" id="desc" role="tabpanel">
                        <div class="product-description mb-3">
                            <c:out value="${detail.description}" escapeXml="false" />
                        </div>
                        <div class="mt-3">
                            <span class="spec-badge"><i class='bx bx-check-shield'></i>
                                Sản phẩm chính hãng</span> <span class="spec-badge"><i
                                class='bx bx-package'></i> Đóng gói cẩn thận</span> <span
                                class="spec-badge"><i class='bx bx-heart-circle'></i> Phù hợp
                                cho chăm sóc thú cưng hằng ngày</span>
                        </div>
                    </div>

                    <div class="tab-pane fade" id="specs" role="tabpanel">
                        <table class="table table-striped table-bordered"
                            style="max-width: 700px;">
                            <tbody>
                                <tr>
                                    <th width="35%">Danh mục</th>
                                    <td>${fn:escapeXml(detail.category)}</td>
                                </tr>
                                <tr>
                                    <th>Giá bán hiện tại</th>
                                    <td>${fn:escapeXml(detail.formattedPrice)}</td>
                                </tr>
                                <tr>
                                    <th>Khuyến mãi</th>
                                    <td><c:choose>
                                            <c:when test="${detail.discount > 0}">Giảm ${detail.discount}% - tiết kiệm ${detail.formattedDiscountAmount}</c:when>
                                            <c:otherwise>Không có khuyến mãi</c:otherwise>
                                        </c:choose></td>
                                </tr>
                                <tr>
                                    <th>Tồn kho</th>
                                    <td>${detail.stock} sản phẩm</td>
                                </tr>
                                <tr>
                                    <th>Đánh giá trung bình</th>
                                    <td>${detail.formattedAverageRating}/5 từ
                                        ${detail.reviewCount} đánh giá</td>
                                </tr>
                                <tr>
                                    <th>Vận chuyển</th>
                                    <td>Giao hàng toàn quốc, hỗ trợ phí ship theo khu vực
                                        khi checkout</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="tab-pane fade" id="reviews" role="tabpanel">
                        <div
                            class="mb-4 d-flex flex-wrap justify-content-between align-items-center gap-3">
                            <div>
                                <h5 class="mb-1">Khách hàng đánh giá (${listReviews.size()})</h5>
                                <p class="text-muted mb-0">
                                    Điểm trung bình hiện tại:
                                    <strong>${detail.formattedAverageRating}/5</strong>
                                </p>
                            </div>
                        </div>

                        <div class="review-list mb-5"
                            style="max-height: 500px; overflow-y: auto;">
                            <c:if test="${empty listReviews}">
                                <div class="review-empty">Chưa có đánh giá nào. Hãy là
                                    người đầu tiên nhận xét sản phẩm này.</div>
                            </c:if>
                            <c:forEach items="${listReviews}" var="r">
                                <div class="d-flex mb-4 border-bottom pb-3">
                                    <div class="flex-shrink-0">
                                        <img
                                            src="https://ui-avatars.com/api/?name=${fn:escapeXml(r.userName)}&background=random"
                                            class="rounded-circle" width="50">
                                    </div>
                                    <div class="flex-grow-1 ms-3">
                                        <div class="d-flex justify-content-between">
                                            <h6 class="mb-0 fw-bold">${fn:escapeXml(r.userName)}</h6>
                                            <small class="text-muted">${r.createdAt}</small>
                                        </div>
                                        <div class="text-warning small mb-1">
                                            <c:forEach begin="1" end="${r.rating}">
                                                <i class="fas fa-star"></i>
                                            </c:forEach>
                                            <c:forEach begin="1" end="${5 - r.rating}">
                                                <i class="far fa-star text-secondary"></i>
                                            </c:forEach>
                                        </div>
                                        <p class="mb-0 text-secondary">${fn:escapeXml(r.comment)}</p>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <div class="card bg-light border-0 p-4">
                            <h5 class="fw-bold mb-3">Viết đánh giá</h5>

                            <c:if test="${not empty sessionScope.user}">
                                <c:choose>
                                    <c:when test="${hasReviewed}">
                                        <div class="alert alert-info mb-0">Bạn đã đánh giá
                                            sản phẩm này rồi. Cảm ơn bạn đã chia sẻ trải nghiệm mua
                                            sắm tại PetShop.</div>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${pageContext.request.contextPath}/add-review"
                                            method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="productId" value="${detail.id}">

                                            <div class="mb-3">
                                                <label class="form-label fw-bold">Chọn số sao:</label>
                                                <div class="rating-css">
                                                    <div class="star-icon">
                                                        <input type="radio" name="rating" value="5" id="r5"
                                                            checked> <label for="r5" class="fas fa-star"></label>
                                                        <input type="radio" name="rating" value="4" id="r4">
                                                        <label for="r4" class="fas fa-star"></label> <input
                                                            type="radio" name="rating" value="3" id="r3"> <label
                                                            for="r3" class="fas fa-star"></label> <input type="radio"
                                                            name="rating" value="2" id="r2"> <label for="r2"
                                                            class="fas fa-star"></label> <input type="radio"
                                                            name="rating" value="1" id="r1"> <label for="r1"
                                                            class="fas fa-star"></label>
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="mb-3">
                                                <textarea name="comment" class="form-control" rows="3"
                                                    placeholder="Chia sẻ cảm nhận của bạn về sản phẩm..."
                                                    required></textarea>
                                            </div>

                                            <button type="submit" class="btn btn-primary">Gửi đánh
                                                giá</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>

                            <c:if test="${empty sessionScope.user}">
                                <div class="alert alert-warning mb-0">
                                    Vui lòng <a
                                        href="${pageContext.request.contextPath}/login?redirect=${productRedirect}">đăng
                                        nhập</a> để viết đánh giá.
                                </div>
                            </c:if>
                        </div>
                    </div>


                </div>
                <div class="mt-5">
                    <h3 class="fw-bold border-bottom pb-2 mb-4">Có thể bạn cũng
                        thích</h3>

                    <div class="row row-cols-1 row-cols-md-4 g-4">
                        <c:forEach items="${relatedProducts}" var="rp">
                            <div class="col">
                                <div class="card h-100 related-card">
                                    <a
                                        href="${pageContext.request.contextPath}/product-detail?id=${rp.id}">
                                        <img
                                        src="${fn:startsWith(rp.image, 'http') ? fn:escapeXml(rp.image) : pageContext.request.contextPath += '/assets/images/shop_pic/' += fn:escapeXml(rp.image)}"
                                        class="card-img-top" alt="${fn:escapeXml(rp.name)}"
                                        style="height: 200px; object-fit: cover;"
                                        onerror="this.src='https://placehold.co/200x200/e2e8f0/1e293b?text=PetShop'">
                                    </a>
                                    <div class="card-body text-center">
                                        <div class="small text-uppercase text-success fw-semibold mb-1">${fn:escapeXml(rp.category)}</div>
                                        <h6 class="card-title fw-bold text-truncate">
                                            <a
                                                href="${pageContext.request.contextPath}/product-detail?id=${rp.id}"
                                                class="text-decoration-none text-dark"> ${fn:escapeXml(rp.name)} </a>
                                        </h6>
                                        <div class="small text-warning mb-2">
                                            <i class="fas fa-star"></i> ${rp.formattedAverageRating}
                                            <span class="text-muted">(${rp.reviewCount})</span>
                                        </div>
                                        <p class="text-danger fw-bold mb-0">
                                            <fmt:formatNumber value="${rp.price}" type="currency"
                                                currencySymbol="₫" />
                                        </p>

                                        <a
                                            href="${pageContext.request.contextPath}/product-detail?id=${rp.id}"
                                            class="btn btn-sm btn-outline-primary mt-2"> Xem ngay </a>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>

                        <c:if test="${empty relatedProducts}">
                            <div class="col-12 text-center text-muted">
                                <p>Không có sản phẩm liên quan nào.</p>
                            </div>
                        </c:if>
                    </div>
                </div>

            </div>
        </div>
    </div>

    <jsp:include page="/components/footer.jsp" />
    <script
        src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <script>
    const availableStock = ${detail.stock};
    const quantityMessages = {
        negative: "Kh\u00f4ng \u0111\u01b0\u1ee3c nh\u1eadp s\u1ed1 \u00e2m.",
        decimal: "Kh\u00f4ng \u0111\u01b0\u1ee3c nh\u1eadp s\u1ed1 th\u1eadp ph\u00e2n.",
        zero: "S\u1ed1 l\u01b0\u1ee3ng ph\u1ea3i l\u1edbn h\u01a1n 0.",
        invalid: "S\u1ed1 l\u01b0\u1ee3ng kh\u00f4ng h\u1ee3p l\u1ec7.",
        exceed: "S\u1ed1 l\u01b0\u1ee3ng v\u01b0\u1ee3t qu\u00e1 t\u1ed3n kho hi\u1ec7n t\u1ea1i."
    };

    function getQtyInput() {
        return document.getElementById('qtyInput');
    }

    function getQtyError() {
        return document.getElementById('qtyError');
    }

    function getSubmitButtons() {
        return [
            document.querySelector("button[onclick=\"submitForm('add')\"]"),
            document.querySelector("button[onclick=\"submitForm('buy')\"]")
        ];
    }

    function setSubmitButtonsDisabled(disabled) {
        getSubmitButtons().forEach(function(button) {
            if (button) {
                button.disabled = disabled;
            }
        });
    }

    function getNormalizedQuantity() {
        var qtyInput = getQtyInput();
        if (!qtyInput) {
            return 0;
        }

        var rawValue = qtyInput.value.trim();
        if (!/^\d+$/.test(rawValue)) {
            return 0;
        }

        var quantity = Number(rawValue);
        return Number.isInteger(quantity) ? quantity : 0;
    }

    function getQuantityValidationMessage() {
        var qtyInput = getQtyInput();
        if (!qtyInput) {
            return "";
        }

        var rawValue = qtyInput.value.trim();
        if (rawValue === "") {
            return "";
        }

        if (rawValue.startsWith("-")) {
            return quantityMessages.negative;
        }

        if (rawValue.includes(".") || rawValue.includes(",")) {
            return quantityMessages.decimal;
        }

        if (!/^\d+$/.test(rawValue)) {
            return quantityMessages.invalid;
        }

        var quantity = Number(rawValue);
        if (!Number.isInteger(quantity)) {
            return quantityMessages.decimal;
        }

        if (quantity === 0) {
            return quantityMessages.zero;
        }

        if (quantity < 0) {
            return quantityMessages.negative;
        }

        if (availableStock > 0 && quantity > availableStock) {
            return quantityMessages.exceed;
        }

        return "";
    }

    function validateQuantityInput(forceMessage) {
        var qtyInput = getQtyInput();
        var qtyError = getQtyError();

        if (!qtyInput) {
            return false;
        }

        var message = getQuantityValidationMessage();
        if (!message && forceMessage && qtyInput.value.trim() === "") {
            message = quantityMessages.invalid;
        }

        var hasError = message !== "";
        qtyInput.classList.toggle("is-invalid", hasError);
        qtyInput.setCustomValidity(hasError ? message : "");

        if (qtyError) {
            qtyError.textContent = hasError ? message : "";
            qtyError.classList.toggle("d-none", !hasError);
        }

        setSubmitButtonsDisabled(availableStock <= 0 || hasError);
        return !hasError;
    }

    function syncStockUi() {
        var qtyInput = getQtyInput();

        if (qtyInput) {
            if (availableStock <= 0) {
                qtyInput.value = "1";
                qtyInput.disabled = true;
            } else {
                qtyInput.disabled = false;
                if (qtyInput.value.trim() === "") {
                    qtyInput.value = "1";
                }
                if (getNormalizedQuantity() > availableStock) {
                    qtyInput.value = String(availableStock);
                }
            }
        }

        validateQuantityInput(false);
    }

    function submitForm(type) {
        if (availableStock <= 0) {
            return;
        }

        if (!validateQuantityInput(true)) {
            var qtyInput = getQtyInput();
            if (qtyInput) {
                qtyInput.focus();
            }
            return;
        }

        var quantity = getNormalizedQuantity();
        if (quantity < 1) {
            return;
        }

        document.getElementById('qtyInput').value = String(Math.min(quantity, availableStock));
        document.getElementById('actionType').value = type;
        document.getElementById('addToCartForm').submit();
    }

    function changeImage(element) {
        var mainImg = document.getElementById('mainImage');
        mainImg.src = element.src;

        var thumbs = document.querySelectorAll('.thumb-img');
        thumbs.forEach(function(thumb) {
            thumb.classList.remove('active');
        });
        element.classList.add('active');
    }

    var qtyInput = getQtyInput();
    if (qtyInput) {
        qtyInput.addEventListener('input', function() {
            validateQuantityInput(false);
        });
    }

    var addToCartForm = document.getElementById('addToCartForm');
    if (addToCartForm) {
        addToCartForm.addEventListener('submit', function(event) {
            if (!validateQuantityInput(true)) {
                event.preventDefault();
            }
        });
    }

    syncStockUi();
    </script>
</body>
</html>
