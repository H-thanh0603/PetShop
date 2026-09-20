package com.petshop.web;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import DAO.CertificateDAO;
import DAO.OrderDAO;
import DAO.OrderSignDAO;
import DAO.OrderSignatureDAO;
import Model.Certificate;
import Model.OrderSign;
import Model.OrderSignature;
import Model.User;
import Util.CertificateGenerator;
import Util.DigitalSigner;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces UploadSignatureServlet (/user/upload-signature) and
 * DownloadPrivateKeyServlet (/user/download-private-key) 1:1.
 */
@Controller
public class SignatureController {

    private static final Logger logger = LoggerFactory.getLogger(SignatureController.class);

    private final OrderSignDAO orderSignDAO;
    private final OrderSignatureDAO orderSignatureDAO;
    private final CertificateDAO certificateDAO;
    private final OrderDAO orderDAO;
    private final Gson gson = new Gson();

    public SignatureController() {
        this(new OrderSignDAO(), new OrderSignatureDAO(), new CertificateDAO(), new OrderDAO());
    }

    SignatureController(OrderSignDAO orderSignDAO, OrderSignatureDAO orderSignatureDAO,
                        CertificateDAO certificateDAO, OrderDAO orderDAO) {
        this.orderSignDAO = orderSignDAO;
        this.orderSignatureDAO = orderSignatureDAO;
        this.certificateDAO = certificateDAO;
        this.orderDAO = orderDAO;
    }

    @PostMapping(value = "/user/upload-signature", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String uploadSignature(
            @RequestParam(value = "orderId", required = false) String orderIdRaw,
            @RequestParam(value = "signature", required = false) String signatureBase64,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Object userObj = session.getAttribute("user");
            if (userObj == null) {
                result.put("success", false);
                result.put("message", "Vui lòng đăng nhập.");
                return gson.toJson(result);
            }
            User user = (User) userObj;

            if (orderIdRaw == null || orderIdRaw.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Thiếu orderId.");
                return gson.toJson(result);
            }

            if (signatureBase64 == null || signatureBase64.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Thiếu chữ ký điện tử.");
                return gson.toJson(result);
            }

            int orderId = Integer.parseInt(orderIdRaw);

            OrderSign orderSign = orderSignDAO.findByOrderId(orderId);
            if (orderSign == null) {
                result.put("success", false);
                result.put("message", "Không tìm thấy dữ liệu ký của đơn hàng.");
                return gson.toJson(result);
            }

            Certificate certificate = certificateDAO.findByOrderId(orderId);
            if (certificate == null) {
                result.put("success", false);
                result.put("message", "Không tìm thấy chứng thư số.");
                return gson.toJson(result);
            }

            OrderSignature existing = orderSignatureDAO.findByOrderId(orderId);
            if (existing == null) {
                orderSignatureDAO.save(orderId, user.getId(), signatureBase64);
            }

            boolean signatureValid = false;
            boolean certificateValid = false;
            String sigError = "";
            String certError = "";

            try {
                signatureValid = DigitalSigner.verifySignature(
                        orderSign.getOrderHash(),
                        signatureBase64,
                        orderSign.getPublicKey()
                );
            } catch (Exception e) {
                sigError = e.getMessage();
            }

            try {
                certificateValid = verifyCertificate(
                        certificate.getCertificateData(),
                        orderSign.getPublicKey()
                );
            } catch (Exception e) {
                certError = e.getMessage();
            }

            if (signatureValid && certificateValid) {
                orderSignatureDAO.updateVerifyStatus(
                        orderId,
                        OrderSignature.VerifyStatus.verified,
                        "Xác thực thành công"
                );
                orderDAO.markOrderAsPaid(orderId);
                orderDAO.updateOrderStatus(orderId, "Paid");

                result.put("success", true);
                result.put("message", "Xác thực chữ ký điện tử thành công.");
            } else {
                String failMsg = "";
                if (!signatureValid) {
                    failMsg = "Chữ ký không hợp lệ";
                    if (sigError != null && !sigError.isEmpty()) {
                        failMsg += " (" + sigError + ")";
                    }
                }
                if (!certificateValid) {
                    if (!failMsg.isEmpty()) failMsg += " — ";
                    failMsg += "Chứng thư số không hợp lệ";
                    if (certError != null && !certError.isEmpty()) {
                        failMsg += " (" + certError + ")";
                    }
                }

                orderSignatureDAO.updateVerifyStatus(
                        orderId,
                        OrderSignature.VerifyStatus.failed,
                        failMsg
                );
                orderDAO.updateOrderStatus(orderId, "Verification Failed");

                result.put("success", false);
                result.put("message", failMsg);
            }
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return gson.toJson(result);
    }

    @GetMapping("/user/download-private-key")
    public ResponseEntity<byte[]> downloadPrivateKey(
            @RequestParam(value = "orderId", required = false) String orderIdParam,
            HttpSession session) {
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) userObj;

        if (orderIdParam == null || orderIdParam.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        int orderId;
        try {
            orderId = Integer.parseInt(orderIdParam);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        OrderSign orderSign = orderSignDAO.findByOrderId(orderId);
        if (orderSign == null) {
            return ResponseEntity.notFound().build();
        }

        if (orderSign.getUserId() != user.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String privateKeyBase64 = orderSign.getPrivateKey();
        if (privateKeyBase64 == null || privateKeyBase64.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"private_key_" + orderId + ".der\"")
                    .contentLength(keyBytes.length)
                    .body(keyBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean verifyCertificate(String certificatePem, String publicKeyBase64) {
        try {
            X509Certificate cert = CertificateGenerator.decodeCertificate(certificatePem);
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            PublicKey publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            return CertificateGenerator.verifyCertificate(cert, publicKey);
        } catch (Exception e) {
            return false;
        }
    }
}
