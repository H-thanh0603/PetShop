package controller.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import DAO.UserDAO;
import DAO.OrderDAO;
import Model.User;
import Model.Order;

@WebServlet(urlPatterns = {"/admin/users", "/admin/users/api"})
public class UserManageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO = new UserDAO();

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
                e.printStackTrace();
            }
        }
        
        request.getRequestDispatcher("/pages/admin/users.jsp").forward(request, response);
    }
    
    // API để lấy pets và appointments của user
    private void handleApiRequest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        String action = request.getParameter("action");
        String userIdStr = request.getParameter("userId");
        
        if (userIdStr == null || userIdStr.isEmpty()) {
            out.print("{\"error\": \"Missing userId\"}");
            return;
        }
        
        try {
            int userId = Integer.parseInt(userIdStr);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            if ("getOrders".equals(action)) {
                OrderDAO orderDAO = new OrderDAO();
                List<Order> orders = orderDAO.getOrdersByUserId(userId);
                
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < orders.size(); i++) {
                    Order o = orders.get(i);
                    if (i > 0) json.append(",");
                    json.append("{");
                    json.append("\"id\":").append(o.getId()).append(",");
                    json.append("\"fullname\":\"").append(escapeJson(o.getFullname())).append("\",");
                    json.append("\"totalAmount\":").append(o.getTotalAmount()).append(",");
                    json.append("\"formattedTotalAmount\":\"").append(escapeJson(o.getFormattedTotalAmount())).append("\",");
                    json.append("\"status\":\"").append(escapeJson(o.getStatus())).append("\",");
                    json.append("\"createdAt\":\"").append(o.getCreatedAt() != null ? sdf.format(o.getCreatedAt()) : "").append("\"");
                    json.append("}");
                }
                json.append("]");
                out.print(json.toString());
            } else {
                out.print("{\"error\": \"Invalid action\"}");
            }
        } catch (Exception e) {
            out.print("{\"error\": \"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    // Helper để escape JSON string
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String message = "";
        String messageType = "success";

        try {
            switch (action) {
                case "add":
                    String username = request.getParameter("username");
                    String password = request.getParameter("password");
                    String fullname = request.getParameter("fullname");
                    String email = request.getParameter("email");
                    String phone = request.getParameter("phone");
                    String role = request.getParameter("role");
                    
                    if (userDAO.checkUsernameExists(username)) {
                        message = "Username đã tồn tại!";
                        messageType = "error";
                    } else if (userDAO.addUser(username, password, fullname, email, phone, role)) {
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
                        message = newStatus.equals("active") ? "Đã mở khóa tài khoản!" : "Đã khóa tài khoản!";
                    } else {
                        message = "Có lỗi xảy ra!";
                        messageType = "error";
                    }
                    break;
                    
                case "resetPassword":
                    int resetUserId = Integer.parseInt(request.getParameter("userId"));
                    String newPassword = request.getParameter("newPassword");
                    
                    if (userDAO.resetUserPassword(resetUserId, newPassword)) {
                        message = "Đã reset mật khẩu thành công!";
                    } else {
                        message = "Có lỗi xảy ra!";
                        messageType = "error";
                    }
                    break;
                    
                case "delete":
                    int deleteId = Integer.parseInt(request.getParameter("userId"));
                    
                    if (userDAO.deleteUser(deleteId)) {
                        message = "Đã xóa người dùng thành công!";
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
            message = "Có lỗi xảy ra: " + e.getMessage();
            messageType = "error";
            e.printStackTrace();
        }

        request.getSession().setAttribute("message", message);
        request.getSession().setAttribute("messageType", messageType);
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }
}
