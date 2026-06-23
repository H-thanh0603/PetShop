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
            String[] parts = privateKeyBase64.split(":");
            java.math.BigInteger d, n;
            if (parts.length == 2) {
                d = new java.math.BigInteger(parts[0].trim(), 16);
                n = new java.math.BigInteger(parts[1].trim(), 16);
            } else {
                byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
                java.security.interfaces.RSAPrivateKey privateKey = (java.security.interfaces.RSAPrivateKey) java.security.KeyFactory.getInstance("RSA").generatePrivate(spec);
                d = privateKey.getPrivateExponent();
                n = privateKey.getModulus();
            }
            java.math.BigInteger hashInt = new java.math.BigInteger(orderHash.trim(), 16);
            if (hashInt.compareTo(n) >= 0)
                throw new IllegalArgumentException("Hash quá lớn so với key size");
            java.math.BigInteger signature = hashInt.modPow(d, n);
            return signature.toString(16);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("privateKey không hợp lệ: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi ký số: " + e.getMessage(), e);
        }
    }



    // xac thuc chu ky so
    // Input:  orderHash (hex string), signatureBase64 (chu ky can xac thuc), publicKeyBase64 (Base64 cua public key)
    // Output: true neu chu ky hop le, false neu khong hop le (moi loi deu tra ve false)
    public static boolean verifySignature(String orderHash, String signatureHex, String publicKeyBase64) {
        if (orderHash == null || orderHash.isEmpty()
                || signatureHex == null || signatureHex.isEmpty()
                || publicKeyBase64 == null || publicKeyBase64.isEmpty()) {
            return false;
        }
        try {
            java.math.BigInteger e, n;
            String[] parts = publicKeyBase64.split(":");
            if (parts.length == 2) {
                e = new java.math.BigInteger(parts[0].trim(), 16);
                n = new java.math.BigInteger(parts[1].trim(), 16);
            } else {
                byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
                java.security.interfaces.RSAPublicKey publicKey = (java.security.interfaces.RSAPublicKey) java.security.KeyFactory.getInstance("RSA").generatePublic(spec);
                e = publicKey.getPublicExponent();
                n = publicKey.getModulus();
            }
            java.math.BigInteger hashInt = new java.math.BigInteger(orderHash.trim(), 16);
            java.math.BigInteger sigInt = new java.math.BigInteger(signatureHex.trim(), 16);
            java.math.BigInteger recovered = sigInt.modPow(e, n);
            return recovered.equals(hashInt);
        } catch (Exception ex) {
            return false;
        }
    }
}
