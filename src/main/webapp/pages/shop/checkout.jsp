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


        .left, .right { width: 100%; }
        .address-form { display: none; border: 1px solid #ccc; padding: 10px; margin-top: 10px; }
        .address-section {
            background: #fff;
            padding: 20px;
            border-radius: 16px;
            margin-bottom: 20px;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
        }

        .address-list {
            display: flex;
            flex-direction: column;
            gap: 14px;
            margin-top: 12px;
        }

        .address-item {
            display: flex;
            align-items: flex-start;
            gap: 10px;
            padding: 14px 16px;
            border: 1px solid #e9ecf5;
            border-radius: 14px;
            background: #fcfcff;
            transition: all 0.25s ease;
            line-height: 1.6;
        }

        .address-item:hover {
            border-color: #4a6cf7;
            background: #f7f9ff;
            transform: translateY(-1px);
        }

        .address-item strong {
            flex-shrink: 0;
            font-size: 12px;
            font-weight: 600;
            color: #fff;
            background: linear-gradient(45deg, #4a6cf7, #6f8cff);
            padding: 4px 10px;
            border-radius: 999px;
            min-width: fit-content;
        }

        .address-item span {
            color: #2f3542;
            font-size: 14px;
            word-break: break-word;
        }

        .address-list p {
            margin: 0;
            color: #888;
            font-style: italic;
        }

        .btn-add {
            background: linear-gradient(45deg, #ff4d4f, #ff7875);
            color: #fff;
            border: none;
            padding: 8px 12px;
            border-radius: 10px;
            cursor: pointer;
            font-size: 13px;
            font-weight: 500;
            transition: 0.25s ease;
        }

        .btn-add:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 16px rgba(255, 77, 79, 0.25);
        }
        .address-form {
            background: #ffffff;
            padding: 20px;
            border-radius: 16px;
            margin-top: 16px;
            border: 1px solid #e9ecf5;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
            max-width: 500px;
            animation: fadeIn 0.3s ease;
        }

        /* Label */
        .address-form .form-label {
            font-size: 13px;
            font-weight: 600;
            color: #2f3542;
            margin-bottom: 6px;
        }

        /* Input + Select */
        .address-form .form-control,
        .address-form .form-select {
            width: 100%;
            padding: 10px 12px;
            border-radius: 10px;
            border: 1px solid #dcdfe6;
            font-size: 14px;
            transition: all 0.2s ease;
            background: #fff;
        }

        /* Focus effect */
        .address-form .form-control:focus,
        .address-form .form-select:focus {
            border-color: #4a6cf7;
            box-shadow: 0 0 0 3px rgba(74, 108, 247, 0.15);
            outline: none;
        }

        /* Disabled select */
        .address-form select:disabled {
            background: #f5f6fa;
            cursor: not-allowed;
        }

        /* Error message */
        .address-form .text-danger {
            color: #ff4d4f;
            font-size: 12px;
            margin-top: 4px;
        }

        /* Checkbox */
        .address-form .form-check {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .address-form .form-check-input {
            width: 16px;
            height: 16px;
            cursor: pointer;
        }

        .address-form .form-check-label {
            font-size: 13px;
            color: #444;
            cursor: pointer;
        }

        /* Button group */
        .address-form .d-flex {
            display: flex;
            gap: 10px;
            margin-top: 10px;
        }

        /* Save button */
        .address-form .btn-primary {
            background: linear-gradient(45deg, #4a6cf7, #6f8cff);
            border: none;
            color: #fff;
            padding: 10px 16px;
            border-radius: 10px;
            font-size: 14px;
            cursor: pointer;
            transition: 0.25s ease;
        }

        .address-form .btn-primary:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 16px rgba(74, 108, 247, 0.3);
        }

        /* Cancel button */
        .address-form .btn-secondary {
            background: #f1f3f9;
            border: none;
            color: #555;
            padding: 10px 16px;
            border-radius: 10px;
            font-size: 14px;
            cursor: pointer;
            transition: 0.25s ease;
        }

        .address-form .btn-secondary:hover {
            background: #e4e7f2;
        }

        /* spacing */
        .mb-3 {
            margin-bottom: 14px;
        }

        /* animation */
        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(8px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
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
                    <span>Địa chỉ giao hàng</span>
                    <button type="button" class="btn-add" onclick="toggleForm()">+ Thêm địa chỉ mới</button>
                </div>

                <div id="addressList" class="address-list">
                    <c:choose>
                        <c:when test="${not empty addressList}">
                            <c:forEach var="addr" items="${addressList}">
                                <div class="address-item">
                                    <c:if test="${addr.defaultt}">
                                        <strong>Mặc định</strong>
                                    </c:if>
                                    <span>
                        ${addr.address}, ${addr.ward}, ${addr.district}, ${addr.province}
                    </span>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p>Chưa có địa chỉ nào.</p>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="right">
                    <form id="addressForm"
                          class="address-form"
                          method="post"
                          action="${pageContext.request.contextPath}/addresses"
                          style="display:none;"
                          onsubmit="return validateAddressForm();">

                        <div class="mb-3">
                            <label for="province" class="form-label">Tỉnh/Thành:</label>
                            <select id="province" name="province" class="form-select" required>
                                <option value="">-- Chọn tỉnh/thành --</option>
                            </select>
                            <div class="text-danger small mt-1" id="provinceError"></div>
                        </div>

                        <div class="mb-3">
                            <label for="district" class="form-label">Quận/Huyện:</label>
                            <select id="district" name="district" class="form-select" required disabled>
                                <option value="">-- Chọn quận/huyện --</option>
                            </select>
                            <div class="text-danger small mt-1" id="districtError"></div>
                        </div>

                        <div class="mb-3">
                            <label for="ward" class="form-label">Phường/Xã:</label>
                            <select id="ward" name="ward" class="form-select" required disabled>
                                <option value="">-- Chọn phường/xã --</option>
                            </select>
                            <div class="text-danger small mt-1" id="wardError"></div>
                        </div>

                        <div class="mb-3">
                            <label for="addressDetail" class="form-label">Chi tiết:</label>
                            <input type="text"
                                   id="addressDetail"
                                   name="addressDetail"
                                   class="form-control"
                                   maxlength="255"
                                   placeholder="Số nhà, tên đường..."
                                   required>
                            <div class="text-danger small mt-1" id="addressDetailError"></div>
                        </div>

                        <div class="form-check mb-3">
                            <input type="checkbox" id="isDefault" name="isDefault" value="true" class="form-check-input">
                            <label for="isDefault" class="form-check-label">Đặt làm mặc định</label>
                        </div>

                        <div class="d-flex gap-2">
                            <button type="submit" class="btn btn-primary">Lưu</button>
                            <button type="button" class="btn btn-secondary" onclick="toggleForm(false)">Đóng</button>
                        </div>
                    </form>
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


        // Dùng v1 để khớp cấu trúc tỉnh -> quận/huyện -> phường/xã
        const API_BASE = "https://provinces.open-api.vn/api/v1";

        let provincesLoaded = false;

        function clearAddressErrors() {
        document.getElementById("provinceError").textContent = "";
        document.getElementById("districtError").textContent = "";
        document.getElementById("wardError").textContent = "";
        document.getElementById("addressDetailError").textContent = "";
    }

        function resetAddressFormOptions() {
        const provinceSelect = document.getElementById("province");
        const districtSelect = document.getElementById("district");
        const wardSelect = document.getElementById("ward");

        if (!provinceSelect || !districtSelect || !wardSelect) return;

        districtSelect.innerHTML = '<option value="">-- Chọn quận/huyện --</option>';
        wardSelect.innerHTML = '<option value="">-- Chọn phường/xã --</option>';
        districtSelect.disabled = true;
        wardSelect.disabled = true;
    }

        async function toggleForm(forceShow) {
        const form = document.getElementById("addressForm");
        if (!form) return;

        const shouldShow = (typeof forceShow === "boolean")
        ? forceShow
        : (form.style.display === "none" || form.style.display === "");

        form.style.display = shouldShow ? "block" : "none";

        if (shouldShow && !provincesLoaded) {
        await loadProvinces();
    }
    }

        async function loadProvinces() {
        const provinceSelect = document.getElementById("province");
        if (!provinceSelect) return;

        provinceSelect.disabled = true;
        provinceSelect.innerHTML = '<option value="">Đang tải tỉnh/thành...</option>';
        resetAddressFormOptions();

        try {
        const res = await fetch(API_BASE + "/p/");
        if (!res.ok) {
        throw new Error("HTTP " + res.status);
    }

        const provinces = await res.json();

        provinceSelect.innerHTML = '<option value="">-- Chọn tỉnh/thành --</option>';

        provinces.forEach(function (p) {
        const option = document.createElement("option");
        option.value = p.name;
        option.textContent = p.name;
        option.dataset.code = p.code;
        provinceSelect.appendChild(option);
    });

        provinceSelect.disabled = false;
        provincesLoaded = true;
    } catch (e) {
        console.error("Lỗi load tỉnh/thành:", e);
        provinceSelect.innerHTML = '<option value="">Không tải được tỉnh/thành</option>';
        alert("Không tải được danh sách tỉnh/thành. Vui lòng thử lại.");
    }
    }

        async function loadDistricts(provinceCode) {
        const districtSelect = document.getElementById("district");
        const wardSelect = document.getElementById("ward");

        districtSelect.disabled = true;
        wardSelect.disabled = true;
        districtSelect.innerHTML = '<option value="">Đang tải quận/huyện...</option>';
        wardSelect.innerHTML = '<option value="">-- Chọn phường/xã --</option>';

        if (!provinceCode) {
        districtSelect.innerHTML = '<option value="">-- Chọn quận/huyện --</option>';
        return;
    }

        try {
        const res = await fetch(API_BASE + "/p/" + provinceCode + "?depth=2");
        if (!res.ok) {
        throw new Error("HTTP " + res.status);
    }

        const province = await res.json();
        districtSelect.innerHTML = '<option value="">-- Chọn quận/huyện --</option>';

        if (province.districts && province.districts.length > 0) {
        province.districts.forEach(function (d) {
        const option = document.createElement("option");
        option.value = d.name;
        option.textContent = d.name;
        option.dataset.code = d.code;
        districtSelect.appendChild(option);
    });
        districtSelect.disabled = false;
    } else {
        districtSelect.innerHTML = '<option value="">Không có quận/huyện</option>';
    }
    } catch (e) {
        console.error("Lỗi load quận/huyện:", e);
        districtSelect.innerHTML = '<option value="">Không tải được quận/huyện</option>';
        alert("Không tải được danh sách quận/huyện. Vui lòng thử lại.");
    }
    }

        async function loadWards(districtCode) {
        const wardSelect = document.getElementById("ward");
        wardSelect.disabled = true;
        wardSelect.innerHTML = '<option value="">Đang tải phường/xã...</option>';

        if (!districtCode) {
        wardSelect.innerHTML = '<option value="">-- Chọn phường/xã --</option>';
        return;
    }

        try {
        const res = await fetch(API_BASE + "/d/" + districtCode + "?depth=2");
        if (!res.ok) {
        throw new Error("HTTP " + res.status);
    }

        const district = await res.json();
        wardSelect.innerHTML = '<option value="">-- Chọn phường/xã --</option>';

        if (district.wards && district.wards.length > 0) {
        district.wards.forEach(function (w) {
        const option = document.createElement("option");
        option.value = w.name;
        option.textContent = w.name;
        option.dataset.code = w.code;
        wardSelect.appendChild(option);
    });
        wardSelect.disabled = false;
    } else {
        wardSelect.innerHTML = '<option value="">Không có phường/xã</option>';
    }
    } catch (e) {
        console.error("Lỗi load phường/xã:", e);
        wardSelect.innerHTML = '<option value="">Không tải được phường/xã</option>';
        alert("Không tải được danh sách phường/xã. Vui lòng thử lại.");
    }
    }

        function validateAddressForm() {
        clearAddressErrors();

        const province = document.getElementById("province").value.trim();
        const district = document.getElementById("district").value.trim();
        const ward = document.getElementById("ward").value.trim();
        const addressDetail = document.getElementById("addressDetail").value.trim();

        let isValid = true;

        if (!province) {
        document.getElementById("provinceError").textContent = "Vui lòng chọn tỉnh/thành.";
        isValid = false;
    }

        if (!district) {
        document.getElementById("districtError").textContent = "Vui lòng chọn quận/huyện.";
        isValid = false;
    }

        if (!ward) {
        document.getElementById("wardError").textContent = "Vui lòng chọn phường/xã.";
        isValid = false;
    }

        if (!addressDetail) {
        document.getElementById("addressDetailError").textContent = "Vui lòng nhập địa chỉ chi tiết.";
        isValid = false;
    } else if (addressDetail.length < 5) {
        document.getElementById("addressDetailError").textContent = "Địa chỉ chi tiết phải có ít nhất 5 ký tự.";
        isValid = false;
    } else if (addressDetail.length > 255) {
        document.getElementById("addressDetailError").textContent = "Địa chỉ chi tiết không được vượt quá 255 ký tự.";
        isValid = false;
    }

        return isValid;
    }

        document.addEventListener("DOMContentLoaded", function () {
        const provinceSelect = document.getElementById("province");
        const districtSelect = document.getElementById("district");

        if (provinceSelect) {
        provinceSelect.addEventListener("change", function () {
        clearAddressErrors();
        const selected = this.options[this.selectedIndex];
        const provinceCode = selected ? selected.dataset.code : "";
        loadDistricts(provinceCode);
    });
    }

        if (districtSelect) {
        districtSelect.addEventListener("change", function () {
        clearAddressErrors();
        const selected = this.options[this.selectedIndex];
        const districtCode = selected ? selected.dataset.code : "";
        loadWards(districtCode);
    });
    }
    });


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