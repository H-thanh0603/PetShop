package Util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertificateGeneratorTest {

    private static PublicKey publicKey;
    private static PrivateKey privateKey;
    private static PublicKey otherPublicKey;

    @BeforeAll
    public static void generateKeyPairs() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair keyPair1 = generator.generateKeyPair();
        publicKey = keyPair1.getPublic();
        privateKey = keyPair1.getPrivate();

        KeyPair keyPair2 = generator.generateKeyPair();
        otherPublicKey = keyPair2.getPublic();
    }

    @Test
    public void testGenerateX509_SubjectFormat() throws Exception {
        X509Certificate cert = CertificateGenerator.generateX509(publicKey, privateKey, 123, 456);
        String subject = cert.getSubjectX500Principal().getName();
        assertTrue(subject.contains("CN=order_123"));
        assertTrue(subject.contains("OU=user_456"));
        assertTrue(subject.contains("O=PetShop"));
        assertTrue(subject.contains("C=VN"));
    }

    @Test
    public void testGenerateX509_ValidForOneYear() throws Exception {
        X509Certificate cert = CertificateGenerator.generateX509(publicKey, privateKey, 100, 200);
        Date notBefore = cert.getNotBefore();
        Date notAfter = cert.getNotAfter();
        long diffMs = notAfter.getTime() - notBefore.getTime();
        long oneYearMs = 365L * 24 * 60 * 60 * 1000;
        // Cho phep sai lech 1 ngay (365 hoac 366 ngay)
        assertTrue(diffMs >= 365L * 24 * 60 * 60 * 1000 && diffMs <= 367L * 24 * 60 * 60 * 1000,
                "Certificate validity should be ~1 year, was " + diffMs + " ms");
    }

    @Test
    public void testGenerateX509_SelfSigned() throws Exception {
        X509Certificate cert = CertificateGenerator.generateX509(publicKey, privateKey, 10, 20);
        // Self-signed: subject = issuer
        assertEquals(cert.getSubjectX500Principal().getName(), cert.getIssuerX500Principal().getName());
    }

    @Test
    public void testEncodeDecodeCertificate_SameContent() throws Exception {
        X509Certificate cert = CertificateGenerator.generateX509(publicKey, privateKey, 55, 99);
        String pem = CertificateGenerator.encodeCertificate(cert);
        assertTrue(pem.contains("-----BEGIN CERTIFICATE-----"));
        assertTrue(pem.contains("-----END CERTIFICATE-----"));

        X509Certificate decoded = CertificateGenerator.decodeCertificate(pem);
        assertArrayEquals(cert.getEncoded(), decoded.getEncoded());
        assertTrue(cert.equals(decoded));
    }

    @Test
    public void testVerifyCertificate_ValidKey_ReturnsTrue() throws Exception {
        X509Certificate cert = CertificateGenerator.generateX509(publicKey, privateKey, 1, 1);
        boolean result = CertificateGenerator.verifyCertificate(cert, publicKey);
        assertTrue(result);
    }

    @Test
    public void testVerifyCertificate_WrongKey_ReturnsFalse() throws Exception {
        X509Certificate cert = CertificateGenerator.generateX509(publicKey, privateKey, 1, 1);
        boolean result = CertificateGenerator.verifyCertificate(cert, otherPublicKey);
        assertFalse(result);
    }

    @Test
    public void testVerifyCertificate_NullInput_ReturnsFalse() {
        assertFalse(CertificateGenerator.verifyCertificate(null, publicKey));
        X509Certificate cert = null;
        try {
            cert = CertificateGenerator.generateX509(publicKey, privateKey, 1, 1);
        } catch (Exception e) {
            fail("Should not throw");
        }
        assertFalse(CertificateGenerator.verifyCertificate(cert, null));
    }

    @Test
    public void testGenerateX509_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CertificateGenerator.generateX509(null, privateKey, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> CertificateGenerator.generateX509(publicKey, null, 1, 1));
    }

    @Test
    public void testEncodeCertificate_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CertificateGenerator.encodeCertificate(null));
    }

    @Test
    public void testDecodeCertificate_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> CertificateGenerator.decodeCertificate(null));
        assertThrows(IllegalArgumentException.class, () -> CertificateGenerator.decodeCertificate(""));
    }
}
