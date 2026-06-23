<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tài khoản của tôi - PetShop</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <style>
        body { background: #f8fafc; font-family: 'Segoe UI', sans-serif; }
        .account-wrap { max-width: 1180px; margin: 32px auto 72px; }
        .hero { background: linear-gradient(135deg, #0ea5e9, #2563eb); color: #fff; border-radius: 24px; padding: 28px; margin-bottom: 24px; box-shadow: 0 18px 40px rgba(37, 99, 235, 0.18); }
        .hero-stat { background: rgba(255,255,255,.14); border: 1px solid rgba(255,255,255,.18); border-radius: 16px; padding: 14px; }
        .panel { background: #fff; border-radius: 20px; padding: 22px; box-shadow: 0 8px 24px rgba(15,23,42,.06); margin-bottom: 24px; }
        .panel-title { font-size: 1.1rem; font-weight: 800; margin-bottom: 18px; display: flex; align-items: center; gap: 8px; }
        .address-card { border: 1px solid #e2e8f0; border-radius: 16px; padding: 16px; margin-bottom: 12px; background: #fcfdff; }
        .address-card.default { border-color: #93c5fd; background: #eff6ff; }
        .badge-soft { display: inline-flex; align-items: center; gap: 6px; border-radius: 999px; padding: 6px 12px; font-size: .82rem; font-weight: 700; }
        .badge-default { background: #dbeafe; color: #1d4ed8; }
        .order-item { border: 1px solid #e2e8f0; border-radius: 16px; padding: 14px; margin-bottom: 12px; }
        .status-pill { border-radius: 999px; padding: 6px 12px; font-size: .82rem; font-weight: 700; }
        .status-pending { background: #fef3c7; color: #92400e; }
        .status-confirmed { background: #dbeafe; color: #1d4ed8; }
        .status-shipping { background: #e0e7ff; color: #4338ca; }
        .status-completed { background: #dcfce7; color: #166534; }
        .status-cancelled { background: #fee2e2; color: #991b1b; }
        .sig-order-item { border: 1px solid #e2e8f0; border-radius: 16px; padding: 16px; margin-bottom: 12px; background: #fcfdff; }
        .sig-order-item.verified { border-color: #86efac; background: #f0fdf4; }
        .sig-order-item.failed { border-color: #fca5a5; background: #fef2f2; }
        .sig-order-item.pending { border-color: #fcd34d; background: #fffbeb; }
        .sig-status { border-radius: 999px; padding: 4px 10px; font-size: .78rem; font-weight: 700; }
        .sig-status.verified { background: #dcfce7; color: #166534; }
        .sig-status.failed { background: #fee2e2; color: #991b1b; }
        .sig-status.pending { background: #fef3c7; color: #92400e; }
        .sig-hash { font-family: monospace; font-size: .78rem; color: #64748b; word-break: break-all; }
        .sig-result { border-radius: 12px; padding: 10px 14px; margin-top: 10px; font-size: .85rem; font-weight: 600; display: none; }
        .sig-result.success { background: #dcfce7; color: #166534; display: block; }
        .sig-result.error { background: #fee2e2; color: #991b1b; display: block; }
        .avatar-circle { width: 56px; height: 56px; border-radius: 50%; background: rgba(255,255,255,.2); border: 2px solid rgba(255,255,255,.4); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
        .avatar-circle span { font-size: 1.5rem; font-weight: 800; color: #fff; text-transform: uppercase; }
    </style>
    <link href="${pageContext.request.contextPath}/assets/css/storefront-polish.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/components/navbar.jsp" />
<jsp:include page="/components/toast.jsp" />

<div class="container account-wrap">
    <nav aria-label="breadcrumb" class="mb-3">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/home">Trang chủ</a></li>
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/shop">Cửa hàng</a></li>
            <li class="breadcrumb-item active" aria-current="page">Tài khoản của tôi</li>
        </ol>
    </nav>
    <div class="hero">
        <div class="row g-3 align-items-center">
            <div class="col-lg-7">
                <div class="d-flex align-items-center gap-3 mb-2">
                    <div class="avatar-circle">
                        <span>${fn:substring(sessionScope.user.fullname, 0, 1)}</span>
                    </div>
                    <div>
                        <h2 class="fw-bold mb-0">${fn:escapeXml(sessionScope.user.fullname)}</h2>
                        <div class="opacity-75 small">@${fn:escapeXml(sessionScope.user.username)} · Thành viên từ <fmt:formatDate value="${memberSince}" pattern="MM/yyyy"/></div>
                    </div>
                </div>
            </div>
            <div class="col-lg-5">
                <div class="row g-2">
                    <div class="col-3"><div class="hero-stat"><div class="small opacity-75">Tổng đơn</div><div class="fs-5 fw-bold">${totalOrders}</div></div></div>
                    <div class="col-3"><div class="hero-stat"><div class="small opacity-75">Đang xử lý</div><div class="fs-5 fw-bold">${countPending}</div></div></div>
                    <div class="col-3"><div class="hero-stat"><div class="small opacity-75">Hoàn thành</div><div class="fs-5 fw-bold">${countCompleted}</div></div></div>
                    <div class="col-3"><div class="hero-stat"><div class="small opacity-75">Chi tiêu</div><div class="fs-6 fw-bold"><fmt:formatNumber value="${totalSpent}" type="number" maxFractionDigits="0"/>đ</div></div></div>
                </div>
            </div>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-lg-5">
            <div class="panel">
                <div class="panel-title"><i class='bx bx-id-card'></i> Hồ sơ cá nhân</div>
                <c:if test="${not empty error}">
                    <div class="alert alert-danger mb-3" style="border-radius:10px;">${fn:escapeXml(error)}</div>
                </c:if>
                <c:if test="${not empty success}">
                    <div class="alert alert-success mb-3" style="border-radius:10px;">${fn:escapeXml(success)}</div>
                </c:if>
                <form action="${pageContext.request.contextPath}/my-account" method="post" class="row g-3">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <div class="col-12">
                        <label class="form-label">Họ và tên</label>
                        <input class="form-control" name="fullname" value="${fn:escapeXml(sessionScope.user.fullname)}" required>
                    </div>
                    <div class="col-12">
                        <label class="form-label">Email</label>
                        <input class="form-control" type="email" name="email" value="${fn:escapeXml(sessionScope.user.email)}" required>
                    </div>
                    <div class="col-12">
                        <label class="form-label">Số điện thoại</label>
                        <input class="form-control" name="phone" value="${fn:escapeXml(sessionScope.user.phone)}" placeholder="Nhập số điện thoại">
                    </div>
                    <div class="col-12">
                        <label class="form-label">Tên đăng nhập</label>
                        <input class="form-control" value="${fn:escapeXml(sessionScope.user.username)}" disabled>
                    </div>
                    <div class="col-12 d-grid">
                        <button class="btn btn-primary" type="submit"><i class='bx bx-save'></i> Lưu thay đổi</button>
                    </div>
                </form>
            </div>

            <div class="panel">
                <div class="panel-title"><i class='bx bx-lock-alt'></i> Bảo mật tài khoản</div>

                <%-- Change password form --%>
                <c:if test="${not empty pwError}">
                    <div class="alert alert-danger mb-3" style="border-radius:10px;">${fn:escapeXml(pwError)}</div>
                </c:if>
                <c:if test="${not empty pwSuccess}">
                    <div class="alert alert-success mb-3" style="border-radius:10px;">${fn:escapeXml(pwSuccess)}</div>
                </c:if>

                <form action="${pageContext.request.contextPath}/my-account" method="post" class="row g-3">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="action" value="changePassword">
                    <div class="col-12">
                        <label class="form-label fw-semibold">Mật khẩu hiện tại</label>
                        <input class="form-control" type="password" name="currentPassword" required placeholder="Nhập mật khẩu hiện tại">
                    </div>
                    <div class="col-12">
                        <label class="form-label fw-semibold">Mật khẩu mới</label>
                        <input class="form-control" type="password" name="newPassword" required placeholder="Tối thiểu 8 ký tự, hoa/thường/số/đặc biệt">
                    </div>
                    <div class="col-12">
                        <label class="form-label fw-semibold">Xác nhận mật khẩu mới</label>
                        <input class="form-control" type="password" name="confirmPassword" required placeholder="Nhập lại mật khẩu mới">
                    </div>
                    <div class="col-12 d-grid">
                        <button class="btn btn-outline-primary" type="submit"><i class='bx bx-lock-open-alt'></i> Đổi mật khẩu</button>
                    </div>
                </form>

                <hr class="my-3">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                    <div>
                        <div class="fw-semibold">Đặt lại qua email</div>
                        <div class="text-muted small">Đổi mật khẩu qua OTP email nếu quên mật khẩu hiện tại.</div>
                    </div>
                    <a href="${pageContext.request.contextPath}/forgot-password" class="btn btn-outline-secondary btn-sm">Quên mật khẩu?</a>
                </div>
            </div>
        </div>

        <div class="col-lg-7">
            <div class="panel">
                <div class="panel-title"><i class='bx bx-map'></i> Địa chỉ giao hàng</div>

                <c:forEach items="${addressList}" var="addr">
                    <div class="address-card ${addr.defaultt ? 'default' : ''}">
                        <div class="d-flex justify-content-between align-items-start gap-2">
                            <div>
                                <div class="fw-bold">${fn:escapeXml(addr.address)}, ${fn:escapeXml(addr.ward)}, ${fn:escapeXml(addr.district)}, ${fn:escapeXml(addr.province)}</div>
                                <div class="text-muted small mt-1">Cập nhật: <fmt:formatDate value="${addr.createAt}" pattern="dd/MM/yyyy HH:mm"/></div>
                            </div>
                            <c:if test="${addr.defaultt}">
                                <span class="badge-soft badge-default"><i class='bx bx-check-circle'></i> Mặc định</span>
                            </c:if>
                        </div>
                        <div class="d-flex gap-2 mt-3 flex-wrap">
                            <c:if test="${not addr.defaultt}">
                                <form action="${pageContext.request.contextPath}/addresses" method="post">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="_method" value="patch">
                                    <input type="hidden" name="action" value="setDefault">
                                    <input type="hidden" name="id" value="${addr.id}">
                                    <input type="hidden" name="redirect" value="account">
                                    <input type="hidden" name="source" value="account">
                                    <button class="btn btn-sm btn-outline-primary" type="submit">Đặt mặc định</button>
                                </form>
                            </c:if>
                            <form action="${pageContext.request.contextPath}/addresses" method="post" onsubmit="return confirm('Xóa địa chỉ này?');">
                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                <input type="hidden" name="_method" value="delete">
                                <input type="hidden" name="id" value="${addr.id}">
                                <input type="hidden" name="redirect" value="account">
                                <input type="hidden" name="source" value="account">
                                <button class="btn btn-sm btn-outline-danger" type="submit">Xóa</button>
                            </form>
                        </div>
                    </div>
                </c:forEach>

                <div class="border rounded-4 p-3 mt-3">
                    <div class="fw-bold mb-3">Thêm địa chỉ mới</div>
                    <div id="addressApiStatus" class="alert alert-warning d-none py-2 px-3 mb-3" role="status" aria-live="polite"></div>
                    <form action="${pageContext.request.contextPath}/addresses" method="post" class="row g-3">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="redirect" value="account">
                        <input type="hidden" name="source" value="account">
                        <div class="col-md-6">
                            <label class="form-label">Tỉnh / Thành</label>
                            <select class="form-select" id="province" name="province" required>
                                <option value="">-- Chọn Tỉnh / Thành --</option>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Quận / Huyện</label>
                            <select class="form-select" id="district" name="district" required disabled>
                                <option value="">-- Chọn Quận / Huyện --</option>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Phường / Xã</label>
                            <select class="form-select" id="ward" name="ward" required disabled>
                                <option value="">-- Chọn Phường / Xã --</option>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Chi tiết địa chỉ</label>
                            <input class="form-control" name="addressDetail" required>
                        </div>
                        <div class="col-12 text-muted small">
                            Lưu xong sẽ tự dùng địa chỉ này làm vị trí giao hàng hiện tại.
                        </div>
                        <div class="col-12 d-grid">
                            <button class="btn btn-primary" type="submit"><i class='bx bx-save'></i> Lưu địa chỉ</button>
                        </div>
                    </form>
                </div>
            </div>

            <div class="panel">
                <div class="panel-title"><i class='bx bx-package'></i> Đơn hàng gần đây</div>
                <c:choose>
                    <c:when test="${empty recentOrders}">
                        <div class="text-muted">Bạn chưa có đơn hàng nào. <a href="${pageContext.request.contextPath}/shop">Mua sắm ngay</a>.</div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${recentOrders}" var="o">
                            <div class="order-item">
                                <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap">
                                    <div>
                                        <div class="fw-bold">Đơn #${o.id}</div>
                                        <div class="text-muted small"><fmt:formatDate value="${o.createdAt}" pattern="dd/MM/yyyy HH:mm"/> · ${o.itemCount} sản phẩm</div>
                                    </div>
                                    <span class="status-pill ${o.statusCssClass}">${fn:escapeXml(o.statusLabel)}</span>
                                </div>
                                <div class="d-flex justify-content-between align-items-center mt-3 flex-wrap gap-2">
                                    <div class="fw-semibold text-primary">${fn:escapeXml(o.formattedTotalAmount)}</div>
                                    <a href="${pageContext.request.contextPath}/my-orders?action=view&id=${o.id}" class="btn btn-sm btn-outline-dark">Xem chi tiết</a>
                                </div>
                            </div>
                        </c:forEach>
                        <a href="${pageContext.request.contextPath}/my-orders" class="btn btn-outline-primary">Xem tất cả đơn hàng</a>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="panel" id="signaturePanel">
                <div class="panel-title"><i class='bx bx-pen'></i> Chữ ký điện tử</div>
                <c:choose>
                    <c:when test="${empty pendingSignatureOrders}">
                        <div class="text-muted">Không có đơn hàng nào cần ký.</div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${pendingSignatureOrders}" var="sig">
                            <c:set var="sigStatus" value="pending" />
                            <c:set var="sigStatusText" value="Chờ ký" />
                            <c:forEach items="${orderSignatures}" var="osig">
                                <c:if test="${osig.orderId eq sig.orderId}">
                                    <c:choose>
                                        <c:when test="${osig.verifyStatus eq 'verified'}">
                                            <c:set var="sigStatus" value="verified" />
                                            <c:set var="sigStatusText" value="Đã ký" />
                                        </c:when>
                                        <c:when test="${osig.verifyStatus eq 'failed'}">
                                            <c:set var="sigStatus" value="failed" />
                                            <c:set var="sigStatusText" value="Thất bại" />
                                        </c:when>
                                    </c:choose>
                                </c:if>
                            </c:forEach>
                            <div class="sig-order-item ${sigStatus}" id="sig-item-${sig.orderId}">
                                <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap">
                                    <div>
                                        <div class="fw-bold">Đơn #${sig.orderId}</div>
                                        <div class="sig-hash mt-1">Hash: ${sig.orderHash}</div>
                                    </div>
                                    <span class="sig-status ${sigStatus}">${sigStatusText}</span>
                                </div>
                                <c:if test="${sigStatus eq 'pending' or sigStatus eq 'failed'}">
                                    <div class="mt-3">
                                        <div class="alert alert-info py-2 px-3 mb-2" style="font-size:.82rem;">
                                            <strong>Hướng dẫn ký:</strong>
                                            <ol class="mb-0 ps-3">
                                                <li><a href="${pageContext.request.contextPath}/user/download-private-key?orderId=${sig.orderId}" target="_blank">Tải private key</a></li>
                                                <li>Mở CryptoTool → Import Key → chọn file .der vừa tải</li>
                                                <li>Nhập Hash bên trên → bấm "Ký số"</li>
                                                <li>Copy kết quả (hex) → dán vào ô bên dưới</li>
                                            </ol>
                                        </div>
                                        <form class="sig-form" data-order-id="${sig.orderId}">
                                            <div class="mb-2">
                                                <label class="form-label small fw-semibold">Chữ ký (hex)</label>
                                                <textarea class="form-control sig-input" rows="3" placeholder="Dán chữ ký điện tử dạng hex vào đây..."></textarea>
                                            </div>
                                            <button type="submit" class="btn btn-sm btn-primary">
                                                <i class='bx bx-upload'></i> Tải lên và xác thực
                                            </button>
                                        </form>
                                        <div class="sig-result" id="sig-result-${sig.orderId}"></div>
                                    </div>
                                </c:if>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/components/footer.jsp" />
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
document.addEventListener("DOMContentLoaded", function () {
    const provinceSelect = document.getElementById("province");
    const districtSelect = document.getElementById("district");
    const wardSelect = document.getElementById("ward");
    const addressApiStatus = document.getElementById("addressApiStatus");
    const apiUrl = "https://provinces.open-api.vn/api/v1";

    function showAddressApiStatus(message) {
        if (!addressApiStatus) {
            return;
        }
        addressApiStatus.textContent = message;
        addressApiStatus.classList.remove("d-none");
    }

    function clearAddressApiStatus() {
        if (!addressApiStatus) {
            return;
        }
        addressApiStatus.textContent = "";
        addressApiStatus.classList.add("d-none");
    }

    fetch(apiUrl + "/p/")
        .then(res => res.json())
        .then(data => {
            clearAddressApiStatus();
            data.forEach(p => {
                const option = document.createElement("option");
                option.value = p.name;
                option.textContent = p.name;
                option.dataset.code = p.code;
                provinceSelect.appendChild(option);
            });
        })
        .catch(() => {
            provinceSelect.innerHTML = '<option value="">Không tải được tỉnh / thành</option>';
            provinceSelect.disabled = true;
            districtSelect.disabled = true;
            wardSelect.disabled = true;
            showAddressApiStatus("Không tải được danh sách tỉnh/thành. Vui lòng thử lại sau hoặc tải lại trang.");
        });

    provinceSelect.addEventListener("change", function () {
        clearAddressApiStatus();
        districtSelect.innerHTML = '<option value="">-- Chọn Quận / Huyện --</option>';
        wardSelect.innerHTML = '<option value="">-- Chọn Phường / Xã --</option>';
        districtSelect.disabled = true;
        wardSelect.disabled = true;

        const selected = this.options[this.selectedIndex];
        const code = selected ? selected.dataset.code : null;
        if (!code) return;

        districtSelect.disabled = false;
        fetch(apiUrl + "/p/" + code + "?depth=2")
            .then(res => res.json())
            .then(data => {
                clearAddressApiStatus();
                data.districts.forEach(d => {
                    const option = document.createElement("option");
                    option.value = d.name;
                    option.textContent = d.name;
                    option.dataset.code = d.code;
                    districtSelect.appendChild(option);
                });
            })
            .catch(() => {
                districtSelect.innerHTML = '<option value="">Không tải được quận / huyện</option>';
                districtSelect.disabled = true;
                wardSelect.disabled = true;
                showAddressApiStatus("Không tải được quận/huyện. Vui lòng chọn lại tỉnh/thành hoặc thử lại sau.");
            });
    });

    districtSelect.addEventListener("change", function () {
        clearAddressApiStatus();
        wardSelect.innerHTML = '<option value="">-- Chọn Phường / Xã --</option>';
        wardSelect.disabled = true;

        const selected = this.options[this.selectedIndex];
        const code = selected ? selected.dataset.code : null;
        if (!code) return;

        wardSelect.disabled = false;
        fetch(apiUrl + "/d/" + code + "?depth=2")
            .then(res => res.json())
            .then(data => {
                clearAddressApiStatus();
                data.wards.forEach(w => {
                    const option = document.createElement("option");
                    option.value = w.name;
                    option.textContent = w.name;
                    option.dataset.code = w.code;
                    wardSelect.appendChild(option);
                });
            })
            .catch(() => {
                wardSelect.innerHTML = '<option value="">Không tải được phường / xã</option>';
                wardSelect.disabled = true;
                showAddressApiStatus("Không tải được phường/xã. Vui lòng chọn lại quận/huyện hoặc thử lại sau.");
            });
    });
});
</script>
<script>
document.addEventListener('DOMContentLoaded', function() {
    var forms = document.querySelectorAll('.sig-form');
    forms.forEach(function(form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            var orderId = form.getAttribute('data-order-id');
            var input = form.querySelector('.sig-input');
            var signature = input.value.trim();
            var resultDiv = document.getElementById('sig-result-' + orderId);

            if (!signature) {
                resultDiv.className = 'sig-result error';
                resultDiv.textContent = 'Vui lòng nhập chữ ký.';
                return;
            }

            var params = new URLSearchParams();
            params.append('orderId', orderId);
            params.append('signature', signature);

            fetch('${pageContext.request.contextPath}/user/upload-signature', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-Token': '${csrfToken}'
                },
                body: params.toString()
            })
            .then(function(res) {
                if (!res.ok) {
                    throw new Error('HTTP ' + res.status);
                }
                return res.json();
            })
            .then(function(data) {
                if (data.success) {
                    resultDiv.className = 'sig-result success';
                    resultDiv.textContent = data.message || 'Xác thực thành công!';
                    var item = document.getElementById('sig-item-' + orderId);
                    item.className = 'sig-order-item verified';
                    var statusBadge = item.querySelector('.sig-status');
                    statusBadge.className = 'sig-status verified';
                    statusBadge.textContent = 'Đã ký';
                    form.style.display = 'none';
                } else {
                    resultDiv.className = 'sig-result error';
                    resultDiv.textContent = data.message || 'Xác thực thất bại.';
                    var item = document.getElementById('sig-item-' + orderId);
                    item.className = 'sig-order-item failed';
                    var statusBadge = item.querySelector('.sig-status');
                    statusBadge.className = 'sig-status failed';
                    statusBadge.textContent = 'Thất bại';
                }
            })
            .catch(function(err) {
                resultDiv.className = 'sig-result error';
                resultDiv.textContent = 'Lỗi: ' + (err.message || 'Không thể kết nối đến server.');
            });
        });
    });
});
</script>
</body>
</html>
