<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 
    Component: page-template.jsp
    Mô tả: Template hướng dẫn cách sử dụng components trong các trang JSP
    
    === HƯỚNG DẪN SỬ DỤNG COMPONENTS ===
    
    Cấu trúc chuẩn cho một trang JSP:
    
    <!DOCTYPE html>
    <html lang="vi">
    <head>
        <%-- Meta tags --%>
        <jsp:include page="/components/meta.jsp" />
        <title>Tiêu đề trang - Animal Doctors</title>
        
        <%-- CSS Libraries --%>
        <jsp:include page="/components/head.jsp" />
        
        <%-- Navbar styles (nếu có header) --%>
        <jsp:include page="/components/navbar-styles.jsp" />
        
        <%-- CSS riêng của trang --%>
        <style>
            /* Custom styles here */
        </style>
    </head>
    <body>
        <%-- Header/Navbar --%>
        <jsp:include page="/components/header.jsp" />
        
        <%-- Nội dung chính --%>
        <main class="container my-4">
            <%-- Alerts/Thông báo --%>
            <jsp:include page="/components/alerts.jsp" />
            
            <%-- Content here --%>
        </main>
        
        <%-- Footer (đã include scripts.jsp bên trong) --%>
        <jsp:include page="/components/footer.jsp" />
        
        <%-- JavaScript riêng của trang --%>
        <script>
            // Custom scripts here
        </script>
    </body>
    </html>
    
    === DANH SÁCH COMPONENTS ===
    
    1. meta.jsp       - Meta tags chuẩn (charset, viewport, description)
    2. head.jsp       - CSS libraries (Bootstrap, Boxicons, Google Fonts)
    3. navbar-styles.jsp - CSS cho navbar
    4. header.jsp     - Navbar với menu và user dropdown
    5. alerts.jsp     - Hiển thị thông báo success/error/warning
    6. footer.jsp     - Footer với thông tin liên hệ (đã include scripts.jsp)
    7. scripts.jsp    - JavaScript libraries (Bootstrap JS)
    
--%>
