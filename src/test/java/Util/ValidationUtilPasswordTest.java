package Util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationUtilPasswordTest {

    @Test
    void passwordValidationRequiresTheSharedStrongPasswordPolicy() {
        assertFalse(ValidationUtil.isValidPassword("abcdef"));
        assertFalse(ValidationUtil.isValidPassword("abcdefgh"));
        assertTrue(ValidationUtil.isValidPassword("PetShop@2026"));
    }
}
