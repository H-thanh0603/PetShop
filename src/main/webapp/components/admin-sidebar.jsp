<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String currentPage = request.getParameter("currentPage");
    if (currentPage == null) currentPage = "";
%>
<!-- Admin Sidebar Component -->
<aside class="admin-sidebar">
    <div class="sidebar-brand">
        <i class='bx bxs-dog'></i>
        <h5>Admin Panel</h5>
    </div>
    <nav class="sidebar-nav">
        <a href="${pageContext.request.contextPath}/pages/admin/dashboard" class="<%= "dashboard".equals(currentPage) ? "active" : "" %>">
            <i class='bx bxs-dashboard'></i> Dashboard
        </a>
        
        <div class="sidebar-section-title">Thương mại</div>
        <a href="${pageContext.request.contextPath}/pages/admin/products" class="<%= "products".equals(currentPage) ? "active" : "" %>">
            <i class='bx bxs-shopping-bag'></i> Quản lý Sản phẩm
        </a>
        <a href="${pageContext.request.contextPath}/admin/orders" class="<%= "orders".equals(currentPage) ? "active" : "" %>">
            <i class='bx bxs-cart-alt'></i> Quản lý Đơn hàng
        </a>
        
        <div class="sidebar-section-title">Hệ thống</div>
        <a href="${pageContext.request.contextPath}/admin/users" class="<%= "users".equals(currentPage) ? "active" : "" %>">
            <i class='bx bxs-group'></i> Người dùng
        </a>
        <a href="${pageContext.request.contextPath}/admin/statistics" class="<%= "statistics".equals(currentPage) ? "active" : "" %>">
            <i class='bx bxs-bar-chart-alt-2'></i> Thống kê doanh thu
        </a>
        
        <div class="sidebar-divider"></div>
        <a href="${pageContext.request.contextPath}/home">
            <i class='bx bx-home'></i> Về trang chủ
        </a>
        <a href="${pageContext.request.contextPath}/logout">
            <i class='bx bx-log-out'></i> Đăng xuất
        </a>
    </nav>
</aside>

<style>
/* ===== Admin Sidebar Styles ===== */
.admin-sidebar {
    min-height: 100vh;
    height: 100vh;
    background: linear-gradient(180deg, #0b1a33 0%, #1a3a5c 100%);
    width: 250px;
    position: fixed;
    left: 0;
    top: 0;
    z-index: 100;
    overflow-y: auto;
    overflow-x: hidden;
}
.admin-sidebar::-webkit-scrollbar { width: 6px; }
.admin-sidebar::-webkit-scrollbar-track { background: rgba(255,255,255,0.05); }
.admin-sidebar::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.2); border-radius: 3px; }
.admin-sidebar::-webkit-scrollbar-thumb:hover { background: rgba(255,255,255,0.3); }

.sidebar-brand {
    padding: 24px 20px;
    border-bottom: 1px solid rgba(255,255,255,0.1);
    display: flex;
    align-items: center;
    gap: 12px;
}
.sidebar-brand i { font-size: 1.6rem; color: #60a5fa; }
.sidebar-brand h5 { color: white; margin: 0; font-weight: 600; font-size: 1.15rem; }

.sidebar-nav { padding: 16px 12px; padding-bottom: 30px; }

.sidebar-section-title {
    color: rgba(255,255,255,0.4);
    font-size: 0.7rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 1px;
    padding: 16px 18px 8px 18px;
    margin-top: 8px;
}

.sidebar-nav a {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 18px;
    color: rgba(255,255,255,0.7);
    text-decoration: none;
    font-size: 0.9rem;
    font-weight: 450;
    border-radius: 10px;
    transition: all 0.2s ease;
    margin-bottom: 2px;
}
.sidebar-nav a:hover { background: rgba(255,255,255,0.1); color: rgba(255,255,255,0.95); }
.sidebar-nav a.active { 
    background: rgba(96, 165, 250, 0.2);
    color: #60a5fa;
    border-left: 3px solid #60a5fa;
}
.sidebar-nav a i { font-size: 1.2rem; opacity: 0.9; width: 22px; }

.sidebar-divider {
    height: 1px;
    background: rgba(255,255,255,0.1);
    margin: 16px 8px;
}

.admin-main {
    margin-left: 250px !important;
    padding: 28px 32px;
    min-height: 100vh;
    background: #f1f5f9;
}
body:not(.admin-page) .admin-main { margin-left: 0; }
</style>
