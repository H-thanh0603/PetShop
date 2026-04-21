<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <jsp:include page="/components/favicon.jsp" />
    <title>Trung tâm cảnh báo - Admin</title>
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        .stats-grid, .alerts-grid { display:grid; gap:20px; }
        .stats-grid { grid-template-columns: repeat(3, 1fr); margin-bottom:24px; }
        .alerts-grid { grid-template-columns: repeat(2, 1fr); }
        .stat-box, .panel { background:#fff; border-radius:16px; border:1px solid #e2e8f0; box-shadow:0 4px 12px rgba(15,23,42,.04); }
        .stat-box { padding:22px; display:flex; align-items:center; gap:14px; }
        .stat-box .icon { width:58px; height:58px; border-radius:14px; display:flex; align-items:center; justify-content:center; color:#fff; font-size:1.6rem; }
        .icon.orange { background:linear-gradient(135deg,#f59e0b,#f97316); }
        .icon.red { background:linear-gradient(135deg,#ef4444,#dc2626); }
        .icon.blue { background:linear-gradient(135deg,#3b82f6,#2563eb); }
        .stat-box h3 { margin:0; font-size:1.8rem; }
        .panel-header { padding:16px 20px; border-bottom:1px solid #e2e8f0; display:flex; justify-content:space-between; align-items:center; }
        .panel-header h5 { margin:0; font-weight:700; display:flex; gap:8px; align-items:center; }
        .panel-body { padding:18px 20px; }
        .simple-table { width:100%; border-collapse:collapse; }
        .simple-table th, .simple-table td { padding:12px 10px; border-bottom:1px solid #f1f5f9; vertical-align:top; }
        .simple-table th { color:#64748b; text-transform:uppercase; font-size:.75rem; }
        .pill { display:inline-flex; padding:4px 10px; border-radius:999px; font-size:.74rem; font-weight:700; }
        .pill.pending { background:#fef3c7; color:#b45309; }
        .pill.low-stock { background:#ffedd5; color:#c2410c; }
        .pill.review { background:#fee2e2; color:#b91c1c; }
        .pill.system { background:#dbeafe; color:#1d4ed8; }
        .muted { color:#64748b; font-size:.87rem; }
        .empty-state { text-align:center; color:#94a3b8; padding:28px 16px; }
        @media (max-width: 992px) {
            .stats-grid, .alerts-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<jsp:include page="/components/admin-sidebar.jsp"><jsp:param name="currentPage" value="notifications"/></jsp:include>

<main class="admin-main">
    <div class="page-header-admin">
        <div>
            <h1 class="page-title"><i class='bx bxs-bell-ring'></i> Trung tâm cảnh báo</h1>
            <p class="page-subtitle">Theo dõi đơn hàng cần xử lý, tồn kho thấp và review xấu</p>
        </div>
        <jsp:include page="/components/admin-header-dropdown.jsp" />
    </div>

    <div class="stats-grid">
        <div class="stat-box">
            <div class="icon orange"><i class='bx bxs-cart-download'></i></div>
            <div><h3>${pendingOrderCount}</h3><div class="muted">Đơn hàng pending mới</div></div>
        </div>
        <div class="stat-box">
            <div class="icon red"><i class='bx bx-package'></i></div>
            <div><h3>${lowStockCount}</h3><div class="muted">Sản phẩm sắp hết hàng</div></div>
        </div>
        <div class="stat-box">
            <div class="icon blue"><i class='bx bx-star'></i></div>
            <div><h3>${lowRatingCount}</h3><div class="muted">Review 1-2 sao cần xử lý</div></div>
        </div>
    </div>

    <div class="alerts-grid">
        <section class="panel">
            <div class="panel-header">
                <h5><i class='bx bx-time-five'></i> Đơn hàng chờ xác nhận</h5>
                <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-sm btn-outline-primary">Quản lý đơn</a>
            </div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty pendingOrders}">
                        <div class="empty-state">Không có đơn hàng pending.</div>
                    </c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead><tr><th>Mã đơn</th><th>Khách hàng</th><th>Tổng tiền</th><th>Ngày tạo</th></tr></thead>
                            <tbody>
                            <c:forEach var="order" items="${pendingOrders}">
                                <tr>
                                    <td><span class="pill pending">#${order.id}</span></td>
                                    <td>${order.fullname}<div class="muted">${order.phone}</div></td>
                                    <td>${order.formattedTotalAmount}</td>
                                    <td><fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section class="panel">
            <div class="panel-header">
                <h5><i class='bx bx-error-circle'></i> Tồn kho thấp</h5>
                <a href="${pageContext.request.contextPath}/pages/admin/products" class="btn btn-sm btn-outline-warning">Xem sản phẩm</a>
            </div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty lowStockProducts}">
                        <div class="empty-state">Không có sản phẩm nào dưới ngưỡng cảnh báo.</div>
                    </c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead><tr><th>Sản phẩm</th><th>Danh mục</th><th>Tồn kho</th></tr></thead>
                            <tbody>
                            <c:forEach var="product" items="${lowStockProducts}">
                                <tr>
                                    <td>${product.name}</td>
                                    <td>${empty product.category ? 'Chưa phân loại' : product.category}</td>
                                    <td><span class="pill low-stock">${product.stock}</span></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section class="panel">
            <div class="panel-header">
                <h5><i class='bx bx-message-square-x'></i> Review tiêu cực</h5>
                <a href="${pageContext.request.contextPath}/admin/reports" class="btn btn-sm btn-outline-danger">Xem báo cáo</a>
            </div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty lowRatingReviews}">
                        <div class="empty-state">Chưa có review 1-2 sao.</div>
                    </c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead><tr><th>Khách hàng</th><th>Sản phẩm</th><th>Điểm</th><th>Nội dung</th></tr></thead>
                            <tbody>
                            <c:forEach var="review" items="${lowRatingReviews}">
                                <tr>
                                    <td>${review.userName}</td>
                                    <td>${review.productName}</td>
                                    <td><span class="pill review">${review.rating}/5</span></td>
                                    <td>${review.comment}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section class="panel">
            <div class="panel-header">
                <h5><i class='bx bx-notification'></i> Thông báo đã lưu</h5>
                <span class="muted">Bảng notifications</span>
            </div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty storedNotifications}">
                        <div class="empty-state">Chưa có bản ghi notifications trong DB.</div>
                    </c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead><tr><th>Tiêu đề</th><th>Người nhận</th><th>Loại</th><th>Thời gian</th></tr></thead>
                            <tbody>
                            <c:forEach var="item" items="${storedNotifications}">
                                <tr>
                                    <td>${item.title}<div class="muted">${item.message}</div></td>
                                    <td>${item.fullname}</td>
                                    <td><span class="pill system">${item.type}</span></td>
                                    <td><fmt:formatDate value="${item.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </div>
</main>
</body>
</html>
