<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Admin Dashboard - PetShop</title>
    <jsp:include page="/components/head.jsp" />
    <jsp:include page="/components/admin-styles.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
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
        .chart-row { display:grid; grid-template-columns: 3fr 2fr; gap:20px; margin-bottom:24px; }
        .chart-card { background:#fff; border:1px solid #e2e8f0; border-radius:16px; box-shadow:0 4px 12px rgba(15,23,42,.04); overflow:hidden; }
        .chart-card .chart-header { padding:16px 20px; border-bottom:1px solid #e2e8f0; }
        .chart-card .chart-header h5 { margin:0; font-weight:700; display:flex; gap:8px; align-items:center; font-size:.95rem; }
        .chart-card .chart-body { padding:18px 20px; position:relative; height:260px; }
        @media (max-width: 1100px) {
            .stats-grid, .content-grid { grid-template-columns: 1fr 1fr; }
            .chart-row { grid-template-columns: 1fr; }
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

    <div class="chart-row">
        <div class="chart-card">
            <div class="chart-header">
                <h5><i class='bx bx-bar-chart-alt-2'></i> Doanh thu theo tháng</h5>
            </div>
            <div class="chart-body">
                <canvas id="dashRevenueChart"></canvas>
            </div>
        </div>
        <div class="chart-card">
            <div class="chart-header">
                <h5><i class='bx bx-pie-chart-alt-2'></i> Trạng thái đơn hàng</h5>
            </div>
            <div class="chart-body">
                <canvas id="dashStatusChart"></canvas>
            </div>
        </div>
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
                                    <td>${fn:escapeXml(order.fullname)}<div class="muted">${fn:escapeXml(order.phone)}</div></td>
                                    <td>${fn:escapeXml(order.formattedTotalAmount)}</td>
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
                                    <td>${fn:escapeXml(product.name)}</td>
                                    <td>${empty product.category ? 'Chưa phân loại' : fn:escapeXml(product.category)}</td>
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
                                    <td>${fn:escapeXml(review.userName)}</td>
                                    <td>${fn:escapeXml(review.productName)}</td>
                                    <td><span class="${review.rating <= 2 ? 'rating-low' : ''}">${review.rating}/5</span></td>
                                    <td>${fn:escapeXml(review.comment)}</td>
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
                                    <td>${fn:escapeXml(product.product)}</td>
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
<script>
(function() {
    var monthLabels = ['T1','T2','T3','T4','T5','T6','T7','T8','T9','T10','T11','T12'];

    // Revenue chart
    var revenueData = ${revenueByMonthJson};
    var revenueCounts = new Array(12).fill(0);
    revenueData.forEach(function(item) { revenueCounts[item.month - 1] = item.revenue; });

    new Chart(document.getElementById('dashRevenueChart'), {
        type: 'bar',
        data: {
            labels: monthLabels,
            datasets: [{
                label: 'Doanh thu (VNĐ)',
                data: revenueCounts,
                backgroundColor: 'rgba(11, 26, 51, 0.85)',
                borderRadius: 6,
                barThickness: 18
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, ticks: { callback: function(v) { return v >= 1000000 ? (v/1000000)+'M' : v >= 1000 ? (v/1000)+'K' : v; } } },
                x: { grid: { display: false } }
            }
        }
    });

    // Order status chart
    var statusData = ${orderStatusJson};
    var statusColors = { 'Pending': '#f59e0b', 'Confirmed': '#3b82f6', 'Shipping': '#8b5cf6', 'Completed': '#10b981', 'Cancelled': '#ef4444' };
    var bgColors = statusData.map(function(item) { return statusColors[item.label] || '#64748b'; });

    new Chart(document.getElementById('dashStatusChart'), {
        type: 'doughnut',
        data: {
            labels: statusData.map(function(item) { return item.label; }),
            datasets: [{
                data: statusData.map(function(item) { return item.count; }),
                backgroundColor: bgColors,
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '65%',
            plugins: { legend: { position: 'right', labels: { padding: 12, usePointStyle: true, pointStyle: 'circle' } } }
        }
    });
})();
</script>
</body>
</html>
