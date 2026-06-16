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
        /* Modern UI Style Upgrades */
        body.admin-page {
            background-color: #f8fafc;
            color: #1e293b;
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
        }
        
        .admin-main {
            padding: 24px 30px;
        }

        /* Stats Grid Cards */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-bottom: 24px;
        }
        
        .stat-card-modern {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 16px;
            padding: 20px;
            display: flex;
            align-items: center;
            gap: 16px;
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.02), 0 2px 4px -2px rgba(0,0,0,0.02);
            transition: all 0.25s ease;
            text-decoration: none;
            color: inherit;
        }
        
        .stat-card-modern:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 15px -3px rgba(0,0,0,0.05), 0 4px 6px -4px rgba(0,0,0,0.05);
            border-color: #cbd5e1;
        }
        
        .stat-card-modern .icon-wrapper {
            width: 56px;
            height: 56px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            flex-shrink: 0;
        }
        
        /* Color variants */
        .stat-card-modern.blue .icon-wrapper { background: #eff6ff; color: #2563eb; }
        .stat-card-modern.green .icon-wrapper { background: #ecfdf5; color: #10b981; }
        .stat-card-modern.orange .icon-wrapper { background: #fffbeb; color: #f59e0b; }
        .stat-card-modern.purple .icon-wrapper { background: #f5f3ff; color: #8b5cf6; }
        
        .stat-card-modern .info-block {
            display: flex;
            flex-direction: column;
            gap: 4px;
            flex-grow: 1;
        }
        
        .stat-card-modern .info-block .card-title {
            color: #64748b;
            font-size: 13px;
            font-weight: 500;
            margin: 0;
        }
        
        .stat-card-modern .info-block .card-value {
            color: #0f172a;
            font-size: 22px;
            font-weight: 800;
            margin: 0;
            letter-spacing: -0.5px;
        }
        
        .stat-card-modern .info-block .trend {
            font-size: 12px;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 4px;
        }
        
        .stat-card-modern .info-block .trend.up { color: #10b981; }
        .stat-card-modern .info-block .trend.down { color: #ef4444; }
        .stat-card-modern .info-block .trend .muted-text { color: #94a3b8; font-weight: 400; }

        /* Charts Grid - 2x2 Layout as requested */
        .charts-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 20px;
            margin-bottom: 24px;
        }
        
        .chart-card-modern {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 16px;
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.02), 0 2px 4px -2px rgba(0,0,0,0.02);
            overflow: hidden;
            display: flex;
            flex-direction: column;
        }
        
        .chart-card-modern .chart-header {
            padding: 16px 20px;
            border-bottom: 1px solid #f1f5f9;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .chart-card-modern .chart-header h5 {
            margin: 0;
            font-size: 14px;
            font-weight: 700;
            color: #0f172a;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        
        .chart-card-modern .chart-body {
            padding: 20px;
            position: relative;
            height: 300px; /* Taller charts for better readability */
            display: flex;
            align-items: center;
            justify-content: center;
            flex-grow: 1;
        }
        
        .doughnut-chart-body {
            position: relative;
        }
        
        .doughnut-center {
            position: absolute;
            top: 45%; /* Slightly offset upward to account for the bottom legend */
            left: 50%;
            transform: translate(-50%, -50%);
            text-align: center;
            pointer-events: none;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
        }
        
        .doughnut-center .num {
            font-size: 26px;
            font-weight: 800;
            color: #0f172a;
            line-height: 1;
        }
        
        .doughnut-center .label {
            font-size: 10px;
            font-weight: 600;
            color: #94a3b8;
            text-transform: uppercase;
            margin-top: 4px;
        }

        /* Bottom Row Panels */
        .bottom-grid {
            display: grid;
            grid-template-columns: 3fr 1fr;
            gap: 20px;
        }
        
        .panel-modern {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 16px;
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.02), 0 2px 4px -2px rgba(0,0,0,0.02);
            overflow: hidden;
        }
        
        .panel-modern .panel-header {
            padding: 18px 24px;
            border-bottom: 1px solid #f1f5f9;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .panel-modern .panel-header h5 {
            margin: 0;
            font-size: 15px;
            font-weight: 700;
            color: #0f172a;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        
        .panel-modern .panel-header .view-all-link {
            font-size: 12px;
            font-weight: 600;
            color: #2563eb;
            text-decoration: none;
            display: flex;
            align-items: center;
            gap: 4px;
            transition: color 0.2s;
        }
        
        .panel-modern .panel-header .view-all-link:hover {
            color: #1d4ed8;
        }
        
        .panel-modern .panel-body {
            padding: 10px 24px 24px;
        }
        
        /* Modern Table */
        .modern-table {
            width: 100%;
            border-collapse: collapse;
        }
        
        .modern-table th {
            text-align: left;
            padding: 14px 10px;
            font-size: 11px;
            font-weight: 600;
            text-transform: uppercase;
            color: #64748b;
            border-bottom: 1px solid #f1f5f9;
            letter-spacing: 0.5px;
        }
        
        .modern-table td {
            padding: 14px 10px;
            border-bottom: 1px solid #f1f5f9;
            font-size: 13px;
            color: #334155;
            vertical-align: middle;
        }
        
        .modern-table tr:last-child td {
            border-bottom: none;
        }
        
        .customer-cell {
            display: flex;
            align-items: center;
            gap: 12px;
        }
        
        .customer-avatar-circle {
            width: 38px;
            height: 38px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 14px;
            color: #ffffff;
            background: linear-gradient(135deg, #3b82f6, #1d4ed8);
            text-transform: uppercase;
            flex-shrink: 0;
        }
        
        .customer-info {
            display: flex;
            flex-direction: column;
            gap: 2px;
        }
        
        .customer-info .name {
            font-weight: 700;
            color: #0f172a;
        }
        
        .customer-info .phone {
            font-size: 11px;
            color: #64748b;
        }
        
        .order-id-link {
            text-decoration: none;
            color: #2563eb;
            font-weight: 700;
        }
        
        .order-id-link:hover {
            color: #1d4ed8;
        }
        
        /* Badges */
        .badge-soft-modern {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 4px 12px;
            border-radius: 9999px;
            font-weight: 600;
            font-size: 11px;
            line-height: 1;
        }
        
        /* Status Badges */
        .badge-soft-modern.status-pending { background: #fffbeb; color: #d97706; }
        .badge-soft-modern.status-confirmed { background: #eff6ff; color: #2563eb; }
        .badge-soft-modern.status-shipping { background: #fdf2f8; color: #db2777; }
        .badge-soft-modern.status-completed { background: #ecfdf5; color: #059669; }
        .badge-soft-modern.status-cancelled { background: #fef2f2; color: #dc2626; }
        
        /* Payment Badges */
        .badge-soft-modern.pay-vnpay { background: #f5f3ff; color: #7c3aed; }
        .badge-soft-modern.pay-cod { background: #eff6ff; color: #3b82f6; }
        .badge-soft-modern.pay-bank { background: #fff7ed; color: #ea580c; }
        
        /* Alerts Panel */
        .alerts-list {
            display: flex;
            flex-direction: column;
            gap: 16px;
        }
        
        .alert-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 16px;
            display: flex;
            gap: 14px;
            align-items: flex-start;
            text-decoration: none;
            color: inherit;
            transition: all 0.2s;
        }
        
        .alert-card:hover {
            transform: translateX(4px);
            border-color: #cbd5e1;
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.02);
        }
        
        .alert-card .alert-icon {
            width: 40px;
            height: 40px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 20px;
            flex-shrink: 0;
        }
        
        .alert-card.danger-alert .alert-icon { background: #fef2f2; color: #ef4444; }
        .alert-card.warning-alert .alert-icon { background: #fffbeb; color: #f59e0b; }
        .alert-card.info-alert .alert-icon { background: #eff6ff; color: #2563eb; }
        .alert-card.success-alert .alert-icon { background: #ecfdf5; color: #10b981; }
        
        .alert-card .alert-content {
            display: flex;
            flex-direction: column;
            gap: 4px;
            flex-grow: 1;
        }
        
        .alert-card .alert-content .alert-title {
            font-size: 13px;
            font-weight: 700;
            color: #0f172a;
            margin: 0;
        }
        
        .alert-card .alert-content .alert-desc {
            font-size: 11px;
            color: #64748b;
            margin: 0;
        }
        
        .alert-card .alert-arrow {
            font-size: 16px;
            color: #94a3b8;
            align-self: center;
        }
        
        .panel-footer-alerts {
            padding: 16px;
            border-top: 1px solid #f1f5f9;
            text-align: center;
        }
        
        .panel-footer-alerts .view-all-alerts {
            font-size: 12px;
            font-weight: 700;
            color: #ef4444;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        
        .panel-footer-alerts .view-all-alerts:hover {
            color: #dc2626;
        }

        /* Responsive */
        @media (max-width: 1150px) {
            .stats-grid { grid-template-columns: repeat(2, 1fr); }
            .charts-grid { grid-template-columns: 1fr; }
            .bottom-grid { grid-template-columns: 1fr; }
        }
        @media (max-width: 768px) {
            .stats-grid { grid-template-columns: 1fr; }
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

    <!-- 1. STAT CARDS -->
    <div class="stats-grid">
        <!-- Revenue Card -->
        <a href="${pageContext.request.contextPath}/admin/reports" class="stat-card-modern blue">
            <div class="icon-wrapper">
                <i class='bx bx-dollar'></i>
            </div>
            <div class="info-block">
                <span class="card-title">Doanh thu hôm nay</span>
                <span class="card-value"><fmt:formatNumber value="${todayRevenue}" type="number" maxFractionDigits="0"/>đ</span>
                <c:choose>
                    <c:when test="${todayRevenueGrowth >= 0}">
                        <span class="trend up"><i class='bx bx-trending-up'></i> +<fmt:formatNumber value="${todayRevenueGrowth}" maxFractionDigits="1"/>% <span class="muted-text">so với hôm qua</span></span>
                    </c:when>
                    <c:otherwise>
                        <span class="trend down"><i class='bx bx-trending-down'></i> <fmt:formatNumber value="${todayRevenueGrowth}" maxFractionDigits="1"/>% <span class="muted-text">so với hôm qua</span></span>
                    </c:otherwise>
                </c:choose>
            </div>
        </a>
        
        <!-- Orders Today Card -->
        <a href="${pageContext.request.contextPath}/admin/orders" class="stat-card-modern green">
            <div class="icon-wrapper">
                <i class='bx bx-shopping-bag'></i>
            </div>
            <div class="info-block">
                <span class="card-title">Đơn hàng hôm nay</span>
                <span class="card-value">${todayOrders}</span>
                <c:choose>
                    <c:when test="${todayOrdersGrowth >= 0}">
                        <span class="trend up"><i class='bx bx-trending-up'></i> +<fmt:formatNumber value="${todayOrdersGrowth}" maxFractionDigits="1"/>% <span class="muted-text">so với hôm qua</span></span>
                    </c:when>
                    <c:otherwise>
                        <span class="trend down"><i class='bx bx-trending-down'></i> <fmt:formatNumber value="${todayOrdersGrowth}" maxFractionDigits="1"/>% <span class="muted-text">so với hôm qua</span></span>
                    </c:otherwise>
                </c:choose>
            </div>
        </a>
        
        <!-- Pending Orders Card -->
        <a href="${pageContext.request.contextPath}/admin/orders" class="stat-card-modern orange">
            <div class="icon-wrapper">
                <i class='bx bx-time-five'></i>
            </div>
            <div class="info-block">
                <span class="card-title">Đơn chờ xử lý</span>
                <span class="card-value">${totalPending}</span>
                <c:choose>
                    <c:when test="${pendingOrdersGrowth >= 0}">
                        <span class="trend up"><i class='bx bx-trending-up'></i> +<fmt:formatNumber value="${pendingOrdersGrowth}" maxFractionDigits="1"/>% <span class="muted-text">so với hôm qua</span></span>
                    </c:when>
                    <c:otherwise>
                        <span class="trend down"><i class='bx bx-trending-down'></i> <fmt:formatNumber value="${pendingOrdersGrowth}" maxFractionDigits="1"/>% <span class="muted-text">so với hôm qua</span></span>
                    </c:otherwise>
                </c:choose>
            </div>
        </a>
        
        <!-- Unpaid Orders Card -->
        <a href="${pageContext.request.contextPath}/admin/orders" class="stat-card-modern purple">
            <div class="icon-wrapper">
                <i class='bx bx-wallet'></i>
            </div>
            <div class="info-block">
                <span class="card-title">Chờ thanh toán</span>
                <span class="card-value">${totalAwaitingPayment}</span>
                <c:choose>
                    <c:when test="${awaitingPaymentGrowth >= 0}">
                        <span class="trend up"><i class='bx bx-trending-up'></i> +<fmt:formatNumber value="${awaitingPaymentGrowth}" maxFractionDigits="1"/>% <span class="muted-text">so với hôm qua</span></span>
                    </c:when>
                    <c:otherwise>
                        <span class="trend down"><i class='bx bx-trending-down'></i> <fmt:formatNumber value="${awaitingPaymentGrowth}" maxFractionDigits="1"/>% <span class="muted-text">so với hôm qua</span></span>
                    </c:otherwise>
                </c:choose>
            </div>
        </a>
    </div>

    <!-- 2. CHARTS GRID (2x2 Layout with Taller Charts) -->
    <div class="charts-grid">
        <!-- Chart 1: Orders by Month (Bar Chart - Upgraded from Daily Line Chart) -->
        <div class="chart-card-modern">
            <div class="chart-header">
                <h5><i class='bx bx-bar-chart-square'></i> Đơn hàng theo tháng</h5>
            </div>
            <div class="chart-body">
                <canvas id="dashOrdersMonthlyChart"></canvas>
            </div>
        </div>
        
        <!-- Chart 2: Order Status with Legend -->
        <div class="chart-card-modern">
            <div class="chart-header">
                <h5><i class='bx bx-pie-chart-alt-2'></i> Trạng thái đơn hàng</h5>
            </div>
            <div class="chart-body doughnut-chart-body" id="status-chart-body">
                <canvas id="dashStatusChart"></canvas>
                <div class="doughnut-center" id="status-chart-center">
                    <div class="num">0</div>
                    <div class="label">Tổng đơn</div>
                </div>
            </div>
        </div>
        
        <!-- Chart 3: Payment Methods -->
        <div class="chart-card-modern">
            <div class="chart-header">
                <h5><i class='bx bx-bar-chart-alt-2'></i> Phương thức thanh toán</h5>
            </div>
            <div class="chart-body">
                <canvas id="dashPaymentMethodsChart"></canvas>
            </div>
        </div>
        
        <!-- Chart 4: Payment Status with Legend -->
        <div class="chart-card-modern">
            <div class="chart-header">
                <h5><i class='bx bx-doughnut-chart'></i> Trạng thái thanh toán</h5>
            </div>
            <div class="chart-body doughnut-chart-body">
                <canvas id="dashPaymentStatusChart"></canvas>
                <div class="doughnut-center" id="payment-status-center">
                    <div class="num">0</div>
                    <div class="label">Giao dịch</div>
                </div>
            </div>
        </div>
    </div>

    <!-- 3. RECENT ORDERS & ALERTS -->
    <div class="bottom-grid">
        <!-- Recent Orders -->
        <section class="panel-modern">
            <div class="panel-header">
                <h5><i class='bx bx-list-ul'></i> Đơn hàng gần đây</h5>
                <a href="${pageContext.request.contextPath}/admin/orders" class="view-all-link">Xem tất cả đơn hàng <i class='bx bx-right-arrow-alt'></i></a>
            </div>
            <div class="panel-body" style="padding-top: 0;">
                <c:choose>
                    <c:when test="${empty recentOrders}">
                        <div class="empty-state" style="padding: 40px 0;">Chưa có đơn hàng nào.</div>
                    </c:when>
                    <c:otherwise>
                        <table class="modern-table">
                            <thead>
                                <tr>
                                    <th>Mã đơn</th>
                                    <th>Khách hàng</th>
                                    <th>Tổng tiền</th>
                                    <th>Thanh toán</th>
                                    <th>Trạng thái</th>
                                    <th>Thời gian</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="order" items="${recentOrders}">
                                    <tr>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/orders?action=view&id=${order.id}" class="order-id-link">
                                                #DH<fmt:formatDate value="${order.createdAt}" pattern="yyyyMMdd"/>-${order.id}
                                            </a>
                                        </td>
                                        <td>
                                            <div class="customer-cell">
                                                <div class="customer-avatar-circle">
                                                    ${fn:substring(order.fullname, 0, 1)}
                                                </div>
                                                <div class="customer-info">
                                                    <span class="name">${fn:escapeXml(order.fullname)}</span>
                                                    <span class="phone">${fn:escapeXml(order.phone)}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td style="font-weight: 700; color: #0f172a;">
                                            <fmt:formatNumber value="${order.totalAmount}" type="number" maxFractionDigits="0"/>đ
                                        </td>
                                        <td>
                                            <c:set var="payClass" value=""/>
                                            <c:choose>
                                                <c:when test="${order.payment_method == 'VNPAY'}"><c:set var="payClass" value="pay-vnpay"/></c:when>
                                                <c:when test="${order.payment_method == 'COD'}"><c:set var="payClass" value="pay-cod"/></c:when>
                                                <c:otherwise><c:set var="payClass" value="pay-bank"/></c:otherwise>
                                            </c:choose>
                                            <span class="badge-soft-modern ${payClass}">${order.payment_method}</span>
                                        </td>
                                        <td>
                                            <c:set var="statClass" value=""/>
                                            <c:choose>
                                                <c:when test="${order.status == 'Pending'}"><c:set var="statClass" value="status-pending"/></c:when>
                                                <c:when test="${order.status == 'Confirmed'}"><c:set var="statClass" value="status-confirmed"/></c:when>
                                                <c:when test="${order.status == 'Shipping'}"><c:set var="statClass" value="status-shipping"/></c:when>
                                                <c:when test="${order.status == 'Completed'}"><c:set var="statClass" value="status-completed"/></c:when>
                                                <c:otherwise><c:set var="statClass" value="status-cancelled"/></c:otherwise>
                                            </c:choose>
                                            <span class="badge-soft-modern ${statClass}">${order.statusLabel}</span>
                                        </td>
                                        <td style="color: #64748b; font-size: 12px;">
                                            <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <!-- Alerts -->
        <section class="panel-modern" style="display: flex; flex-direction: column;">
            <div class="panel-header" style="border-bottom: none;">
                <h5><i class='bx bx-bell'></i> Cảnh báo</h5>
            </div>
            <div class="panel-body" style="flex-grow: 1;">
                <div class="alerts-list">
                    <!-- Low Stock Alert -->
                    <a href="${pageContext.request.contextPath}/pages/admin/products" class="alert-card danger-alert">
                        <div class="alert-icon">
                            <i class='bx bx-store'></i>
                        </div>
                        <div class="alert-content">
                            <span class="alert-title">${lowStockCount} sản phẩm sắp hết hàng</span>
                            <span class="alert-desc">Kiểm tra và nhập hàng kịp thời</span>
                        </div>
                        <i class='bx bx-chevron-right alert-arrow'></i>
                    </a>
                    
                    <!-- Awaiting Payment Alert -->
                    <a href="${pageContext.request.contextPath}/admin/orders" class="alert-card warning-alert">
                        <div class="alert-icon">
                            <i class='bx bx-time'></i>
                        </div>
                        <div class="alert-content">
                            <span class="alert-title">${totalAwaitingPayment} đơn hàng chờ thanh toán</span>
                            <span class="alert-desc">Tổng giá trị: <fmt:formatNumber value="${awaitingPaymentAmount}" type="number" maxFractionDigits="0"/>đ</span>
                        </div>
                        <i class='bx bx-chevron-right alert-arrow'></i>
                    </a>
                    
                    <!-- Reconciliation Alert -->
                    <a href="${pageContext.request.contextPath}/admin/orders" class="alert-card warning-alert" style="border-color: #fef08a; background: #fefcf0;">
                        <div class="alert-icon" style="background: #fef9c3; color: #ca8a04;">
                            <i class='bx bx-error-circle'></i>
                        </div>
                        <div class="alert-content">
                            <span class="alert-title">${reconciliationCount} đơn hàng chờ đối soát</span>
                            <span class="alert-desc">Vui lòng đối soát với cổng thanh toán</span>
                        </div>
                        <i class='bx bx-chevron-right alert-arrow'></i>
                    </a>
                    
                    <!-- Weekly Growth Alert -->
                    <a href="${pageContext.request.contextPath}/admin/reports" class="alert-card info-alert">
                        <div class="alert-icon">
                            <i class='bx bx-trending-up'></i>
                        </div>
                        <div class="alert-content">
                            <c:choose>
                                <c:when test="${weeklyRevenueGrowth >= 0}">
                                    <span class="alert-title">Doanh thu tuần này tăng <fmt:formatNumber value="${weeklyRevenueGrowth}" maxFractionDigits="1"/>%</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="alert-title" style="color: #ef4444;">Doanh thu tuần này giảm <fmt:formatNumber value="${-weeklyRevenueGrowth}" maxFractionDigits="1"/>%</span>
                                </c:otherwise>
                            </c:choose>
                            <span class="alert-desc">So với tuần trước</span>
                        </div>
                        <i class='bx bx-chevron-right alert-arrow'></i>
                    </a>
                </div>
            </div>
            
            <div class="panel-footer-alerts">
                <a href="${pageContext.request.contextPath}/admin/notifications" class="view-all-alerts">
                    Xem tất cả cảnh báo <i class='bx bx-right-arrow-alt'></i>
                </a>
            </div>
        </section>
    </div>
</main>

<jsp:include page="/components/scripts.jsp" />
<script>
(function() {
    var monthLabels = ['T1','T2','T3','T4','T5','T6','T7','T8','T9','T10','T11','T12'];

    // 1. Chart 1: Đơn hàng theo tháng (Bar Chart - Upgraded from Daily Line Chart)
    const rawMonthlyData = ${monthlyOrdersJson};
    const monthlyCounts = new Array(12).fill(0);
    rawMonthlyData.forEach(item => {
        if (item.month >= 1 && item.month <= 12) {
            monthlyCounts[item.month - 1] = item.count;
        }
    });

    new Chart(document.getElementById('dashOrdersMonthlyChart'), {
        type: 'bar',
        data: {
            labels: monthLabels,
            datasets: [{
                label: 'Số đơn hàng',
                data: monthlyCounts,
                backgroundColor: 'rgba(37, 99, 235, 0.85)',
                borderRadius: 6,
                barThickness: 16
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { 
                    beginAtZero: true, 
                    grid: { color: '#f1f5f9' },
                    ticks: { precision: 0 }
                },
                x: { grid: { display: false } }
            }
        }
    });

    // 2. Chart 2: Trạng thái đơn hàng (Doughnut Chart with Legend at bottom)
    const statusData = ${orderStatusJson};
    const statusLabelsMap = {
        'Pending': 'Chờ xác nhận',
        'Awaiting Payment': 'Chờ thanh toán',
        'Confirmed': 'Đã xác nhận',
        'Shipping': 'Đang giao',
        'Completed': 'Đã hoàn thành',
        'Cancelled': 'Đã hủy',
        'Paid': 'Đã thanh toán',
        'Delivered': 'Đã giao hàng'
    };
    
    const statusColors = {
        'Pending': '#f59e0b',
        'Awaiting Payment': '#7c3aed',
        'Confirmed': '#3b82f6',
        'Shipping': '#db2777',
        'Completed': '#059669',
        'Cancelled': '#ef4444',
        'Paid': '#10b981',
        'Delivered': '#10b981'
    };

    const labelsStatus = statusData.map(item => statusLabelsMap[item.label] || item.label);
    const countsStatus = statusData.map(item => item.count);
    const colorsStatus = statusData.map(item => statusColors[item.label] || '#64748b');
    
    // Update center count dynamically
    const totalOrdersCount = countsStatus.reduce((sum, val) => sum + val, 0);
    document.querySelector('#status-chart-center .num').textContent = totalOrdersCount;

    new Chart(document.getElementById('dashStatusChart'), {
        type: 'doughnut',
        data: {
            labels: labelsStatus,
            datasets: [{
                data: countsStatus,
                backgroundColor: colorsStatus,
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '72%',
            plugins: { 
                legend: { 
                    display: true,
                    position: 'bottom',
                    labels: {
                        usePointStyle: true,
                        pointStyle: 'circle',
                        padding: 15,
                        font: { size: 11 }
                    }
                }
            }
        }
    });

    // 3. Chart 3: Phương thức thanh toán (Bar Chart)
    const paymentMethodsData = ${paymentMethodsJson};
    const payLabelsMap = {
        'VNPAY': 'VNPay',
        'COD': 'COD',
        'BANK_TRANSFER': 'Chuyển khoản',
        'SEPAY': 'SePay'
    };
    
    const payColors = ['#3b82f6', '#7c3aed', '#f59e0b', '#10b981'];
    
    const labelsPay = paymentMethodsData.map(item => payLabelsMap[item.method] || item.method);
    const countsPay = paymentMethodsData.map(item => item.count);
    const colorsPay = paymentMethodsData.map((_, i) => payColors[i % payColors.length]);

    new Chart(document.getElementById('dashPaymentMethodsChart'), {
        type: 'bar',
        data: {
            labels: labelsPay,
            datasets: [{
                data: countsPay,
                backgroundColor: colorsPay,
                borderRadius: 6,
                barThickness: 16
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { 
                    beginAtZero: true, 
                    grid: { color: '#f1f5f9' },
                    ticks: { precision: 0 }
                },
                x: { grid: { display: false } }
            }
        }
    });

    // 4. Chart 4: Trạng thái thanh toán (Doughnut Chart with Legend at bottom)
    const payStatusData = ${paymentStatusJson};
    const labelsPayStatus = ['Đã thanh toán', 'Chờ thanh toán', 'Chờ đối soát', 'Thất bại', 'Hoàn tiền'];
    const countsPayStatus = [
        payStatusData.paid,
        payStatusData.unpaid,
        payStatusData.reconciliation,
        payStatusData.failed,
        payStatusData.refunded
    ];
    const colorsPayStatus = ['#10b981', '#f59e0b', '#7c3aed', '#ef4444', '#3b82f6'];
    
    // Update center count dynamically
    const totalTransactions = countsPayStatus.reduce((sum, val) => sum + val, 0);
    document.querySelector('#payment-status-center .num').textContent = totalTransactions;

    new Chart(document.getElementById('dashPaymentStatusChart'), {
        type: 'doughnut',
        data: {
            labels: labelsPayStatus,
            datasets: [{
                data: countsPayStatus,
                backgroundColor: colorsPayStatus,
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '72%',
            plugins: { 
                legend: { 
                    display: true,
                    position: 'bottom',
                    labels: {
                        usePointStyle: true,
                        pointStyle: 'circle',
                        padding: 15,
                        font: { size: 11 }
                    }
                }
            }
        }
    });
})();
</script>
</body>
</html>
