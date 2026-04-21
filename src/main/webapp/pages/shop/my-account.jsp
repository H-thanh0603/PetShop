<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
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
    </style>
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
                <h2 class="fw-bold mb-1"><i class='bx bx-user-circle me-2'></i>Tài khoản của tôi</h2>
                <div class="opacity-75">Quản lý hồ sơ, địa chỉ giao hàng và xem nhanh trạng thái đơn hàng gần đây.</div>
            </div>
            <div class="col-lg-5">
                <div class="row g-2">
                    <div class="col-4"><div class="hero-stat"><div class="small opacity-75">Đang xử lý</div><div class="fs-5 fw-bold">${countPending}</div></div></div>
                    <div class="col-4"><div class="hero-stat"><div class="small opacity-75">Hoàn thành</div><div class="fs-5 fw-bold">${countCompleted}</div></div></div>
                    <div class="col-4"><div class="hero-stat"><div class="small opacity-75">Địa chỉ</div><div class="fs-5 fw-bold">${addressList.size()}</div></div></div>
                </div>
            </div>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-lg-5">
            <div class="panel">
                <div class="panel-title"><i class='bx bx-id-card'></i> Hồ sơ cá nhân</div>
                <form action="${pageContext.request.contextPath}/my-account" method="post" class="row g-3">
                    <div class="col-12">
                        <label class="form-label">Họ và tên</label>
                        <input class="form-control" name="fullname" value="${sessionScope.user.fullname}" required>
                    </div>
                    <div class="col-12">
                        <label class="form-label">Email</label>
                        <input class="form-control" type="email" name="email" value="${sessionScope.user.email}" required>
                    </div>
                    <div class="col-12">
                        <label class="form-label">Số điện thoại</label>
                        <input class="form-control" name="phone" value="${sessionScope.user.phone}" placeholder="Nhập số điện thoại">
                    </div>
                    <div class="col-12">
                        <label class="form-label">Tên đăng nhập</label>
                        <input class="form-control" value="${sessionScope.user.username}" disabled>
                    </div>
                    <div class="col-12 d-grid">
                        <button class="btn btn-primary" type="submit"><i class='bx bx-save'></i> Lưu thay đổi</button>
                    </div>
                </form>
            </div>

            <div class="panel">
                <div class="panel-title"><i class='bx bx-lock-alt'></i> Bảo mật tài khoản</div>
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                    <div>
                        <div class="fw-semibold">Đặt lại mật khẩu</div>
                        <div class="text-muted small">Đổi mật khẩu qua OTP email để an toàn hơn.</div>
                    </div>
                    <a href="${pageContext.request.contextPath}/forgot-password" class="btn btn-outline-primary">Đặt lại mật khẩu</a>
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
                                <div class="fw-bold">${addr.address}, ${addr.ward}, ${addr.district}, ${addr.province}</div>
                                <div class="text-muted small mt-1">Cập nhật: <fmt:formatDate value="${addr.createAt}" pattern="dd/MM/yyyy HH:mm"/></div>
                            </div>
                            <c:if test="${addr.defaultt}">
                                <span class="badge-soft badge-default"><i class='bx bx-check-circle'></i> Mặc định</span>
                            </c:if>
                        </div>
                        <div class="d-flex gap-2 mt-3 flex-wrap">
                            <c:if test="${not addr.defaultt}">
                                <form action="${pageContext.request.contextPath}/addresses" method="get">
                                    <input type="hidden" name="defaultId" value="${addr.id}">
                                    <input type="hidden" name="redirect" value="account">
                                    <button class="btn btn-sm btn-outline-primary" type="submit">Đặt mặc định</button>
                                </form>
                            </c:if>
                            <form action="${pageContext.request.contextPath}/addresses" method="post" onsubmit="return confirm('Xóa địa chỉ này?');">
                                <input type="hidden" name="_method" value="delete">
                                <input type="hidden" name="id" value="${addr.id}">
                                <input type="hidden" name="redirect" value="account">
                                <button class="btn btn-sm btn-outline-danger" type="submit">Xóa</button>
                            </form>
                        </div>
                    </div>
                </c:forEach>

                <div class="border rounded-4 p-3 mt-3">
                    <div class="fw-bold mb-3">Thêm địa chỉ mới</div>
                    <form action="${pageContext.request.contextPath}/addresses" method="post" class="row g-3">
                        <input type="hidden" name="redirect" value="account">
                        <div class="col-md-6">
                            <label class="form-label">Tỉnh / Thành</label>
                            <input class="form-control" name="province" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Quận / Huyện</label>
                            <input class="form-control" name="district" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Phường / Xã</label>
                            <input class="form-control" name="ward" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Chi tiết địa chỉ</label>
                            <input class="form-control" name="addressDetail" required>
                        </div>
                        <div class="col-12 form-check ms-1">
                            <input class="form-check-input" type="checkbox" name="isDefault" id="isDefaultAccount">
                            <label class="form-check-label" for="isDefaultAccount">Đặt làm địa chỉ mặc định</label>
                        </div>
                        <div class="col-12 d-grid">
                            <button class="btn btn-primary" type="submit"><i class='bx bx-plus'></i> Thêm địa chỉ</button>
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
                                    <span class="status-pill ${o.statusCssClass}">${o.statusLabel}</span>
                                </div>
                                <div class="d-flex justify-content-between align-items-center mt-3 flex-wrap gap-2">
                                    <div class="fw-semibold text-primary">${o.formattedTotalAmount}</div>
                                    <a href="${pageContext.request.contextPath}/my-orders?action=view&id=${o.id}" class="btn btn-sm btn-outline-dark">Xem chi tiết</a>
                                </div>
                            </div>
                        </c:forEach>
                        <a href="${pageContext.request.contextPath}/my-orders" class="btn btn-outline-primary">Xem tất cả đơn hàng</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/components/footer.jsp" />
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
