<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <jsp:include page="/components/meta.jsp" />
    <title>Đăng ký - PetShop</title>
    <jsp:include page="/components/head.jsp" />
    <style>
        body {
            background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px 0;
        }
        .register-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.15);
            padding: 40px;
            max-width: 500px;
            width: 100%;
        }
        .register-title {
            color: #1976d2;
            font-weight: bold;
            margin-bottom: 30px;
            text-align: center;
        }
        .btn-register {
            background: #1976d2;
            border: none;
            color: white;
            padding: 12px;
            font-weight: bold;
        }
        .btn-register:hover:not(:disabled) {
            background: #1565c0;
            color: white;
        }
        .btn-register:disabled {
            background: #90caf9;
            cursor: not-allowed;
        }
        .form-control:focus {
            border-color: #1976d2;
            box-shadow: 0 0 0 0.2rem rgba(25, 118, 210, 0.25);
        }
        .form-control.is-invalid {
            border-color: #dc3545;
        }
        .form-control.is-valid {
            border-color: #28a745;
        }
        .invalid-feedback {
            display: none;
            color: #dc3545;
            font-size: 0.85rem;
            margin-top: 4px;
        }
        .form-control.is-invalid ~ .invalid-feedback {
            display: block;
        }
        .valid-feedback {
            display: none;
            color: #28a745;
            font-size: 0.85rem;
            margin-top: 4px;
        }
        .form-control.is-valid ~ .valid-feedback {
            display: block;
        }
        .otp-section {
            background: #e3f2fd;
            padding: 15px;
            border-radius: 8px;
            margin-top: 10px;
            border: 1px solid #90caf9;
        }
        .btn-send-otp {
            white-space: nowrap;
            background: #1976d2;
            color: white;
            border: none;
        }
        .btn-send-otp:hover:not(:disabled) {
            background: #1565c0;
            color: white;
        }
        .btn-send-otp:disabled {
            background: #90caf9;
            color: white;
        }
        .field-hint {
            font-size: 0.8rem;
            color: #6c757d;
            margin-top: 4px;
        }
        .input-group {
            position: relative;
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
        .strength-meter {
            height: 4px;
            background: #e9ecef;
            border-radius: 2px;
            margin-top: 5px;
            overflow: hidden;
        }
        .strength-meter-fill {
            height: 100%;
            transition: width 0.3s, background 0.3s;
        }
        .checking-indicator {
            font-size: 0.8rem;
            color: #6c757d;
        }
        .input-group-text {
            background: #e3f2fd;
            border-color: #90caf9;
            color: #1976d2;
        }
    </style>
</head>
<body>
    <div class="register-container">
        <h2 class="register-title"><i class='bx bxs-shopping-bags'></i> PETSHOP REGISTER</h2>
        
        <jsp:include page="/components/alerts.jsp" />
        
        <form method="post" action="${pageContext.request.contextPath}/register" id="registerForm" novalidate>
            <!-- A. Họ và tên -->
            <div class="mb-3">
                <label class="form-label fw-bold">Họ và tên <span class="text-danger">*</span></label>
                <div class="input-group">
                    <span class="input-group-text"><i class='bx bx-user'></i></span>
                    <input type="text" class="form-control" 
                           name="fullName" id="fullName" placeholder="Nhập họ và tên"
                           value="${form.fullName}" autocomplete="name">
                </div>
                <div class="invalid-feedback" id="fullNameError">Vui lòng nhập họ và tên</div>
            </div>
            
            <!-- B. Email -->
            <div class="mb-3">
                <label class="form-label fw-bold">Email <span class="text-danger">*</span></label>
                <div class="input-group">
                    <span class="input-group-text"><i class='bx bx-envelope'></i></span>
                    <input type="email" class="form-control" 
                           name="email" id="email" placeholder="Nhập email"
                           value="${form.email}" autocomplete="email">
                    <button type="button" class="btn btn-send-otp" id="btnSendOTP">
                        Gửi OTP
                    </button>
                </div>
                <div class="invalid-feedback" id="emailError">Email không hợp lệ</div>
            </div>
            
            <!-- OTP Section - Luôn hiển thị -->
            <div class="mb-3" id="otpSection">
                <label class="form-label fw-bold">Mã OTP <span class="text-danger">*</span></label>
                <div class="input-group">
                    <span class="input-group-text"><i class='bx bx-key'></i></span>
                    <input type="text" class="form-control" id="otpInput" name="otp"
                           placeholder="Nhập mã 6 số từ email" maxlength="6" inputmode="numeric">
                </div>
                <div class="invalid-feedback" id="otpError">Mã OTP không đúng hoặc đã hết hạn</div>
                <small class="text-muted" id="otpHint">Bấm "Gửi OTP" ở trên để nhận mã xác thực qua email</small>
            </div>
            
            <!-- C. Tên đăng nhập -->
            <div class="mb-3">
                <label class="form-label fw-bold">Tên đăng nhập <span class="text-danger">*</span></label>
                <div class="input-group">
                    <span class="input-group-text"><i class='bx bx-at'></i></span>
                    <input type="text" class="form-control" 
                           name="username" id="username" placeholder="Nhập tên đăng nhập"
                           value="${form.username}" autocomplete="username">
                </div>
                <div class="invalid-feedback" id="usernameError">Tên đăng nhập không hợp lệ</div>
                <div class="valid-feedback" id="usernameValid">Tên đăng nhập hợp lệ</div>
                <div class="checking-indicator" id="usernameChecking" style="display:none;">
                    <span class="spinner-border spinner-border-sm"></span> Đang kiểm tra...
                </div>
                <div class="field-hint">Chỉ gồm chữ thường, số và dấu gạch dưới. 3–20 ký tự.</div>
            </div>
            
            <!-- D. Số điện thoại -->
            <div class="mb-3">
                <label class="form-label fw-bold">Số điện thoại</label>
                <div class="input-group">
                    <span class="input-group-text"><i class='bx bx-phone'></i></span>
                    <input type="tel" class="form-control" 
                           name="phone" id="phone" placeholder="Nhập số điện thoại (không bắt buộc)"
                           value="${form.phone}" maxlength="10" inputmode="numeric">
                </div>
                <div class="invalid-feedback" id="phoneError">Số điện thoại không hợp lệ</div>
                <div class="field-hint">10 số, bắt đầu bằng số 0</div>
            </div>
            
            <!-- E. Mật khẩu -->
            <div class="mb-3">
                <label class="form-label fw-bold">Mật khẩu <span class="text-danger">*</span></label>
                <div class="input-group">
                    <span class="input-group-text"><i class='bx bx-lock-alt'></i></span>
                    <input type="password" class="form-control" 
                           name="password" id="password" placeholder="Nhập mật khẩu"
                           autocomplete="new-password">
                    <span class="password-toggle" id="togglePassword">
                        <i class='bx bx-hide'></i>
                    </span>
                </div>
                <div class="strength-meter">
                    <div class="strength-meter-fill" id="strengthMeter"></div>
                </div>
                <div class="invalid-feedback" id="passwordError">
                    Mật khẩu phải có tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt
                </div>
                <div class="field-hint">Tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt</div>
            </div>
            
            <!-- F. Xác nhận mật khẩu -->
            <div class="mb-4">
                <label class="form-label fw-bold">Xác nhận mật khẩu <span class="text-danger">*</span></label>
                <div class="input-group">
                    <span class="input-group-text"><i class='bx bx-lock'></i></span>
                    <input type="password" class="form-control" 
                           name="confirmPassword" id="confirmPassword" placeholder="Nhập lại mật khẩu"
                           autocomplete="new-password">
                    <span class="password-toggle" id="toggleConfirmPassword">
                        <i class='bx bx-hide'></i>
                    </span>
                </div>
                <div class="invalid-feedback" id="confirmPasswordError">Mật khẩu xác nhận không khớp</div>
            </div>
            
            <button type="submit" class="btn btn-register w-100 mb-3" id="btnSubmit">
                <i class='bx bx-user-plus'></i> Đăng ký
            </button>
            
            <div class="text-center">
                <p class="mb-2">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login" class="text-decoration-none" style="color: #1976d2;">Đăng nhập</a></p>
                <p><a href="${pageContext.request.contextPath}/home" class="text-muted"><i class='bx bx-arrow-back'></i> Quay về trang chủ</a></p>
            </div>
        </form>
    </div>
    
    <jsp:include page="/components/scripts.jsp" />
    <jsp:include page="/components/loading-indicator.jsp" />
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        
        let state = {
            fullNameValid: false,
            emailValid: false,
            otpSent: false,
            usernameValid: false,
            usernameAvailable: false,
            phoneValid: true,
            passwordValid: false,
            confirmPasswordValid: false
        };
        
        function debounce(func, wait) {
            let timeout;
            return function(...args) {
                clearTimeout(timeout);
                timeout = setTimeout(() => func.apply(this, args), wait);
            };
        }
        
        function updateSubmitButton() {
            // Không disable nút, để người dùng luôn có thể click
        }
        
        function showError(fieldId, message) {
            const field = document.getElementById(fieldId);
            const errorEl = document.getElementById(fieldId + 'Error');
            field.classList.add('is-invalid');
            field.classList.remove('is-valid');
            if (errorEl && message) errorEl.textContent = message;
            if (errorEl) errorEl.style.display = 'block';
        }
        
        function showValid(fieldId) {
            const field = document.getElementById(fieldId);
            const errorEl = document.getElementById(fieldId + 'Error');
            field.classList.remove('is-invalid');
            field.classList.add('is-valid');
            if (errorEl) errorEl.style.display = 'none';
        }
        
        function clearValidation(fieldId) {
            const field = document.getElementById(fieldId);
            field.classList.remove('is-invalid', 'is-valid');
        }
        
        // A. Họ và tên
        document.getElementById('fullName').addEventListener('input', function() {
            const value = this.value.trim();
            const regex = /^[\p{L}\s]+$/u;
            
            if (!value) {
                showError('fullName', 'Vui lòng nhập họ và tên');
                state.fullNameValid = false;
            } else if (!regex.test(value) || value.replace(/\s/g, '').length === 0) {
                showError('fullName', 'Họ tên chỉ được chứa chữ cái và khoảng trắng');
                state.fullNameValid = false;
            } else if (value.length < 2) {
                showError('fullName', 'Họ tên phải có ít nhất 2 ký tự');
                state.fullNameValid = false;
            } else {
                showValid('fullName');
                state.fullNameValid = true;
            }
            updateSubmitButton();
        });
        
        // B. Email
        document.getElementById('email').addEventListener('input', function() {
            const value = this.value.trim();
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            
            // Reset OTP khi email thay đổi
            if (state.otpSent) {
                state.otpSent = false;
                document.getElementById('otpInput').value = '';
                document.getElementById('btnSendOTP').disabled = false;
                document.getElementById('btnSendOTP').textContent = 'Gửi OTP';
                document.getElementById('otpHint').textContent = 'Bấm "Gửi OTP" ở trên để nhận mã xác thực qua email';
            }
            
            if (!value) {
                showError('email', 'Vui lòng nhập email');
                state.emailValid = false;
            } else if (!emailRegex.test(value)) {
                showError('email', 'Email không hợp lệ');
                state.emailValid = false;
            } else {
                clearValidation('email');
                state.emailValid = true;
            }
            updateSubmitButton();
        });
        
        // Send OTP
        document.getElementById('btnSendOTP').addEventListener('click', function() {
            const email = document.getElementById('email').value.trim();
            if (!email || !state.emailValid) {
                showError('email', 'Vui lòng nhập email hợp lệ');
                return;
            }
            
            this.disabled = true;
            this.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang gửi...';
            
            fetch(contextPath + '/register', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'action=sendOTP&email=' + encodeURIComponent(email)
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    state.otpSent = true;
                    document.getElementById('otpHint').textContent = 'Mã OTP đã được gửi đến email (hết hạn sau 5 phút)';
                    document.getElementById('otpInput').focus();
                    let countdown = 60;
                    const btn = document.getElementById('btnSendOTP');
                    const interval = setInterval(() => {
                        countdown--;
                        btn.innerHTML = 'Gửi lại (' + countdown + 's)';
                        if (countdown <= 0) {
                            clearInterval(interval);
                            btn.disabled = false;
                            btn.innerHTML = 'Gửi lại OTP';
                        }
                    }, 1000);
                } else {
                    showError('email', data.message);
                    this.disabled = false;
                    this.innerHTML = 'Gửi OTP';
                }
            })
            .catch(err => {
                showError('email', 'Có lỗi xảy ra, vui lòng thử lại');
                this.disabled = false;
                this.innerHTML = 'Gửi OTP';
            });
        });
        
        // OTP input - chỉ cho nhập số
        document.getElementById('otpInput').addEventListener('input', function() {
            this.value = this.value.replace(/[^0-9]/g, '');
            // Clear error khi user nhập
            if (this.value.length > 0) {
                this.classList.remove('is-invalid');
                document.getElementById('otpError').style.display = 'none';
            }
        });
        
        // C. Username
        const checkUsername = debounce(function(username) {
            if (!username) return;
            document.getElementById('usernameChecking').style.display = 'block';
            
            fetch(contextPath + '/register', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'action=checkUsername&username=' + encodeURIComponent(username)
            })
            .then(res => res.json())
            .then(data => {
                document.getElementById('usernameChecking').style.display = 'none';
                if (data.available) {
                    showValid('username');
                    document.getElementById('usernameValid').style.display = 'block';
                    state.usernameAvailable = true;
                } else {
                    showError('username', 'Tên đăng nhập đã tồn tại');
                    state.usernameAvailable = false;
                }
                updateSubmitButton();
            })
            .catch(err => {
                document.getElementById('usernameChecking').style.display = 'none';
            });
        }, 400);
        
        document.getElementById('username').addEventListener('input', function() {
            let value = this.value.toLowerCase().replace(/[^a-z0-9_]/g, '');
            this.value = value;
            
            const regex = /^[a-z0-9_]{3,20}$/;
            document.getElementById('usernameValid').style.display = 'none';
            state.usernameAvailable = false;
            
            if (!value) {
                showError('username', 'Vui lòng nhập tên đăng nhập');
                state.usernameValid = false;
            } else if (!regex.test(value)) {
                if (value.length < 3) {
                    showError('username', 'Tên đăng nhập phải có ít nhất 3 ký tự');
                } else {
                    showError('username', 'Tên đăng nhập không hợp lệ');
                }
                state.usernameValid = false;
            } else {
                clearValidation('username');
                state.usernameValid = true;
                checkUsername(value);
            }
            updateSubmitButton();
        });
        
        // D. Phone
        document.getElementById('phone').addEventListener('input', function() {
            let value = this.value.replace(/[^0-9]/g, '');
            this.value = value;
            
            if (!value) {
                clearValidation('phone');
                state.phoneValid = true;
            } else if (!/^0\d{9}$/.test(value)) {
                if (value.length > 0 && value[0] !== '0') {
                    showError('phone', 'Số điện thoại phải bắt đầu bằng số 0');
                } else if (value.length !== 10) {
                    showError('phone', 'Số điện thoại phải có đúng 10 số');
                } else {
                    showError('phone', 'Số điện thoại không hợp lệ');
                }
                state.phoneValid = false;
            } else {
                showValid('phone');
                state.phoneValid = true;
            }
            updateSubmitButton();
        });
        
        // E. Password
        function checkPasswordStrength(password) {
            let strength = 0;
            if (password.length >= 8) strength++;
            if (/[a-z]/.test(password)) strength++;
            if (/[A-Z]/.test(password)) strength++;
            if (/[0-9]/.test(password)) strength++;
            if (/[^a-zA-Z0-9\s]/.test(password)) strength++;
            return strength;
        }
        
        function updateStrengthMeter(strength) {
            const meter = document.getElementById('strengthMeter');
            const colors = ['#dc3545', '#fd7e14', '#ffc107', '#28a745', '#20c997'];
            const widths = ['20%', '40%', '60%', '80%', '100%'];
            meter.style.width = widths[strength - 1] || '0%';
            meter.style.background = colors[strength - 1] || '#e9ecef';
        }
        
        document.getElementById('password').addEventListener('input', function() {
            const value = this.value;
            const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^\w\s]).{8,}$/;
            
            const strength = checkPasswordStrength(value);
            updateStrengthMeter(strength);
            
            if (!value) {
                showError('password', 'Vui lòng nhập mật khẩu');
                state.passwordValid = false;
            } else if (!regex.test(value)) {
                showError('password', 'Mật khẩu phải có tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt');
                state.passwordValid = false;
            } else {
                showValid('password');
                state.passwordValid = true;
            }
            
            const confirmPassword = document.getElementById('confirmPassword').value;
            if (confirmPassword) {
                if (confirmPassword !== value) {
                    showError('confirmPassword', 'Mật khẩu xác nhận không khớp');
                    state.confirmPasswordValid = false;
                } else {
                    showValid('confirmPassword');
                    state.confirmPasswordValid = true;
                }
            }
            updateSubmitButton();
        });
        
        // Toggle password
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
        
        document.getElementById('toggleConfirmPassword').addEventListener('click', function() {
            const input = document.getElementById('confirmPassword');
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
        
        // F. Confirm password
        document.getElementById('confirmPassword').addEventListener('input', function() {
            const password = document.getElementById('password').value;
            const value = this.value;
            
            if (!value) {
                showError('confirmPassword', 'Vui lòng xác nhận mật khẩu');
                state.confirmPasswordValid = false;
            } else if (value !== password) {
                showError('confirmPassword', 'Mật khẩu xác nhận không khớp');
                state.confirmPasswordValid = false;
            } else {
                showValid('confirmPassword');
                state.confirmPasswordValid = true;
            }
            updateSubmitButton();
        });
        
        // Form submit - Cải thiện UX
        document.getElementById('registerForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Validate tất cả các field và tìm field đầu tiên chưa hợp lệ
            let firstInvalidField = null;
            
            // 1. Kiểm tra họ tên
            const fullName = document.getElementById('fullName').value.trim();
            if (!fullName) {
                showError('fullName', 'Vui lòng nhập họ và tên');
                state.fullNameValid = false;
                if (!firstInvalidField) firstInvalidField = 'fullName';
            }
            
            // 2. Kiểm tra email
            const email = document.getElementById('email').value.trim();
            if (!email) {
                showError('email', 'Vui lòng nhập email');
                state.emailValid = false;
                if (!firstInvalidField) firstInvalidField = 'email';
            } else if (!state.emailValid) {
                if (!firstInvalidField) firstInvalidField = 'email';
            }
            
            // 3. Kiểm tra OTP
            const otp = document.getElementById('otpInput').value.trim();
            if (!otp) {
                document.getElementById('otpInput').classList.add('is-invalid');
                document.getElementById('otpError').textContent = 'Vui lòng nhập mã OTP';
                document.getElementById('otpError').style.display = 'block';
                if (!firstInvalidField) firstInvalidField = 'otpInput';
            } else if (otp.length !== 6) {
                document.getElementById('otpInput').classList.add('is-invalid');
                document.getElementById('otpError').textContent = 'Mã OTP phải có 6 số';
                document.getElementById('otpError').style.display = 'block';
                if (!firstInvalidField) firstInvalidField = 'otpInput';
            }
            
            // 4. Kiểm tra username
            const username = document.getElementById('username').value.trim();
            if (!username) {
                showError('username', 'Vui lòng nhập tên đăng nhập');
                state.usernameValid = false;
                if (!firstInvalidField) firstInvalidField = 'username';
            } else if (!state.usernameValid || !state.usernameAvailable) {
                if (!firstInvalidField) firstInvalidField = 'username';
            }
            
            // 5. Kiểm tra phone (optional nhưng nếu có thì phải hợp lệ)
            if (!state.phoneValid) {
                if (!firstInvalidField) firstInvalidField = 'phone';
            }
            
            // 6. Kiểm tra password
            const password = document.getElementById('password').value;
            if (!password) {
                showError('password', 'Vui lòng nhập mật khẩu');
                state.passwordValid = false;
                if (!firstInvalidField) firstInvalidField = 'password';
            } else if (!state.passwordValid) {
                if (!firstInvalidField) firstInvalidField = 'password';
            }
            
            // 7. Kiểm tra confirm password
            const confirmPassword = document.getElementById('confirmPassword').value;
            if (!confirmPassword) {
                showError('confirmPassword', 'Vui lòng xác nhận mật khẩu');
                state.confirmPasswordValid = false;
                if (!firstInvalidField) firstInvalidField = 'confirmPassword';
            } else if (!state.confirmPasswordValid) {
                if (!firstInvalidField) firstInvalidField = 'confirmPassword';
            }
            
            // Nếu có field không hợp lệ, focus vào field đó
            if (firstInvalidField) {
                document.getElementById(firstInvalidField).focus();
                document.getElementById(firstInvalidField).scrollIntoView({ behavior: 'smooth', block: 'center' });
                return;
            }
            
            // Tất cả hợp lệ, submit form
            const btn = document.getElementById('btnSubmit');
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang tạo tài khoản...';
            
            // Submit form
            this.submit();
        });
        
        // Trim on blur
        ['fullName', 'email', 'username'].forEach(id => {
            document.getElementById(id).addEventListener('blur', function() {
                this.value = this.value.trim();
            });
        });
    </script>
</body>
</html>
