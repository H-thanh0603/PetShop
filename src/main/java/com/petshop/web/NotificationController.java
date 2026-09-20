package com.petshop.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import DAO.NotificationDAO;
import Model.User;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces UserNotificationServlet 1:1 — guest returns empty payloads,
 * authenticated users get unread count / list / mark-all-read.
 */
@Controller
public class NotificationController {

    private final NotificationDAO notificationDAO;
    private final Gson gson = new Gson();

    public NotificationController() {
        this(new NotificationDAO());
    }

    NotificationController(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    @GetMapping(value = "/notifications/unread-count", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String unreadCount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", user == null ? 0 : notificationDAO.getUnreadCountByUserId(user.getId()));
        return gson.toJson(result);
    }

    @GetMapping(value = "/notifications/list", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String list(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return gson.toJson(new ArrayList<>());
        }
        List<Map<String, Object>> list = notificationDAO.getNotificationsByUserId(user.getId(), 10);
        return gson.toJson(list);
    }

    @PostMapping(value = "/notifications/mark-read", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String markRead(HttpSession session,
                           jakarta.servlet.http.HttpServletResponse response) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
            return "{\"error\":\"Unauthorized\"}";
        }
        boolean success = notificationDAO.markAllAsRead(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return gson.toJson(result);
    }
}
