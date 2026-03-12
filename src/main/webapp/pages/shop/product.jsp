<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>${detail.name}|AnimalDoctorsStore</title>
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
	rel="stylesheet">
<link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css'
	rel='stylesheet'>
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">

<style>
/* Custom CSS */
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

/* Tabs */
.nav-tabs .nav-link {
	color: #555;
	font-weight: 600;
}

.nav-tabs .nav-link.active {
	color: #0d6efd;
	border-top: 3px solid #0d6efd;
}

/* Related Products */
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

.star-icon label {
	color: #ffc107;
}

.star-icon input:checked ~ label {
	color: #ffc107;
}

.star-icon label:hover, .star-icon label:hover ~ label {
	color: #ffdb58;
}
</style>
</head>
<body>

	<jsp:include page="/components/navbar.jsp" />

	<div class="bg-light py-3 mb-4">
		<div class="container">
			<nav aria-label="breadcrumb">
				<ol class="breadcrumb mb-0">
					<li class="breadcrumb-item"><a
						href="${pageContext.request.contextPath}/home"
						class="text-decoration-none">Trang chủ</a></li>
					<li class="breadcrumb-item"><a
						href="${pageContext.request.contextPath}/shop"
						class="text-decoration-none">Cửa hàng</a></li>
					<li class="breadcrumb-item active" aria-current="page">${detail.name}</li>
				</ol>
			</nav>
		</div>
	</div>

	<div class="container mb-5">

		<div class="row g-5">
			<div class="col-lg-5">
				<div class="product-img-main text-center mb-3">
					<img id="mainImage"
						src="${pageContext.request.contextPath}/assets/images/shop_pic/${detail.image}"
						class="img-fluid" alt="${detail.name}" style="max-height: 400px;"
						onerror="this.src='https://placehold.co/400x400/e2e8f0/1e293b?text=PetShop'">
				</div>

				<div class="d-flex justify-content-center gap-2">
					<img
						src="${pageContext.request.contextPath}/assets/images/shop_pic/${detail.image}"
						class="thumb-img active" onclick="changeImage(this)"> <img
						src="${pageContext.request.contextPath}/assets/images/shop_pic/${detail.image}"
						class="thumb-img" onclick="changeImage(this)"> <img
						src="${pageContext.request.contextPath}/assets/images/shop_pic/${detail.image}"
						class="thumb-img" onclick="changeImage(this)">
				</div>
			</div>

			<div class="col-lg-7">
				<span class="badge bg-info text-dark mb-2">Chính hãng</span>
				<h2 class="fw-bold text-dark mb-2">${detail.name}</h2>

				<div class="d-flex align-items-center mb-3 text-warning small">
					<i class="fas fa-star"></i> <i class="fas fa-star"></i> <i
						class="fas fa-star"></i> <i class="fas fa-star"></i> <i
						class="fas fa-star-half-alt"></i> <span class="text-muted ms-2">(Xem
						24 đánh giá)</span> <span class="text-muted border-start ms-3 ps-3">Mã
						SP: <strong>SP00${detail.id}</strong>
					</span>
				</div>

				<div class="mb-4 bg-light p-3 rounded">
					<span class="price-tag"> <fmt:formatNumber
							value="${detail.price}" type="currency" currencySymbol="₫" />
					</span>
					<div class="mt-2 text-success">
						<i class='bx bxs-check-circle'></i> Còn hàng - Sẵn sàng giao ngay
					</div>
				</div>

				<p class="text-muted mb-4">Sản phẩm chất lượng cao, được các bác
					sĩ thú y khuyên dùng. Giúp thú cưng phát triển khỏe mạnh, tăng
					cường hệ miễn dịch và tiêu hóa tốt.</p>

				<form id="addToCartForm"
					action="${pageContext.request.contextPath}/add-to-cart"
					method="post" class="pb-4 border-bottom">
					<input type="hidden" name="id" value="${detail.id}"> <input
						type="hidden" name="actionType" id="actionType" value="add">

					<div class="row align-items-end">
						<div class="col-md-3 mb-3">
							<label class="form-label fw-bold">Số lượng:</label>
							<div class="input-group">
								<button class="btn btn-outline-secondary" type="button"
									onclick="decreaseQty()">-</button>
								<input type="number" id="qtyInput" name="quantity"
									class="form-control text-center" value="1" min="1" max="99">
								<button class="btn btn-outline-secondary" type="button"
									onclick="increaseQty()">+</button>
							</div>
						</div>

						<div class="col-md-9 mb-3 d-flex gap-2">
							<button type="button" onclick="submitForm('add')"
								class="btn btn-primary btn-lg flex-grow-1 shadow-sm">
								<i class='bx bx-cart-add'></i> Thêm vào giỏ
							</button>

							<button type="button" onclick="submitForm('buy')"
								class="btn btn-danger btn-lg flex-grow-1 shadow-sm">
								Mua Ngay</button>

							<button type="button" class="btn btn-outline-danger"
								title="Thêm vào yêu thích">
								<i class='bx bx-heart fs-4'></i>
							</button>
						</div>
					</div>
				</form>

				<div class="row mt-4 small text-secondary">
					<div class="col-6 mb-2">
						<i class='bx bx-shield-quarter text-primary me-2'></i> Bảo hành
						chính hãng
					</div>
					<div class="col-6 mb-2">
						<i class='bx bx-refresh text-primary me-2'></i> 1 đổi 1 trong 30
						ngày
					</div>
					<div class="col-6">
						<i class='bx bxs-truck text-primary me-2'></i> Freeship đơn từ
						500k
					</div>
					<div class="col-6">
						<i class='bx bx-support text-primary me-2'></i> Hỗ trợ kỹ thuật
						trọn đời
					</div>
				</div>

				<div class="mt-4">
					<span class="me-2 fw-bold">Chia sẻ:</span>
					<button class="btn btn-sm btn-outline-primary border-0">
						<i class="fab fa-facebook-f"></i>
					</button>
					<button class="btn btn-sm btn-outline-info border-0">
						<i class="fab fa-twitter"></i>
					</button>
					<button class="btn btn-sm btn-outline-danger border-0">
						<i class="fab fa-pinterest"></i>
					</button>
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
							data-bs-target="#specs" type="button">Thông số kỹ thuật</button>
					</li>
					<li class="nav-item" role="presentation">
						<button class="nav-link" id="reviews-tab" data-bs-toggle="tab"
							data-bs-target="#reviews" type="button">Đánh giá (24)</button>
					</li>
				</ul>

				<div
					class="tab-content border border-top-0 p-4 bg-white rounded-bottom"
					id="myTabContent">

					<div class="tab-pane fade show active" id="desc" role="tabpanel">
						<div class="product-description">
							<c:out value="${detail.description}" escapeXml="false" />
						</div>
					</div>

					<div class="tab-pane fade" id="specs" role="tabpanel">
						<table class="table table-striped table-bordered"
							style="max-width: 600px;">
							<tbody>
								<tr>
									<th width="30%">Thương hiệu</th>
									<td>Royal Canin</td>
								</tr>
								<tr>
									<th>Xuất xứ</th>
									<td>Pháp</td>
								</tr>
								<tr>
									<th>Đối tượng</th>
									<td>Chó / Mèo trên 12 tháng tuổi</td>
								</tr>
								<tr>
									<th>Trọng lượng</th>
									<td>1.5kg / 3kg / 10kg</td>
								</tr>
								<tr>
									<th>Hạn sử dụng</th>
									<td>18 tháng kể từ ngày sản xuất</td>
								</tr>
							</tbody>
						</table>
					</div>

					<div class="tab-pane fade" id="reviews" role="tabpanel">
						<div class="mb-4">
							<h5>Khách hàng đánh giá (${listReviews.size()})</h5>
							<c:if test="${empty listReviews}">
								<p class="text-muted">Chưa có đánh giá nào.</p>
							</c:if>
						</div>

						<div class="review-list mb-5"
							style="max-height: 500px; overflow-y: auto;">
							<c:forEach items="${listReviews}" var="r">
								<div class="d-flex mb-4 border-bottom pb-3">
									<div class="flex-shrink-0">
										<img
											src="https://ui-avatars.com/api/?name=${r.userName}&background=random"
											class="rounded-circle" width="50">
									</div>
									<div class="flex-grow-1 ms-3">
										<div class="d-flex justify-content-between">
											<h6 class="mb-0 fw-bold">${r.userName}</h6>
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
										<p class="mb-0 text-secondary">${r.comment}</p>
									</div>
								</div>
							</c:forEach>
						</div>

						<div class="card bg-light border-0 p-4">
							<h5 class="fw-bold mb-3">Viết đánh giá</h5>

							<c:if test="${not empty sessionScope.user}">
								<form action="${pageContext.request.contextPath}/add-review"
									method="post">
									<input type="hidden" name="productId" value="${detail.id}">

									<div class="mb-3">
										<label class="form-label fw-bold">Chọn số sao:</label>
										<div class="rating-css">
											<div class="star-icon">
												<input type="radio" name="rating" value="5" id="r5" checked>
												<label for="r5" class="fas fa-star"></label> <input
													type="radio" name="rating" value="4" id="r4"> <label
													for="r4" class="fas fa-star"></label> <input type="radio"
													name="rating" value="3" id="r3"> <label for="r3"
													class="fas fa-star"></label> <input type="radio"
													name="rating" value="2" id="r2"> <label for="r2"
													class="fas fa-star"></label> <input type="radio"
													name="rating" value="1" id="r1"> <label for="r1"
													class="fas fa-star"></label>
											</div>
										</div>
									</div>

									<div class="mb-3">
										<textarea name="comment" class="form-control" rows="3"
											placeholder="Nhập đánh giá của bạn..." required></textarea>
									</div>

									<button type="submit" class="btn btn-primary">Gửi đánh
										giá</button>
								</form>
							</c:if>

							<c:if test="${empty sessionScope.user}">
								<div class="alert alert-warning">
									Vui lòng <a href="${pageContext.request.contextPath}/login">Đăng
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
										src="${pageContext.request.contextPath}/assets/images/shop_pic/${rp.image}"
										class="card-img-top" alt="${rp.name}"
										style="height: 200px; object-fit: cover;"
										onerror="this.src='https://placehold.co/200x200/e2e8f0/1e293b?text=PetShop'">
									</a>
									<div class="card-body text-center">
										<h6 class="card-title fw-bold text-truncate">
											<a
												href="${pageContext.request.contextPath}/product-detail?id=${rp.id}"
												class="text-decoration-none text-dark"> ${rp.name} </a>
										</h6>
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
	
	// --- nút bấm ---
    function submitForm(type) {
        document.getElementById('actionType').value = type;

        document.getElementById('addToCartForm').submit();
    }
	
        // Tăng giảm số lượng
        function increaseQty() {
            var input = document.getElementById('qtyInput');
            var value = parseInt(input.value, 10);
            value = isNaN(value) ? 0 : value;
            if(value < 99) input.value = value + 1;
        }

        function decreaseQty() {
            var input = document.getElementById('qtyInput');
            var value = parseInt(input.value, 10);
            value = isNaN(value) ? 0 : value;
            if(value > 1) input.value = value - 1;
        }
        
        // Đổi ảnh khi click thumbnail
        function changeImage(element) {
            var mainImg = document.getElementById('mainImage');
            mainImg.src = element.src;
            
            // Active class styling
            var thumbs = document.querySelectorAll('.thumb-img');
            thumbs.forEach(t => t.classList.remove('active'));
            element.classList.add('active');
        }
    </script>
</body>
</html>