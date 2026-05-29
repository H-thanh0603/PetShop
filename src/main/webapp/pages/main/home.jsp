<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>PetShop - Thế giới phụ kiện & thức ăn thú cưng</title>

<!-- Favicon -->
<link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/assets/images/favicon/favicon.ico">
<link rel="icon" type="image/png" sizes="32x32" href="${pageContext.request.contextPath}/assets/images/favicon/favicon-32x32.png">

<!-- Bootstrap CSS - Load trước -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

<!-- Boxicons -->
<link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>

<!-- Custom CSS - Load sau để override -->
<link href="${pageContext.request.contextPath}/assets/css/home.css" rel="stylesheet">

<style>
/* Backup CSS inline để đảm bảo layout không vỡ */
.hero-section {
    position: relative;
    height: 100vh;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    overflow: hidden;
    color: #fff;
}
.back-video {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: -2;
    object-fit: cover;
}
.hero-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.4);
    z-index: -1;
}
.hero-content {
    z-index: 1;
    max-width: 800px;
    padding: 20px;
}
.hero-content h1 {
    font-size: 60px;
    font-weight: 700;
    margin-bottom: 20px;
    text-shadow: 2px 2px 10px rgba(0, 0, 0, 0.5);
    text-transform: uppercase;
}
.hero-content p {
    font-size: 20px;
    margin-bottom: 30px;
    text-shadow: 2px 2px 10px rgba(0, 0, 0, 0.5);
}

/* Features Section */
.features-section {
    background-color: #FFFAF4;
    padding: 80px 0;
    border-radius: 50px 50px 0 0;
    position: relative;
    margin-top: -50px;
    z-index: 1;
    width: 100%;
}
.section-title {
    color: #1a2e5a;
    font-weight: 700;
    font-size: 2.5rem;
    margin-bottom: 40px;
}
.feature-item {
    padding: 10px;
    text-align: center;
}
.img-box {
    width: 100%;
    height: 200px;
    border-radius: 20px;
    overflow: hidden;
    margin-bottom: 20px;
}
.img-box img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.6s ease;
}
.feature-item:hover .img-box img {
    transform: scale(1.1);
}
.feature-item h5 {
    color: #1a2e5a;
    font-weight: 700;
    font-size: 1.1rem;
    min-height: 50px;
}
.feature-item p {
    color: #555;
    font-size: 0.9rem;
}

/* Services Dark Section */
.services-dark-section {
    background-color: #1a1a1a;
    padding: 100px 0;
    color: #fff;
    position: relative;
    overflow: hidden;
    width: 100%;
}
.quick-stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-top: 18px;
}
.quick-stat {
    background: rgba(255,255,255,0.14);
    border: 1px solid rgba(255,255,255,0.18);
    border-radius: 18px;
    padding: 16px;
    backdrop-filter: blur(10px);
}
.quick-stat strong {
    display: block;
    font-size: 1.3rem;
}
.category-strip {
    margin: 26px auto 0;
    max-width: 1100px;
}
.category-pill {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    background: #fff;
    color: #0b1a33;
    border-radius: 999px;
    padding: 10px 16px;
    text-decoration: none;
    box-shadow: 0 10px 24px rgba(15,23,42,.08);
    font-weight: 600;
    margin: 6px;
}
.category-pill:hover {
    color: #00bfa5;
}
@media (max-width: 991px) {
    .quick-stats {
        grid-template-columns: repeat(2, 1fr);
    }
}
@media (max-width: 576px) {
    .quick-stats {
        grid-template-columns: 1fr;
    }
}

