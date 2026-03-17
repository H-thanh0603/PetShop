<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty sessionScope.success}">
    <c:set var="success" value="${sessionScope.success}" scope="request"/>
    <c:remove var="success" scope="session"/>
</c:if>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Đăng nhập - PetShop</title>
    <jsp:include page="/components/head.jsp" />
    <style>
        body {
            background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .login-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.15);
            padding: 40px;
            max-width: 420px;
            width: 100%;
        }
        .login-title {
            color: #1976d2;
            font-weight: bold;
            margin-bottom: 30px;
            text-align: center;
        }
        .btn-login {
            background: #1976d2;
            border: none;
            color: white;
            padding: 12px;
            font-weight: bold;
        }
        .btn-login:hover {
            background: #1565c0;
            color: white;
        }
        .form-control:focus {
            border-color: #1976d2;
            box-shadow: 0 0 0 0.2rem rgba(25, 118, 210, 0.25);
        }
        .form-control.is-invalid {
            border-color: #dc3545;
        }
        .invalid-feedback {
            display: block;
            color: #dc3545;
            font-size: 0.85rem;
            margin-top: 4px;
        }
        .form-check-input:checked {
            background-color: #1976d2;
            border-color: #1976d2;
        }
        .input-group-text {
            background: #e3f2fd;
            border-color: #90caf9;
            color: #1976d2;
        }
        .password-toggle {
            cursor: pointer;
            padding: 0.375rem 0.75rem;
            background: #f8f9fa;
            border: 1px solid #ced4da;
            border-left: none;
            display: flex;
            align-items: center;
            color: #6c757d;
        }
        .password-toggle:hover {
            color: #1976d2;
        }
        a {
            color: #1976d2;
        }
        a:hover {
            color: #1565c0;
        }
        .social-login{
            display: flex;
            gap: 15px;
            justify-content: center;
        }

        .social-item{
            width: 50px;
            height: 50px;
            border-radius: 50%;
            display: flex;
            justify-content: center;
            align-items: center;
            cursor: pointer;
            transition: 0.3s;
            font-size: 22px;
        }

        /* Google */
        .google{
            border: 2px solid #db4437;
            color: #db4437;
        }

        .google:hover{
            background: #db4437;
            color: white;
            transform: translateY(-5px);
        }

        /* Facebook */
        .facebook{
            border: 2px solid #1877f2;
            color: #1877f2;
        }

        .facebook:hover{
            background: #1877f2;
            color: white;
            transform: translateY(-5px);
        }
    </style>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>
    <div class="login-container">
        <h2 class="login-title"><i class='bx bxs-shopping-bags'></i> PETSHOP LOGIN</h2>
        
        <jsp:include page="/components/alerts.jsp" />
        
        <form method="post" action="${pageContext.request.contextPath}/login">
            <div class="mb-3">
                <label class="form-label fw-bold">Email</label>
                <div class="input-group">
                    <span class="input-group-text"><i class='bx bx-envelope'></i></span>
                    <input type="email" class="form-control ${not empty errors.email ? 'is-invalid' : ''}" 
                           name="email" id="emailInput" placeholder="Nhập email" required
                           value="${not empty form.email ? form.email : savedEmail}"
                           autocomplete="email">
                    <span class="password-toggle" id="clearEmail" title="Xóa email" style="display: none;">
                        <i class='bx bx-x'></i>
                    </span>
                </div>
                <c:if test="${not empty errors.email}">
                    <div class="invalid-feedback">${errors.email}</div>
                </c:if>
            </div>
            
            <div class="mb-3">
                <label class="form-label fw-bold">Mật khẩu</label>
                <div class="input-group">
                    <span class="input-group-text"><i class='bx bx-lock-alt'></i></span>
                    <input type="password" class="form-control ${not empty errors.password ? 'is-invalid' : ''}" 
                           name="password" id="password" placeholder="Nhập mật khẩu" required
                           autocomplete="current-password">
                    <span class="password-toggle" id="togglePassword">
                        <i class='bx bx-hide'></i>
                    </span>
                </div>
                <c:if test="${not empty errors.password}">
                    <div class="invalid-feedback">${errors.password}</div>
                </c:if>
            </div>
            
            <div class="mb-3 d-flex justify-content-between align-items-center">
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" name="rememberMe" id="rememberMe"
                           ${not empty savedEmail ? 'checked' : ''}>
                    <label class="form-check-label" for="rememberMe">Ghi nhớ đăng nhập</label>
                </div>
                <a href="${pageContext.request.contextPath}/forgot-password" class="text-decoration-none small">
                    Quên mật khẩu?
                </a>
            </div>
            
            <button type="submit" class="btn btn-login w-100 mb-3">
                <i class='bx bx-log-in'></i> Đăng nhập
            </button>
