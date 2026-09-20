package Util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

// ponytail: covers BCrypt roundtrip + legacy jBCrypt $2a$ hash compat after
// migrating off org.mindrot:jbcrypt. Expand only if hash format changes.
class PasswordUtilTest {

    @Test
    void roundtrip() {
        String hash = PasswordUtil.hashPassword("Str0ng!Pass");
        assertTrue(PasswordUtil.verifyPassword("Str0ng!Pass", hash));
        assertFalse(PasswordUtil.verifyPassword("Wrong!Pass1", hash));
    }

    @Test
    void verifiesLegacyJBcryptHash() {
        // Real org.mindrot jBCrypt 0.4 hash of "password" at cost 10,
        // generated from jbcrypt-0.4.jar (gradle cache).
        String legacy = "$2a$10$n6K8liV8CA1WAHHHkuNnU.x9flkMT73o7J3GzHIDlfu7/6hVYCW8e";
        assertTrue(PasswordUtil.verifyPassword("password", legacy));
        assertFalse(PasswordUtil.verifyPassword("not-password", legacy));
    }

    @Test
    void nullSafe() {
        assertFalse(PasswordUtil.verifyPassword(null, "$2a$10$n6K8liV8CA1WAHHHkuNnU.x9flkMT73o7J3GzHIDlfu7/6hVYCW8e"));
        assertFalse(PasswordUtil.verifyPassword("password", null));
        assertFalse(PasswordUtil.verifyPassword("password", "not-a-hash"));
    }
}
