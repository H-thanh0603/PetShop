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
.service-title {
    font-size: 2.5rem;
    font-weight: 700;
    margin-bottom: 40px;
    color: #f0f0f0;
}
.service-list {
    list-style: none;
    padding: 0;
    margin: 0;
}
.service-item {
    font-size: 1.1rem;
    padding: 15px 0;
    color: #888;
    cursor: pointer;
    transition: 0.3s;
    border-left: 3px solid transparent;
}
.service-item:hover {
    color: #ccc;
}
.service-item.active {
    color: #fff;
    font-weight: 600;
    padding-left: 15px;
    border-left: 3px solid #8B0000;
}
.service-display {
    position: relative;
    width: 100%;
    height: 400px;
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
    background-color: #000;
}
.service-main-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
.service-content-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    padding: 30px;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.9), transparent);
    color: #fff;
}
.service-content-overlay p {
    font-size: 1rem;
    margin-bottom: 20px;
    max-width: 80%;
}
.btn-service-more {
    background-color: #721c24;
    color: #fff;
    padding: 8px 20px;
    border-radius: 5px;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 5px;
}
.btn-service-more:hover {
    background-color: #a71d2a;
    color: #fff;
}
.service-dots {
    display: flex;
    justify-content: center;
    gap: 10px;
    margin-top: 15px;
}
.dot {
    width: 8px;
    height: 8px;
    background-color: #555;
    border-radius: 50%;
    display: inline-block;
}
.dot.active {
    background-color: #fff;
    transform: scale(1.2);
}
.decor-bottom-left {
    position: absolute;
    bottom: 0;
    left: 20px;
    width: 150px;
    opacity: 0.3;
    filter: invert(1);
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
			<p>Thế giới phụ kiện, thức ăn và đồ chơi cao cấp cho thú cưng</p>
			<a href="${pageContext.request.contextPath}/shop" class="btn btn-booking">Mua sắm ngay</a>
		</div>
	</section>

	<section class="features-section">
		<div class="container">
			<div class="text-center mb-5">
				<h2 class="section-title">Sự chăm sóc tốt nhất dành cho thú
					cưng</h2>
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
