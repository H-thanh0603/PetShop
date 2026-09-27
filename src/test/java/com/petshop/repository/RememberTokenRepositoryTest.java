package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import java.time.LocalDateTime;
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
class RememberTokenRepositoryTest {

    @Autowired
    private RememberTokenRepository repository;

    @Test
    void saveTokenThenFindMatchingTokenReturnsIdAndUserId() {
        assertTrue(repository.saveToken(1, "plain-secret-1"));
        int[] outUserId = new int[1];
        int id = repository.findMatchingToken("plain-secret-1", outUserId);
        assertTrue(id > 0);
        assertEquals(1, outUserId[0]);
    }

    @Test
    void findMatchingTokenReturnsMinusOneForWrongSecret() {
        repository.saveToken(1, "right-secret");
        assertEquals(-1, repository.findMatchingToken("wrong-secret", new int[1]));
    }

    @Test
    void deleteExpiredTokensKeepsFreshTokens() {
        repository.saveToken(1, "fresh");
        repository.saveToken(1, "stale");
        assertEquals(2, repository.findAll().size());
        repository.deleteExpiredTokens(LocalDateTime.now().plusDays(8));
        assertEquals(0, repository.findAll().size());
    }

    @Test
    void deleteAllTokensForUserRemovesOnlyThatUser() {
        repository.saveToken(1, "u1-token");
        repository.saveToken(2, "u2-token");
        repository.deleteAllTokensForUser(1);
        assertEquals(0, repository.findByUserId(1).size());
        assertEquals(1, repository.findByUserId(2).size());
    }
}
