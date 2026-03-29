<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- Header cho trang home - style transparent với mẫu mã PetShop --%>

<style>
    /* CRITICAL: Override navbar background cho trang home */
    #navbar {
        position: fixed !important;
        top: 0 !important;
        left: 0 !important;
        width: 100% !important;
        z-index: 99999 !important;
        background: transparent !important;
        background-color: transparent !important;
        transition: all 0.4s ease !important;
        padding: 15px 0 !important;
        border-bottom: 1px solid rgba(255,255,255,0.1) !important;
    }
    
    #navbar.navbar-scrolled {
        background: #0b1a33 !important;
        background-color: #0b1a33 !important;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2) !important;
        border-bottom: none !important;
        padding: 12px 0 !important;
    }
    
    #navbar .navbar-brand {
        color: #ffffff !important;
        font-weight: 700 !important;
        font-size: 1.5rem !important;
        display: flex !important;
        align-items: center !important;
        gap: 10px !important;
    }
    #navbar .navbar-brand i { color: #00ffcc !important; }
    
    #navbar .nav-link {
        color: rgba(255,255,255,0.9) !important;
        font-weight: 600 !important;
        font-size: 0.95rem !important;
        padding: 10px 18px !important;
        transition: color 0.2s !important;
    }
    #navbar .nav-link:hover {
        color: #00ffcc !important;
    }
    
    #navbar .navbar-collapse {
        background: transparent !important;
    }

    #navbar .nav-search-form {
        flex-shrink: 0;
    }
    #navbar .nav-search-bar {
        display: flex;
        align-items: center;
        gap: 8px;
        min-width: 260px;
        padding: 8px 16px;
        border-radius: 50px;
        border: 1px solid rgba(255,255,255,0.25);
        background: rgba(255,255,255,0.12);
        backdrop-filter: blur(12px);
        -webkit-backdrop-filter: blur(12px);
        transition: all 0.3s ease;
    }
    #navbar .nav-search-bar:focus-within {
        border-color: rgba(0,255,204,0.7);
        background: rgba(255,255,255,0.18);
        box-shadow: 0 0 0 3px rgba(0,255,204,0.12);
    }
    #navbar .nav-search-bar i {
        color: rgba(255,255,255,0.9) !important;
        font-size: 1.1rem;
    }
    #navbar .nav-search-bar input {
        width: 100%;
        border: none;
        outline: none;
        background: transparent;
        color: #ffffff;
        font-size: 0.9rem;
        font-weight: 500;
    }
    #navbar .nav-search-bar input::placeholder {
        color: rgba(255,255,255,0.72);
    }

    #navbar.navbar-scrolled .nav-search-bar {
        border-color: #d8e3ec;
        background: #ffffff;
        box-shadow: 0 8px 24px rgba(11, 26, 51, 0.08);
    }
    #navbar.navbar-scrolled .nav-search-bar:focus-within {
        border-color: #00bfa5;
        box-shadow: 0 0 0 3px rgba(0, 191, 165, 0.12);
    }
    #navbar.navbar-scrolled .nav-search-bar i {
        color: #64748b !important;
    }
    #navbar.navbar-scrolled .nav-search-bar input {
        color: #0f172a;
    }
    #navbar.navbar-scrolled .nav-search-bar input::placeholder {
        color: #94a3b8;
    }
    
    /* Dropdown Menu */
    #navbar .dropdown-menu {
        display: none !important;
        position: absolute !important;
        z-index: 999999 !important;
        border: 1px solid #e2e8f0 !important;
        box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15) !important;
        border-radius: 12px !important;
        padding: 10px 0 !important;
        margin-top: 15px !important;
        background: #ffffff !important;
        min-width: 220px !important;
        top: 100% !important;
    }
    #navbar .dropdown-menu-end {
        right: 0 !important;
        left: auto !important;
    }
    #navbar .dropdown-menu.show {
        display: block !important;
    }
    #navbar .dropdown-item {
        padding: 12px 20px !important;
        color: #334155 !important;
        font-size: 0.95rem !important;
        font-weight: 500 !important;
        display: flex !important;
        align-items: center !important;
        transition: all 0.2s !important;
    }
    #navbar .dropdown-item:hover {
        background-color: #f1f5f9 !important;
        color: #0b1a33 !important;
    }
    #navbar .dropdown-item i {
        margin-right: 12px !important;
        color: #00bfa5 !important;
        width: 18px !important;
        font-size: 1.1rem !important;
    }
    #navbar .dropdown-divider {
        margin: 8px 12px !important;
    }
    
    /* Buttons */
    #navbar .btn-nav-user {
        background: rgba(255,255,255,0.1) !important;
        border: 2px solid rgba(255,255,255,0.3) !important;
        color: white !important;
        padding: 10px 20px !important;
        border-radius: 50px !important;
        font-weight: 600 !important;
        display: inline-flex !important;
        align-items: center !important;
        gap: 8px !important;
        transition: all 0.2s ease !important;
    }
    #navbar .btn-nav-user:hover {
        background: rgba(255,255,255,0.2) !important;
        border-color: white !important;
    }
    
    #navbar .btn-nav-login {
        color: white !important;
        border: 2px solid rgba(255,255,255,0.5) !important;
        padding: 10px 22px !important;
        border-radius: 50px !important;
        font-weight: 600 !important;
        text-decoration: none !important;
        display: inline-flex !important;
        align-items: center !important;
        gap: 8px !important;
    }
    #navbar .btn-nav-login:hover {
        background: white !important;
        color: #0b1a33 !important;
    }

    #navbar .btn-nav-cart {
        color: white !important;
        font-size: 1.6rem !important;
        position: relative !important;
        padding: 8px !important;
        transition: all 0.2s ease !important;
        display: flex !important;
        align-items: center !important;
    }
    #navbar .btn-nav-cart:hover {
        color: #00ffcc !important;
    }
    #navbar .cart-badge {
        position: absolute !important;
        top: 0px !important;
        right: 0px !important;
        font-size: 0.65rem !important;
        padding: 0.25em 0.5em !important;
        background-color: #ef4444 !important;
        color: white !important;
        border-radius: 50rem !important;
        border: 2px solid rgba(255,255,255,0.8) !important;
        transform: translate(25%, -25%) !important;
    }

    @media (max-width: 991px) {
        #navbar .navbar-collapse {
            background: #ffffff !important;
            padding: 20px !important;
            border-radius: 16px !important;
            margin-top: 15px !important;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2) !important;
        }
        #navbar .nav-link { color: #334155 !important; }
        #navbar .btn-nav-user, #navbar .btn-nav-login { 
            width: 100% !important; 
            justify-content: center !important;
            color: #334155 !important;
            border-color: #e2e8f0 !important;
        }
        #navbar .btn-nav-cart { color: #334155 !important; margin: 0 auto; }
        #navbar .nav-search-form { display: none !important; }
    }
