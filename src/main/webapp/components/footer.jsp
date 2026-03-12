<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<footer class="footer-section">
    <div class="container">
        <div class="row">
            <div class="col-lg-3 col-md-6 mb-4">
                <div class="footer-logo mb-3">
                    <h3 class="text-white fw-bold"><i class='bx bxs-dog'></i> PetShop</h3>
                    <span class="text-white-50">Thế giới phụ kiện thú cưng</span>
                </div>
                <p class="text-white-50 mb-2">Đăng ký nhận tin khuyến mãi</p>
                <form class="subscribe-form d-flex mb-3">
                    <input type="email" class="form-control me-1" placeholder="Email của bạn">
                    <button type="submit" class="btn btn-primary"><i class='bx bx-right-arrow-alt'></i></button>
                </form>
                <div class="social-icons">
                    <a href="#"><i class='bx bxl-facebook'></i></a>
                    <a href="#"><i class='bx bxl-instagram'></i></a>
                    <a href="#"><i class='bx bxl-linkedin'></i></a>
                </div>
            </div>

            <div class="col-lg-2 col-md-6 mb-4">
                <h5 class="text-white mb-3">Sản phẩm</h5>
                <ul class="list-unstyled footer-links">
                    <li><a href="${pageContext.request.contextPath}/shop">Thức ăn</a></li>
                    <li><a href="${pageContext.request.contextPath}/shop">Đồ chơi</a></li>
                    <li><a href="${pageContext.request.contextPath}/shop">Phụ kiện</a></li>
                    <li><a href="${pageContext.request.contextPath}/shop">Chuồng & Nệm</a></li>
                    <li><a href="${pageContext.request.contextPath}/shop">Vệ sinh</a></li>
                </ul>
            </div>

            <div class="col-lg-2 col-md-6 mb-4">
                <h5 class="text-white mb-3">Hỗ trợ</h5>
                <ul class="list-unstyled footer-links">
                    <li><a href="#">Chính sách đổi trả</a></li>
                    <li><a href="#">Chính sách vận chuyển</a></li>
                    <li><a href="#">Hướng dẫn mua hàng</a></li>
                    <li><a href="#">Tư vấn khách hàng</a></li>
                    <li><a href="#">VIP Pet Card</a></li>
                </ul>
            </div>

            <div class="col-lg-5 col-md-6 mb-4">
                <h5 class="text-white mb-3">LIÊN HỆ <span class="float-end">-</span></h5>
                <ul class="list-unstyled text-white-50 contact-info">
                    <li class="mb-2"><strong class="text-white">Hotline:</strong> 1900 123 456</li>
                    <li class="mb-2"><strong class="text-white">Email:</strong> support@petshop.vn</li>
                    <li class="mb-2">
                        <strong class="text-white">Trụ sở chính:</strong><br>
                        123 Đường ABC, Quận 1, TP. Hồ Chí Minh
                    </li>
                    <li class="mb-2">
                        <strong class="text-white">Chi nhánh Hà Nội:</strong><br>
                        456 Đường XYZ, Quận Cầu Giấy, Hà Nội
                    </li>
                    <li class="mt-3 text-white">
                        <strong>Giờ mở cửa:</strong> 8:00 AM - 9:00 PM (Tất cả các ngày trong tuần)
                    </li>
                </ul>
            </div>
        </div>
    </div>
    
    <jsp:include page="/components/toast.jsp" />
    <div class="footer-bottom text-center py-3">
        <p class="text-white-50 m-0">&copy; 2025 PetShop. All Rights Reserved.</p>
    </div>
</footer>

<style>
    .footer-section {
        background-color: #0b1a33;
        color: #fff;
        padding-top: 60px;
        font-size: 14px;
        position: relative;
        z-index: 10;
    }

    .subscribe-form .form-control {
        border-radius: 8px;
        border: none;
        padding: 10px 15px;
    }

    .subscribe-form .btn {
        border-radius: 8px;
        background-color: #00bfa5;
        border: none;
        padding: 10px 15px;
        color: white;
    }

    .social-icons a {
        color: #fff;
        font-size: 24px;
        margin-right: 15px;
        text-decoration: none;
        transition: color 0.2s;
    }
    .social-icons a:hover { color: #00bfa5; }

    .footer-links li {
        margin-bottom: 12px;
    }

    .footer-links a {
        color: #94a3b8;
        text-decoration: none;
        transition: all 0.2s;
    }

    .footer-links a:hover {
        color: #00bfa5;
        padding-left: 5px;
    }

    .contact-info li {
        line-height: 1.6;
    }

    .footer-bottom {
        border-top: 1px solid rgba(255,255,255,0.1);
        background-color: #081426;
        margin-top: 40px;
    }
</style>