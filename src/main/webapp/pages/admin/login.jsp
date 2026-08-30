<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Login - PetShop</title>
    <jsp:include page="/components/favicon.jsp" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Nunito', sans-serif;
            background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
        }
        .login-card {
            width: 100%;
            max-width: 420px;
            background: white;
            border-radius: 16px;
            box-shadow: 0 20px 50px rgba(0,0,0,0.1);
            padding: 40px;
            border: 1px solid #fff;
        }
        .login-header {
            text-align: center;
            margin-bottom: 30px;
        }
        .login-header i {
            font-size: 3rem;
            color: #3b82f6;
        }
        .login-header h2 {
            font-weight: 700;
            color: #1e293b;
            margin-top: 10px;
        }
        .btn-primary {
            background-color: #3b82f6;
            border-color: #3b82f6;
            font-weight: 600;
            padding: 12px;
        }
        .form-control:focus {
            box-shadow: 0 0 0 0.25rem rgba(59, 130, 246, 0.25);
            border-color: #3b82f6;
        }
    </style>
</head>
<body>
    <div class="login-card">
        <div class="login-header">
            <i class='bx bxs-dog'></i>
            <h2>Admin Panel</h2>
        </div>

        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger">${fn:escapeXml(requestScope.error)}</div>
        </c:if>

        <form method="POST" action="${pageContext.request.contextPath}/admin/login">
             <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
            <div class="mb-3">
                <label for="email" class="form-label fw-bold">Email</label>
                <input type="email" class="form-control" id="email" name="email" value="${fn:escapeXml(param.email)}" required>
            </div>
            <div class="mb-4">
                <label for="password" class="form-label fw-bold">Mật khẩu</label>
                <input type="password" class="form-control" id="password" name="password" required>
            </div>
            <button type="submit" class="btn btn-primary w-100">Đăng nhập</button>
        </form>
    </div>
</body>
</html>
