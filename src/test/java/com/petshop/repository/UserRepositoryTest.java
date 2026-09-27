package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    private String unique(String prefix) {
        return prefix + "_" + Math.abs(System.nanoTime() % 1000000);
    }

    @Test
    void registerThenLoginByUsername() {
        String username = unique("loginuser");
        assertTrue(repository.register(username, "StrongPass1!", "Login User", username + "@test.local"));
        User found = repository.login(username, "StrongPass1!");
        assertNotNull(found);
        assertEquals(username, found.getUsername());
        assertNull(repository.login(username, "WrongPass1!"));
    }

    @Test
    void registerDuplicateUsernameReturnsFalseOnConstraintViolation() {
        String username = unique("dupuser");
        assertTrue(repository.register(username, "StrongPass1!", "Dup", username + "@test.local"));
        assertEquals(false, repository.register(username, "StrongPass1!", "Dup2", username + "2@test.local"));
    }

    @Test
    void statusConverterReadsActiveAsTrue() {
        String username = unique("statususer");
        repository.register(username, "StrongPass1!", "Status", username + "@test.local");
        User found = repository.login(username, "StrongPass1!");
        assertNotNull(found);
        // DB default is the VARCHAR 'active'; legacy getBoolean+catch read it as true.
        assertTrue(found.getStatus());
    }

    @Test
    void verificationTokenFlow() {
        String username = unique("verifyuser");
        repository.register(username, "StrongPass1!", "Verify", username + "@test.local");
        User user = repository.login(username, "StrongPass1!");
        repository.saveVerificationToken(user.getId(), "tok123", new java.sql.Timestamp(System.currentTimeMillis() + 60000));
        assertNotNull(repository.getUserByVerificationToken("tok123"));
        assertTrue(repository.markEmailVerified(user.getId()));
        assertTrue(repository.isEmailVerified(username + "@test.local"));
    }

    @Test
    void lockoutCounterRoundTrip() {
        String username = unique("lockuser");
        repository.register(username, "StrongPass1!", "Lock", username + "@test.local");
        assertEquals(0, repository.getFailedLoginAttempts(username));
        repository.incrementFailedAttempts(username);
        assertEquals(1, repository.getFailedLoginAttempts(username));
        repository.lockAccount(username, 5);
        assertTrue(repository.isAccountLocked(username));
        repository.resetFailedAttempts(username);
        assertEquals(0, repository.getFailedLoginAttempts(username));
        assertFalse(repository.isAccountLocked(username));
    }
}
