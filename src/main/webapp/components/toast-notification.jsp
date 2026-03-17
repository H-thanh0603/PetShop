<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- 
    Component: toast-notification.jsp
    Mô tả: Toast notification hiển thị góc trên bên phải - tự động ẩn sau 3 giây
    Sử dụng: <jsp:include page="/components/toast-notification.jsp" />
--%>

<style>
    /* Toast Container - Fixed position top right */
    .toast-notification-container {
        position: fixed;
        top: 90px;
        right: 20px;
        z-index: 999999;
        display: flex;
        flex-direction: column;
        gap: 12px;
        max-width: 400px;
        pointer-events: none;
    }
    
    /* Toast Item */
    .toast-notification {
        display: flex;
        align-items: center;
        gap: 14px;
        padding: 16px 20px;
        border-radius: 14px;
        box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15), 0 4px 12px rgba(0, 0, 0, 0.1);
        backdrop-filter: blur(10px);
        animation: toastSlideIn 0.4s cubic-bezier(0.16, 1, 0.3, 1);
        pointer-events: auto;
        min-width: 300px;
        border: 1px solid rgba(255, 255, 255, 0.2);
    }
    
    .toast-notification.hiding {
        animation: toastSlideOut 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }
    
    /* Toast Icon */
    .toast-notification .toast-icon {
        width: 42px;
        height: 42px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
    }
    
    .toast-notification .toast-icon i {
        font-size: 1.5rem;
    }
    
    /* Toast Content */
    .toast-notification .toast-content {
        flex: 1;
        min-width: 0;
    }
    
    .toast-notification .toast-title {
        font-weight: 700;
        font-size: 0.95rem;
        margin-bottom: 2px;
    }
    
    .toast-notification .toast-message {
        font-size: 0.9rem;
        opacity: 0.9;
        line-height: 1.4;
        word-wrap: break-word;
    }
    
    /* Toast Close Button */
    .toast-notification .toast-close {
        width: 28px;
        height: 28px;
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
    
    .toast-notification .toast-close:hover {
        background: rgba(0, 0, 0, 0.2);
        opacity: 1;
    }
    
    /* Progress Bar */
    .toast-notification .toast-progress {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 3px;
        border-radius: 0 0 14px 14px;
        overflow: hidden;
    }
    
    .toast-notification .toast-progress-bar {
        height: 100%;
        animation: toastProgress 3s linear forwards;
    }
    
    /* Success Toast */
    .toast-notification.toast-success {
        background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
        color: #065f46;
    }
    
    .toast-notification.toast-success .toast-icon {
        background: linear-gradient(135deg, #10b981, #059669);
        color: white;
    }
    
    .toast-notification.toast-success .toast-progress-bar {
        background: linear-gradient(90deg, #10b981, #059669);
    }
    
    /* Error Toast */
    .toast-notification.toast-error {
        background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
        color: #991b1b;
    }
    
    .toast-notification.toast-error .toast-icon {
        background: linear-gradient(135deg, #ef4444, #dc2626);
        color: white;
    }
    
    .toast-notification.toast-error .toast-progress-bar {
        background: linear-gradient(90deg, #ef4444, #dc2626);
    }
    
    /* Warning Toast */
    .toast-notification.toast-warning {
        background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
        color: #92400e;
    }
    
    .toast-notification.toast-warning .toast-icon {
        background: linear-gradient(135deg, #f59e0b, #d97706);
        color: white;
    }
    
    .toast-notification.toast-warning .toast-progress-bar {
        background: linear-gradient(90deg, #f59e0b, #d97706);
    }
    
    /* Info Toast */
    .toast-notification.toast-info {
        background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
        color: #1e40af;
    }
    
    .toast-notification.toast-info .toast-icon {
        background: linear-gradient(135deg, #3b82f6, #2563eb);
        color: white;
    }
    
    .toast-notification.toast-info .toast-progress-bar {
        background: linear-gradient(90deg, #3b82f6, #2563eb);
    }
    
    /* Animations */
    @keyframes toastSlideIn {
        from {
            opacity: 0;
            transform: translateX(100px) scale(0.9);
        }
        to {
            opacity: 1;
            transform: translateX(0) scale(1);
        }
    }
    
    @keyframes toastSlideOut {
        from {
            opacity: 1;
            transform: translateX(0) scale(1);
        }
        to {
            opacity: 0;
            transform: translateX(100px) scale(0.9);
        }
    }
    
    @keyframes toastProgress {
        from { width: 100%; }
        to { width: 0%; }
    }
    
    /* Responsive */
    @media (max-width: 480px) {
        .toast-notification-container {
            left: 12px;
            right: 12px;
            max-width: none;
        }
        
        .toast-notification {
            min-width: auto;
        }
    }
</style>

<!-- Toast Container -->
<div class="toast-notification-container" id="toastContainer">
    <c:if test="${not empty success}">
        <div class="toast-notification toast-success" style="position: relative;">
            <div class="toast-icon">
                <i class='bx bx-check'></i>
            </div>
            <div class="toast-content">
                <div class="toast-title">Thành công!</div>
                <div class="toast-message">${success}</div>
            </div>
            <button class="toast-close" onclick="closeToast(this)">
                <i class='bx bx-x'></i>
            </button>
            <div class="toast-progress">
                <div class="toast-progress-bar"></div>
            </div>
        </div>
    </c:if>
    
    <c:if test="${not empty error}">
        <div class="toast-notification toast-error" style="position: relative;">
            <div class="toast-icon">
                <i class='bx bx-x'></i>
            </div>
            <div class="toast-content">
                <div class="toast-title">Lỗi!</div>
                <div class="toast-message">${error}</div>
            </div>
            <button class="toast-close" onclick="closeToast(this)">
                <i class='bx bx-x'></i>
            </button>
            <div class="toast-progress">
                <div class="toast-progress-bar"></div>
            </div>
        </div>
    </c:if>
    
    <c:if test="${not empty warning}">
        <div class="toast-notification toast-warning" style="position: relative;">
            <div class="toast-icon">
                <i class='bx bx-error'></i>
            </div>
            <div class="toast-content">
                <div class="toast-title">Cảnh báo!</div>
                <div class="toast-message">${warning}</div>
            </div>
            <button class="toast-close" onclick="closeToast(this)">
                <i class='bx bx-x'></i>
            </button>
            <div class="toast-progress">
                <div class="toast-progress-bar"></div>
            </div>
        </div>
    </c:if>
    
    <c:if test="${not empty info}">
        <div class="toast-notification toast-info" style="position: relative;">
            <div class="toast-icon">
                <i class='bx bx-info-circle'></i>
            </div>
            <div class="toast-content">
                <div class="toast-title">Thông tin</div>
                <div class="toast-message">${info}</div>
            </div>
            <button class="toast-close" onclick="closeToast(this)">
                <i class='bx bx-x'></i>
            </button>
            <div class="toast-progress">
                <div class="toast-progress-bar"></div>
            </div>
        </div>
    </c:if>
</div>

<script>
    // Close toast manually
    function closeToast(btn) {
        var toast = btn.closest('.toast-notification');
        toast.classList.add('hiding');
        setTimeout(function() {
            toast.remove();
        }, 400);
    }
    
    // Auto-hide toasts after 3 seconds
    document.addEventListener('DOMContentLoaded', function() {
        var toasts = document.querySelectorAll('.toast-notification');
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
            }, 3000);
        });
    });
    
    // Global function to show toast programmatically
    function showToast(type, title, message) {
        var container = document.getElementById('toastContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toastContainer';
            container.className = 'toast-notification-container';
            document.body.appendChild(container);
        }
        
        var icons = {
            success: 'bx-check',
            error: 'bx-x',
            warning: 'bx-error',
            info: 'bx-info-circle'
        };
        
        var toast = document.createElement('div');
        toast.className = 'toast-notification toast-' + type;
        toast.style.position = 'relative';
        toast.innerHTML = 
            '<div class="toast-icon"><i class="bx ' + icons[type] + '"></i></div>' +
            '<div class="toast-content">' +
                '<div class="toast-title">' + title + '</div>' +
                '<div class="toast-message">' + message + '</div>' +
            '</div>' +
            '<button class="toast-close" onclick="closeToast(this)"><i class="bx bx-x"></i></button>' +
            '<div class="toast-progress"><div class="toast-progress-bar"></div></div>';
        
        container.appendChild(toast);
        
        // Auto-hide after 3 seconds
        setTimeout(function() {
            if (toast && toast.parentNode) {
                toast.classList.add('hiding');
                setTimeout(function() {
                    if (toast && toast.parentNode) {
                        toast.remove();
                    }
                }, 400);
            }
        }, 3000);
    }
</script>