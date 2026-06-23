package controller.shop;

import Context.DBContext;
import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import Model.Order;
import Model.PaymentTransaction;
import Util.VnpayUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@WebServlet("/vnpay-return")
public class VnpayReturnServlet extends HttpServlet {
    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        boolean validSignature = VnpayUtil.verifyReturn(request);

        String orderIdRaw = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionStatus = request.getParameter("vnp_TransactionStatus");
        String providerTransactionId = request.getParameter("vnp_TransactionNo");
        BigDecimal amount = parseVnpayAmount(request.getParameter("vnp_Amount"));

        if (!validSignature || orderIdRaw == null) {
            request.setAttribute("paymentStatus", "failed");
            request.setAttribute("paymentMessage", "Giao dich khong hop le.");
            request.getRequestDispatcher("/pages/shop/payment-failed.jsp").forward(request, response);
            return;
        }

        int orderId = Integer.parseInt(orderIdRaw);
        Order order = orderDAO.getOrderById(orderId);
        if (order == null || amount == null || amount.setScale(2, RoundingMode.HALF_UP).compareTo(order.getTotalAmount().setScale(2, RoundingMode.HALF_UP)) != 0) {
            paymentTransactionDAO.updateLatestProviderResultForOrder(
                    orderId,
                    "VNPAY",
                    providerTransactionId,
                    amount,
                    buildProviderMetadata(request),
                    "FAILED",
                    "MISMATCH",
                    "VNPAY amount does not match order total."
            );
            request.setAttribute("orderId", orderId);
            request.setAttribute("paymentStatus", 0);
            request.setAttribute("paymentMessage", "So tien thanh toan VNPay khong khop don hang.");
            request.getRequestDispatcher("/pages/shop/payment-failed.jsp").forward(request, response);
            return;
        }

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            boolean recorded = paymentTransactionDAO.updateLatestProviderResultForOrder(
                    orderId,
                    "VNPAY",
                    providerTransactionId,
                    amount,
                    buildProviderMetadata(request),
                    "VERIFIED",
                    "VERIFIED",
                    "VNPAY payment verified."
            );
            if (!recorded) {
                try (Connection conn = DBContext.getConnection()) {
                    PaymentTransaction newTx = new PaymentTransaction();
                    newTx.setOrderId(orderId);
                    newTx.setProviderKey("VNPAY");
                    newTx.setAmount(amount);
                    newTx.setAmountReceived(amount);
                    newTx.setCurrency("VND");
                    newTx.setProviderTransactionId(providerTransactionId);
                    newTx.setProviderMetadata(buildProviderMetadata(request));
                    newTx.setStatus("VERIFIED");
                    newTx.setVerificationStatus("VERIFIED");
                    newTx.setVerificationMessage("VNPAY payment verified on return.");
                    newTx.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    newTx.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    paymentTransactionDAO.save(conn, newTx);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (!orderDAO.markOnlinePaymentPaidAndFinalize(orderId, "VNPAY")) {
                request.setAttribute("paymentStatus", "failed");
                request.setAttribute("paymentMessage", "Khong cap nhat duoc trang thai don hang VNPay.");
                request.getRequestDispatcher("/pages/shop/payment-failed.jsp").forward(request, response);
                return;
            }

            HttpSession session = request.getSession();
            session.setAttribute("paymentMethod", "VNPAY");
            session.setAttribute("paymentStatus", 1);
            session.setAttribute("successOrderId", orderId);

            response.sendRedirect(request.getContextPath() + "/order-success");
        } else {
            paymentTransactionDAO.updateLatestProviderResultForOrder(
                    orderId,
                    "VNPAY",
                    providerTransactionId,
                    amount,
                    buildProviderMetadata(request),
                    "FAILED",
                    "FAILED",
                    "VNPAY payment was not completed."
            );
            orderDAO.markOnlinePaymentAwaiting(orderId, "VNPAY");

            request.setAttribute("orderId", orderId);
            request.setAttribute("paymentStatus", 0);
            request.setAttribute("paymentMessage", "Don hang dang cho thanh toan VNPay hoac ban da huy giao dich.");
            request.getRequestDispatcher("/pages/shop/payment-failed.jsp").forward(request, response);
        }
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
                + ";payDate=" + safe(request.getParameter("vnp_PayDate"));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
