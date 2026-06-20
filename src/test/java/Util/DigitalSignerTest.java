package Util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class DigitalSignerTest {

    private static String publicKeyBase64;
    private static String privateKeyBase64;

    @BeforeAll
    public static void generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        publicKeyBase64 = java.util.Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        privateKeyBase64 = java.util.Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    @Test
    public void testHashOrderData_Returns64HexChars() {
        String hash = DigitalSigner.hashOrderData("orderId=123&amount=500000");
        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    public void testHashOrderData_SameInputSameOutput() {
        String hash1 = DigitalSigner.hashOrderData("orderId=123&amount=500000");
        String hash2 = DigitalSigner.hashOrderData("orderId=123&amount=500000");
        assertEquals(hash1, hash2);
    }

    @Test
    public void testHashOrderData_DifferentInputDifferentOutput() {
        String hash1 = DigitalSigner.hashOrderData("orderId=123");
        String hash2 = DigitalSigner.hashOrderData("orderId=124");
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testHashOrderData_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> DigitalSigner.hashOrderData(null));
    }

    @Test
    public void testHashOrderData_EmptyInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> DigitalSigner.hashOrderData(""));
    }

    @Test
    public void testSignOrderHash_ReturnsBase64() {
        String hash = DigitalSigner.hashOrderData("orderId=123&amount=500000");
        String signature = DigitalSigner.signOrderHash(hash, privateKeyBase64);
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        assertTrue(signature.matches("^[A-Za-z0-9+/]+=*$"));
    }

    @Test
    public void testVerifySignature_ValidSignature_ReturnsTrue() {
        String hash = DigitalSigner.hashOrderData("orderId=123&amount=500000");
        String signature = DigitalSigner.signOrderHash(hash, privateKeyBase64);
        boolean result = DigitalSigner.verifySignature(hash, signature, publicKeyBase64);
        assertTrue(result);
    }

    @Test
    public void testVerifySignature_DataChanged_ReturnsFalse() {
        String hash = DigitalSigner.hashOrderData("orderId=123&amount=500000");
        String signature = DigitalSigner.signOrderHash(hash, privateKeyBase64);
        String changedHash = DigitalSigner.hashOrderData("orderId=123&amount=999999");
        boolean result = DigitalSigner.verifySignature(changedHash, signature, publicKeyBase64);
        assertFalse(result);
    }

    @Test
    public void testVerifySignature_NullInput_ReturnsFalse() {
        assertFalse(DigitalSigner.verifySignature(null, "abc", publicKeyBase64));
        assertFalse(DigitalSigner.verifySignature("abc", null, publicKeyBase64));
        assertFalse(DigitalSigner.verifySignature("abc", "abc", null));
    }

    @Test
    public void testVerifySignature_EmptyInput_ReturnsFalse() {
        assertFalse(DigitalSigner.verifySignature("", "abc", publicKeyBase64));
        assertFalse(DigitalSigner.verifySignature("abc", "", publicKeyBase64));
        assertFalse(DigitalSigner.verifySignature("abc", "abc", ""));
    }

    @Test
    public void testVerifySignature_InvalidBase64_ReturnsFalse() {
        String hash = DigitalSigner.hashOrderData("test");
        assertFalse(DigitalSigner.verifySignature(hash, "not-base64!!!", publicKeyBase64));
        assertFalse(DigitalSigner.verifySignature(hash, hash, "not-base64!!!"));
    }
}
