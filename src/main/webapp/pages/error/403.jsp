<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Access Denied</title>
    <jsp:include page="../../components/head.jsp" />
    <style>
        .error-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: calc(100vh - 200px);
            text-align: center;
        }
        .error-container h1 {
            font-size: 4rem;
            font-weight: bold;
            color: #dc3545;
        }
        .error-container p {
            font-size: 1.25rem;
            color: #6c757d;
        }
        .error-container .btn {
            margin-top: 20px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="error-container">
            <h1>403</h1>
            <p>Rất tiếc, bạn không có quyền truy cập vào trang này.</p>
            <c:choose>
                <c:when test="${sessionScope.user.role == 'shipper'}">
                    <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-primary">Quay về trang đơn hàng</a>
                </c:when>
                <c:when test="${not empty sessionScope.user.role}">
                    <a href="${pageContext.request.contextPath}/pages/admin/dashboard" class="btn btn-primary">Quay về Dashboard</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Quay về trang chủ</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>
