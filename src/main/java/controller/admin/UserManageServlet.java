package controller.admin;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import DAO.UserDAO;
import DAO.OrderDAO;
import DAO.AdminActionLogDAO;
import Model.User;
import Model.Order;
import Util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(urlPatterns = {"/admin/users", "/admin/users/api"})
public class UserManageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(UserManageServlet.class);
    private UserDAO userDAO = new UserDAO();
    private AdminActionLogDAO actionLog = new AdminActionLogDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        // API endpoint để lấy pets và appointments
        if (path.equals("/admin/users/api")) {
            handleApiRequest(request, response);
            return;
        }
        
        String keyword = request.getParameter("keyword");
        String roleFilter = request.getParameter("role");
        
        List<User> users;
        
        // Tìm kiếm hoặc lọc
        if ((keyword != null && !keyword.isEmpty()) || (roleFilter != null && !roleFilter.isEmpty())) {
            users = userDAO.searchUsers(keyword, roleFilter);
        } else {
            users = userDAO.getAllUsersWithStats();
        }
        
        // Thống kê
        request.setAttribute("users", users);
        request.setAttribute("totalUsers", userDAO.countUsers());
        request.setAttribute("totalAdmins", userDAO.countUsersByRole("admin"));
        request.setAttribute("totalRegularUsers", userDAO.countUsersByRole("user"));
        request.setAttribute("newUsersThisWeek", userDAO.countNewUsersThisWeek());
        request.setAttribute("selectedRole", roleFilter);
        request.setAttribute("keyword", keyword);
        
        // Lấy chi tiết user nếu có
        String viewId = request.getParameter("viewId");
        if (viewId != null && !viewId.isEmpty()) {
            try {
                int userId = Integer.parseInt(viewId);
                User viewUser = userDAO.getUserFullById(userId);
                request.setAttribute("viewUser", viewUser);
                
                OrderDAO orderDAO = new OrderDAO();
                request.setAttribute("userOrders", orderDAO.getOrdersByUserId(userId));
            } catch (Exception e) {
                logger.error("Error loading orders for user id={}", viewId, e);
            }
        }
        
        request.getRequestDispatcher("/pages/admin/users.jsp").forward(request, response);
    }
    
    // API để lấy pets và appointments của user
    private void handleApiRequest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        String userIdStr = request.getParameter("userId");
        
        if (userIdStr == null || userIdStr.isEmpty()) {
            writeJsonError(response, "Missing userId");
            return;
        }
        
        try {
            int userId = Integer.parseInt(userIdStr);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            if ("getOrders".equals(action)) {
                OrderDAO orderDAO = new OrderDAO();
                List<Order> orders = orderDAO.getOrdersByUserId(userId);
                
                List<Map<String, Object>> orderList = new ArrayList<>();
                for (Order o : orders) {
                    Map<String, Object> orderData = new HashMap<>();
                    orderData.put("id", o.getId());
                    orderData.put("fullname", o.getFullname());
                    orderData.put("totalAmount", o.getTotalAmount());
                    orderData.put("formattedTotalAmount", o.getFormattedTotalAmount());
                    orderData.put("status", o.getStatus());
                    orderData.put("createdAt", o.getCreatedAt() != null ? sdf.format(o.getCreatedAt()) : "");
                    orderList.add(orderData);
                }
                response.getWriter().write(gson.toJson(orderList));
            } else {
                writeJsonError(response, "Invalid action");
            }
        } catch (Exception e) {
            writeJsonError(response, "An error occurred");
        }
    }
    
    private void writeJsonError(HttpServletResponse response, String message) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        response.getWriter().write(gson.toJson(error));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String message = "";
        String messageType = "success";
        User admin = (User) request.getSession().getAttribute("user");
        int adminId = admin != null ? admin.getId() : 1;

        try {
            switch (action) {
                case "add":
                    String username = request.getParameter("username");
                    String password = request.getParameter("password");
                    String fullname = request.getParameter("fullname");
                    String email = request.getParameter("email");
                    String phone = request.getParameter("phone");
                    String role = request.getParameter("role");
                    
                    if (!PasswordUtil.isStrongPassword(password)) {
                        message = "Mật khẩu phải có tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.";
                        messageType = "error";
                    } else if (userDAO.checkUsernameExists(username)) {
                        message = "Username đã tồn tại!";
                        messageType = "error";
                    } else if (userDAO.addUser(username, password, fullname, email, phone, role)) {
                        actionLog.log(adminId, "ADD_USER", "user", null,
                                "username=" + username + ";role=" + role);
                        message = "Thêm người dùng thành công!";
                    } else {
                        message = "Có lỗi xảy ra!";
                        messageType = "error";
                    }
                    break;
                    
                case "update":
                    int updateId = Integer.parseInt(request.getParameter("userId"));
                    String updateFullname = request.getParameter("fullname");
                    String updateEmail = request.getParameter("email");
                    String updatePhone = request.getParameter("phone");
                    String updateAddress = request.getParameter("address");
                    
                    if (userDAO.updateUser(updateId, updateFullname, updateEmail, updatePhone, updateAddress)) {
                        actionLog.log(adminId, "UPDATE_USER", "user", updateId, null);
                        message = "Cập nhật thông tin thành công!";
                    } else {
                        message = "Có lỗi xảy ra!";
                        messageType = "error";
                    }
                    break;
                    
                case "updateRole":
                    int roleUserId = Integer.parseInt(request.getParameter("userId"));
                    String newRole = request.getParameter("role");
                    
                    if (userDAO.updateUserRole(roleUserId, newRole)) {
                        actionLog.log(adminId, "UPDATE_ROLE", "user", roleUserId,
                                "newRole=" + newRole);
                        message = "Đã cập nhật quyền thành công!";
                    } else {
                        message = "Có lỗi xảy ra!";
                        messageType = "error";
                    }
                    break;
                    
                case "toggleStatus":
                    int statusUserId = Integer.parseInt(request.getParameter("userId"));
                    String newStatus = request.getParameter("status");
                    
                    if (userDAO.updateUserStatus(statusUserId, newStatus)) {
                        actionLog.log(adminId, "TOGGLE_STATUS", "user", statusUserId,
                                "newStatus=" + newStatus);
                        message = newStatus.equals("active") ? "Đã mở khóa tài khoản!" : "Đã khóa tài khoản!";
                    } else {
                        message = "Có lỗi xảy ra!";
                        messageType = "error";
                    }
                    break;
                    
                case "resetPassword":
                    int resetUserId = Integer.parseInt(request.getParameter("userId"));
                    String newPassword = request.getParameter("newPassword");
                    
                    if (!PasswordUtil.isStrongPassword(newPassword)) {
                        message = "Mật khẩu mới phải có tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.";
                        messageType = "error";
                    } else if (userDAO.resetUserPassword(resetUserId, newPassword)) {
                        actionLog.log(adminId, "RESET_PASSWORD", "user", resetUserId, null);
                        message = "Đã reset mật khẩu thành công!";
                    } else {
                        message = "Có lỗi xảy ra!";
                        messageType = "error";
                    }
                    break;
                    
                case "delete":
                    int deleteId = Integer.parseInt(request.getParameter("userId"));
                    
                    if (userDAO.deactivateUser(deleteId)) {
                        actionLog.log(adminId, "DELETE_USER", "user", deleteId, null);
                        message = "Đã vô hiệu hóa tài khoản thành công!";
                    } else {
                        message = "Có lỗi xảy ra khi xóa!";
                        messageType = "error";
                    }
                    break;
                    
                default:
                    message = "Hành động không hợp lệ!";
                    messageType = "error";
            }
        } catch (Exception e) {
            message = "Có lỗi xảy ra.";
            messageType = "error";
            logger.error("Admin user management action='{}' failed", action, e);
        }

        request.getSession().setAttribute("message", message);
        request.getSession().setAttribute("messageType", messageType);
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }
}
