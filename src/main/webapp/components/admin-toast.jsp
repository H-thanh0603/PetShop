<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- 
    Component: admin-toast.jsp
    Mô tả: Toast notification cho admin - hiển thị message từ session
    Sử dụng: <jsp:include page="/components/admin-toast.jsp" />
--%>

<%-- Chuyển session messages sang request scope và xóa khỏi session --%>
<c:if test="${not empty sessionScope.message}">
    <c:set var="toastMessage" value="${sessionScope.message}" scope="request"/>
    <c:set var="toastType" value="${sessionScope.messageType}" scope="request"/>
    <c:remove var="message" scope="session"/>
    <c:remove var="messageType" scope="session"/>
</c:if>

<style>
    /* Admin Toast Container */
    .admin-toast-container {
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 999999;
        display: flex;
        flex-direction: column;
        gap: 12px;
        max-width: 420px;
        pointer-events: none;
    }
    
    /* Toast Item */
    .admin-toast {
        display: flex;
        align-items: center;
        gap: 14px;
        padding: 16px 20px;
        border-radius: 14px;
        box-shadow: 0 10px 40px rgba(0, 0, 0, 0.18), 0 4px 12px rgba(0, 0, 0, 0.12);
        animation: adminToastIn 0.4s cubic-bezier(0.16, 1, 0.3, 1);
        pointer-events: auto;
        min-width: 320px;
        position: relative;
        overflow: hidden;
    }
    
    .admin-toast.hiding {
        animation: adminToastOut 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }
    
    /* Toast Icon */
    .admin-toast .toast-icon {
        width: 44px;
        height: 44px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
    }
    
    .admin-toast .toast-icon i {
        font-size: 1.5rem;
    }
    
    /* Toast Content */
    .admin-toast .toast-content {
        flex: 1;
        min-width: 0;
    }
    
    .admin-toast .toast-title {
        font-weight: 700;
        font-size: 0.95rem;
        margin-bottom: 3px;
    }
    
    .admin-toast .toast-message {
        font-size: 0.9rem;
        opacity: 0.9;
        line-height: 1.4;
    }
    
    /* Toast Close */
    .admin-toast .toast-close {
        width: 30px;
        height: 30px;
        border-radius: 8px;
        border: none;
        background: rgba(0, 0, 0, 0.1);
        color: inherit;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.2s;
        flex-shrink: 0;
        opacity: 0.7;
    }
    
    .admin-toast .toast-close:hover {
        background: rgba(0, 0, 0, 0.2);
        opacity: 1;
        transform: scale(1.1);
    }
    
    /* Progress Bar */
    .admin-toast .toast-progress {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 4px;
    }
    
    .admin-toast .toast-progress-bar {
        height: 100%;
        animation: toastProgressBar 4s linear forwards;
    }
    
    /* Success Toast */
    .admin-toast.toast-success {
        background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
        color: #065f46;
        border: 1px solid #a7f3d0;
    }
    .admin-toast.toast-success .toast-icon {
        background: linear-gradient(135deg, #10b981, #059669);
        color: white;
    }
    .admin-toast.toast-success .toast-progress-bar {
        background: linear-gradient(90deg, #10b981, #059669);
    }
    
    /* Error Toast */
    .admin-toast.toast-error {
        background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
        color: #991b1b;
        border: 1px solid #fecaca;
    }
    .admin-toast.toast-error .toast-icon {
        background: linear-gradient(135deg, #ef4444, #dc2626);
        color: white;
    }
    .admin-toast.toast-error .toast-progress-bar {
        background: linear-gradient(90deg, #ef4444, #dc2626);
    }
    
    /* Warning Toast */
    .admin-toast.toast-warning {
        background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
        color: #92400e;
        border: 1px solid #fde68a;
    }
    .admin-toast.toast-warning .toast-icon {
        background: linear-gradient(135deg, #f59e0b, #d97706);
        color: white;
    }
    .admin-toast.toast-warning .toast-progress-bar {
        background: linear-gradient(90deg, #f59e0b, #d97706);
    }
    
    /* Info Toast */
    .admin-toast.toast-info {
        background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
        color: #1e40af;
        border: 1px solid #bfdbfe;
    }
    .admin-toast.toast-info .toast-icon {
        background: linear-gradient(135deg, #3b82f6, #2563eb);
        color: white;
    }
    .admin-toast.toast-info .toast-progress-bar {
        background: linear-gradient(90deg, #3b82f6, #2563eb);
    }
    
    /* Animations */
    @keyframes adminToastIn {
        from {
            opacity: 0;
            transform: translateX(100px) scale(0.9);
        }
        to {
            opacity: 1;
            transform: translateX(0) scale(1);
        }
    }
    
    @keyframes adminToastOut {
        from {
            opacity: 1;
            transform: translateX(0) scale(1);
        }
        to {
            opacity: 0;
            transform: translateX(100px) scale(0.9);
        }
    }
    
    @keyframes toastProgressBar {
        from { width: 100%; }
        to { width: 0%; }
    }
</style>

<!-- Admin Toast Container -->
<div class="admin-toast-container" id="adminToastContainer">
    <c:if test="${not empty toastMessage}">
        <c:set var="toastClass" value="${toastType == 'error' ? 'toast-error' : (toastType == 'warning' ? 'toast-warning' : (toastType == 'info' ? 'toast-info' : 'toast-success'))}"/>
        <c:set var="toastIcon" value="${toastType == 'error' ? 'bx-x-circle' : (toastType == 'warning' ? 'bx-error' : (toastType == 'info' ? 'bx-info-circle' : 'bx-check-circle'))}"/>
        <c:set var="toastTitle" value="${toastType == 'error' ? 'Lỗi!' : (toastType == 'warning' ? 'Cảnh báo!' : (toastType == 'info' ? 'Thông tin' : 'Thành công!'))}"/>
        
        <div class="admin-toast ${toastClass}">
            <div class="toast-icon">
                <i class='bx ${toastIcon}'></i>
            </div>
            <div class="toast-content">
                <div class="toast-title">${toastTitle}</div>
                <div class="toast-message">${toastMessage}</div>
            </div>
            <button class="toast-close" onclick="closeAdminToast(this)">
                <i class='bx bx-x'></i>
            </button>
            <div class="toast-progress">
                <div class="toast-progress-bar"></div>
            </div>
        </div>
    </c:if>
</div>

<script>
    // Close toast
    function closeAdminToast(btn) {
        var toast = btn.closest('.admin-toast');
        toast.classList.add('hiding');
        setTimeout(function() {
            toast.remove();
        }, 400);
    }
    
    // Auto-hide after 4 seconds
    document.addEventListener('DOMContentLoaded', function() {
        var toasts = document.querySelectorAll('.admin-toast');
        toasts.forEach(function(toast) {
            setTimeout(function() {
                if (toast && toast.parentNode) {
                    toast.classList.add('hiding');
                    setTimeout(function() {
                        if (toast && toast.parentNode) {
                            toast.remove();
                        }
                    }, 400);
                }
            }, 4000);
        });
    });
    
    // Global function to show admin toast
    function showAdminToast(type, message) {
        var container = document.getElementById('adminToastContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'adminToastContainer';
            container.className = 'admin-toast-container';
            document.body.appendChild(container);
        }
        
        var config = {
            success: { icon: 'bx-check-circle', title: 'Thành công!' },
            error: { icon: 'bx-x-circle', title: 'Lỗi!' },
            warning: { icon: 'bx-error', title: 'Cảnh báo!' },
            info: { icon: 'bx-info-circle', title: 'Thông tin' }
        };
        
        var c = config[type] || config.success;
        
        var toast = document.createElement('div');
        toast.className = 'admin-toast toast-' + type;
        toast.innerHTML = 
            '<div class="toast-icon"><i class="bx ' + c.icon + '"></i></div>' +
            '<div class="toast-content">' +
                '<div class="toast-title">' + c.title + '</div>' +
                '<div class="toast-message">' + message + '</div>' +
            '</div>' +
            '<button class="toast-close" onclick="closeAdminToast(this)"><i class="bx bx-x"></i></button>' +
            '<div class="toast-progress"><div class="toast-progress-bar"></div></div>';
        
        container.appendChild(toast);
        
        setTimeout(function() {
            if (toast && toast.parentNode) {
                toast.classList.add('hiding');
                setTimeout(function() {
                    if (toast && toast.parentNode) {
                        toast.remove();
                    }
                }, 400);
            }
        }, 4000);
    }
</script>
