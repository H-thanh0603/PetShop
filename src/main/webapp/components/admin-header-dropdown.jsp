<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- Admin Header Dropdown Component -->
<div class="admin-user-dropdown" id="adminUserDropdown">
    <div class="admin-badge" onclick="toggleAdminDropdown(event)">
        <i class='bx bxs-user-circle'></i> 
        ${sessionScope.user.fullname}
        <i class='bx bx-chevron-down dropdown-arrow'></i>
    </div>
    <div class="admin-dropdown-menu">
        <a href="${pageContext.request.contextPath}/home">
            <i class='bx bx-home'></i> Về trang chủ
        </a>
        <a href="${pageContext.request.contextPath}/logout" class="logout-link">
            <i class='bx bx-log-out'></i> Đăng xuất
        </a>
    </div>
</div>

<script>
function toggleAdminDropdown(e) {
    e.stopPropagation();
    document.getElementById('adminUserDropdown').classList.toggle('open');
}

// Close dropdown when clicking outside
document.addEventListener('click', function(e) {
    var dropdown = document.getElementById('adminUserDropdown');
    if (dropdown && !dropdown.contains(e.target)) {
        dropdown.classList.remove('open');
    }
});
</script>
