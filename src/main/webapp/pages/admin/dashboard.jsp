<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Admin Dashboard - PetShop</title>
    <jsp:include page="/components/head.jsp" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <jsp:include page="/components/admin-styles.jsp" />
    <style>
        /* Stats Grid */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-bottom: 24px;
        }
        .stat-card {
            border-radius: 14px;
            padding: 22px;
            color: white;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            transition: transform 0.2s, box-shadow 0.2s;
            text-decoration: none;
            display: block;
        }
        .stat-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 25px rgba(0,0,0,0.15);
            color: white;
        }
        .stat-card h3 {
            font-size: 2rem;
            margin: 0 0 6px 0;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .stat-card p { margin: 0; opacity: 0.9; font-size: 0.9rem; }
        .stat-card.blue { background: linear-gradient(135deg, #0b1a33 0%, #1a3a5c 100%); }
        .stat-card.green { background: linear-gradient(135deg, #059669 0%, #10b981 100%); }
        .stat-card.orange { background: linear-gradient(135deg, #ea580c 0%, #f97316 100%); }
        .stat-card.purple { background: linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%); }
        
        /* Quick Actions */
        .quick-actions {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 12px;
            margin-bottom: 24px;
        }
        .quick-action-btn {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 14px 18px;
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
            text-decoration: none;
            color: #334155;
            font-weight: 600;
            font-size: 0.9rem;
            transition: all 0.2s;
        }
        .quick-action-btn:hover {
            background: #f8fafc;
            border-color: #0d9488;
            color: #0d9488;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.08);
        }
        .quick-action-btn i { font-size: 1.3rem; }
        
        /* Card */
        .card {
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.04);
            overflow: hidden;
            margin-bottom: 24px;
        }
        .card-header {
            padding: 16px 20px;
            border-bottom: 1px solid #e2e8f0;
            background: #f8fafc;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .card-header h5 {
            margin: 0;
            font-size: 1rem;
            font-weight: 600;
            color: #0f172a;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .card-header h5 i { color: #64748b; }
        .card-body { padding: 0; }
        
        /* Table */
        .table {
            width: 100%;
            border-collapse: collapse;
            margin: 0;
        }
        .table th {
            text-align: left;
            padding: 12px 16px;
            font-size: 0.75rem;
            font-weight: 600;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            background: #f8fafc;
            border-bottom: 1px solid #e2e8f0;
        }
        .table td {
            padding: 14px 16px;
            border-bottom: 1px solid #f1f5f9;
            font-size: 0.875rem;
            color: #334155;
        }
        .table tr:hover { background: #f8fafc; }
        
        /* Badge */
        .badge {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            padding: 4px 10px;
            border-radius: 6px;
            font-size: 0.75rem;
            font-weight: 600;
        }
        .badge.success { background: #dcfce7; color: #15803d; }
        .badge.warning { background: #fef3c7; color: #b45309; }
        .badge.info { background: #dbeafe; color: #1d4ed8; }
        .badge.danger { background: #fee2e2; color: #dc2626; }
        
        .dashboard-grid {
            display: grid;
            grid-template-columns: 1fr;
            gap: 24px;
        }
        
        .empty-state {
            text-align: center;
            padding: 40px 20px;
            color: #94a3b8;
        }
        .empty-state i { font-size: 3rem; margin-bottom: 12px; opacity: 0.5; }
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
                <p class="page-subtitle">Quản lý cửa hàng PetShop</p>
            </div>
            <jsp:include page="/components/admin-header-dropdown.jsp" />
        </div>
        
        <jsp:include page="/components/alerts.jsp" />
        
        <!-- Main Stats -->
        <div class="stats-grid">
            <a href="${pageContext.request.contextPath}/admin/users" class="stat-card blue">
                <h3><i class='bx bxs-user'></i> ${totalUsers != null ? totalUsers : 0}</h3>
                <p>Khách hàng</p>
            </a>
            <a href="${pageContext.request.contextPath}/admin/orders" class="stat-card orange">
                <h3><i class='bx bxs-cart-alt'></i> ${pendingOrders != null ? pendingOrders : 0}</h3>
                <p>Đơn hàng mới</p>
            </a>
            <a href="${pageContext.request.contextPath}/pages/admin/products" class="stat-card green">
                <h3><i class='bx bxs-shopping-bag'></i> ${totalProducts != null ? totalProducts : 0}</h3>
                <p>Tổng sản phẩm</p>
            </a>
            <div class="stat-card purple">
                <h3><i class='bx bxs-bar-chart-alt-2'></i> 
                <fmt:formatNumber value="${todayRevenue != null ? todayRevenue : 0}" type="number" maxFractionDigits="0"/>đ
                </h3>
                <p>Doanh thu hôm nay</p>
            </div>
        </div>
        
        <!-- Quick Actions -->
        <div class="quick-actions">
            <a href="${pageContext.request.contextPath}/pages/admin/products?action=add" class="quick-action-btn">
                <i class='bx bx-plus-circle' style="color:#7c3aed;"></i> Thêm sản phẩm mới
            </a>
            <a href="${pageContext.request.contextPath}/admin/orders" class="quick-action-btn">
                <i class='bx bx-list-check' style="color:#0d9488;"></i> Xem đơn hàng
            </a>
            <a href="${pageContext.request.contextPath}/admin/users" class="quick-action-btn">
                <i class='bx bx-user-plus' style="color:#db2777;"></i> Quản lý User
            </a>
            <a href="${pageContext.request.contextPath}/admin/statistics" class="quick-action-btn">
                <i class='bx bx-line-chart' style="color:#ea580c;"></i> Báo cáo doanh thu
            </a>
        </div>
        
        <!-- Two Column Content -->
        <div class="dashboard-grid">
            <!-- Recent Orders -->
            <div class="card">
                <div class="card-header">
                    <h5><i class='bx bx-cart'></i> Đơn hàng gần đây</h5>
                    <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-sm btn-outline-primary">Xem tất cả</a>
                </div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${empty recentOrders}">
                            <div class="empty-state">
                                <i class='bx bx-shopping-bag'></i>
                                <p>Chưa có đơn hàng nào</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <table class="table">
                                <thead>
                                    <tr>
                                        <th>Mã đơn</th>
                                        <th>Khách hàng</th>
                                        <th>Tổng tiền</th>
                                        <th>Trạng thái</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="order" items="${recentOrders}">
                                        <tr>
                                            <td><strong>#${order.id}</strong></td>
                                            <td>${order.fullname}</td>
                                            <td>${order.formattedTotalAmount}</td>
                                            <td>
                                                <span class="badge ${order.status == 'Pending' ? 'warning' : 'success'}">
                                                    ${order.status}
                                                </span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            

        </div>
    </main>
    
    <jsp:include page="/components/scripts.jsp" />
</body>
</html>
