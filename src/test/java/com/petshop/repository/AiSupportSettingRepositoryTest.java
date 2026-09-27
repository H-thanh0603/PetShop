package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AiSupportSettingRepositoryTest {

    @Autowired
    private AiSupportSettingRepository repository;

    @Test
    void getSettingReturnsSeededValue() {
        // Seeded by LegacySchemaMigrator on fresh DBs.
        assertEquals("true", repository.getSetting("AI_SUPPORT_ENABLED", "fallback"));
    }

    @Test
    void getSettingReturnsDefaultForMissingKey() {
        assertEquals("fallback", repository.getSetting("NO_SUCH_KEY_P2", "fallback"));
    }

    @Test
    void updateSettingThenGetReflectsNewValue() {
        assertTrue(repository.updateSetting("AI_SUPPORT_ENABLED", "false"));
        assertEquals("false", repository.getSetting("AI_SUPPORT_ENABLED", "fallback"));
    }

    @Test
    void getAllSettingsContainsSeededKeys() {
        assertTrue(repository.getAllSettings().containsKey("AI_SUPPORT_ENABLED"));
    }
}
