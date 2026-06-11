package controller.shop;

import DAO.ChatDAO;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint("/chat/{userId}")
public class ChatWebSocketServer {

    private static final Map<Integer, Session> clients =
            Collections.synchronizedMap(new HashMap<>());

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") int userId) {
        clients.put(userId, session);
        System.out.println("Chat mở - User ID: " + userId);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") int senderId) {
        try {
            JSONObject json = new JSONObject(message);
            int receiverId  = json.getInt("receiverId");
            String text     = json.getString("text");
            boolean isAdmin = json.optBoolean("isAdmin", false);

            // Lưu vào DB
            new ChatDAO().saveMessage(senderId, receiverId, text, isAdmin);

            // Gửi real-time nếu người nhận đang online
            Session receiverSession = clients.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                JSONObject out = new JSONObject();
                out.put("senderId", senderId);
                out.put("text", text);
                out.put("isAdmin", isAdmin);
                receiverSession.getBasicRemote().sendText(out.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") int userId) {
        clients.remove(userId);
        System.out.println("Chat đóng - User ID: " + userId);
    }

    @OnError
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    // Cho phép Admin gửi tin nhắn đến user cụ thể
    public static void sendToUser(int userId, JSONObject message) {
        Session session = clients.get(userId);
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}