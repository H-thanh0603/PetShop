package controller.shop;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import DAO.NotificationDAO;
import Model.Notification;
import Model.User;

/**
 * API Servlet cho Push Notifications
 */
@WebServlet("/api/notifications")
public class NotificationApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.getWriter().write("{\"error\":\"Unauthorized\"}");
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("count".equals(action)) {
            int count = notificationDAO.countUnread(user.getId());
            response.getWriter().write("{\"count\":" + count + "}");
        } else {
            // Lấy danh sách notifications
            int limit = 20;
            try { limit = Integer.parseInt(request.getParameter("limit")); } catch (Exception e) {}
            
            List<Notification> notifications = notificationDAO.getNotificationsByUserId(user.getId(), limit);
            int unreadCount = notificationDAO.countUnread(user.getId());
            
            StringBuilder json = new StringBuilder("{\"unreadCount\":" + unreadCount + ",\"notifications\":[");
            for (int i = 0; i < notifications.size(); i++) {
                Notification n = notifications.get(i);
                if (i > 0) json.append(",");
                json.append("{\"id\":").append(n.getId())
                    .append(",\"title\":\"").append(escapeJson(n.getTitle())).append("\"")
                    .append(",\"message\":\"").append(escapeJson(n.getMessage())).append("\"")
                    .append(",\"type\":\"").append(n.getType() != null ? n.getType() : "system").append("\"")
                    .append(",\"icon\":\"").append(n.getIcon()).append("\"")
                    .append(",\"link\":\"").append(n.getLink() != null ? escapeJson(n.getLink()) : "").append("\"")
                    .append(",\"isRead\":").append(n.isRead())
                    .append(",\"createdAt\":\"").append(n.getCreatedAt() != null ? n.getCreatedAt().toString() : "").append("\"")
                    .append("}");
            }
            json.append("]}");
            response.getWriter().write(json.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.getWriter().write("{\"error\":\"Unauthorized\"}");
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("markRead".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            boolean success = notificationDAO.markAsRead(id, user.getId());
            response.getWriter().write("{\"success\":" + success + "}");
        } else if ("markAllRead".equals(action)) {
            boolean success = notificationDAO.markAllAsRead(user.getId());
            response.getWriter().write("{\"success\":" + success + "}");
        }
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"")
                  .replace("\n", "\\n").replace("\r", "\\r");
    }
}
