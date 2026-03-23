<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Quên mật khẩu - PetShop</title>
    <jsp:include page="/components/head.jsp" />
    <style>
        body {
            background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .forgot-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.15);
            padding: 40px;
            max-width: 420px;
            width: 100%;
        }
        .forgot-title {
            color: #1976d2;
            font-weight: bold;
            margin-bottom: 10px;
            text-align: center;
        }
        .forgot-subtitle {
            color: #666;
            text-align: center;
            margin-bottom: 30px;
            font-size: 0.95rem;
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
        .input-group-text {
            background: #e3f2fd;
            border-color: #90caf9;
            color: #1976d2;
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
    </style>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>
    <div class="forgot-container">
        <div class="icon-header">
            <i class='bx bx-lock-open-alt'></i>
        </div>
        <h2 class="forgot-title">Quên mật khẩu?</h2>
        <p class="forgot-subtitle">Nhập email của bạn để nhận mã xác thực</p>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class='bx bx-error-circle me-2'></i>${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>
        
        <form method="post" action="${pageContext.request.contextPath}/forgot-password">
            <div class="mb-4">
                <label class="form-label fw-bold">Email</label>
                <div class="input-group">
                    <span class="input-group-text"><i class='bx bx-envelope'></i></span>
                    <input type="email" class="form-control" name="email" 
                           placeholder="Nhập email đã đăng ký" required
                           value="${email}" autocomplete="email">
                </div>
            </div>
            
            <button type="submit" class="btn btn-submit w-100 mb-3">
                <i class='bx bx-send me-1'></i> Gửi mã xác thực
            </button>
            
            <div class="text-center">
                <a href="${pageContext.request.contextPath}/login" class="text-decoration-none">
                    <i class='bx bx-arrow-back'></i> Quay lại đăng nhập
                </a>
            </div>
        </form>
    </div>
    
    <jsp:include page="/components/scripts.jsp" />
</body>
</html>
