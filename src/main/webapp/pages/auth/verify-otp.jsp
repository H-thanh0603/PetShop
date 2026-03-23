<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Xác thực OTP - PetShop</title>
    <jsp:include page="/components/head.jsp" />
    <style>
        body {
            background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .otp-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.15);
            padding: 40px;
            max-width: 420px;
            width: 100%;
        }
        .otp-title {
            color: #1976d2;
            font-weight: bold;
            margin-bottom: 10px;
            text-align: center;
        }
        .otp-subtitle {
            color: #666;
            text-align: center;
            margin-bottom: 30px;
            font-size: 0.95rem;
        }
        .otp-email {
            color: #1976d2;
            font-weight: bold;
        }
        .btn-submit {
            background: #1976d2;
            border: none;
            color: white;
            padding: 12px;
            font-weight: bold;
        }
        .btn-submit:hover {
            background: #1565c0;
            color: white;
        }
        .form-control:focus {
            border-color: #1976d2;
            box-shadow: 0 0 0 0.2rem rgba(25, 118, 210, 0.25);
        }
        .otp-input {
            text-align: center;
            font-size: 24px;
            letter-spacing: 10px;
            font-weight: bold;
        }
        a { color: #1976d2; }
        a:hover { color: #1565c0; }
        .icon-header {
            text-align: center;
            margin-bottom: 20px;
        }
        .icon-header i {
            font-size: 60px;
            color: #1976d2;
        }
        .alert {
            border-radius: 10px;
        }
        .resend-link {
            font-size: 0.9rem;
        }
    </style>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>
    <div class="otp-container">
        <div class="icon-header">
            <i class='bx bx-message-square-check'></i>
        </div>
        <h2 class="otp-title">Nhập mã xác thực</h2>
        <p class="otp-subtitle">
            Mã OTP đã được gửi đến<br>
            <span class="otp-email">${sessionScope.resetEmail}</span>
        </p>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class='bx bx-error-circle me-2'></i>${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>
        
        <form method="post" action="${pageContext.request.contextPath}/verify-otp">
            <div class="mb-4">
                <label class="form-label fw-bold">Mã OTP (6 số)</label>
                <input type="text" class="form-control otp-input" name="otp" 
                       maxlength="6" pattern="[0-9]{6}" required
                       placeholder="------" autocomplete="one-time-code">
            </div>
            
            <button type="submit" class="btn btn-submit w-100 mb-3">
                <i class='bx bx-check-circle me-1'></i> Xác nhận
            </button>
            
            <div class="text-center resend-link">
                <span class="text-muted">Không nhận được mã?</span>
                <a href="${pageContext.request.contextPath}/forgot-password" class="text-decoration-none">
                    Gửi lại
                </a>
            </div>
            
            <hr class="my-3">
            
            <div class="text-center">
                <a href="${pageContext.request.contextPath}/login" class="text-decoration-none text-muted">
                    <i class='bx bx-arrow-back'></i> Quay lại đăng nhập
                </a>
            </div>
        </form>
    </div>
    
    <jsp:include page="/components/scripts.jsp" />
    <script>
        // Auto focus và format OTP input
        const otpInput = document.querySelector('.otp-input');
        otpInput.addEventListener('input', function(e) {
            this.value = this.value.replace(/[^0-9]/g, '');
        });
        otpInput.focus();
    </script>
</body>
</html>
