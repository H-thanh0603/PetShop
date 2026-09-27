package com.petshop.repository;

import com.petshop.model.SecurityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Integer> {

    @Transactional
    default void log(String eventType, String principal, String ipAddress, String details) {
        try {
            SecurityEvent event = new SecurityEvent();
            event.setEventType(eventType);
            event.setPrincipal(principal);
            event.setIpAddress(ipAddress);
            event.setDetails(details);
            save(event);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(SecurityEventRepository.class)
                    .error("Failed to write security event {}", eventType, e);
        }
    }
}