<%--            login by gg and fb--%>
            <div class="social-login">
                <div class="social-item google" title="Login by google">
                    <a href="https://accounts.google.com/o/oauth2/auth?scope=email profile openid&redirect_uri=http://localhost:8080/PetShop_war/LoginByGoogleServlet&response_type=code&client_id=875562698779-ahopbc7kehsmb02gvpi4r9rhb2o8309q.apps.googleusercontent.com&approval_prompt=force">
                        <i class="bi bi-google"></i>
                    </a>
                </div>
                <div class="social-item facebook" title="Login by facebook">
                    <a href="https://www.facebook.com/v19.0/dialog/oauth?client_id=1485991816200631&redirect_uri=http://localhost:8080/PetShop_war/LoginByFacebookServlet&scope=email,public_profile">
                        <i class="bi bi-facebook"></i>
                    </a>
                </div>
            </div>

            <div class="text-center">
                <p class="mb-2">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register" class="text-decoration-none">Đăng ký ngay</a></p>
                <p><a href="${pageContext.request.contextPath}/home" class="text-muted"><i class='bx bx-arrow-back'></i> Quay về trang chủ</a></p>
            </div>
        </form>
    </div>
    
    <jsp:include page="/components/scripts.jsp" />
    <script>
        document.getElementById('togglePassword').addEventListener('click', function() {
            const input = document.getElementById('password');
            const icon = this.querySelector('i');
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('bx-hide');
                icon.classList.add('bx-show');
            } else {
                input.type = 'password';
                icon.classList.remove('bx-show');
                icon.classList.add('bx-hide');
            }
        });
        
        // Xử lý nút xóa email
        var emailInput = document.getElementById('emailInput');
        var clearEmailBtn = document.getElementById('clearEmail');
        var lastUserValue = emailInput.value; // Lưu giá trị người dùng nhập
        var isUserEditing = false;
        
        // Hiển thị/ẩn nút xóa
        function updateClearButton() {
            if (emailInput.value.length > 0) {
                clearEmailBtn.style.display = 'flex';
            } else {
                clearEmailBtn.style.display = 'none';
            }
        }
        
        // Kiểm tra khi load trang
        updateClearButton();
        
        // Theo dõi khi người dùng đang nhập
        emailInput.addEventListener('keydown', function(e) {
            isUserEditing = true;
        });
        
        emailInput.addEventListener('input', function(e) {
            isUserEditing = true;
            lastUserValue = this.value;
            updateClearButton();
        });
        
        // Chặn browser autofill ghi đè
        var autofillCheckInterval = null;
        
        emailInput.addEventListener('focus', function() {
            isUserEditing = true;
            // Kiểm tra liên tục trong 500ms để chặn autofill
            autofillCheckInterval = setInterval(function() {
                if (isUserEditing && emailInput.value !== lastUserValue) {
                    // Browser đã tự động điền, khôi phục giá trị người dùng
                    emailInput.value = lastUserValue;
                    updateClearButton();
                }
            }, 10);
            
            setTimeout(function() {
                clearInterval(autofillCheckInterval);
            }, 500);
        });
        
        emailInput.addEventListener('blur', function() {
            isUserEditing = false;
            clearInterval(autofillCheckInterval);
        });
        
        // Nút xóa email
        clearEmailBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            // Tắt autocomplete hoàn toàn
            emailInput.setAttribute('autocomplete', 'off');
            emailInput.setAttribute('readonly', true);
            
            // Xóa giá trị
            emailInput.value = '';
            lastUserValue = '';
            
            // Bỏ readonly sau 100ms để có thể nhập lại
            setTimeout(function() {
                emailInput.removeAttribute('readonly');
                emailInput.focus();
            }, 100);
            
            this.style.display = 'none';
            
            // Xóa cookie
            document.cookie = 'rememberEmail=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
            document.getElementById('rememberMe').checked = false;
        });
        
        // Chặn sự kiện change từ autofill
        emailInput.addEventListener('change', function(e) {
            if (!isUserEditing && this.value !== lastUserValue) {
                // Đây là autofill, khôi phục giá trị
                this.value = lastUserValue;
                updateClearButton();
            }
        });
    </script>
</body>
</html>
