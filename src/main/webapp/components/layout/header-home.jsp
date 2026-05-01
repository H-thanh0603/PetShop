<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- Header cho trang home - style transparent --%>
<style>
    #navbar {
        position: fixed !important;
        top: 0 !important;
        left: 0 !important;
        width: 100% !important;
        z-index: 99999 !important;
        background: transparent !important;
        background-color: transparent !important;
        transition: background 0.3s ease;
    }
    #navbar.scrolled {
        background: rgba(255,255,255,0.95) !important;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }
</style>

<jsp:include page="/components/navbar.jsp" />

<script>
window.addEventListener('scroll', function() {
    var navbar = document.getElementById('navbar');
    if (navbar) {
        if (window.scrollY > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    }
});
</script>
