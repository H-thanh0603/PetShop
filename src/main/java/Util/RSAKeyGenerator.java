package Util;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAKeyGenerator {
    // su dung rsa
    private static final String ASYMETRIC = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final int KEY_SIZE = 2048;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public RSAKeyGenerator() throws Exception {
        // khoi tao rsa
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ASYMETRIC);
        generator.initialize(KEY_SIZE, new SecureRandom()); // khoi tao kich thuoc rsa
        KeyPair keyPair = generator.generateKeyPair();  // generate public and private key
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }

    public RSAKeyGenerator(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }


    // chuyen ve String
    public String encodePublicKey() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    public String encodePrivateKey() {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }


    // chuyen nguoc ve ma goc
    public static PublicKey decodePublicKey(String encoded) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(encoded);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        return KeyFactory.getInstance(ASYMETRIC).generatePublic(spec);
    }
    public static PrivateKey decodePrivateKey(String encoded) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(encoded);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        return KeyFactory.getInstance(ASYMETRIC).generatePrivate(spec);
    }


    public String sign(String data) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    public static boolean verify(String data, String signedBase64, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data.getBytes("UTF-8"));
        return signature.verify(Base64.getDecoder().decode(signedBase64));
    }
}
