<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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

/* Popular Categories */
.popular-categories-section {
    --category-primary: #00bfa5;
    --category-primary-dark: #008f7a;
    --category-ink: #1a2e5a;
    --category-ink-dark: #0b1a33;
    --category-soft: #e7fbf7;
    padding: 72px 0 38px;
    background: linear-gradient(180deg, #ffffff 0%, #f9fdfc 100%);
}
.popular-categories-wrap {
    background: #ffffff;
    border-radius: 32px;
    padding: 4px 0 0;
}
.popular-categories-title {
    color: var(--category-ink-dark);
    font-size: 2.15rem;
    font-weight: 800;
    letter-spacing: -0.02em;
    margin-bottom: 0;
}
.popular-categories-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 30px;
}
.popular-categories-controls {
    display: flex;
    align-items: center;
    gap: 12px;
}
.popular-categories-btn {
    width: 46px;
    height: 46px;
    border-radius: 50%;
    border: 1px solid rgba(0, 191, 165, 0.18);
    background: var(--category-soft);
    color: var(--category-primary-dark);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 1.4rem;
    box-shadow: 0 10px 22px rgba(0, 191, 165, 0.08);
    transition: all 0.25s ease;
}
.popular-categories-btn:hover {
    background: linear-gradient(135deg, var(--category-primary), var(--category-primary-dark));
    border-color: transparent;
    color: #ffffff;
}
.popular-categories-btn:disabled {
    opacity: 0.45;
    cursor: not-allowed;
    background: #f4fbf9;
    color: #89b7af;
}
.popular-categories-carousel {
    position: relative;
}
.popular-categories-track {
    display: flex;
    gap: 30px;
    overflow-x: auto;
    scroll-behavior: smooth;
    scroll-snap-type: x mandatory;
    padding: 6px 4px 18px;
    scrollbar-width: none;
    -ms-overflow-style: none;
    cursor: grab;
    user-select: none;
}
.popular-categories-track::-webkit-scrollbar {
    display: none;
}
.popular-category-card {
    flex: 0 0 168px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-decoration: none;
    color: var(--category-ink);
    transition: transform 0.25s ease;
    scroll-snap-align: start;
}
.popular-category-card:hover {
    transform: translateY(-6px);
    color: var(--category-ink-dark);
}
.popular-category-icon {
    width: 184px;
    height: 184px;
    border-radius: 50%;
    background: radial-gradient(circle at 30% 28%, #15d8bf 0%, var(--category-primary) 35%, var(--category-primary-dark) 72%, #0e695f 100%);
    border: 6px solid #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 22px 42px rgba(0, 143, 122, 0.18);
    margin-bottom: 24px;
}
.popular-category-svg {
    width: 98px;
    height: 98px;
    stroke: #ffffff;
    fill: none;
    stroke-width: 2.15;
    stroke-linecap: round;
    stroke-linejoin: round;
}
.popular-category-svg .fill-white {
    fill: #ffffff;
    stroke: none;
}
.popular-category-svg .stroke-thin {
    stroke-width: 1.8;
}
.popular-category-name {
    font-size: 1rem;
    font-weight: 500;
    text-align: center;
    line-height: 1.5;
    min-height: 52px;
    max-width: 170px;
}

@media (max-width: 1399px) {
    .popular-category-card {
        flex-basis: 160px;
    }
    .popular-category-icon {
        width: 170px;
        height: 170px;
    }
}

@media (max-width: 767px) {
    .popular-categories-section {
        padding: 56px 0 22px;
    }
    .popular-categories-header {
        align-items: flex-start;
        flex-direction: column;
    }
    .popular-categories-title {
        font-size: 1.75rem;
    }
    .popular-categories-controls {
        display: none;
    }
    .popular-categories-track {
        gap: 18px;
        padding-bottom: 12px;
    }
    .popular-category-icon {
        width: 132px;
        height: 132px;
        margin-bottom: 16px;
    }
    .popular-category-svg {
        width: 72px;
        height: 72px;
    }
    .popular-category-name {
        font-size: 0.95rem;
        min-height: auto;
        max-width: 132px;
    }
    .popular-category-card {
        flex-basis: 132px;
    }
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

    <section class="popular-categories-section">
        <div class="container">
            <div class="popular-categories-wrap">
                <div class="popular-categories-header">
                    <h2 class="popular-categories-title">Danh Mục Được Mua Nhiều</h2>
                    <div class="popular-categories-controls">
                        <button type="button" class="popular-categories-btn" id="popularCategoriesPrev" aria-label="Danh mục trước">
                            <i class='bx bx-chevron-left'></i>
                        </button>
                        <button type="button" class="popular-categories-btn" id="popularCategoriesNext" aria-label="Danh mục tiếp theo">
                            <i class='bx bx-chevron-right'></i>
                        </button>
                    </div>
                </div>

                <div class="popular-categories-carousel">
                    <div class="popular-categories-track" id="popularCategoriesTrack">
                        <c:forEach var="item" items="${homeCategories}">
                            <a class="popular-category-card" href="${item.href}">
                                <div class="popular-category-icon">
                                    <c:choose>
                                        <c:when test="${item.iconKey == 'food'}">
                                            <svg class="popular-category-svg" viewBox="0 0 96 96" aria-hidden="true">
                                                <path d="M26 16h18l6 6v12l-6 6H26l-6-6V22z"/>
                                                <path d="M24 48h24v26H24z"/>
                                                <path d="M29 55h14"/>
                                                <path d="M31 61h10"/>
                                                <path d="M63 52c5-9 9-16 14-22"/>
                                                <path d="M58 58c8-1 16 1 20 7"/>
                                                <path d="M54 66c6-2 15 0 20 6"/>
                                                <path d="M66 25l4-4"/>
                                                <path d="M72 31l4-4"/>
                                            </svg>
                                        </c:when>
                                        <c:when test="${item.iconKey == 'hygiene'}">
                                            <svg class="popular-category-svg" viewBox="0 0 96 96" aria-hidden="true">
                                                <path d="M38 18h20"/>
                                                <path d="M42 18v8"/>
                                                <path d="M54 18v8"/>
                                                <path d="M35 26h26l5 6v34c0 7-5 12-12 12H42c-7 0-12-5-12-12V32z"/>
                                                <path d="M42 42c1-4 4-6 8-6s7 2 8 6"/>
                                                <path d="M40 55c3-2 5-5 6-9c1 4 3 7 6 9"/>
                                                <circle cx="24" cy="36" r="4"/>
                                                <circle cx="21" cy="52" r="5"/>
                                                <circle cx="74" cy="44" r="4"/>
                                                <circle cx="70" cy="60" r="5"/>
                                            </svg>
                                        </c:when>
                                        <c:when test="${item.iconKey == 'bowl'}">
                                            <svg class="popular-category-svg" viewBox="0 0 96 96" aria-hidden="true">
                                                <ellipse cx="48" cy="32" rx="26" ry="7"/>
                                                <path d="M22 32v11c0 7 12 13 26 13s26-6 26-13V32"/>
                                                <path d="M27 55h42"/>
                                                <path d="M39 35c2 2 4 4 9 4s7-2 9-4"/>
                                            </svg>
                                        </c:when>
                                        <c:when test="${item.iconKey == 'litter'}">
                                            <svg class="popular-category-svg" viewBox="0 0 96 96" aria-hidden="true">
                                                <path d="M20 52h42l9 16H29c-5 0-9-4-9-9z"/>
                                                <path d="M26 42h20c8 0 14 6 14 14v2"/>
                                                <path d="M61 31l9 28"/>
                                                <path d="M32 60h2"/>
                                                <path d="M40 58h2"/>
                                                <path d="M48 61h2"/>
                                                <path d="M56 57h2"/>
                                            </svg>
                                        </c:when>
                                        <c:when test="${item.iconKey == 'toy'}">
                                            <svg class="popular-category-svg" viewBox="0 0 96 96" aria-hidden="true">
                                                <path d="M30 52l16-16"/>
                                                <path d="M46 36l10 10"/>
                                                <path d="M24 58c-4 0-8 4-8 8s4 8 8 8 8-4 8-8"/>
                                                <path d="M64 30c0-4 4-8 8-8s8 4 8 8-4 8-8 8"/>
                                                <path d="M18 44l12 12"/>
                                                <path d="M58 24l12 12"/>
                                                <path d="M51 62c8 0 15 5 19 12c-10 2-22-1-28-8"/>
                                                <path d="M38 67c-3-7-1-16 4-22c6 2 11 7 12 15"/>
                                            </svg>
                                        </c:when>
                                        <c:when test="${item.iconKey == 'health'}">
                                            <svg class="popular-category-svg" viewBox="0 0 96 96" aria-hidden="true">
                                                <path d="M31 26h34"/>
                                                <path d="M38 26v-8h20v8"/>
                                                <path d="M28 34h40l6 10v24c0 6-4 10-10 10H32c-6 0-10-4-10-10V44z"/>
                                                <path d="M48 44v16"/>
                                                <path d="M40 52h16"/>
                                                <path d="M73 27l4 4"/>
                                                <path d="M77 23l-4 4"/>
                                            </svg>
                                        </c:when>
                                        <c:when test="${item.iconKey == 'carrier'}">
                                            <svg class="popular-category-svg" viewBox="0 0 96 96" aria-hidden="true">
                                                <path d="M34 25v-5c0-4 4-7 14-7s14 3 14 7v5"/>
                                                <path d="M24 30h48l6 10v24c0 7-5 12-12 12H30c-7 0-12-5-12-12V40z"/>
                                                <path d="M36 40v26"/>
                                                <path d="M48 40v26"/>
                                                <path d="M60 40v26"/>
                                                <path d="M30 53h36"/>
                                                <path d="M44 22h8"/>
                                            </svg>
                                        </c:when>
                                        <c:when test="${item.iconKey == 'sale'}">
                                            <svg class="popular-category-svg" viewBox="0 0 96 96" aria-hidden="true">
                                                <path d="M21 28h42l12 12v36L57 58H21z"/>
                                                <path d="M38 41l20 12"/>
                                                <path d="M55 36l-14 22"/>
                                                <circle cx="37" cy="37" r="4"/>
                                                <circle cx="59" cy="57" r="4"/>
                                            </svg>
                                        </c:when>
                                        <c:otherwise>
                                            <svg class="popular-category-svg" viewBox="0 0 96 96" aria-hidden="true">
                                                <circle cx="48" cy="48" r="24"/>
                                                <path d="M48 34v28"/>
                                                <path d="M34 48h28"/>
                                            </svg>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <span class="popular-category-name">${item.name}</span>
                            </a>
                        </c:forEach>
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

        (function () {
            const track = document.getElementById('popularCategoriesTrack');
            const prevBtn = document.getElementById('popularCategoriesPrev');
            const nextBtn = document.getElementById('popularCategoriesNext');

            if (!track || !prevBtn || !nextBtn) return;

            const getScrollAmount = () => {
                const firstCard = track.querySelector('.popular-category-card');
                if (!firstCard) return 220;
                const cardStyle = window.getComputedStyle(firstCard);
                return firstCard.offsetWidth + parseFloat(cardStyle.marginRight || 0) + 26;
            };

            const updateButtons = () => {
                const maxScroll = track.scrollWidth - track.clientWidth;
                prevBtn.disabled = track.scrollLeft <= 8;
                nextBtn.disabled = track.scrollLeft >= maxScroll - 8;
            };

            prevBtn.addEventListener('click', function () {
                track.scrollBy({ left: -getScrollAmount() * 2, behavior: 'smooth' });
            });

            nextBtn.addEventListener('click', function () {
                track.scrollBy({ left: getScrollAmount() * 2, behavior: 'smooth' });
            });

            let isDown = false;
            let startX = 0;
            let startScrollLeft = 0;

            track.addEventListener('mousedown', function (event) {
                isDown = true;
                startX = event.pageX;
                startScrollLeft = track.scrollLeft;
                track.style.cursor = 'grabbing';
            });

            window.addEventListener('mouseup', function () {
                isDown = false;
                track.style.cursor = '';
            });

            track.addEventListener('mouseleave', function () {
                isDown = false;
                track.style.cursor = '';
            });

            track.addEventListener('mousemove', function (event) {
                if (!isDown) return;
                event.preventDefault();
                const distance = event.pageX - startX;
                track.scrollLeft = startScrollLeft - distance;
            });

            track.addEventListener('scroll', updateButtons, { passive: true });
            window.addEventListener('resize', updateButtons);
            updateButtons();
        })();
    </script>
    
    
	<jsp:include page="/components/footer.jsp" />

	<!-- Bootstrap JS -->
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
