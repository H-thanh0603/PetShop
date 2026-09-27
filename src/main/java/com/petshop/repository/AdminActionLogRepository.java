package com.petshop.repository;

import com.petshop.model.AdminActionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Integer> {

    @Transactional
    default void log(int adminId, String actionType, String targetType, Integer targetId, String details) {
        try {
            AdminActionLog entry = new AdminActionLog();
            entry.setAdminId(adminId);
            entry.setActionType(actionType);
            entry.setTargetType(targetType);
            entry.setTargetId(targetId);
            entry.setDetails(details);
            save(entry);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AdminActionLogRepository.class)
                    .warn("[AdminActionLog] Failed to log action: " + e.getMessage());
        }
    }
}
