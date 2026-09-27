package com.petshop.repository;

import com.petshop.model.Notification;
import com.petshop.model.NotificationView;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    @Query("SELECT n.id AS id, n.userId AS userId, n.title AS title, n.message AS message, "
            + "n.type AS type, n.link AS link, n.read AS read, n.createdAt AS createdAt "
            + "FROM Notification n WHERE n.userId = :userId ORDER BY n.createdAt DESC LIMIT :limit")
    List<NotificationView> findViewsByUserId(@Param("userId") int userId, @Param("limit") int limit);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.read = false")
    int getUnreadCountByUserId(@Param("userId") int userId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") int userId);

    @Transactional
    default List<java.util.Map<String, Object>> getNotificationsByUserId(int userId, int limit) {
        List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        try {
            for (NotificationView view : findViewsByUserId(userId, limit)) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", view.getId());
                map.put("userId", view.getUserId());
                map.put("title", view.getTitle());
                map.put("message", view.getMessage());
                map.put("type", view.getType());
                map.put("link", view.getLink());
                map.put("isRead", view.getRead());
                map.put("createdAt", view.getCreatedAt() == null ? null : view.getCreatedAt().toString());
                list.add(map);
            }
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(NotificationRepository.class)
                    .error("Error fetching notifications for user={}", userId, e);
        }
        return list;
    }

    @Transactional
    default boolean create(int userId, String title, String message, String type, String link) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(type);
            notification.setLink(link);
            notification.setRead(false);
            save(notification);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(NotificationRepository.class)
                    .error("Error creating notification for user={}", userId, e);
            return false;
        }
    }

    @Transactional
    default boolean markAllAsReadBool(int userId) {
        try {
            return markAllAsRead(userId) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(NotificationRepository.class)
                    .error("Error marking notifications read for user={}", userId, e);
            return false;
        }
    }
}
