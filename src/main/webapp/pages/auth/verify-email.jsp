<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Xác thực email - PetShop</title>
    <jsp:include page="/components/head.jsp" />
    <style>
        body { background: linear-gradient(135deg,#e3f2fd,#bbdefb); min-height:100vh; display:flex; align-items:center; justify-content:center; }
        .card { background:#fff; border-radius:16px; box-shadow:0 10px 40px rgba(0,0,0,.15); padding:40px; max-width:440px; width:100%; text-align:center; }
        .icon-wrap { width:80px; height:80px; border-radius:50%; display:grid; place-items:center; font-size:2rem; margin:0 auto 20px; }
        .icon-error { background:#fee2e2; color:#dc2626; }
        .btn-primary { background:#1976d2; border:none; color:#fff; padding:12px 28px; border-radius:8px; font-weight:700; text-decoration:none; display:inline-block; }
        .btn-primary:hover { background:#1565c0; color:#fff; }
        .btn-outline { border:2px solid #1976d2; color:#1976d2; padding:10px 24px; border-radius:8px; font-weight:700; background:transparent; cursor:pointer; }
    </style>
</head>
<body>
<div class="card">
    <div class="icon-wrap icon-error"><i class='bx bx-error-circle'></i></div>
    <h3 class="fw-bold mb-3">Xác thực email thất bại</h3>
    <p class="text-muted mb-4">${verifyError}</p>

    <c:if test="${not empty expiredEmail}">
        <p class="mb-3">Bạn muốn nhận lại email xác thực?</p>
        <form action="${pageContext.request.contextPath}/verify-email" method="post">
            <input type="hidden" name="email" value="${expiredEmail}">
            <button type="submit" class="btn-primary mb-3">Gửi lại email xác thực</button>
        </form>
        <br>
    </c:if>

    <a href="${pageContext.request.contextPath}/login" class="btn-outline">Quay lại đăng nhập</a>
</div>
<jsp:include page="/components/scripts.jsp" />
</body>
</html>
