<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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

        .address-section {
            background: #fff;
            padding: 20px;
            border-radius: 12px;
            margin-bottom: 20px;
        }

        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }

        .btn-add {
            background: #ff4d4f;
            color: #fff;
            border: none;
            padding: 3px 7px;
            border-radius: 8px;
            cursor: pointer;
        }

        .address-list {
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .address-card {
            border: 1px solid #eee;
            padding: 12px;
            border-radius: 10px;
            cursor: pointer;
            transition: 0.2s;
        }

        .address-card:hover {
            border-color: #ff4d4f;
        }

        .address-card.active {
            border: 2px solid #ff4d4f;
            background: #fff1f0;
        }

        .badge-default {
            background: #ff4d4f;
            color: #fff;
            padding: 2px 8px;
            margin-left: 10px;
            border-radius: 6px;
            font-size: 12px;
        }

        .address-actions button {
            background: none;
            border: none;
            color: #555;
            margin-left: 10px;
            cursor: pointer;
        }
        .left, .right { width: 50%; }
        .address-form { display: none; border: 1px solid #ccc; padding: 10px; margin-top: 10px; }
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
                        <img class="product-img"
                             src="${pageContext.request.contextPath}/assets/images/shop_pic/${item.product.image}">
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
                    <span>Địa chỉ giao hàng${size}</span>
                    <button type="button" class="btn-add" onclick="toggleForm()">+ Thêm địa chỉ mới</button>
                </div>

                <div id="addressList" class="address-list">
                    <c:forEach var="addr" items="${addressList}">
                        <c:if test="${addr.defaultt}">
                            <p>${addr.address}, ${addr.ward}, ${addr.district}, ${addr.province}</p>
                        </c:if>
                    </c:forEach>

                    <form method="get" action="addresses">
                        <select name="defaultId" onchange="this.form.submit()">
                            <option value="">Chọn địa chỉ</option>
                            <c:forEach var="addr" items="${addressList}">
                                <c:if test="${!addr.defaultt}">
                                    <option value="${addr.id}">${addr.address}, ${addr.ward}, ${addr.district}, ${addr.province}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </form>
                </div>

<%--                form địa chỉ--%>
                <div class="right">
                    <form id="addressForm" class="address-form" method="post" action="addresses">
                        <label>Tỉnh/Thành:</label>
                        <select id="province" name="province" onchange="populateDistrict()">
                            <c:forEach var="p" items="${['Hà Nội','Hồ Chí Minh']}">
                                <option value="${p}">${p}</option>
                            </c:forEach>
                        </select><br><br>

                        <label>Quận/Huyện:</label>
                        <select id="district" name="district" onchange="populateWard()"></select><br><br>

                        <label>Phường/Xã:</label>
                        <select id="ward" name="ward"></select><br><br>

                        <label>Chi tiết:</label>
                        <input type="text" name="addressDetail" required><br><br>

                        <label>Đặt làm mặc định:</label>
                        <input type="checkbox" name="isDefault"><br><br>

                        <button type="submit">Lưu</button>
                    </form>
                </div>
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

    // Mock dữ liệu tỉnh/quận/phường Việt Nam
    const data = {
        "Hà Nội": {
            "Quận Hoàn Kiếm": ["Phường Hàng Trống", "Phường Hàng Bạc"],
            "Quận Ba Đình": ["Phường Điện Biên", "Phường Trúc Bạch"]
        },
        "Hồ Chí Minh": {
            "Quận 1": ["Phường Bến Nghé", "Phường Tân Định"],
            "Quận 3": ["Phường Võ Thị Sáu", "Phường Nguyễn Thái Bình"]
        }
    };

    function populateDistrict() {
        const province = document.getElementById("province").value;
        const districtSelect = document.getElementById("district");
        districtSelect.innerHTML = "";
        for (let d in data[province]) {
            districtSelect.add(new Option(d, d));
        }
        populateWard();
    }

    function populateWard() {
        const province = document.getElementById("province").value;
        const district = document.getElementById("district").value;
        const wardSelect = document.getElementById("ward");
        wardSelect.innerHTML = "";
        data[province][district].forEach(w => {
            wardSelect.add(new Option(w, w));
        });
    }

    function toggleForm() {
        const form = document.getElementById("addressForm");
        form.style.display = form.style.display === "none" ? "block" : "none";
    }

    <%--function addAddress() {--%>
    <%--    const fullname = document.getElementById("fullname").value;--%>
    <%--    const phone = document.getElementById("phone").value;--%>
    <%--    const address = document.getElementById("address").value;--%>
    <%--    const isDefault = document.getElementById("isDefault").checked;--%>

    <%--    fetch('/user/address/add', {--%>
    <%--        method: 'POST',--%>
    <%--        headers: {'Content-Type': 'application/x-www-form-urlencoded'},--%>
    <%--        body: `fullname=${fullname}&phone=${phone}&address=${address}&isDefault=${isDefault}`--%>
    <%--    }).then(() => location.reload());--%>
    <%--}--%>

    <%--function selectAddress(id, element) {--%>
    <%--    fetch("/user/address/select", {--%>
    <%--        method: "POST",--%>
    <%--        headers: {--%>
    <%--            "Content-Type": "application/x-www-form-urlencoded"--%>
    <%--        },--%>
    <%--        body: "id=" + id--%>
    <%--    })--%>
    <%--        .then(res => res.text())--%>
    <%--        .then(data => {--%>
    <%--            // remove active tất cả--%>
    <%--            document.querySelectorAll('.address-card')--%>
    <%--                .forEach(el => el.classList.remove('active'));--%>

    <%--            // add active cái được chọn--%>
    <%--            element.classList.add('active');--%>
    <%--        });--%>
    <%--}--%>

    <%--function deleteAddress(id) {--%>
    <%--    fetch("/user/address/delete", {--%>
    <%--        method: "POST",--%>
    <%--        headers: {--%>
    <%--            "Content-Type": "application/x-www-form-urlencoded"--%>
    <%--        },--%>
    <%--        body: "id=" + id--%>
    <%--    })--%>
    <%--        .then(res => res.json())--%>
    <%--        .then(data => {--%>
    <%--            renderAddress(data.addressList);--%>
    <%--            showToast("Đã xóa");--%>
    <%--        });--%>
    <%--}--%>

    <%--function renderAddress(list) {--%>
    <%--    let html = "";--%>

    <%--    list.forEach(addr => {--%>
    <%--        html += `--%>
    <%--    <div class="address-card ${addr.isDefault ? 'active' : ''}" onclick="selectAddress(${addr.id})">--%>
    <%--        <div>${addr.address} ${addr.isDefault ? '<span class="badge-default">Mặc định</span>' : ''}</div>--%>
    <%--        <div>--%>
    <%--            <button onclick="event.stopPropagation(); deleteAddress(${addr.id})">Xóa</button>--%>
    <%--        </div>--%>
    <%--    </div>--%>
    <%--    `;--%>
    <%--    });--%>

    <%--    document.getElementById("addressList").innerHTML = html;--%>
    <%--}--%>

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
    //
    // function openAddAddressModal() {
    //     var modal = new bootstrap.Modal(document.getElementById('addAddressModal'));
    //     modal.show();
    // }
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