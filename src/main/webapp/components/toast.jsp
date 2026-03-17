<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:if test="${not empty sessionScope.toastMessage}">
    
    <style>
        .custom-toast-container {
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 105000;
        }
        .custom-toast {
            min-width: 320px;
            max-width: 420px;
            background: white;
            border-radius: 16px;
            box-shadow: 0 10px 40px rgba(0, 77, 64, 0.25);
            overflow: hidden;
            animation: slideInRight 0.4s ease-out;
            border: none;
        }
        @keyframes slideInRight {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        .toast-success {
            border-left: 5px solid #00bfa5;
        }
        .toast-error {
            border-left: 5px solid #ef4444;
        }
        .toast-warning {
            border-left: 5px solid #f59e0b;
        }
        .toast-info {
            border-left: 5px solid #3b82f6;
        }
        .custom-toast .toast-body-custom {
            display: flex;
            align-items: center;
            gap: 15px;
            padding: 18px 20px;
        }
        .toast-icon {
            width: 45px;
            height: 45px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
        }
        .toast-success .toast-icon {
            background: linear-gradient(135deg, #e0f7f4 0%, #b2dfdb 100%);
            color: #00bfa5;
        }
        .toast-error .toast-icon {
            background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
            color: #ef4444;
        }
        .toast-warning .toast-icon {
            background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
            color: #f59e0b;
        }
        .toast-info .toast-icon {
            background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
            color: #3b82f6;
        }
        .toast-icon i {
            font-size: 1.5rem;
        }
        .toast-content {
            flex: 1;
        }
        .toast-title {
            font-weight: 700;
            font-size: 0.95rem;
            margin-bottom: 3px;
        }
        .toast-success .toast-title {
            color: #004d40;
        }
        .toast-error .toast-title {
            color: #b91c1c;
        }
        .toast-warning .toast-title {
            color: #92400e;
        }
        .toast-info .toast-title {
            color: #1e40af;
        }
        .toast-message {
            font-size: 0.9rem;
            color: #555;
            line-height: 1.4;
        }
        .toast-close {
            background: none;
            border: none;
            color: #999;
            font-size: 1.3rem;
            cursor: pointer;
            padding: 5px;
            margin-left: 10px;
            transition: all 0.2s;
            border-radius: 8px;
        }
        .toast-close:hover {
            background: #f5f5f5;
            color: #333;
        }
        .toast-progress {
            height: 4px;
            background: #e0e0e0;
        }
        .toast-progress-bar {
            height: 100%;
            animation: progressShrink 5s linear forwards;
        }
        .toast-success .toast-progress-bar {
            background: linear-gradient(90deg, #00bfa5, #004d40);
        }
        .toast-error .toast-progress-bar {
            background: linear-gradient(90deg, #ef4444, #b91c1c);
        }
        .toast-warning .toast-progress-bar {
            background: linear-gradient(90deg, #f59e0b, #d97706);
        }
        .toast-info .toast-progress-bar {
            background: linear-gradient(90deg, #3b82f6, #1d4ed8);
        }
        @keyframes progressShrink {
            from { width: 100%; }
            to { width: 0%; }
        }
    </style>
    
    <div class="custom-toast-container">
        <div id="customToast" class="custom-toast toast-${sessionScope.toastType == 'error' ? 'error' : (sessionScope.toastType == 'warning' ? 'warning' : (sessionScope.toastType == 'info' ? 'info' : 'success'))}">
            <div class="toast-body-custom">
                <div class="toast-icon">
                    <c:choose>
                        <c:when test="${sessionScope.toastType == 'error'}">
                            <i class='bx bxs-x-circle'></i>
                        </c:when>
                        <c:when test="${sessionScope.toastType == 'warning'}">
                            <i class='bx bxs-error'></i>
                        </c:when>
                        <c:when test="${sessionScope.toastType == 'info'}">
                            <i class='bx bxs-info-circle'></i>
                        </c:when>
                        <c:otherwise>
                            <i class='bx bxs-check-circle'></i>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="toast-content">
                    <div class="toast-title">
                        <c:choose>
                            <c:when test="${sessionScope.toastType == 'error'}">Có lỗi xảy ra!</c:when>
                            <c:when test="${sessionScope.toastType == 'warning'}">Cảnh báo</c:when>
                            <c:when test="${sessionScope.toastType == 'info'}">Thông tin</c:when>
                            <c:otherwise>Thành công!</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="toast-message">${sessionScope.toastMessage}</div>
                </div>
                <button type="button" class="toast-close" onclick="closeToast()">
                    <i class='bx bx-x'></i>
                </button>
            </div>
            <div class="toast-progress">
                <div class="toast-progress-bar"></div>
            </div>
        </div>
    </div>

    <script>
        function closeToast() {
            const toast = document.getElementById('customToast');
            if (toast) {
                toast.style.animation = 'slideOutRight 0.3s ease-in forwards';
                setTimeout(() => {
                    toast.parentElement.remove();
                }, 300);
            }
        }
        
        // Auto close after 5 seconds
        setTimeout(closeToast, 5000);
        
        // Add slide out animation
        const style = document.createElement('style');
        style.textContent = `
            @keyframes slideOutRight {
                from {
                    transform: translateX(0);
                    opacity: 1;
                }
                to {
                    transform: translateX(100%);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(style);
    </script>

    <% 
        session.removeAttribute("toastMessage");
        session.removeAttribute("toastType");
    %>
</c:if>
