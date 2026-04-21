<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <jsp:include page="/components/head.jsp" />
    <title>404 - Trang không tìm thấy | PetShop</title>
    <!-- Boxicons CDN (fallback in case head.jsp fails) -->
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Roboto', 'Segoe UI', Arial, sans-serif;
            background: linear-gradient(135deg, #0b1a33 0%, #1a2d4d 50%, #0b1a33 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #ffffff;
            overflow: hidden;
            position: relative;
        }
        /* Decorative pet paw prints */
        .paw-decoration {
            position: absolute;
            opacity: 0.05;
            font-size: 80px;
            color: #ffffff;
        }
        .paw-1 { top: 10%; left: 8%; transform: rotate(-25deg); }
        .paw-2 { top: 20%; right: 12%; transform: rotate(15deg); font-size: 60px; }
        .paw-3 { bottom: 15%; left: 15%; transform: rotate(30deg); font-size: 70px; }
        .paw-4 { bottom: 25%; right: 8%; transform: rotate(-10deg); font-size: 50px; }
        .paw-5 { top: 50%; left: 3%; transform: rotate(45deg); font-size: 40px; }

        .error-container {
            text-align: center;
            padding: 40px 30px;
            max-width: 560px;
            width: 90%;
            position: relative;
            z-index: 1;
        }
        .pet-icon {
            font-size: 48px;
            color: rgba(255, 255, 255, 0.3);
            margin-bottom: 10px;
        }
        .error-icon {
            font-size: 100px;
            color: #f59e0b;
            margin-bottom: 16px;
            animation: pulse 2s ease-in-out infinite;
        }
        @keyframes pulse {
            0%, 100% { transform: scale(1); opacity: 1; }
            50% { transform: scale(1.08); opacity: 0.85; }
        }
        .error-code {
            font-size: 72px;
            font-weight: 700;
            color: #3b82f6;
            line-height: 1;
            margin-bottom: 8px;
            text-shadow: 0 2px 10px rgba(59, 130, 246, 0.3);
        }
        .error-container h1 {
            font-size: 28px;
            font-weight: 600;
            margin-bottom: 12px;
            color: #ffffff;
        }
        .error-subtitle {
            font-size: 16px;
            color: rgba(255, 255, 255, 0.65);
            margin-bottom: 32px;
            line-height: 1.6;
        }
        .error-actions {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 12px;
        }
        .btn-home {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 12px 32px;
            background: #3b82f6;
            color: #ffffff;
            text-decoration: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 500;
            transition: background 0.2s ease, transform 0.2s ease;
        }
        .btn-home:hover {
            background: #2563eb;
            transform: translateY(-2px);
            color: #ffffff;
            text-decoration: none;
        }
        .btn-admin {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 24px;
            background: transparent;
            color: rgba(255, 255, 255, 0.7);
            text-decoration: none;
            border: 1px solid rgba(255, 255, 255, 0.25);
            border-radius: 8px;
            font-size: 14px;
            font-weight: 400;
            transition: all 0.2s ease;
        }
        .btn-admin:hover {
            background: rgba(255, 255, 255, 0.1);
            color: #ffffff;
            border-color: rgba(255, 255, 255, 0.5);
            text-decoration: none;
        }
        .brand-footer {
            margin-top: 40px;
            font-size: 13px;
            color: rgba(255, 255, 255, 0.3);
        }
    </style>
</head>
<body>
    <!-- Decorative paw prints -->
    <i class='bx bxs-dog paw-decoration paw-1'></i>
    <i class='bx bxs-dog paw-decoration paw-2'></i>
    <i class='bx bxs-dog paw-decoration paw-3'></i>
    <i class='bx bxs-dog paw-decoration paw-4'></i>
    <i class='bx bxs-dog paw-decoration paw-5'></i>

    <div class="error-container">
        <div class="pet-icon">
            <i class='bx bxs-dog'></i>
        </div>
        <div class="error-icon">
            <i class='bx bx-error'></i>
        </div>
        <div class="error-code">404</div>
        <h1>Trang không tìm thấy</h1>
        <p class="error-subtitle">
            Trang bạn đang tìm kiếm không tồn tại hoặc đã bị di chuyển.
        </p>
        <div class="error-actions">
            <a href="${pageContext.request.contextPath}/home" class="btn-home">
                <i class='bx bx-home'></i> Về trang chủ
            </a>
            <%
                String requestURI = (String) request.getAttribute("jakarta.servlet.error.request_uri");
                if (requestURI == null) {
                    requestURI = request.getRequestURI();
                }
                if (requestURI != null && requestURI.contains("/pages/admin/")) {
            %>
            <a href="${pageContext.request.contextPath}/pages/admin/dashboard" class="btn-admin">
                <i class='bx bxs-dashboard'></i> Về Admin Dashboard
            </a>
            <% } %>
        </div>
        <div class="brand-footer">
            &copy; PetShop &mdash; Animal Doctors International
        </div>
    </div>
</body>
</html>
