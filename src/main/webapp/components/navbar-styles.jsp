<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 
    Component: navbar-styles.jsp
    Mô tả: CSS styles cho navbar/header
    Sử dụng: <jsp:include page="/components/navbar-styles.jsp" />
--%>

<style>
    /* Navbar Styles */
    #navbar {
        background-color: #0b1a33;
        padding: 15px 0;
        position: relative;
        z-index: 9999 !important;
    }
    
    #navbar .navbar-brand {
        color: #fff;
        font-weight: bold;
        font-size: 1.5rem;
    }
    
    #navbar .navbar-brand i {
        color: #dc3545;
    }
    
    #navbar .nav-link {
        color: rgba(255,255,255,0.85);
        font-weight: 500;
        padding: 8px 16px;
        transition: color 0.3s;
    }
    
    #navbar .nav-link:hover {
        color: #fff;
    }
    
    #navbar .btn-booking {
        background-color: #dc3545;
        color: #fff;
        border: none;
        padding: 8px 20px;
        border-radius: 5px;
        font-weight: 500;
    }
    
    #navbar .btn-booking:hover {
        background-color: #c82333;
        color: #fff;
    }
    
    #navbar .dropdown-menu {
        z-index: 10000 !important;
        border: none;
        box-shadow: 0 4px 15px rgba(0,0,0,0.15);
    }
    
    #navbar .dropdown-item {
        padding: 10px 20px;
    }
    
    #navbar .dropdown-item:hover {
        background-color: #f8f9fa;
    }
    
    #navbar .dropdown-item i {
        margin-right: 8px;
        color: #0b1a33;
    }
    
    .lang-select {
        cursor: pointer;
        font-weight: 600;
    }
</style>
