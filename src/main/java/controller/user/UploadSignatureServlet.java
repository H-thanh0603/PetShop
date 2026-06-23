package controller.user;

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

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/user/upload-signature")
public class UploadSignatureServlet extends HttpServlet {
private final OrderSignDAO orderSignDAO = new OrderSignDAO();
private final OrderSignatureDAO orderSignatureDAO = new OrderSignatureDAO();
private final CertificateDAO certificateDAO = new CertificateDAO();
private final OrderDAO orderDAO = new OrderDAO();

@Override
protected void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {

    Map<String, Object> result = new HashMap<>();

    try {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            result.put("success", false);
            result.put("message", "Vui lòng đăng nhập.");
            write(response, result);
            return;
        }

        User user = (User) session.getAttribute("user");

        String orderIdRaw = request.getParameter("orderId");
        String signatureBase64 = request.getParameter("signature");

        if (orderIdRaw == null || orderIdRaw.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Thiếu orderId.");
            write(response, result);
            return;
        }

        if (signatureBase64 == null || signatureBase64.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Thiếu chữ ký điện tử.");
            write(response, result);
            return;
        }

        int orderId = Integer.parseInt(orderIdRaw);

        OrderSign orderSign = orderSignDAO.findByOrderId(orderId);

        if (orderSign == null) {
            result.put("success", false);
            result.put("message", "Không tìm thấy dữ liệu ký của đơn hàng.");
            write(response, result);
            return;
        }

        Certificate certificate =
                certificateDAO.findByOrderId(orderId);

        if (certificate == null) {
            result.put("success", false);
            result.put("message", "Không tìm thấy chứng thư số.");
            write(response, result);
            return;
        }

        OrderSignature existing =
                orderSignatureDAO.findByOrderId(orderId);

        if (existing == null) {

            orderSignatureDAO.save(
                    orderId,
                    user.getId(),
                    signatureBase64
            );
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

            orderDAO.updateOrderStatus(
                    orderId,
                    "Paid"
            );

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

            orderDAO.updateOrderStatus(
                    orderId,
                    "Verification Failed"
            );

            result.put("success", false);
            result.put("message", failMsg);
        }

    } catch (Exception e) {

        e.printStackTrace();

        result.put("success", false);
        result.put("message", e.getMessage());
    }

    write(response, result);
}

private boolean verifyCertificate(String certificatePem,
                                  String publicKeyBase64) {

    try {

        X509Certificate cert =
                CertificateGenerator.decodeCertificate(
                        certificatePem
                );

        byte[] publicKeyBytes =
                Base64.getDecoder().decode(publicKeyBase64);

        PublicKey publicKey =
                KeyFactory.getInstance("RSA")
                        .generatePublic(
                                new X509EncodedKeySpec(
                                        publicKeyBytes
                                )
                        );

        return CertificateGenerator.verifyCertificate(
                cert,
                publicKey
        );

    } catch (Exception e) {
        return false;
    }
}

private void write(HttpServletResponse response,
                   Map<String, Object> data)
        throws IOException {

    response.setContentType(
            "application/json;charset=UTF-8"
    );

    response.setCharacterEncoding("UTF-8");

    response.getWriter().write(
            new Gson().toJson(data)
    );
}

}
