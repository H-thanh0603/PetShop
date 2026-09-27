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
class SecurityEventRepositoryTest {

    @Autowired
    private SecurityEventRepository repository;

    @Test
    void logPersistsEvent() {
        repository.log("LOGIN_FAIL", "user@example.com", "127.0.0.1", "bad password");
        assertEquals(1, repository.findAll().size());
    }
}
