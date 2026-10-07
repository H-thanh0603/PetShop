package com.petshop.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.petshop.repository.OrderRepository;
import com.petshop.repository.PaymentTransactionRepository;
import com.petshop.model.Order;
import com.petshop.util.Json;
import com.petshop.util.VnpayConfig;
import com.petshop.util.VnpayUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Replaces VnpayIpnServlet (/api/payment/vnpay-ipn, GET+POST) 1:1 — same
 * HMAC verify, same TmnCode/amount checks, same VNPAY IPN RspCode contract
 * (00/01/02/04/97/99), idempotent finalize.
 */
@Controller
public class VnpayIpnController {

    private static final Logger logger = LoggerFactory.getLogger(VnpayIpnController.class);

    private final OrderRepository orderDAO;
    private final PaymentTransactionRepository paymentTransactionDAO;

    @Autowired
    public VnpayIpnController(OrderRepository orderDAO, PaymentTransactionRepository paymentTransactionDAO) {
        this.orderDAO = orderDAO;
        this.paymentTransactionDAO = paymentTransactionDAO;
    }

    @GetMapping(value = "/api/payment/vnpay-ipn", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String vnpayIpnGet(HttpServletRequest request) throws IOException {
        return handle(request);
    }

    @PostMapping(value = "/api/payment/vnpay-ipn", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String vnpayIpnPost(HttpServletRequest request) throws IOException {
        return handle(request);
    }

    private String handle(HttpServletRequest request) throws IOException {
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
                return write(rsp, "97", "Invalid Checksum");
            }

            if (VnpayConfig.getTmnCode() != null && !VnpayConfig.getTmnCode().isBlank()
                    && !VnpayConfig.getTmnCode().trim().equals(tmnCode)) {
                logger.warn("VNPAY IPN rejected: TmnCode mismatch, got={}", tmnCode);
                return write(rsp, "99", "Unknown Error");
            }

            if (orderIdRaw == null || orderIdRaw.isBlank()) {
                return write(rsp, "01", "Order Not Found");
            }

            int orderId = Integer.parseInt(orderIdRaw);
            Order order = orderDAO.getOrderById(orderId);
            if (order == null) {
                return write(rsp, "01", "Order Not Found");
            }

            if (amount == null || amount.setScale(2, RoundingMode.HALF_UP)
                    .compareTo(order.getTotalAmount().setScale(2, RoundingMode.HALF_UP)) != 0) {
                logger.warn("VNPAY IPN amount mismatch for order {}: vnpayAmount={} orderTotal={}",
                        orderId, amount, order.getTotalAmount());
                return write(rsp, "04", "Amount Invalid");
            }

            if (!"00".equals(responseCode) || !"00".equals(transactionStatus)) {
                // Payment not completed (customer cancelled, failed...) — record it.
                paymentTransactionDAO.updateLatestProviderResultForOrder(
                        orderId, "VNPAY", providerTransactionId, amount,
                        buildProviderMetadata(request),
                        "FAILED", "FAILED", "VNPAY payment was not completed (IPN).");
                return write(rsp, "00", "Confirm Success");
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
                return write(rsp, "02", "Order Already Confirmed");
            }
            if (!orderDAO.markOnlinePaymentPaidAndFinalize(orderId, "VNPAY")) {
                return write(rsp, "99", "Unknown Error");
            }

            logger.info("VNPAY IPN: order {} confirmed paid (transaction {})",
                    orderId, providerTransactionId);
            return write(rsp, "00", "Confirm Success");
        } catch (NumberFormatException e) {
            return write(rsp, "01", "Order Not Found");
        } catch (Exception e) {
            logger.error("VNPAY IPN processing failed", e);
            return write(rsp, "99", "Unknown Error");
        }
    }

    private String write(Map<String, String> rsp, String code, String message) {
        rsp.put("RspCode", code);
        rsp.put("Message", message);
        return Json.MAPPER.writeValueAsString(rsp);
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
