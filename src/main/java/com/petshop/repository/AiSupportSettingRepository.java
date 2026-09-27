package com.petshop.repository;

import com.petshop.model.AiSupportSetting;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AiSupportSettingRepository extends JpaRepository<AiSupportSetting, Integer> {

    AiSupportSetting findBySettingKey(String settingKey);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE AiSupportSetting s SET s.settingValue = :value WHERE s.settingKey = :key")
    int updateSettingValue(@Param("key") String key, @Param("value") String value);

    @Transactional
    default String getSetting(String key, String defaultValue) {
        try {
            AiSupportSetting setting = findBySettingKey(key);
            if (setting != null && setting.getSettingValue() != null) {
                return setting.getSettingValue();
            }
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AiSupportSettingRepository.class)
                    .error("Error getting setting key={}", key, e);
        }
        return defaultValue;
    }

    @Transactional
    default boolean updateSetting(String key, String value) {
        try {
            return updateSettingValue(key, value) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AiSupportSettingRepository.class)
                    .error("Error updating setting key={}", key, e);
            return false;
        }
    }

    @Transactional
    default Map<String, String> getAllSettings() {
        Map<String, String> map = new HashMap<>();
        try {
            for (AiSupportSetting setting : findAll()) {
                map.put(setting.getSettingKey(), setting.getSettingValue());
            }
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AiSupportSettingRepository.class)
                    .error("Error fetching all settings", e);
        }
        return map;
    }
}
