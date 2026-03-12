<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<style>
    /* White navbar style - dùng cho tất cả trang trừ home */
    .navbar-white {
        background-color: #fff !important;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        position: relative;
        z-index: 9999 !important;
    }
    .navbar-white .navbar-brand,
    .navbar-white .nav-link {
        color: #333 !important;
    }
    .navbar-white .nav-link:hover {
        color: #0b1a33 !important;
    }
    .navbar-white .navbar-toggler {
        border-color: #333;
    }
    .navbar-white .navbar-toggler-icon {
        filter: none !important; /* Không invert cho navbar trắng */
    }
    .navbar-white .btn-booking {
        background: linear-gradient(135deg, #0b1a33 0%, #1a3a5c 100%);
        color: white;
        border: none;
        padding: 8px 20px;
        border-radius: 25px;
        font-weight: 500;
        transition: all 0.3s;
    }
    .navbar-white .btn-booking:hover {
        transform: translateY(-2px);
        box-shadow: 0 5px 15px rgba(11, 26, 51, 0.3);
        color: white;
    }
    .navbar-white .btn-outline-light {
        color: #333 !important;
        border-color: #333 !important;
    }
    .navbar-white .btn-outline-light:hover {
        background-color: #0b1a33 !important;
        border-color: #0b1a33 !important;
        color: white !important;
    }
    .navbar-white .lang-select {
        color: #333 !important;
    }
    .navbar-white .dropdown-menu {
        z-index: 10000 !important;
    }
</style>

<nav class="navbar navbar-expand-lg navbar-white" id="navbar">
    <div class="container-fluid px-5"> 
        
        <a class="navbar-brand" href="${pageContext.request.contextPath}/home">
            <i class='bx bxs-plus-medical'></i> Animal Doctors
        </a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent">
            <span class="navbar-toggler-icon"></span> 
        </button>

        <div class="collapse navbar-collapse justify-content-center" id="navbarContent">
            <ul class="navbar-nav mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/about">Về Chúng Tôi</a>
                </li>
                
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/services">Các dịch vụ</a>
                </li>
                
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/community">Cộng đồng</a></li>
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/schedule">Đặt Hẹn</a></li>
            </ul>
        </div>

        <div class="d-flex align-items-center">
            <a href="${pageContext.request.contextPath}/booking" class="btn btn-booking text-decoration-none">Đặt Hẹn</a>
            
            <%-- Hiển thị nút đăng nhập/đăng xuất --%>
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <div class="dropdown ms-3">
                        <button class="btn btn-light dropdown-toggle" type="button" data-bs-toggle="dropdown">
                            <i class='bx bxs-user'></i> ${sessionScope.username}
                        </button>
                        <ul class="dropdown-menu">
                            <c:if test="${sessionScope.role == 'admin'}">
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/pages/admin/dashboard">
                                    <i class='bx bxs-dashboard'></i> Admin Panel
                                </a></li>
                            </c:if>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/schedule">
                                <i class='bx bxs-calendar'></i> Lịch hẹn của tôi
                            </a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/logout">
                                <i class='bx bx-log-out'></i> Đăng xuất
                            </a></li>
                        </ul>
                    </div>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-light ms-3">
                        <i class='bx bx-log-in'></i> Đăng nhập
                    </a>
                </c:otherwise>
            </c:choose>
            
            <div class="lang-select ms-3" style="cursor: pointer; font-weight: 600;">
                <i class='bx bx-world'></i> Vi <i class='bx bx-chevron-down'></i>
            </div>
        </div>

    </div>
</nav>