</style>

<nav class="navbar navbar-expand-lg" id="navbar">
    <div class="container-fluid px-lg-5"> 
        
        <a class="navbar-brand" href="${pageContext.request.contextPath}/home">
            <i class='bx bxs-dog'></i> PetShop
        </a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent">
            <span class="navbar-toggler-icon" style="filter: invert(1);"></span> 
        </button>

        <div class="collapse navbar-collapse justify-content-center" id="navbarContent">
            <ul class="navbar-nav mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/home">Trang chủ</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/shop">Sản phẩm</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/about">Về chúng tôi</a>
                </li>
            </ul>
        </div>

        <form action="${pageContext.request.contextPath}/shop" method="get" class="nav-search-form d-none d-lg-flex ms-auto me-3">
            <div class="nav-search-bar">
                <i class='bx bx-search'></i>
                <input type="text" name="search" placeholder="Tìm kiếm sản phẩm..." autocomplete="off" value="${param.search}">
            </div>
        </form>

        <div class="d-flex align-items-center gap-3">
            
            <a href="${pageContext.request.contextPath}/pages/shop/cart.jsp" class="btn-nav-cart text-decoration-none">
                <i class='bx bx-cart'></i>
                <c:if test="${not empty sessionScope.totalQuantity and sessionScope.totalQuantity > 0}">
                    <span class="cart-badge">${sessionScope.totalQuantity}</span>
                </c:if>
            </a>
            
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <div class="dropdown">
                        <button class="btn-nav-user dropdown-toggle" type="button" id="userBtn" aria-expanded="false">
                            <i class='bx bxs-user'></i> ${sessionScope.username}
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end">
                            <c:if test="${sessionScope.role == 'admin'}">
                                <li>
                                    <a class="dropdown-item" href="${pageContext.request.contextPath}/pages/admin/dashboard">
                                        <i class='bx bxs-dashboard'></i> Admin Panel
                                    </a>
                                </li>
                                <li><hr class="dropdown-divider"></li>
                            </c:if>
                            <c:if test="${sessionScope.role != 'admin'}">
                                <li>
                                    <a class="dropdown-item" href="${pageContext.request.contextPath}/my-orders">
                                        <i class='bx bx-package'></i> Đơn hàng của tôi
                                    </a>
                                </li>
                                <li>
                                    <a class="dropdown-item" href="${pageContext.request.contextPath}/pages/shop/cart.jsp">
                                        <i class='bx bx-cart'></i> Giỏ hàng của tôi
                                    </a>
                                </li>
                                <li><hr class="dropdown-divider"></li>
                            </c:if>
                            <li>
                                <a class="dropdown-item" href="${pageContext.request.contextPath}/logout">
                                    <i class='bx bx-log-out'></i> Đăng xuất
                                </a>
                            </li>
                        </ul>
                    </div>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login" class="btn-nav-login">
                        <i class='bx bx-log-in'></i> Đăng nhập
                    </a>
                </c:otherwise>
            </c:choose>
            
            <div class="lang-select ms-2 d-none d-lg-flex" style="cursor: pointer; color: white; font-weight: 600; align-items: center; gap: 5px;">
                <i class='bx bx-globe'></i> Vi <i class='bx bx-chevron-down'></i>
            </div>
        </div>

    </div>
</nav>

<script>
document.addEventListener('DOMContentLoaded', function() {
    // Scroll effect
    window.addEventListener('scroll', function() {
        const nav = document.getElementById('navbar');
        if (window.scrollY > 50) {
            nav.classList.add('navbar-scrolled');
        } else {
            nav.classList.remove('navbar-scrolled');
        }
    });

    // Dropdown fix
    var dropdownToggles = document.querySelectorAll('#navbar .dropdown-toggle');
    dropdownToggles.forEach(function(toggle) {
        toggle.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            var menu = this.nextElementSibling;
            var isOpen = menu.classList.contains('show');
            
            document.querySelectorAll('#navbar .dropdown-menu.show').forEach(m => m.classList.remove('show'));
            if (!isOpen) menu.classList.add('show');
        });
    });

    document.addEventListener('click', function(e) {
        if (!e.target.closest('#navbar .dropdown')) {
            document.querySelectorAll('#navbar .dropdown-menu.show').forEach(m => m.classList.remove('show'));
        }
    });
});
</script>
