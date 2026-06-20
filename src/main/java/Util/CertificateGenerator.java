package Util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import javax.security.auth.x500.X500Principal;

public class CertificateGenerator {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static X509Certificate generateX509(PublicKey publicKey, PrivateKey privateKey, int orderId, int userId) throws Exception {
        if (publicKey == null)
            throw new IllegalArgumentException("publicKey không được null");
        if (privateKey == null)
            throw new IllegalArgumentException("privateKey không được null");

        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis()).add(BigInteger.valueOf(new Random().nextInt(100000)));

        Calendar notBefore = Calendar.getInstance();
        Calendar notAfter = (Calendar) notBefore.clone();
        notAfter.add(Calendar.YEAR, 1);
        Date startDate = notBefore.getTime();
        Date endDate = notAfter.getTime();

        X500Principal subject = new X500Principal("CN=order_" + orderId + ", OU=user_" + userId + ", O=PetShop, C=VN");

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject, serialNumber, startDate, endDate, subject, publicKey);

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
        X509CertificateHolder certHolder = certBuilder.build(signer);
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
    }

    public static String encodeCertificate(X509Certificate cert) throws Exception {
        if (cert == null)
            throw new IllegalArgumentException("cert không được null");
        StringWriter writer = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
            pemWriter.writeObject(cert);
        }
        return writer.toString();
    }

    public static X509Certificate decodeCertificate(String pem) throws Exception {
        if (pem == null || pem.isEmpty())
            throw new IllegalArgumentException("pem không được null hoặc rỗng");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new java.io.ByteArrayInputStream(pem.getBytes("UTF-8")));
    }

    public static boolean verifyCertificate(X509Certificate cert, PublicKey publicKey) {
        if (cert == null || publicKey == null)
            return false;
        try {
            cert.verify(publicKey);
            return true;
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            return false;
        }
    }
}
