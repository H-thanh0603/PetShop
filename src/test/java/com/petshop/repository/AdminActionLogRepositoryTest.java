package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminActionLogRepositoryTest {

    @Autowired
    private AdminActionLogRepository repository;

    @Test
    void logPersistsEntry() {
        repository.log(1, "UPDATE_STATUS", "order", 42, "details here");
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void logWithNullTargetId() {
        repository.log(1, "LOGIN", "session", null, null);
        assertEquals(1, repository.findAll().size());
    }
}
