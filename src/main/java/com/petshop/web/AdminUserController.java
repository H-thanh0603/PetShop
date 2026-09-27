package com.petshop.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import DAO.AdminActionLogDAO;
import DAO.OrderDAO;
import DAO.UserDAO;
import Model.Order;
import Model.User;
import Util.Json;
import Util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces UserManageServlet (/admin/users + /admin/users/api) 1:1 — same
 * list/search page, same user-detail panel, same add/update/role/status/
 * resetPassword/delete flows. AuthorizationFilter still guards admin paths.
 */
@Controller
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    private final UserDAO userDAO;
    private final OrderDAO orderDAO;
    private final AdminActionLogDAO actionLog;

    public AdminUserController() {
        this(new UserDAO(), new OrderDAO(), new AdminActionLogDAO());
    }

    AdminUserController(UserDAO userDAO, OrderDAO orderDAO, AdminActionLogDAO actionLog) {
        this.userDAO = userDAO;
        this.orderDAO = orderDAO;
        this.actionLog = actionLog;
    }

    @GetMapping("/admin/users")
    public String users(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String roleFilter,
            @RequestParam(value = "viewId", required = false) String viewIdRaw,
            Model model) {
        List<User> users;

        // Tìm kiếm hoặc lọc
        if ((keyword != null && !keyword.isEmpty()) || (roleFilter != null && !roleFilter.isEmpty())) {
            users = userDAO.searchUsers(keyword, roleFilter);
        } else {
            users = userDAO.getAllUsersWithStats();
        }

        // Thống kê
        model.addAttribute("users", users);
        model.addAttribute("totalUsers", userDAO.countUsers());
        model.addAttribute("totalAdmins", userDAO.countUsersByRole("admin"));
        model.addAttribute("totalRegularUsers", userDAO.countUsersByRole("user"));
        model.addAttribute("newUsersThisWeek", userDAO.countNewUsersThisWeek());
        model.addAttribute("selectedRole", roleFilter);
        model.addAttribute("keyword", keyword);

        // Lấy chi tiết user nếu có
        if (viewIdRaw != null && !viewIdRaw.isEmpty()) {
            try {
                int userId = Integer.parseInt(viewIdRaw);
                User viewUser = userDAO.getUserFullById(userId);
                model.addAttribute("viewUser", viewUser);
                model.addAttribute("userOrders", orderDAO.getOrdersByUserId(userId));
            } catch (Exception e) {
                logger.error("Error loading orders for user id={}", viewIdRaw, e);
            }
        }

        return "pages/admin/users";
    }

    @GetMapping(value = "/admin/users/api", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String usersApi(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "userId", required = false) String userIdStr) {
        if (userIdStr == null || userIdStr.isEmpty()) {
            return Json.MAPPER.writeValueAsString(Map.of("error", "Missing userId"));
        }

        try {
            int userId = Integer.parseInt(userIdStr);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            if ("getOrders".equals(action)) {
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
                return Json.MAPPER.writeValueAsString(orderList);
            }
            return Json.MAPPER.writeValueAsString(Map.of("error", "Invalid action"));
        } catch (Exception e) {
            return Json.MAPPER.writeValueAsString(Map.of("error", "An error occurred"));
        }
    }

    @PostMapping("/admin/users")
    public String usersPost(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "fullname", required = false) String fullname,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "userId", required = false) String userIdRaw,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "status", required = false) String newStatus,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            HttpServletRequest request,
            HttpSession session) {
        String message = "";
        String messageType = "success";
        User admin = (User) session.getAttribute("user");
        int adminId = admin != null ? admin.getId() : 1;

        try {
            switch (action == null ? "" : action) {
                case "add":
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
                    int updateId = Integer.parseInt(userIdRaw);
                    if (userDAO.updateUser(updateId, fullname, email, phone, address)) {
                        actionLog.log(adminId, "UPDATE_USER", "user", updateId, null);
                        message = "Cập nhật thông tin thành công!";
                    } else {
                        message = "Có lỗi xảy ra!";
                        messageType = "error";
                    }
                    break;

                case "updateRole":
                    int roleUserId = Integer.parseInt(userIdRaw);
                    if (userDAO.updateUserRole(roleUserId, role)) {
                        actionLog.log(adminId, "UPDATE_ROLE", "user", roleUserId,
                                "newRole=" + role);
                        message = "Đã cập nhật quyền thành công!";
                    } else {
                        message = "Có lỗi xảy ra!";
                        messageType = "error";
                    }
                    break;

                case "toggleStatus":
                    int statusUserId = Integer.parseInt(userIdRaw);
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
                    int resetUserId = Integer.parseInt(userIdRaw);
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
                    int deleteId = Integer.parseInt(userIdRaw);
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

        session.setAttribute("message", message);
        session.setAttribute("messageType", messageType);
        return "redirect:" + request.getContextPath() + "/admin/users";
    }
}
