<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Admin Dashboard - PetShop</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        .stats-grid, .content-grid { display:grid; gap:20px; }
        .stats-grid { grid-template-columns: repeat(4, 1fr); margin-bottom: 24px; }
        .content-grid { grid-template-columns: repeat(2, 1fr); }
        .stat-card, .panel { background:#fff; border:1px solid #e2e8f0; border-radius:16px; box-shadow:0 4px 12px rgba(15,23,42,.04); }
        .stat-card { padding:22px; color:#fff; text-decoration:none; display:block; }
        .stat-card:hover { color:#fff; transform:translateY(-2px); }
        .stat-card.blue { background:linear-gradient(135deg, #0b1a33 0%, #1a3a5c 100%); }
        .stat-card.green { background:linear-gradient(135deg, #059669 0%, #10b981 100%); }
        .stat-card.orange { background:linear-gradient(135deg, #ea580c 0%, #fb923c 100%); }
        .stat-card.purple { background:linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%); }
        .stat-card h3 { margin:0 0 6px; font-size:1.9rem; font-weight:800; display:flex; gap:10px; align-items:center; }
        .stat-card p, .stat-card small { margin:0; opacity:.92; }
        .panel-header { padding:16px 20px; border-bottom:1px solid #e2e8f0; display:flex; justify-content:space-between; align-items:center; }
        .panel-header h5 { margin:0; font-weight:700; display:flex; gap:8px; align-items:center; }
        .panel-body { padding:18px 20px; }
        .simple-table { width:100%; border-collapse:collapse; }
        .simple-table th, .simple-table td { padding:12px 10px; border-bottom:1px solid #f1f5f9; font-size:.92rem; vertical-align:top; }
        .simple-table th { color:#64748b; text-transform:uppercase; font-size:.76rem; }
        .badge-soft { display:inline-flex; padding:4px 10px; border-radius:999px; font-size:.76rem; font-weight:700; }
        .badge-soft.warning { background:#fef3c7; color:#b45309; }
        .badge-soft.success { background:#dcfce7; color:#15803d; }
        .badge-soft.danger { background:#fee2e2; color:#dc2626; }
        .badge-soft.info { background:#dbeafe; color:#1d4ed8; }
        .muted { color:#64748b; font-size:.88rem; }
        .rating-low { color:#dc2626; font-weight:700; }
        .stock-low { color:#b45309; font-weight:700; }
        .empty-state { text-align:center; color:#94a3b8; padding:28px 16px; }
        @media (max-width: 1100px) {
            .stats-grid, .content-grid { grid-template-columns: 1fr 1fr; }
        }
        @media (max-width: 768px) {
            .stats-grid, .content-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body class="admin-page">
<jsp:include page="/components/admin-sidebar.jsp">
    <jsp:param name="currentPage" value="dashboard"/>
</jsp:include>

<main class="admin-main">
    <div class="page-header-admin">
        <div>
            <h1 class="page-title"><i class='bx bxs-dashboard'></i> Dashboard</h1>
            <p class="page-subtitle">Tổng quan vận hành cửa hàng PetShop</p>
        </div>
        <jsp:include page="/components/admin-header-dropdown.jsp" />
    </div>

    <div class="stats-grid">
        <a href="${pageContext.request.contextPath}/admin/users" class="stat-card blue">
            <h3><i class='bx bxs-user'></i> ${overview.totalUsers}</h3>
            <p>Khách hàng</p>
            <small>Tổng user mua hàng</small>
        </a>
        <a href="${pageContext.request.contextPath}/admin/orders" class="stat-card orange">
            <h3><i class='bx bxs-cart-alt'></i> ${overview.pendingOrders}</h3>
            <p>Đơn chờ xử lý</p>
            <small>${completedOrders} đơn đã hoàn tất</small>
        </a>
        <a href="${pageContext.request.contextPath}/pages/admin/products" class="stat-card green">
            <h3><i class='bx bxs-shopping-bag'></i> ${overview.totalProducts}</h3>
            <p>Sản phẩm đang bán</p>
            <small>${overview.lowStockProducts} sản phẩm sắp hết</small>
        </a>
        <a href="${pageContext.request.contextPath}/admin/reports" class="stat-card purple">
            <h3><i class='bx bxs-wallet'></i> <fmt:formatNumber value="${currentMonthRevenue}" type="number" maxFractionDigits="0"/>đ</h3>
            <p>Doanh thu tháng này</p>
            <small>Tổng doanh thu: <fmt:formatNumber value="${totalRevenue}" type="number" maxFractionDigits="0"/>đ</small>
        </a>
    </div>

    <div class="content-grid">
        <section class="panel">
            <div class="panel-header">
                <h5><i class='bx bx-cart'></i> Đơn hàng mới</h5>
                <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-sm btn-outline-primary">Xem tất cả</a>
            </div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty recentOrders}">
                        <div class="empty-state">Chưa có đơn hàng.</div>
                    </c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead>
                            <tr><th>Mã đơn</th><th>Khách hàng</th><th>Tổng tiền</th><th>Trạng thái</th></tr>
                            </thead>
                            <tbody>
                            <c:forEach var="order" items="${recentOrders}">
                                <tr>
                                    <td><strong>#${order.id}</strong></td>
                                    <td>${order.fullname}<div class="muted">${order.phone}</div></td>
                                    <td>${order.formattedTotalAmount}</td>
                                    <td>
                                        <span class="badge-soft ${order.status == 'Pending' ? 'warning' : (order.status == 'Completed' ? 'success' : 'info')}">${order.statusLabel}</span>
                                    </td>
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
                <h5><i class='bx bx-error-circle'></i> Sản phẩm sắp hết hàng</h5>
                <a href="${pageContext.request.contextPath}/admin/notifications" class="btn btn-sm btn-outline-warning">Cảnh báo</a>
            </div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty lowStockProducts}">
                        <div class="empty-state">Không có sản phẩm nào dưới ngưỡng tồn kho.</div>
                    </c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead>
                            <tr><th>Sản phẩm</th><th>Danh mục</th><th>Tồn kho</th><th>Đánh giá</th></tr>
                            </thead>
                            <tbody>
                            <c:forEach var="product" items="${lowStockProducts}">
                                <tr>
                                    <td>${product.name}</td>
                                    <td>${empty product.category ? 'Chưa phân loại' : product.category}</td>
                                    <td><span class="stock-low">${product.stock}</span></td>
                                    <td>${product.formattedAverageRating} ★ / ${product.reviewCount}</td>
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
                <h5><i class='bx bx-star'></i> Review mới nhất</h5>
                <a href="${pageContext.request.contextPath}/admin/reports" class="btn btn-sm btn-outline-secondary">Xem báo cáo</a>
            </div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty recentReviews}">
                        <div class="empty-state">Chưa có đánh giá sản phẩm.</div>
                    </c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead>
                            <tr><th>Khách hàng</th><th>Sản phẩm</th><th>Điểm</th><th>Nội dung</th></tr>
                            </thead>
                            <tbody>
                            <c:forEach var="review" items="${recentReviews}">
                                <tr>
                                    <td>${review.userName}</td>
                                    <td>${review.productName}</td>
                                    <td><span class="${review.rating <= 2 ? 'rating-low' : ''}">${review.rating}/5</span></td>
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
                <h5><i class='bx bx-trophy'></i> Top sản phẩm bán chạy</h5>
                <a href="${pageContext.request.contextPath}/admin/reports" class="btn btn-sm btn-outline-success">Báo cáo bán hàng</a>
            </div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty topProducts}">
                        <div class="empty-state">Chưa có dữ liệu bán hàng.</div>
                    </c:when>
                    <c:otherwise>
                        <table class="simple-table">
                            <thead>
                            <tr><th>Sản phẩm</th><th>Đã bán</th><th>Doanh thu</th></tr>
                            </thead>
                            <tbody>
                            <c:forEach var="product" items="${topProducts}">
                                <tr>
                                    <td>${product.product}</td>
                                    <td>${product.count}</td>
                                    <td><fmt:formatNumber value="${product.revenue}" type="number" maxFractionDigits="0"/>đ</td>
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

<jsp:include page="/components/scripts.jsp" />
</body>
</html>
