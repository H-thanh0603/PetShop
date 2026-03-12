package controller.auth;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.UserDAO;
import Util.FormHelper;
import Util.OTPUtil;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/pages/auth/register.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        if ("sendOTP".equals(action)) {
            handleSendOTP(request, response);
        } else if ("verifyOTP".equals(action)) {
            handleVerifyOTP(request, response);
        } else if ("checkUsername".equals(action)) {
            handleCheckUsername(request, response);
        } else {
            handleRegister(request, response);
        }
    }
    
    // Check username availability (realtime)
    private void handleCheckUsername(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String username = request.getParameter("username");
        
        if (username == null || username.trim().isEmpty()) {
            response.getWriter().write("{\"available\":false}");
            return;
        }
        
        UserDAO dao = new UserDAO();
        boolean exists = dao.checkUsernameExists(username.trim().toLowerCase());
        response.getWriter().write("{\"available\":" + !exists + "}");
    }
    
    // Gửi OTP xác thực email
    private void handleSendOTP(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String email = request.getParameter("email");
        
        if (email == null || email.trim().isEmpty()) {
            response.getWriter().write("{\"success\":false,\"message\":\"Email không được để trống\"}");
            return;
        }
        
        UserDAO dao = new UserDAO();
        if (dao.checkEmailExists(email)) {
            response.getWriter().write("{\"success\":false,\"message\":\"Email này đã được đăng ký\"}");
            return;
        }
        
        boolean sent = OTPUtil.generateAndSendOTP(email);
        if (sent) {
            response.getWriter().write("{\"success\":true,\"message\":\"Đã gửi mã OTP đến email của bạn\"}");
        } else {
            response.getWriter().write("{\"success\":false,\"message\":\"Không thể gửi OTP. Vui lòng thử lại\"}");
        }
    }
    
    // Xác thực OTP
    private void handleVerifyOTP(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String email = request.getParameter("email");
        String otp = request.getParameter("otp");
        
        if (OTPUtil.verifyOTP(email, otp)) {
            HttpSession session = request.getSession();
            session.setAttribute("emailVerified", email);
            response.getWriter().write("{\"success\":true,\"message\":\"Xác thực thành công\"}");
        } else {
            response.getWriter().write("{\"success\":false,\"message\":\"Mã OTP không đúng hoặc đã hết hạn\"}");
        }
    }
    
    // Đăng ký tài khoản
    private void handleRegister(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        FormHelper form = new FormHelper(request);
        
        String username = form.get("username");
        String email = form.get("email");
        String fullname = form.get("fullName");
        String phone = form.get("phone");
        String password = form.getRaw("password");
        String confirmPassword = form.getRaw("confirmPassword");
        
        // === VALIDATION ===
        boolean valid = true;
        
        // A. Họ và tên - trim, không toàn khoảng trắng, chỉ chữ có dấu + khoảng trắng
        if (fullname == null || fullname.trim().isEmpty()) {
            form.addError("fullName", "Vui lòng nhập họ và tên");
            valid = false;
        } else {
            fullname = fullname.trim();
            if (!fullname.matches("^[\\p{L}\\s]+$") || fullname.replaceAll("\\s", "").isEmpty()) {
                form.addError("fullName", "Họ tên chỉ được chứa chữ cái và khoảng trắng");
                valid = false;
            } else if (fullname.length() < 2) {
                form.addError("fullName", "Họ tên phải có ít nhất 2 ký tự");
                valid = false;
            }
        }
        
        // B. Email
        if (!form.validateRequired("email", "Email")) {
            valid = false;
        } else if (!form.validateEmail("email")) {
            form.addError("email", "Email không hợp lệ");
            valid = false;
        }
        
        // C. Tên đăng nhập - chữ thường, số, gạch dưới, 3-20 ký tự
        if (username == null || username.trim().isEmpty()) {
            form.addError("username", "Vui lòng nhập tên đăng nhập");
            valid = false;
        } else {
            username = username.trim().toLowerCase();
            if (!username.matches("^[a-z0-9_]{3,20}$")) {
                form.addError("username", "Tên đăng nhập không hợp lệ");
                valid = false;
            }
        }
        
        // D. Số điện thoại - 10 số, bắt đầu bằng 0
        if (phone != null && !phone.trim().isEmpty()) {
            phone = phone.replaceAll("[^0-9]", "");
            if (!phone.matches("^0\\d{9}$")) {
                form.addError("phone", "Số điện thoại không hợp lệ");
                valid = false;
            }
        }
        
        // E. Mật khẩu - 8 ký tự, chữ hoa, chữ thường, số, ký tự đặc biệt
        if (password == null || password.isEmpty()) {
            form.addError("password", "Vui lòng nhập mật khẩu");
            valid = false;
        } else if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$")) {
            form.addError("password", "Mật khẩu phải có tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
            valid = false;
        }
        
        // F. Xác nhận mật khẩu
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            form.addError("confirmPassword", "Vui lòng xác nhận mật khẩu");
            valid = false;
        } else if (!confirmPassword.equals(password)) {
            form.addError("confirmPassword", "Mật khẩu xác nhận không khớp");
            valid = false;
        }
        
        if (!valid) {
            form.applyToRequest();
            request.getRequestDispatcher("/pages/auth/register.jsp").forward(request, response);
            return;
        }
        
        // === KIỂM TRA TRÙNG ===
        UserDAO dao = new UserDAO();
        
        if (dao.checkUsernameExists(username)) {
            form.addError("username", "Tên đăng nhập đã tồn tại");
            form.addGeneralError("Tên đăng nhập đã tồn tại!");
            form.applyToRequest();
            request.getRequestDispatcher("/pages/auth/register.jsp").forward(request, response);
            return;
        }
        
        if (dao.checkEmailExists(email)) {
            form.addError("email", "Email đã được sử dụng");
            form.addGeneralError("Email đã tồn tại!");
            form.applyToRequest();
            request.getRequestDispatcher("/pages/auth/register.jsp").forward(request, response);
            return;
        }
        
        if (phone != null && !phone.isEmpty() && dao.checkPhoneExists(phone)) {
            form.addError("phone", "Số điện thoại đã được sử dụng");
            form.addGeneralError("Số điện thoại đã tồn tại!");
            form.applyToRequest();
            request.getRequestDispatcher("/pages/auth/register.jsp").forward(request, response);
            return;
        }
        
        // === KIỂM TRA OTP ===
        String otp = form.get("otp");
        if (otp == null || otp.trim().isEmpty()) {
            form.addError("otp", "Vui lòng nhập mã OTP");
            form.addGeneralError("Vui lòng nhập mã OTP!");
            form.applyToRequest();
            request.getRequestDispatcher("/pages/auth/register.jsp").forward(request, response);
            return;
        }
        
        if (!OTPUtil.verifyOTP(email, otp.trim())) {
            form.addError("otp", "Mã OTP không đúng hoặc đã hết hạn");
            form.addGeneralError("Mã OTP không đúng hoặc đã hết hạn!");
            form.applyToRequest();
            request.getRequestDispatcher("/pages/auth/register.jsp").forward(request, response);
            return;
        }
        
        // === ĐĂNG KÝ ===
        HttpSession session = request.getSession();
        boolean success = dao.register(username, password, fullname, email);
        
        if (success) {
            // Lưu email để gợi ý khi đăng nhập
            session.setAttribute("registeredEmail", email);
            session.setAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            form.addGeneralError("Đăng ký thất bại! Vui lòng thử lại.");
            form.applyToRequest();
            request.getRequestDispatcher("/pages/auth/register.jsp").forward(request, response);
        }
    }
}
