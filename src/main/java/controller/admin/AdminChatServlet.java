package controller.admin;

import DAO.ChatDAO;
import Model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import controller.shop.ChatWebSocketServer;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/chat")
public class AdminChatServlet extends HttpServlet {

    private static final int ADMIN_ID = 1; // ID admin mặc định

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        ChatDAO dao = new ChatDAO();
        List<Map<String, Object>> users = dao.getUsersWithMessages();
        request.setAttribute("userList", users);

        // Nếu chọn user cụ thể thì load lịch sử chat
        String userIdParam = request.getParameter("userId");
        if (userIdParam != null) {
            int selectedUserId = Integer.parseInt(userIdParam);
            request.setAttribute("selectedUserId", selectedUserId);
            request.setAttribute("history", dao.getHistory(selectedUserId, ADMIN_ID));
        }

        request.getRequestDispatcher("/pages/admin/admin-chat.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        int receiverId  = Integer.parseInt(request.getParameter("receiverId"));
        String text     = request.getParameter("message");

        // Lưu DB
        new ChatDAO().saveMessage(ADMIN_ID, receiverId, text, true);

        // Gửi real-time qua WebSocket
        JSONObject out = new JSONObject();
        out.put("senderId", ADMIN_ID);
        out.put("text", text);
        out.put("isAdmin", true);
        ChatWebSocketServer.sendToUser(receiverId, out);

        response.sendRedirect(request.getContextPath() + "/admin/chat?userId=" + receiverId);
    }
}