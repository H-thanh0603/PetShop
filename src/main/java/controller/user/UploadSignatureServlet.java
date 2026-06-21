package controller.user;

import DAO.CertificateDAO;
import DAO.OrderDAO;
import DAO.OrderSignDAO;
import DAO.OrderSignatureDAO;
import Model.Certificate;
import Model.OrderSign;
import Model.OrderSignature;
import Util.CertificateGenerator;
import Util.DigitalSigner;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/user/upload-signature")
public class UploadSignatureServlet extends HttpServlet {

    private static final Logger log =
            LoggerFactory.getLogger(UploadSignatureServlet.class);

    private final OrderSignDAO orderSignDAO = new OrderSignDAO();
    private final CertificateDAO certificateDAO = new CertificateDAO();
    private final OrderSignatureDAO orderSignatureDAO = new OrderSignatureDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        Map<String, Object> result = new HashMap<>();

        try {

            String orderIdRaw = request.getParameter("orderId");
            String signature = request.getParameter("signature");

            if (orderIdRaw == null || orderIdRaw.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Thiếu orderId");
                writeJson(response, result);
                return;
            }

            if (signature == null || signature.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Chưa cung cấp chữ ký số");
                writeJson(response, result);
                return;
            }

            int orderId = Integer.parseInt(orderIdRaw);

            // 1. Lấy order sign
            OrderSign orderSign =
                    orderSignDAO.findByOrderId(orderId);

            if (orderSign == null) {
                result.put("success", false);
                result.put("message", "Không tìm thấy dữ liệu ký đơn hàng");
                writeJson(response, result);
                return;
            }

            // 2. Lấy certificate
            Certificate certificate =
                    certificateDAO.findByOrderId(orderId);

            if (certificate == null) {
                result.put("success", false);
                result.put("message", "Không tìm thấy chứng thư số");
                writeJson(response, result);
                return;
            }

            // 3. Verify Signature
            boolean signatureValid =
                    DigitalSigner.verifySignature(
                            orderSign.getOrderHash(),
                            signature,
                            orderSign.getPublicKey()
                    );

            // 4. Verify Certificate
            X509Certificate cert =
                    CertificateGenerator.decodeCertificate(
                            certificate.getCertificateData()
                    );

            byte[] publicKeyBytes =
                    Base64.getDecoder()
                            .decode(orderSign.getPublicKey());

            PublicKey publicKey =
                    KeyFactory.getInstance("RSA")
                            .generatePublic(
                                    new X509EncodedKeySpec(publicKeyBytes)
                            );

            boolean certificateValid =
                    CertificateGenerator.verifyCertificate(
                            cert,
                            publicKey
                    );

            // Lưu chữ ký nếu chưa tồn tại
            if (orderSignatureDAO.findByOrderId(orderId) == null) {
                orderSignatureDAO.save(
                        orderId,
                        orderSign.getUserId(),
                        signature
                );
            }

            // 5. Cập nhật kết quả
            if (signatureValid && certificateValid) {

                orderSignatureDAO.updateVerifyStatus(
                        orderId,
                        OrderSignature.VerifyStatus.verified,
                        "Xác thực thành công"
                );

                // Theo yêu cầu ISSUE
                orderDAO.updateStatus(
                        orderId,
                        "Paid"
                );

                result.put("success", true);
                result.put("message", "Xác thực thành công");

            } else {

                String reason;

                if (!signatureValid && !certificateValid) {
                    reason = "Chữ ký và chứng thư đều không hợp lệ";
                } else if (!signatureValid) {
                    reason = "Chữ ký không hợp lệ";
                } else {
                    reason = "Chứng thư số không hợp lệ";
                }

                orderSignatureDAO.updateVerifyStatus(
                        orderId,
                        OrderSignature.VerifyStatus.failed,
                        reason
                );

                // Theo đặc tả ISSUE
                orderSignatureDAO.updateVerifyStatus(
                        orderId,
                        OrderSignature.VerifyStatus.failed,
                        "Chữ ký không hợp lệ"
                );

                result.put("success", false);
                result.put("message", reason);
            }

        } catch (Exception e) {

            log.error("Upload signature error", e);

            result.put("success", false);
            result.put("message", "Lỗi xử lý xác thực chữ ký");
        }

        writeJson(response, result);
    }

    private void writeJson(HttpServletResponse response,
                           Map<String, Object> data)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                new Gson().toJson(data)
        );
    }
}
