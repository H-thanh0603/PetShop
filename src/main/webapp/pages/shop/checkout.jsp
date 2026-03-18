<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Checkout</title>

    <!-- FONT -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap"
          rel="stylesheet">

    <!-- BOOTSTRAP -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
        body {
            background: #f5f6fa;
            font-family: 'Poppins', sans-serif;
        }

        .checkout-container {
            max-width: 1400px;
            margin: auto;
            padding: 30px;
        }

        .card-modern {
            background: white;
            border-radius: 16px;
            padding: 25px;
            box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05);
        }

        .product-item {
            display: flex;
            align-items: center;
            gap: 15px;
            padding: 15px;
            border-radius: 12px;
            transition: 0.3s;
        }

        .product-item:hover {
            background: #f8f9ff;
        }

        .product-img {
            width: 80px;
            height: 80px;
            object-fit: cover;
            border-radius: 10px;
        }

        .qty-badge {
            background: #eef1ff;
            padding: 5px 12px;
            border-radius: 20px;
            font-size: 14px;
        }

        .countdown {
            background: #ffe9e9;
            color: #ff3b3b;
            padding: 8px 16px;
            border-radius: 20px;
            font-weight: 600;
        }

        .payment-card {
            border: 2px solid #eee;
            border-radius: 14px;
            padding: 15px;
            cursor: pointer;
            transition: 0.3s;
        }

        .payment-card:hover {
            border-color: #4a6cf7;
            background: #f7f9ff;
        }

        .payment-card input {
            margin-right: 10px;
        }

        .btn-checkout {
            width: 100%;
            padding: 14px;
            border: none;
            border-radius: 14px;
            background: linear-gradient(45deg, #4a6cf7, #6f8cff);
            color: white;
            font-size: 18px;
            font-weight: 600;
            transition: 0.3s;
        }

        .btn-checkout:hover {
            transform: scale(1.03);
        }

        .info-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 10px;
        }

        .edit-btn {
            font-size: 13px;
            color: #4a6cf7;
            cursor: pointer;
        }

        textarea {
            border-radius: 12px !important;
        }

        .info-box {
            background: #fff;
            padding: 25px;
            border-radius: 18px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, .05);
            font-family: 'Poppins';
        }

        .info-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 12px;
        }

        .missing {
            color: #999;
            font-style: italic;
        }

        .edit-btn {
            color: #4a6cf7;
            cursor: pointer;
            font-size: 14px;
            margin-left: 10px;
        }

        .edit-btn:hover {
            text-decoration: underline;
        }

        .gradient-btn {
            background: linear-gradient(45deg, #4a6cf7, #6f8cff);
            border: none;
        }

        .modal-dialog {
            display: flex;
            align-items: center;
            min-height: 90vh;
        }

        .modal {
            z-index: 999999 !important;
        }
    </style>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</head>
<body>
<jsp:include page="/components/navbar.jsp"/>
<div class="checkout-container">
    <div class="row g-4">

        <!-- LEFT PRODUCT LIST -->
        <div class="col-lg-7">
            <div class="card-modern">

                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h4>🛒 Sản phẩm thanh toán</h4>
                    <div class="countdown">
                        ⏳ <span id="timer">15:00</span>
                    </div>
                </div>

                <c:forEach var="item" items="${cartItems}">
                    <div class="product-item">
                        <img class="product-img" src="${item.product.image}">
                        <div class="flex-grow-1">
                            <div class="fw-semibold">${item.product.name}</div>
                            <div class="text-muted">${item.product.price}₫</div>
                        </div>

                        <div class="qty-badge">
                            SL: ${item.quantity}
                        </div>

                        <div class="fw-bold text-danger">
                                ${item.product.price * item.quantity} ₫
                        </div>
                    </div>
                </c:forEach>

                <hr>

                <div class="info-row">
                    <span>Tổng tiền hàng</span>
                    <span>${totalAmount}₫</span>
                </div>

                <div class="info-row">
                    <span>Phí ship</span>
                    <span>30.000 ₫</span>
                </div>

                <div class="info-row">
                    <span>Giảm giá</span>
                    <span class="text-success">- ${discount} ₫</span>
                </div>

                <hr>

                <div class="info-row fs-5 fw-bold text-primary">
                    <span>Tổng thanh toán</span>
                    <span>${finalTotal} ₫</span>
                </div>

            </div>
        </div>


        <!-- RIGHT USER INFO -->
        <div class="col-lg-5">
            <div class="card-modern">

                <h4 class="mb-3">👤 Thông tin nhận hàng</h4>

                <div class="info-row">
                    <span>Họ tên</span>
                    <span>
            <c:choose>
                <c:when test="${empty user.fullname}">
                    <span class="missing">Chưa cập nhật</span>
                    <span class="edit-btn" data-bs-toggle="modal" data-bs-target="#profileModal">Thêm thông tin</span>
                </c:when>
                <c:otherwise>
                    ${user.fullname}
                    <span class="edit-btn" data-bs-toggle="modal" data-bs-target="#profileModal">Thay đổi</span>
                </c:otherwise>
            </c:choose>
        </span>
                </div>

                <div class="info-row">
                    <span>Email</span>
                    <span>${user.email}</span>
                </div>

                <div class="info-row">
                    <span>SĐT</span>
                    <span>
            <c:choose>
                <c:when test="${empty user.phone}">
                    <span class="missing">Chưa cập nhật</span>
                    <span class="edit-btn" data-bs-toggle="modal" data-bs-target="#profileModal">Thêm thông tin</span>
                </c:when>
                <c:otherwise>
                    ${user.phone}
                    <span class="edit-btn" data-bs-toggle="modal" data-bs-target="#profileModal">Thay đổi</span>
                </c:otherwise>
            </c:choose>
        </span>
                </div>

                <div class="info-row">
                    <span>Địa chỉ</span>
                    <span>
            <c:choose>
                <c:when test="${empty user.address}">
                    <span class="missing">Chưa cập nhật</span>
                    <span class="edit-btn" data-bs-toggle="modal" data-bs-target="#profileModal">Thêm thông tin</span>
                </c:when>
                <c:otherwise>
                    ${user.address}
                    <span class="edit-btn" data-bs-toggle="modal" data-bs-target="#profileModal">Thay đổi</span>
                </c:otherwise>
            </c:choose>
        </span>
                </div>

                <hr>

                <label>📦 Ghi chú</label>
                <textarea class="form-control mb-3" rows="3" placeholder="Nhập ghi chú cho shop..."></textarea>

                <label>🎟 Mã giảm giá</label>
                <div class="input-group mb-3">
                    <input class="form-control" placeholder="Nhập mã coupon">
                    <button class="btn btn-primary">Áp dụng</button>
                </div>

                <label class="mb-2">💳 Phương thức thanh toán</label>

                <div class="payment-card mb-2">
                    <input type="radio" name="payment"> Thanh toán khi nhận hàng (COD)
                </div>

                <div class="payment-card mb-2">
                    <input type="radio" name="payment"> Ví điện tử
                </div>

                <div class="payment-card mb-3">
                    <input type="radio" name="payment"> Chuyển khoản ngân hàng
                </div>

                <button type="button" class="btn-checkout">
                    Đặt hàng ngay
                </button>

            </div>
        </div>

    </div>
</div>

<script>


    function validateForm() {
        let phone = document.getElementById("phone").value;

        let regex = /^[0-9]{9,11}$/;

        if (!regex.test(phone)) {
            alert("Số điện thoại không hợp lệ");
            return false;
        }
        return true;
    }


    let time = 15 * 60;
    let timer = document.getElementById("timer");

    setInterval(() => {
        let minutes = Math.floor(time / 60);
        let seconds = time % 60;

        timer.innerHTML =
            String(minutes).padStart(2, '0') + ":" +
            String(seconds).padStart(2, '0');

        time--;

        if (time < 0) {
            alert("Hết thời gian thanh toán!");
            window.location.href = "cart";
        }

    }, 1000);
</script>
<jsp:include page="/components/footer.jsp"/>
<%--        modal cập nhật thông tin--%>
<div class="modal fade" id="profileModal">
    <div class="modal-dialog">
        <div class="modal-content" style="border-radius:20px">

            <form action="${pageContext.request.contextPath}/update-profile-checkout" method="post"
                  onsubmit="return validateForm()">

                <div class="modal-header">
                    <h5>Cập nhật thông tin nhận hàng</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>

                <div class="modal-body">

                    <label>Họ tên</label>
                    <input id="fullname" name="fullname" class="form-control mb-3"
                           value="${user.fullname}" required>

                    <label>Số điện thoại</label>
                    <input id="phone" name="phone" class="form-control mb-3"
                           value="${user.phone}" required>

                    <label>Địa chỉ</label>
                    <textarea id="address" name="address" class="form-control"
                              required>${user.address}</textarea>

                </div>

                <div class="modal-footer">
                    <button type="submit" class="btn gradient-btn text-white">Lưu thông tin</button>
                </div>

            </form>

        </div>
    </div>
</div>
</body>
</html>