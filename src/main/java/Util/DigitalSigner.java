package Util;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class DigitalSigner {
    // hash don hang
    // Input:  "orderId=123&amount=500000"
    //  Output: "a1b2c3d4e5f6...64 ký tự hex..."
    public static String hashOrderData(String data) {
        if (data == null || data.isEmpty())
            throw new IllegalArgumentException("Data không được null hoặc rỗng");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 không khả dụng", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }



    // ky hash don hang
    // Input:  orderHash (hex string 64 ky tu), privateKeyBase64 (Base64 cua private key)
    // Output: chu ky so dang Base64
    public static String signOrderHash(String orderHash, String privateKeyBase64) {
        if (orderHash == null || orderHash.isEmpty())
            throw new IllegalArgumentException("orderHash không được null hoặc rỗng");
        if (privateKeyBase64 == null || privateKeyBase64.isEmpty())
            throw new IllegalArgumentException("privateKeyBase64 không được null hoặc rỗng");
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            // tuong tu modulus + exponent (d) + p + q
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(orderHash.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("privateKeyBase64 không hợp lệ: không phải Base64", e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi ký số: " + e.getMessage(), e);
        }
    }



    // xac thuc chu ky so
    // Input:  orderHash (hex string), signatureBase64 (chu ky can xac thuc), publicKeyBase64 (Base64 cua public key)
    // Output: true neu chu ky hop le, false neu khong hop le (moi loi deu tra ve false)
    public static boolean verifySignature(String orderHash, String signatureBase64, String publicKeyBase64) {
        if (orderHash == null || orderHash.isEmpty()
                || signatureBase64 == null || signatureBase64.isEmpty()
                || publicKeyBase64 == null || publicKeyBase64.isEmpty()) {
            return false;
        }
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(orderHash.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(signatureBase64));
        } catch (Exception e) {
            return false;
        }
    }
}
