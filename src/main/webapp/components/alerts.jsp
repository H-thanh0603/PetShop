<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- 
    Component: alerts.jsp
    Mô tả: Hiển thị thông báo success/error/warning - tự động ẩn sau 3 giây
    Sử dụng: <jsp:include page="/components/alerts.jsp" />
--%>

<c:if test="${not empty error}">
    <div class="alert alert-danger alert-dismissible fade show alert-auto-hide" role="alert">
        <i class='bx bx-error-circle'></i> ${error}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<c:if test="${not empty success}">
    <div class="alert alert-success alert-dismissible fade show alert-auto-hide" role="alert">
        <i class='bx bx-check-circle'></i> ${success}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<c:if test="${not empty warning}">
    <div class="alert alert-warning alert-dismissible fade show alert-auto-hide" role="alert">
        <i class='bx bx-info-circle'></i> ${warning}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<script>
    // Tự động ẩn thông báo sau 3 giây
    document.addEventListener('DOMContentLoaded', function() {
        var alerts = document.querySelectorAll('.alert-auto-hide');
        alerts.forEach(function(alert) {
            setTimeout(function() {
                alert.style.transition = 'opacity 0.5s ease';
                alert.style.opacity = '0';
                setTimeout(function() {
                    alert.remove();
                }, 500);
            }, 3000);
        });
    });
</script>
