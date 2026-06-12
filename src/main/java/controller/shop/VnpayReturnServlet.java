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
            request.getRequestDispatcher("/pages/shop/payment-failed.jsp").forward(request, response);
            return;
        }

        int orderId = Integer.parseInt(orderIdRaw);

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            orderDAO.markOnlinePaymentPaid(orderId, "VNPAY");

            HttpSession session = request.getSession();
            session.setAttribute("paymentMethod", "VNPAY");
            session.setAttribute("paymentStatus", 1);
            session.setAttribute("successOrderId", orderId);

            response.sendRedirect(request.getContextPath() + "/order-success");
        } else {
            orderDAO.markOnlinePaymentAwaiting(orderId, "VNPAY");

            request.setAttribute("orderId", orderId);
            request.setAttribute("paymentStatus", 0);
            request.setAttribute("paymentMessage", "Đơn hàng đang chờ thanh toán VNPay hoặc bạn đã hủy giao dịch.");
            request.getRequestDispatcher("/pages/shop/payment-failed.jsp").forward(request, response);
        }
    }
}