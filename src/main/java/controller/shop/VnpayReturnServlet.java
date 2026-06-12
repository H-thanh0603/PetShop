package controller.shop;

import DAO.OrderDAO;
import Util.VnpayUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/vnpay-return")
public class VnpayReturnServlet extends HttpServlet {
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        boolean validSignature = VnpayUtil.verifyReturn(request);

        String orderIdRaw = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionStatus = request.getParameter("vnp_TransactionStatus");

        if (!validSignature || orderIdRaw == null) {
            request.setAttribute("paymentStatus", "failed");
            request.setAttribute("paymentMessage", "Giao dịch không hợp lệ.");
            request.getRequestDispatcher("/pages/shop/payment-result.jsp").forward(request, response);
            return;
        }

        int orderId = Integer.parseInt(orderIdRaw);

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            orderDAO.updateOrderPaymentStatus(orderId, "PAID");
            request.setAttribute("paymentStatus", "success");
            request.setAttribute("paymentMessage", "Thanh toán VNPay thành công.");
            response.sendRedirect(request.getContextPath() + "/order-success");
        } else {
            orderDAO.updateOrderPaymentStatus(orderId, "FAILED");
            request.setAttribute("orderId", orderId);
            request.setAttribute("paymentStatus", "failed");
            request.setAttribute("paymentMessage", "Thanh toán VNPay thất bại hoặc đã hủy.");
            request.getRequestDispatcher("/pages/shop/payment-failed.jsp").forward(request, response);
            return;
        }
    }
}