/* Button booking */
.btn-booking {
    background-color: #00bfa5;
    color: white !important;
    padding: 10px 25px;
    border-radius: 50px;
    font-weight: 600;
    border: none;
}
.btn-booking:hover {
    background-color: #008f7a;
}
</style>
<link href="${pageContext.request.contextPath}/assets/css/storefront-polish.css" rel="stylesheet">
</head>
<body>

	<jsp:include page="/components/layout/header-home.jsp" />

	<section class="hero-section">
		<video autoplay muted loop playsinline class="back-video">
			<source src="${pageContext.request.contextPath}/assets/video/catvid.mp4"
				type="video/mp4">
		</video>

		<div class="hero-overlay"></div>

		<div class="hero-content">
			<h1>CHÀO MỪNG ĐẾN VỚI PETSHOP</h1>
			<p>Thức ăn, phụ kiện và đồ chơi chính hãng cho chó mèo với giao hàng nhanh và nhiều ưu đãi mỗi ngày.</p>
			<a href="${pageContext.request.contextPath}/shop" class="btn btn-booking">Mua sắm ngay</a>
            <div class="quick-stats text-start">
                <div class="quick-stat"><strong>1000+</strong><span>Sản phẩm cho thú cưng</span></div>
                <div class="quick-stat"><strong>Chính hãng</strong><span>Thương hiệu được tuyển chọn</span></div>
                <div class="quick-stat"><strong>Nhanh</strong><span>Giao hàng toàn quốc</span></div>
                <div class="quick-stat"><strong>Hỗ trợ</strong><span>Tư vấn mua sắm mỗi ngày</span></div>
            </div>
		</div>
	</section>

	<section class="features-section">
		<div class="container">
			<div class="text-center mb-5">
				<h2 class="section-title">Vì sao khách hàng chọn PetShop</h2>
			</div>

			<div class="row">
				<div class="col-lg-3 col-md-6 mb-4">
					<div class="feature-item">
						<div class="img-box">
							<img
								src="${pageContext.request.contextPath}/assets/images/homepage_pic/webpic2.jpg"
								alt="Bác sĩ">
						</div>
						<h5>Sản phẩm chính hãng, chất lượng cao</h5>
						<p>Chúng tôi chỉ cung cấp các thương hiệu uy tín nhất thế giới.</p>
					</div>
				</div>

				<div class="col-lg-3 col-md-6 mb-4">
					<div class="feature-item">
						<div class="img-box">
							<img
								src="${pageContext.request.contextPath}/assets/images/homepage_pic/webpic5.jpg"
								alt="Công nghệ">
						</div>
						<h5>Giao hàng nhanh chóng & Tiện lợi</h5>
						<p>Nhận hàng ngay tại nhà với dịch vụ giao hàng siêu tốc.</p>
					</div>
				</div>

				<div class="col-lg-3 col-md-6 mb-4">
					<div class="feature-item">
						<div class="img-box">
							<img
								src="${pageContext.request.contextPath}/assets/images/homepage_pic/webpic3.jpg"
								alt="Chuyên gia">
						</div>
						<h5>Giá cả cạnh tranh, nhiều ưu đãi</h5>
						<p>Luôn có các chương trình khuyến mãi hấp dẫn cho khách hàng thân thiết.</p>
					</div>
				</div>

				<div class="col-lg-3 col-md-6 mb-4">
					<div class="feature-item">
						<div class="img-box">
							<img
								src="${pageContext.request.contextPath}/assets/images/homepage_pic/webpic4.jpg"
								alt="Khách hàng">
						</div>
						<h5>Hỗ trợ tư vấn 24/7</h5>
						<p>Đội ngũ nhân viên luôn sẵn sàng giải đáp thắc mắc của bạn.</p>
					</div>
				</div>
			</div>
		</div>
	</section>

    <section class="container mb-5 text-center">
        <div class="p-5 bg-light rounded-5 shadow-sm">
            <h2 class="section-title mb-4" style="margin-top: 0; color: #1a2e5a;">Sẵn sàng mua sắm?</h2>
            <p class="lead mb-4">Khám phá hàng ngàn sản phẩm dành cho "người bạn nhỏ" của bạn ngay hôm nay.</p>
            <a href="${pageContext.request.contextPath}/shop" class="btn btn-lg btn-booking p-3 px-5">Đến Cửa Hàng <i class='bx bx-shopping-bag'></i></a>
        </div>
    </section>
    <section class="category-strip text-center mb-5">
        <a class="category-pill" href="${pageContext.request.contextPath}/shop?pet=dog"><i class='bx bxs-dog'></i> Mua cho chó</a>
        <a class="category-pill" href="${pageContext.request.contextPath}/shop?pet=cat"><i class='bx bxs-cat'></i> Mua cho mèo</a>
        <a class="category-pill" href="${pageContext.request.contextPath}/shop?sort=discount"><i class='bx bx-trending-up'></i> Ưu đãi nổi bật</a>
        <a class="category-pill" href="${pageContext.request.contextPath}/shop?discountOnly=true"><i class='bx bx-purchase-tag-alt'></i> Sản phẩm giảm giá</a>
    </section>

	<script>
        const contextPath = "${pageContext.request.contextPath}";

        window.addEventListener('scroll', function() {
            var nav = document.getElementById('navbar');
            if (nav) {
                if (window.scrollY > 50) {
                    nav.classList.add('navbar-scrolled'); 
                } else {
                    nav.classList.remove('navbar-scrolled'); 
                }
            }
        });
    </script>
    
    
	<jsp:include page="/components/footer.jsp" />

	<!-- Bootstrap JS -->
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
