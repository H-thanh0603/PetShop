<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thanh toán | PetShop</title>

    <!-- FONT -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap"
          rel="stylesheet">

    <!-- BOOTSTRAP -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/checkout.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/storefront-polish.css" rel="stylesheet">
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
    <div class="checkout-steps" aria-label="Tiến trình thanh toán">
        <div class="checkout-step is-done"><span>1</span><strong>Giỏ hàng</strong></div>
        <div class="checkout-step is-active"><span>2</span><strong>Nhận hàng</strong></div>
        <div class="checkout-step"><span>3</span><strong>Thanh toán</strong></div>
        <div class="checkout-step"><span>4</span><strong>Hoàn tất</strong></div>
    </div>
    <div class="row g-4">

        <!-- LEFT PRODUCT LIST -->
        <div class="col-lg-7">
            <div class="card-modern checkout-summary-card">

                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h4>Sản phẩm thanh toán</h4>
                    <div class="countdown">
                        Giữ giá <span id="timer">10:00</span>
                    </div>
                </div>
                <div id="checkoutTimerWarning" class="checkout-timer-warning" role="status" aria-live="polite" hidden>
                    Thời gian giữ giỏ đã hết. Vui lòng kiểm tra lại giỏ hàng trước khi đặt đơn.
                    <a href="${pageContext.request.contextPath}/cart">Quay lại giỏ hàng</a>
                </div>

                <c:forEach var="item" items="${cartItems}">
                    <div class="product-item" data-product-id="${item.product.id}" data-quantity="${item.quantity}">
                        <img loading="lazy" class="product-img"
                             src="${fn:startsWith(item.product.image, 'http') ? fn:escapeXml(item.product.image) : pageContext.request.contextPath += '/assets/images/shop_pic/' += fn:escapeXml(item.product.image)}">
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

                <div class="checkout-total-box">
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
                </div>
                <c:if test="${not empty shippingMessage}">
                    <div class="alert alert-warning mt-2">${fn:escapeXml(shippingMessage)}</div>
                </c:if>
            </div>
        </div>


        <!-- RIGHT USER INFO -->
        <div class="col-lg-5">
            <div class="card-modern checkout-action-card">
                <h4 class="mb-3">Thông tin nhận hàng</h4>
                <div class="checkout-section-intro">
                    Hoàn tất địa chỉ và phương thức thanh toán để shop xử lý đơn nhanh hơn.
                </div>
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






                <div class="checkout-address-current">
                    <div class="section-label">Vị trí giao hàng</div>
                    <c:choose>
                        <c:when test="${not empty defaultAddress}">
                            <div class="current-address-card">
                                <div class="current-address-badge">Đang giao đến</div>
                                <strong>${fn:escapeXml(defaultAddress.address)}, ${fn:escapeXml(defaultAddress.ward)}, ${fn:escapeXml(defaultAddress.district)}, ${fn:escapeXml(defaultAddress.province)}</strong>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="current-address-card is-empty">
                                <strong>Chưa có vị trí giao hàng.</strong>
                                <span>Hãy thêm địa chỉ mới hoặc chọn một địa chỉ bên dưới.</span>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="address-toolbar mt-4">
                    <div>
                        <div class="section-label mb-1">Danh sách địa chỉ</div>
                        <div class="section-hint">Chọn nhanh địa chỉ bạn muốn dùng cho đơn hàng này.</div>
                        <div id="addressApiStatus" class="text-danger small mt-2" role="status" aria-live="polite"></div>
                    </div>
                    <button type="button" class="btn-add" onclick="toggleForm()">+ Thêm địa chỉ mới</button>
                </div>

                <div id="addressList" class="address-list">
                    <c:choose>
                        <c:when test="${not empty addressList}">
                            <c:forEach var="addr" items="${addressList}">
                                <div class="address-item ${selectedAddressId == addr.id ? 'is-selected' : ''}">
                                    <div class="address-item-body">
                                        <c:if test="${selectedAddressId == addr.id}">
                                            <span class="badge bg-success mb-2">Đang dùng</span><br>
                                        </c:if>
                                        <span class="address-text">
                                            ${fn:escapeXml(addr.address)}, ${fn:escapeXml(addr.ward)}, ${fn:escapeXml(addr.district)}, ${fn:escapeXml(addr.province)}
                                        </span>
                                    </div>
                                    <div class="address-actions">
                                        <c:if test="${selectedAddressId != addr.id}">
                                            <form action="${pageContext.request.contextPath}/addresses" method="post" class="d-inline">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                                                <input type="hidden" name="_method" value="patch">
                                                <input type="hidden" name="action" value="setDefault">
                                                <input type="hidden" name="id" value="${addr.id}">
                                                <input type="hidden" name="redirect" value="checkout">
                                                <input type="hidden" name="source" value="checkout">
                                                <button type="submit" class="btn btn-sm btn-outline-primary">Giao đến đây</button>
                                            </form>
                                        </c:if>
                                        <a class="btn btn-sm btn-light" href="${pageContext.request.contextPath}/my-account">Quản lý</a>
                                    </div>
                                </div>

                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p>Chưa có địa chỉ nào. Hãy thêm địa chỉ mới để tiếp tục thanh toán.</p>
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
                        <input type="hidden" name="source" value="checkout">
                        <input type="hidden" name="redirect" value="checkout">
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

                        <div class="text-muted small mb-3">Lưu xong sẽ dùng địa chỉ này cho checkout.</div>

                        <div class="d-flex gap-2">
                            <button type="submit" class="btn btn-primary">Lưu</button>
                            <button type="button" class="btn btn-secondary" onclick="toggleForm(false)">Đóng</button>
                        </div>
                    </form>
                </div>
                <hr>

                <div class="checkout-field-group">
                <label>Ghi chú</label>
                <textarea id="note" name="note" class="form-control mb-3" rows="3" placeholder="Nhập ghi chú cho shop...">${fn:escapeXml(sessionScope.checkoutNote)}</textarea>
                </div>

                <div class="checkout-field-group">
                <label>Mã giảm giá</label>
                <form action="${pageContext.request.contextPath}/checkout" method="post" id="couponForm">
                    <input type="hidden" name="csrfToken" value="${csrfToken}" />
                    <input type="hidden" name="action" value="applyCoupon">
                    <input type="hidden" name="note" id="couponNoteHidden">
                    <div class="input-group mb-3">
                        <input name="couponCode" class="form-control" placeholder="Nhập mã coupon" value="${fn:escapeXml(appliedCouponCode)}">
                        <button type="submit" class="btn btn-primary">Áp dụng</button>
                    </div>
                </form>

                <c:if test="${not empty couponMessage}">
                    <div class="alert alert-info">${fn:escapeXml(couponMessage)}</div>
                </c:if>
                </div>

                <div class="checkout-field-group">
                <label class="mb-2 d-block">Phương thức thanh toán</label>

                <div class="payment-card mb-2">
                    <label>
                        <input type="radio" name="payment" value="cod" checked>
                        <span>
                            <strong>Thanh toán khi nhận hàng</strong>
                            <small>Thanh toán trực tiếp cho đơn vị giao hàng.</small>
                        </span>
                    </label>
                </div>

                <div class="payment-card mb-2">
                    <label>
                        <input type="radio" name="payment" value="momo">
                        <span>
                            <strong>Ví điện tử MoMo</strong>
                            <small>Demo thanh toán ví điện tử.</small>
                        </span>
                    </label>
                </div>

                <div class="payment-card mb-3" id="bankCard">
                    <label style="width:100%; cursor:pointer;">
                        <input type="radio" name="payment" value="bank_transfer">
                        <span>
                            <strong>Chuyển khoản ngân hàng</strong>
                            <small>Quét QR và chuyển khoản đúng nội dung.</small>
                        </span>
                    </label>
                    <div id="bankInfo">
                        <div class="bank-qr-wrapper">
                            <img id="bankQrImg" src="" alt="QR Chuyển khoản" hidden>
                            <div class="bank-qr-note" id="bankQrNote">Chọn chuyển khoản để hiển thị mã QR thanh toán.</div>
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
                </div>

                <input type="hidden" id="checkoutCsrfToken" value="${fn:escapeXml(csrfToken)}">
                <div class="checkout-submit-box">
                    <button type="button" id="btnCheckout" class="btn-checkout">Đặt hàng</button>
                    <div class="checkout-safe-note">
                        Shop chỉ xác nhận đơn khi thông tin nhận hàng hợp lệ. Bạn có thể theo dõi trạng thái trong “Đơn hàng của tôi”.
                    </div>
                </div>

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
     data-bank-transfer-prefix="${fn:escapeXml(bankTransferPrefix)}"
     data-bank-transfer-reference="${fn:escapeXml(bankTransferReference)}"
     data-bank-payment-ttl-seconds="${fn:escapeXml(bankPaymentTtlSeconds)}"></div>
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
