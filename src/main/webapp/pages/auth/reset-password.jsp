<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Đặt lại mật khẩu - PetShop</title>
    <jsp:include page="/components/head.jsp" />
    <style>
        body {
            background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .reset-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.15);
            padding: 40px;
            max-width: 420px;
            width: 100%;
        }
        .reset-title {
            color: #1976d2;
            font-weight: bold;
            margin-bottom: 10px;
            text-align: center;
        }
        .reset-subtitle {
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
        a { color: #1976d2; }
        a:hover { color: #1565c0; }
        .icon-header {
            text-align: center;
            margin-bottom: 20px;
        }
        .icon-header i {
            font-size: 60px;
            color: #4caf50;
        }
        .alert {
            border-radius: 10px;
        }
        .password-strength {
            height: 4px;
            border-radius: 2px;
            margin-top: 8px;
            transition: all 0.3s;
        }
        .strength-weak { background: #f44336; width: 33%; }
        .strength-medium { background: #ff9800; width: 66%; }
        .strength-strong { background: #4caf50; width: 100%; }
    </style>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>
    <div class="reset-container">
        <div class="icon-header">
            <i class='bx bx-check-shield'></i>
        </div>
        <h2 class="reset-title">Đặt lại mật khẩu</h2>
        <p class="reset-subtitle">Tạo mật khẩu mới cho tài khoản của bạn</p>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class='bx bx-error-circle me-2'></i>${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>
        
        <form method="post" action="${pageContext.request.contextPath}/reset-password" id="resetForm">
            <div class="mb-3">
                <label class="form-label fw-bold">Mật khẩu mới</label>
                <div class="input-group">
                    <span class="input-group-text"><i class='bx bx-lock-alt'></i></span>
                    <input type="password" class="form-control" name="password" id="password"
                           placeholder="Nhập mật khẩu mới" required minlength="6">
                    <span class="password-toggle" onclick="togglePassword('password', this)">
                        <i class='bx bx-hide'></i>
                    </span>
                </div>
                <div class="password-strength" id="strengthBar"></div>
                <small class="text-muted">Mật khẩu phải có ít nhất 6 ký tự</small>
            </div>
            
            <div class="mb-4">
                <label class="form-label fw-bold">Xác nhận mật khẩu</label>
                <div class="input-group">
                    <span class="input-group-text"><i class='bx bx-lock'></i></span>
                    <input type="password" class="form-control" name="confirmPassword" id="confirmPassword"
                           placeholder="Nhập lại mật khẩu" required>
                    <span class="password-toggle" onclick="togglePassword('confirmPassword', this)">
                        <i class='bx bx-hide'></i>
                    </span>
                </div>
                <div id="matchMessage" class="small mt-1"></div>
            </div>
            
            <button type="submit" class="btn btn-submit w-100 mb-3" id="submitBtn">
                <i class='bx bx-save me-1'></i> Đặt lại mật khẩu
            </button>
            
            <div class="text-center">
                <a href="${pageContext.request.contextPath}/login" class="text-decoration-none text-muted">
                    <i class='bx bx-arrow-back'></i> Quay lại đăng nhập
                </a>
            </div>
        </form>
    </div>
    
    <jsp:include page="/components/scripts.jsp" />
    <script>
        function togglePassword(inputId, toggle) {
            const input = document.getElementById(inputId);
            const icon = toggle.querySelector('i');
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('bx-hide');
                icon.classList.add('bx-show');
            } else {
                input.type = 'password';
                icon.classList.remove('bx-show');
                icon.classList.add('bx-hide');
            }
        }
        
        // Password strength indicator
        document.getElementById('password').addEventListener('input', function() {
            const bar = document.getElementById('strengthBar');
            const val = this.value;
            bar.className = 'password-strength';
            
            if (val.length === 0) {
                bar.style.width = '0';
            } else if (val.length < 6) {
                bar.classList.add('strength-weak');
            } else if (val.length < 10 || !/[A-Z]/.test(val) || !/[0-9]/.test(val)) {
                bar.classList.add('strength-medium');
            } else {
                bar.classList.add('strength-strong');
            }
            
            checkMatch();
        });
        
        // Check password match
        document.getElementById('confirmPassword').addEventListener('input', checkMatch);
        
        function checkMatch() {
            const pass = document.getElementById('password').value;
            const confirm = document.getElementById('confirmPassword').value;
            const msg = document.getElementById('matchMessage');
            
            if (confirm.length === 0) {
                msg.innerHTML = '';
            } else if (pass === confirm) {
                msg.innerHTML = '<span class="text-success"><i class="bx bx-check"></i> Mật khẩu khớp</span>';
            } else {
                msg.innerHTML = '<span class="text-danger"><i class="bx bx-x"></i> Mật khẩu không khớp</span>';
            }
        }
        
        // Form validation
        document.getElementById('resetForm').addEventListener('submit', function(e) {
            const pass = document.getElementById('password').value;
            const confirm = document.getElementById('confirmPassword').value;
            
            if (pass !== confirm) {
                e.preventDefault();
                alert('Mật khẩu xác nhận không khớp!');
            }
        });
    </script>
</body>
</html>
