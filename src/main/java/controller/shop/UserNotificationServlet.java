package controller.shop;

import DAO.NotificationDAO;
import Model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet({"/notifications/unread-count", "/notifications/list", "/notifications/mark-read"})
public class UserNotificationServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(UserNotificationServlet.class);
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            // Guest has 0 notifications
            if ("/notifications/unread-count".equals(path)) {
                JsonObject result = new JsonObject();
                result.addProperty("unreadCount", 0);
                response.getWriter().write(gson.toJson(result));
            } else if ("/notifications/list".equals(path)) {
                response.getWriter().write(gson.toJson(new ArrayList<>()));
            }
            return;
        }

        if ("/notifications/unread-count".equals(path)) {
            int count = notificationDAO.getUnreadCountByUserId(user.getId());
            JsonObject result = new JsonObject();
            result.addProperty("unreadCount", count);
            response.getWriter().write(gson.toJson(result));
            
        } else if ("/notifications/list".equals(path)) {
            List<Map<String, Object>> list = notificationDAO.getNotificationsByUserId(user.getId(), 10);
            response.getWriter().write(gson.toJson(list));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Unauthorized\"}");
            return;
        }

        if ("/notifications/mark-read".equals(path)) {
            boolean success = notificationDAO.markAllAsRead(user.getId());
            JsonObject result = new JsonObject();
            result.addProperty("success", success);
            response.getWriter().write(gson.toJson(result));
        }
    }
}
