package controller.payment;

import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import Model.Order;
import Util.VnpayConfig;
import Util.VnpayUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * VNPAY IPN (Instant Payment Notification) endpoint.
 *
 * VNPAY calls this server-to-server (GET or POST) after the customer pays,
 * so payments are confirmed even if the customer never returns to the app's
 * return URL (closed browser, lost connection...). Protocol:
 * https://sandbox.vnpayment.vn/devdocs — responses use VNPAY's IPN codes:
 *  00 Confirm Success | 01 Order not found | 02 Order already confirmed
 *  04 Amount invalid  | 97 Invalid checksum  | 99 Unknown error
 */
public class VnpayIpnServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(VnpayIpnServlet.class);
    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Map<String, String> rsp = new HashMap<>();

        try {
            String orderIdRaw = request.getParameter("vnp_TxnRef");
            String responseCode = request.getParameter("vnp_ResponseCode");
            String transactionStatus = request.getParameter("vnp_TransactionStatus");
            String providerTransactionId = request.getParameter("vnp_TransactionNo");
            String tmnCode = request.getParameter("vnp_TmnCode");
            BigDecimal amount = parseVnpayAmount(request.getParameter("vnp_Amount"));

            if (!VnpayUtil.verifyReturn(request)) {
                logger.warn("VNPAY IPN rejected: invalid checksum, txnRef={}", orderIdRaw);
                write(response, rsp, "97", "Invalid Checksum");
                return;
            }

            if (VnpayConfig.getTmnCode() != null && !VnpayConfig.getTmnCode().isBlank()
                    && !VnpayConfig.getTmnCode().trim().equals(tmnCode)) {
                logger.warn("VNPAY IPN rejected: TmnCode mismatch, got={}", tmnCode);
                write(response, rsp, "99", "Unknown Error");
                return;
            }

            if (orderIdRaw == null || orderIdRaw.isBlank()) {
                write(response, rsp, "01", "Order Not Found");
                return;
            }

            int orderId = Integer.parseInt(orderIdRaw);
            Order order = orderDAO.getOrderById(orderId);
            if (order == null) {
                write(response, rsp, "01", "Order Not Found");
                return;
            }

            if (amount == null || amount.setScale(2, RoundingMode.HALF_UP)
                    .compareTo(order.getTotalAmount().setScale(2, RoundingMode.HALF_UP)) != 0) {
                logger.warn("VNPAY IPN amount mismatch for order {}: vnpayAmount={} orderTotal={}",
                        orderId, amount, order.getTotalAmount());
                write(response, rsp, "04", "Amount Invalid");
                return;
            }

            if (!"00".equals(responseCode) || !"00".equals(transactionStatus)) {
                // Payment not completed (customer cancelled, failed...) — record it.
                paymentTransactionDAO.updateLatestProviderResultForOrder(
                        orderId, "VNPAY", providerTransactionId, amount,
                        buildProviderMetadata(request),
                        "FAILED", "FAILED", "VNPAY payment was not completed (IPN).");
                write(response, rsp, "00", "Confirm Success");
                return;
            }

            boolean recorded = paymentTransactionDAO.updateLatestProviderResultForOrder(
                    orderId, "VNPAY", providerTransactionId, amount,
                    buildProviderMetadata(request),
                    "VERIFIED", "VERIFIED", "VNPAY payment verified via IPN.");
            if (!recorded) {
                logger.warn("VNPAY IPN: no payment transaction to update for order {}", orderId);
            }

            // markOnlinePaymentPaidAndFinalize is idempotent: it locks the order
            // row, checks payment_status and skips stock finalization when the
            // order is already paid.
            if (order.getPayment_status()) {
                write(response, rsp, "02", "Order Already Confirmed");
                return;
            }
            if (!orderDAO.markOnlinePaymentPaidAndFinalize(orderId, "VNPAY")) {
                write(response, rsp, "99", "Unknown Error");
                return;
            }

            logger.info("VNPAY IPN: order {} confirmed paid (transaction {})",
                    orderId, providerTransactionId);
            write(response, rsp, "00", "Confirm Success");
        } catch (NumberFormatException e) {
            write(response, rsp, "01", "Order Not Found");
        } catch (Exception e) {
            logger.error("VNPAY IPN processing failed", e);
            write(response, rsp, "99", "Unknown Error");
        }
    }

    private void write(HttpServletResponse response, Map<String, String> rsp,
                       String code, String message) throws IOException {
        rsp.put("RspCode", code);
        rsp.put("Message", message);
        response.getWriter().write(new Gson().toJson(rsp));
    }

    private BigDecimal parseVnpayAmount(String rawAmount) {
        if (rawAmount == null || rawAmount.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(rawAmount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildProviderMetadata(HttpServletRequest request) {
        return "responseCode=" + safe(request.getParameter("vnp_ResponseCode"))
                + ";transactionStatus=" + safe(request.getParameter("vnp_TransactionStatus"))
                + ";bankCode=" + safe(request.getParameter("vnp_BankCode"))
                + ";payDate=" + safe(request.getParameter("vnp_PayDate"))
                + ";source=ipn";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
