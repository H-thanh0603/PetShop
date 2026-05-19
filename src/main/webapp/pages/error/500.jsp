<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="java.util.UUID, org.slf4j.Logger, org.slf4j.LoggerFactory" %>
<%
    /*
     * 500.jsp — Internal Server Error page
     *
     * Security rules:
     *   1. NEVER print exception details / stacktrace to the browser.
     *   2. DO log the full exception server-side with a unique Error ID.
     *   3. Show the Error ID to the user so they can report it to support.
     */

    // --- 1. Capture exception (set by Tomcat via jakarta.servlet.error.exception) ---
    Throwable serverError = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
    if (serverError == null) {
        serverError = exception; // isErrorPage=true exposes this scriptlet variable
    }

    // --- 2. Generate a unique, opaque error ID for support tracing ---
    String errorId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    // --- 3. Log the full details server-side (never shown to user) ---
    Logger errorLogger = LoggerFactory.getLogger("PetShop.ErrorPage");
    String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");
    if (requestUri == null) requestUri = request.getRequestURI();

    if (serverError != null) {
        errorLogger.error("[ERROR-{}] Unhandled server error at URI: {}", errorId, requestUri, serverError);
    } else {
        errorLogger.error("[ERROR-{}] 500 response at URI: {} (no exception attached)", errorId, requestUri);
    }

    // --- 4. Set correct HTTP status (Tomcat may have already set it, but be explicit) ---
    response.setStatus(500);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <jsp:include page="/components/head.jsp" />
    <title>500 - Lỗi máy chủ | PetShop</title>
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
            color: #ef4444;
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
            margin-bottom: 24px;
            line-height: 1.6;
        }
        /* Error ID badge - shown to user for support tracing */
        .error-id-box {
            display: inline-block;
            background: rgba(255, 255, 255, 0.06);
            border: 1px solid rgba(255, 255, 255, 0.15);
            border-radius: 6px;
            padding: 8px 18px;
            margin-bottom: 28px;
            font-size: 13px;
            color: rgba(255, 255, 255, 0.55);
            letter-spacing: 0.02em;
        }
        .error-id-box strong {
            color: rgba(255, 255, 255, 0.85);
            font-family: 'Courier New', monospace;
            letter-spacing: 0.08em;
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
        .btn-retry {
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
            cursor: pointer;
        }
        .btn-retry:hover {
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
            <i class='bx bx-error-circle'></i>
        </div>
        <div class="error-code">500</div>
        <h1>Lỗi máy chủ</h1>
        <p class="error-subtitle">
            Đã xảy ra lỗi không mong muốn. Đội ngũ kỹ thuật đã được ghi nhận.<br>
            Vui lòng thử lại sau hoặc liên hệ hỗ trợ nếu lỗi tiếp tục xảy ra.
        </p>

        <!-- Error ID: shown to user, useful for log tracing — NO stacktrace ever shown -->
        <div class="error-id-box">
            Mã lỗi: <strong><%= errorId %></strong>
        </div>

        <div class="error-actions">
            <a href="${pageContext.request.contextPath}/home" class="btn-home">
                <i class='bx bx-home'></i> Về trang chủ
            </a>
            <a href="javascript:history.back()" class="btn-retry">
                <i class='bx bx-left-arrow-alt'></i> Quay lại trang trước
            </a>
        </div>
        <div class="brand-footer">
            &copy; PetShop &mdash; Animal Doctors International
        </div>
    </div>
</body>
</html>
