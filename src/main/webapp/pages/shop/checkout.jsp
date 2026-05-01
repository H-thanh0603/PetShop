<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<head>
    <title>Thanh toán | PetShop</title>

    <!-- FONT -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap"
          rel="stylesheet">

    <!-- BOOTSTRAP -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/checkout.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</head>
<body>
<jsp:include page="/components/navbar.jsp"/>
<jsp:include page="/components/toast.jsp"/>
<div class="checkout-container">
    <div class="checkout-header">
        <div>
            <h1>Thanh toán đơn hàng</h1>
            <p>Kiểm tra địa chỉ, mã giảm giá và phương thức thanh toán trước khi xác nhận.</p>
        </div>
        <a class="checkout-back-link" href="${pageContext.request.contextPath}/cart">
            ← Quay lại giỏ hàng
        </a>
    </div>
    <div class="row g-4">

        <!-- LEFT PRODUCT LIST -->
        <div class="col-lg-7">
            <div class="card-modern">

                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h4>🛒 Sản phẩm thanh toán</h4>
                </div>

                <c:forEach var="item" items="${cartItems}">
                    <div class="product-item" data-product-id="${item.product.id}" data-quantity="${item.quantity}">
                        <img class="product-img"
                             src="${pageContext.request.contextPath}/assets/images/shop_pic/${fn:escapeXml(item.product.image)}">
                        <div class="flex-grow-1">
                            <div class="fw-semibold">${fn:escapeXml(item.product.name)}</div>
                            <div class="text-muted">${fn:escapeXml(item.product.price)}₫</div>
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
                    <span>${totalAmount} ₫</span>
                </div>

                <div class="info-row">
                    <span>Phí ship</span>
                    <span>
                        <c:choose>
                            <c:when test="${shippingFee == 0}">
                                Freeship
                            </c:when>
                            <c:otherwise>
                                ${shippingFee} ₫
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>

                <div class="info-row">
                    <span>Giảm giá</span>
                    <span class="text-success">${discount} ₫</span>
                </div>

                <hr>

                <div class="info-row fs-5 fw-bold text-primary">
                    <span>Tổng thanh toán</span>
                    <span>${finalTotal} ₫</span>
                </div>
                <c:if test="${not empty shippingMessage}">
                    <div class="alert alert-warning mt-2">${fn:escapeXml(shippingMessage)}</div>
                </c:if>
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
                            <c:otherwise>${fn:escapeXml(user.fullname)}
                                <span class="edit-btn" data-bs-toggle="modal" data-bs-target="#profileModal">Thay đổi</span>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
                <div class="info-row">
                    <span>Email</span>
                    <span>${fn:escapeXml(user.email)}</span>
                </div>
                <div class="info-row">
                    <span>SĐT</span>
                    <span>
                        <c:choose>
                            <c:when test="${empty user.phone}">
                                <span class="missing">Chưa cập nhật</span>
                                <span class="edit-btn" data-bs-toggle="modal" data-bs-target="#profileModal">Thêm thông tin</span>
                            </c:when>
                            <c:otherwise>${fn:escapeXml(user.phone)}
                                <span class="edit-btn" data-bs-toggle="modal" data-bs-target="#profileModal">Thay đổi</span>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>






                <div class="info-row">
                    <span>Địa chỉ giao hàng</span>
                    <button type="button" class="btn-add" onclick="toggleForm()">+ Thêm địa chỉ mới</button>
                </div>
                <div class="info-row">
                    <span><strong>Danh sách địa chỉ</strong></span>
                </div>

                <div class="right">
                    <form id="editAddressForm"
                          class="address-form"
                          method="post"
                          action="${pageContext.request.contextPath}/addresses"
                          style="display:none;"
                          onsubmit="return validateEditAddressForm();">

                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                        <input type="hidden" name="_method" value="put">
                        <input type="hidden" id="editAddressId" name="id">
                        <span>
                            <strong>Sửa Thông Tin</strong>
                        </span>
                        <div class="mb-3">
                            <label for="editProvince" class="form-label">Tỉnh/Thành:</label>
                            <select id="editProvince" name="province" class="form-select" required>
                                <option value="">-- Chọn tỉnh/thành --</option>
                            </select>
                            <div class="text-danger small mt-1" id="editProvinceError"></div>
                        </div>

                        <div class="mb-3">
                            <label for="editDistrict" class="form-label">Quận/Huyện:</label>
                            <select id="editDistrict" name="district" class="form-select" required disabled>
                                <option value="">-- Chọn quận/huyện --</option>
                            </select>
                            <div class="text-danger small mt-1" id="editDistrictError"></div>
                        </div>

                        <div class="mb-3">
                            <label for="editWard" class="form-label">Phường/Xã:</label>
                            <select id="editWard" name="ward" class="form-select" required disabled>
                                <option value="">-- Chọn phường/xã --</option>
                            </select>
                            <div class="text-danger small mt-1" id="editWardError"></div>
                        </div>
                        <div class="mb-3">
                            <label for="editAddressDetail" class="form-label">Chi tiết:</label>
                            <input type="text"
                                   id="editAddressDetail"
                                   name="addressDetail"
                                   class="form-control"
                                   maxlength="255"
                                   placeholder="Số nhà, tên đường..."
                                   autocomplete="address-line1"
                                   required
                                   title="Địa chỉ chỉ được chứa chữ, số, khoảng trắng, dấu phẩy, chấm, gạch ngang, gạch chéo và phải có ý nghĩa.">
                            <div class="text-danger small mt-1" id="editAddressDetailError"></div>
                        </div>
                        <div class="form-check mb-3">
                            <input type="checkbox" id="editIsDefault" name="isDefault" value="true" class="form-check-input">
                            <label for="editIsDefault" class="form-check-label">Đặt làm mặc định</label>
                        </div>

                        <div class="d-flex gap-2">
                            <button type="submit" class="btn btn-primary">Cập nhật</button>
                            <button type="button" class="btn btn-danger" onclick="confirmDeleteAddress()">Xóa</button>
                            <button type="button" class="btn btn-secondary" onclick="toggleEditForm(false)">Đóng</button>
                        </div>
                    </form>

                    <form id="deleteAddressForm"
                          method="post"
                          action="${pageContext.request.contextPath}/addresses"
                          style="display:none;">
                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                        <input type="hidden" name="_method" value="delete">
                        <input type="hidden" id="deleteAddressId" name="id">
                    </form>

                    <div id="deleteConfirmModal" class="delete-modal" style="display:none;">
                        <div class="delete-modal-content">
                            <h4>Xác nhận xóa</h4>
                            <p>Bạn đã chắc chắn muốn xóa địa chỉ này chưa?</p>
                            <div class="delete-modal-actions">
                                <button type="button" class="btn btn-danger" onclick="deleteAddressNow()">Rồi</button>
                                <button type="button" class="btn btn-secondary" onclick="closeDeleteConfirm()">Chưa</button>
                            </div>
                        </div>
                    </div>
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
                                        ${fn:escapeXml(addr.address)}, ${fn:escapeXml(addr.ward)}, ${fn:escapeXml(addr.district)}, ${fn:escapeXml(addr.province)}
                                    </span>
                                    <button type="button"
                                            class="btn btn-secondary"
                                            onclick="openEditAddress(
                                                    '${addr.id}',
                                                    '${addr.province}',
                                                    '${addr.district}',
                                                    '${addr.ward}',
                                                    '${addr.address}',
                                                    '${addr.defaultt}'
                                                    )">
                                        Sửa
                                    </button>
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
                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                        <span>
                        <strong>Thêm Địa Chỉ</strong>
                        </span>
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
                                   autocomplete="address-line1"
                                   required
                                   title="Địa chỉ chỉ được chứa chữ, số, khoảng trắng, dấu phẩy, chấm, gạch ngang, gạch chéo và phải có ý nghĩa.">
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
                <textarea id="note" name="note" class="form-control mb-3" rows="3" placeholder="Nhập ghi chú cho shop...">${fn:escapeXml(sessionScope.checkoutNote)}</textarea>

                <label>🎟 Mã giảm giá</label>
                <form action="${pageContext.request.contextPath}/checkout" method="post" id="couponForm">
                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                    <input type="hidden" name="action" value="applyCoupon">
                    <input type="hidden" name="note" id="couponNoteHidden">
                    <input type="hidden" name="paymentMethod" id="couponPaymentMethodHidden" value="${checkoutPaymentMethod}">
                    <div class="input-group mb-3">
                        <input name="couponCode" class="form-control" placeholder="Nhập mã coupon" value="${fn:escapeXml(appliedCouponCode)}">
                        <button type="submit" class="btn btn-primary">Áp dụng</button>
                    </div>
                </form>

                <c:if test="${not empty couponMessage}">
                    <div class="alert alert-info">${fn:escapeXml(couponMessage)}</div>
                </c:if>

                <label class="mb-2 d-block">💳 Phương thức thanh toán</label>

                <div class="payment-card mb-2">
                    <label>
                        <input type="radio" name="payment" value="cod"
                        ${checkoutPaymentMethod == 'COD' ? 'checked' : ''}>
                        Thanh toán khi nhận hàng (COD)
                    </label>
                </div>

                <div class="payment-card mb-2">
                    <label>
                        <input type="radio" name="payment" value="momo">
                        Ví điện tử MoMo <span style="font-size: 0.75rem; color: #94a3b8; font-style: italic;">(Demo)</span>
                    </label>
                </div>

                <div class="payment-card mb-3" id="bankCard">
                    <label style="width:100%; cursor:pointer;">
                        <input type="radio" name="payment" value="bank_transfer">
                        🏦 Chuyển khoản ngân hàng
                    </label>
                    <div id="bankInfo">
                        <div class="bank-qr-wrapper">
                            <img id="bankQrImg" src="" alt="QR Chuyển khoản">
                            <div class="bank-qr-note">Quét mã QR để chuyển khoản</div>
                        </div>
                        <div><b>Ngân hàng:</b> <span id="bankDisplayName">${fn:escapeXml(bankDisplayName)}</span></div>
                        <div><b>Số tài khoản:</b> <span id="bankAccountNumber" class="bank-account-number">${fn:escapeXml(bankAccountNumber)}</span></div>
                        <div><b>Chủ tài khoản:</b> <span id="bankAccountName">${fn:escapeXml(bankAccountName)}</span></div>
                        <div><b>Số tiền:</b> <span id="bankAmount"></span></div>
                        <div><b>Nội dung CK:</b> <span id="bankContent"></span></div>
                        <div class="bank-warning">
                            ⚠️ Vui lòng chuyển khoản đúng số tiền và nội dung. Đơn hàng sẽ được xác nhận sau khi nhận được thanh toán.
                        </div>
                    </div>
                </div>

                <input type="hidden" id="checkoutCsrfToken" value="${fn:escapeXml(csrfToken)}">
                <button type="button" id="btnCheckout" class="btn-checkout">Đặt hàng</button>

                <div id="paymentResult" class="mt-3"></div>

            </div>
        </div>

    </div>
</div>
<div id="checkoutConfig"
     hidden
     data-context-path="${fn:escapeXml(pageContext.request.contextPath)}"
     data-csrf-token="${fn:escapeXml(csrfToken)}"
     data-provinces-api-base-url="${fn:escapeXml(provincesApiBaseUrl)}"
     data-bank-id="${fn:escapeXml(bankId)}"
     data-bank-display-name="${fn:escapeXml(bankDisplayName)}"
     data-bank-account-number="${fn:escapeXml(bankAccountNumber)}"
     data-bank-account-name="${fn:escapeXml(bankAccountName)}"
     data-bank-transfer-prefix="${fn:escapeXml(bankTransferPrefix)}"></div>
<script src="${pageContext.request.contextPath}/assets/js/checkout.js"></script>
<jsp:include page="/components/footer.jsp"/>
<%--        modal cập nhật thông tin--%>
<div class="modal fade" id="profileModal">
    <div class="modal-dialog">
        <div class="modal-content" style="border-radius:20px">

            <form action="${pageContext.request.contextPath}/update-profile-checkout" method="post"
                  onsubmit="return validateForm()">
                <input type="hidden" name="csrfToken" value="${csrfToken}" />

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
