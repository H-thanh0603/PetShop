<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Về Chúng Tôi - PetShop</title>

    <!-- Favicon -->
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/assets/images/favicon/favicon.ico">

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Boxicons -->
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    
    <!-- Custom CSS -->
    <link href="${pageContext.request.contextPath}/assets/css/about.css" rel="stylesheet">
    
    <style>
    /* Color Variables */
    :root {
        --primary: #00bfa5;
        --primary-dark: #008f7a;
        --accent-orange: #ff7043;
        --accent-purple: #7c4dff;
        --accent-pink: #f50057;
        --dark-blue: #1a2e5a;
        --bg-cream: #FFFAF4;
    }
    
    /* Base */
    body { font-family: 'Montserrat', sans-serif; background: var(--bg-cream); }
    img { max-width: 100%; height: auto; }
    
    /* Hero Section - Shop Green Colors */
    .about-hero {
        background: linear-gradient(135deg, #00bfa5 0%, #005f52 100%);
        padding: 160px 0 100px;
        text-align: center;
        position: relative;
    }
    .about-hero::before {
        content: '';
        position: absolute;
        top: 0; left: 0; right: 0; bottom: 0;
        background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="10" cy="10" r="3" fill="rgba(255,255,255,0.1)"/><circle cx="90" cy="20" r="2" fill="rgba(255,255,255,0.08)"/><circle cx="50" cy="80" r="4" fill="rgba(255,255,255,0.06)"/></svg>');
        background-size: 80px 80px;
    }
    .about-hero h1 { color: #fff; font-size: 3.5rem; font-weight: 700; text-shadow: 2px 2px 20px rgba(0,0,0,0.2); position: relative; }
    .about-hero p { color: rgba(255,255,255,0.9); font-size: 1.2rem; position: relative; }
    
    /* About Section */
    .about-section { background: var(--bg-cream); padding: 100px 0; }
    .about-img-wrapper { position: relative; padding: 20px; }
    .about-img-main { 
        width: 100%; max-width: 500px; height: 400px; object-fit: cover; 
        border-radius: 20px; box-shadow: 0 20px 60px rgba(102, 126, 234, 0.3);
        border: 5px solid #fff;
    }
    .about-img-small { 
        position: absolute; bottom: -30px; right: 0; width: 200px; height: 150px; 
        object-fit: cover; border-radius: 15px; border: 5px solid var(--bg-cream);
        box-shadow: 0 10px 30px rgba(0,0,0,0.15);
    }
    .sub-title { 
        color: var(--primary); font-weight: 600; font-size: 1rem; 
        text-transform: uppercase; letter-spacing: 3px; 
    }
    .main-title { color: var(--dark-blue); font-size: 2.5rem; font-weight: 700; margin: 15px 0 20px; }
    .about-desc { color: #666; font-size: 1rem; line-height: 1.8; }
    
    /* Feature Boxes - Project Colors */
    .about-features { display: flex; flex-direction: column; gap: 20px; margin-top: 30px; }
    .feature-box { 
        display: flex; align-items: flex-start; gap: 15px; padding: 20px;
        background: #fff; border-radius: 15px; 
        box-shadow: 0 5px 25px rgba(0,0,0,0.08);
        transition: all 0.3s ease;
        border-left: 4px solid var(--primary);
    }
    .feature-box:hover { transform: translateX(10px); box-shadow: 0 10px 40px rgba(0,191,165,0.15); }
    
    .icon-box { 
        width: 55px; height: 55px; min-width: 55px; border-radius: 15px;
        display: flex; align-items: center; justify-content: center;
        background: linear-gradient(135deg, var(--primary), var(--primary-dark));
    }
    .icon-box i { font-size: 26px; color: #fff; }
    .feature-box h5 { color: var(--dark-blue); font-weight: 600; font-size: 1.05rem; margin-bottom: 5px; }
    .feature-box p { color: #888; font-size: 0.9rem; margin: 0; }
    
    /* Team Section - Dark with accent */
    .team-section {
        background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
        padding: 100px 0;
        position: relative;
    }
    .section-header { text-align: center; margin-bottom: 60px; }
    .section-header h2 { color: #fff; font-size: 2.5rem; font-weight: 700; }
    .section-header p { color: rgba(255,255,255,0.7); max-width: 600px; margin: 15px auto 25px; }
    .section-header h3 { 
        color: transparent; font-size: 1.3rem; font-weight: 600;
        background: linear-gradient(90deg, #00bfa5, #00e5ff);
        -webkit-background-clip: text;
    }
    
    .doctor-card {
        background: rgba(255,255,255,0.05); border-radius: 20px; overflow: hidden;
        border: 1px solid rgba(255,255,255,0.1);
        transition: all 0.3s ease;
    }
    .doctor-card:hover { 
        transform: translateY(-10px); 
        background: rgba(255,255,255,0.1);
        box-shadow: 0 20px 50px rgba(0, 191, 165, 0.2);
    }
    .doctor-img-box { width: 100%; height: 300px; overflow: hidden; }
    .doctor-img-box img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
    .doctor-card:hover .doctor-img-box img { transform: scale(1.1); }
    .doctor-info { padding: 25px; text-align: center; }
    .doctor-info h4 { color: #fff; font-size: 1.2rem; font-weight: 600; margin: 0; }
    
    /* Care Section */
    .care-section { background: linear-gradient(180deg, var(--bg-cream) 0%, #fff 100%); padding: 100px 0; }
    .care-section > .container > h2 { 
        color: var(--dark-blue); font-size: 2.5rem; font-weight: 700; 
        text-align: center; margin-bottom: 60px;
    }
    
    .care-list { list-style: none; padding: 0; margin: 0; }
    .care-item {
        padding: 18px 25px; color: #888; font-size: 1.05rem; cursor: pointer;
        transition: all 0.3s; border-left: 4px solid transparent;
        margin-bottom: 8px; border-radius: 0 12px 12px 0;
    }
    .care-item:hover { color: var(--dark-blue); background: rgba(0,191,165,0.05); }
    .care-item.active {
        color: #fff; font-weight: 600;
        background: linear-gradient(90deg, var(--primary), var(--primary-dark));
        border-left-color: var(--accent-purple);
        box-shadow: 0 5px 20px rgba(0,191,165,0.3);
    }
    
    .care-content-box {
        background: #fff; border-radius: 20px; padding: 40px;
        box-shadow: 0 15px 50px rgba(0,0,0,0.08);
        border-top: 4px solid var(--primary);
        min-height: 200px;
    }
    .tab-content { display: none; }
    .tab-content.active-content { display: block; animation: fadeIn 0.4s ease; }
    .tab-content p { color: #555; font-size: 1.05rem; line-height: 1.9; }
    
    @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
    
    /* Responsive */
    @media (max-width: 991px) {
        .about-hero h1 { font-size: 2.5rem; }
        .main-title, .section-header h2, .care-section > .container > h2 { font-size: 2rem; }
        .about-img-main { height: 350px; }
    }
    @media (max-width: 767px) {
        .about-hero { padding: 140px 0 80px; }
        .about-hero h1 { font-size: 2rem; }
        .about-img-small { display: none !important; }
        .about-section, .team-section, .care-section { padding: 60px 0; }
    }
    </style>
</head>
<body>

    <jsp:include page="/components/navbar.jsp" />

    <!-- Hero Section -->
    <section class="about-hero">
        <div class="container">
            <h1>Về Chúng Tôi</h1>
            <p>PetShop - Cửa hàng phụ kiện & thức ăn thú cưng hàng đầu Việt Nam</p>
        </div>
    </section>

    <!-- About Section -->
    <section class="about-section">
        <div class="container">
            <div class="row align-items-center g-5">
                <div class="col-lg-6">
                    <div class="about-img-wrapper">
                        <img src="${pageContext.request.contextPath}/assets/images/homepage_pic/webpic2.jpg" 
                             class="about-img-main" alt="Animal Doctors Team">
                        <img src="${pageContext.request.contextPath}/assets/images/homepage_pic/webpic6.jpg" 
                             class="about-img-small" alt="Pet Care">
                    </div>
                </div>

                <div class="col-lg-6">
                    <span class="sub-title">PetShop</span>
                    <h2 class="main-title">Đồng hành cùng thú cưng của bạn</h2>
                    <p class="about-desc">
                        Tại PetShop, chúng tôi hiểu rằng thú cưng không chỉ là động vật, 
                        mà là những thành viên yêu quý trong gia đình. Sứ mệnh của chúng tôi là 
                        mang đến những sản phẩm chất lượng nhất với giá cả hợp lý cho "người bạn nhỏ" của bạn.
                    </p>

                    <div class="about-features">
                        <div class="feature-box">
                            <div class="icon-box"><i class='bx bxs-check-shield'></i></div>
                            <div>
                                <h5>Sản phẩm chính hãng 100%</h5>
                                <p>Cam kết chỉ cung cấp các thương hiệu uy tín nhất thế giới</p>
                            </div>
                        </div>
                        <div class="feature-box">
                            <div class="icon-box"><i class='bx bxs-truck'></i></div>
                            <div>
                                <h5>Giao hàng nhanh chóng</h5>
                                <p>Nhận hàng trong 24h nội thành, 2-3 ngày toàn quốc</p>
                            </div>
                        </div>
                        <div class="feature-box">
                            <div class="icon-box"><i class='bx bxs-heart'></i></div>
                            <div>
                                <h5>Tư vấn tận tâm</h5>
                                <p>Đội ngũ nhân viên am hiểu sẵn sàng hỗ trợ bạn 24/7</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Team Section -->
    <section class="team-section">
        <div class="container">
            <div class="section-header">
                <h2>Đội Ngũ Của Chúng Tôi</h2>
                <p>Đội ngũ PetShop luôn tận tâm, yêu thương động vật và sẵn sàng tư vấn 
                   giúp bạn chọn sản phẩm phù hợp nhất.</p>
                <h3>Chuyên Gia Tư Vấn</h3>
            </div>

            <div class="row g-4 justify-content-center">
                <div class="col-lg-4 col-md-6">
                    <div class="doctor-card">
                        <div class="doctor-img-box">
                            <img src="${pageContext.request.contextPath}/assets/images/aboutUs_pic/webpic14.jpg" 
                                 alt="Chuyên gia 1">
                        </div>
                        <div class="doctor-info">
                            <h4>Nguyễn Văn An</h4>
                        </div>
                    </div>
                </div>
                <div class="col-lg-4 col-md-6">
                    <div class="doctor-card">
                        <div class="doctor-img-box">
                            <img src="${pageContext.request.contextPath}/assets/images/aboutUs_pic/webpic15.jpg" 
                                 alt="Chuyên gia 2">
                        </div>
                        <div class="doctor-info">
                            <h4>Trần Thị Bình</h4>
                        </div>
                    </div>
                </div>
                <div class="col-lg-4 col-md-6">
                    <div class="doctor-card">
                        <div class="doctor-img-box">
                            <img src="${pageContext.request.contextPath}/assets/images/aboutUs_pic/webpic16.jpg" 
                                 alt="Chuyên gia 3">
                        </div>
                        <div class="doctor-info">
                            <h4>Lê Minh Châu</h4>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Care Section -->
    <section class="care-section">
        <div class="container">
            <h2>Cam kết của chúng tôi</h2>

            <div class="row g-4">
                <div class="col-lg-4">
                    <ul class="care-list">
                        <li class="care-item active" onclick="openTab(event, 'tab1')">Sản phẩm chất lượng</li>
                        <li class="care-item" onclick="openTab(event, 'tab2')">Giá cả cạnh tranh</li>
                        <li class="care-item" onclick="openTab(event, 'tab3')">Giao hàng tận nơi</li>
                        <li class="care-item" onclick="openTab(event, 'tab4')">Hỗ trợ sau mua hàng</li>
                    </ul>
                </div>

                <div class="col-lg-8">
                    <div class="care-content-box">
                        <div id="tab1" class="tab-content active-content">
                            <p>Tất cả sản phẩm tại PetShop đều là hàng chính hãng, được nhập khẩu trực tiếp 
                               từ các thương hiệu uy tín trên thế giới. Chúng tôi cam kết hoàn tiền 200% nếu 
                               phát hiện hàng giả, hàng nhái. Mỗi sản phẩm đều được kiểm tra chất lượng kỹ lưỡng 
                               trước khi đến tay khách hàng.</p>
                        </div>
                        <div id="tab2" class="tab-content">
                            <p>Với hệ thống kho hàng lớn và chiến lược nhập hàng trực tiếp, PetShop luôn mang đến 
                               mức giá tốt nhất cho khách hàng. Ngoài ra, chúng tôi thường xuyên có các chương trình 
                               khuyến mãi hấp dẫn và chính sách tích điểm VIP Pet Card cho khách hàng thân thiết.</p>
                        </div>
                        <div id="tab3" class="tab-content">
                            <p>PetShop cung cấp dịch vụ giao hàng nhanh chóng trên toàn quốc. Nội thành nhận hàng 
                               trong 24h, ngoại thành 2-3 ngày. Miễn phí vận chuyển cho đơn hàng từ 500.000đ. 
                               Đóng gói cẩn thận, đảm bảo sản phẩm đến tay bạn trong tình trạng hoàn hảo.</p>
                        </div>
                        <div id="tab4" class="tab-content">
                            <p>Chính sách đổi trả linh hoạt trong 7 ngày. Đội ngũ chăm sóc khách hàng luôn sẵn sàng 
                               hỗ trợ bạn qua hotline, email và chat trực tuyến. Chúng tôi cũng cung cấp tư vấn 
                               miễn phí về dinh dưỡng và chăm sóc thú cưng từ các chuyên gia giàu kinh nghiệm.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <script>
        function openTab(evt, tabName) {
            document.querySelectorAll('.tab-content').forEach(function(tab) {
                tab.classList.remove('active-content');
            });
            
            document.querySelectorAll('.care-item').forEach(function(item) {
                item.classList.remove('active');
            });
            
            document.getElementById(tabName).classList.add('active-content');
            evt.currentTarget.classList.add('active');
        }
    </script>

    <jsp:include page="/components/footer.jsp" />
    <jsp:include page="/components/back-button.jsp" />
    
    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
