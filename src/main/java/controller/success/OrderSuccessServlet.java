package controller.success;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/order-success")
public class OrderSuccessServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        Integer orderId = (Integer) session.getAttribute("successOrderId");
        if (orderId == null) {
            // Không có data -> về shop
            response.sendRedirect(request.getContextPath() + "/shop");
            return;
        }

        // Chuyển session attribute sang request attribute cho JSP
        request.setAttribute("orderId", orderId);
        request.setAttribute("user", session.getAttribute("successUser"));
        request.setAttribute("totalAmount", session.getAttribute("successTotalAmount"));
        request.setAttribute("shippingFee", session.getAttribute("successShippingFee"));
        request.setAttribute("discount", session.getAttribute("successDiscount"));
        request.setAttribute("finalTotal", session.getAttribute("successFinalTotal"));
        request.setAttribute("shippingAddress", session.getAttribute("successShippingAddress"));
        request.setAttribute("orderNote", session.getAttribute("successOrderNote"));
        request.setAttribute("orderItems", session.getAttribute("successOrderItems"));
        request.setAttribute("paymentMethod", session.getAttribute("paymentMethod"));
        Object pending = session.getAttribute("pendingVerification");
        request.setAttribute("pendingVerification", pending != null && (Boolean) pending);
        request.setAttribute("transferReference", session.getAttribute("transferReference"));
        request.setAttribute("paymentExpiresAt", session.getAttribute("paymentExpiresAt"));
        request.setAttribute("bankId", session.getAttribute("bankId"));
        request.setAttribute("bankDisplayName", session.getAttribute("bankDisplayName"));
        request.setAttribute("bankAccountNumber", session.getAttribute("bankAccountNumber"));
        request.setAttribute("bankAccountName", session.getAttribute("bankAccountName"));
        request.setAttribute("paymentTtlSeconds", session.getAttribute("paymentTtlSeconds"));

        // Xóa session sau khi đã dùng (tránh hiện lại khi F5)
        session.removeAttribute("successOrderId");
        session.removeAttribute("successUser");
        session.removeAttribute("successTotalAmount");
        session.removeAttribute("successShippingFee");
        session.removeAttribute("successDiscount");
        session.removeAttribute("successFinalTotal");
        session.removeAttribute("successShippingAddress");
        session.removeAttribute("successOrderNote");
        session.removeAttribute("successOrderItems");
        session.removeAttribute("paymentMethod");
        session.removeAttribute("pendingVerification");
        session.removeAttribute("transferReference");
        session.removeAttribute("paymentExpiresAt");
        session.removeAttribute("bankId");
        session.removeAttribute("bankDisplayName");
        session.removeAttribute("bankAccountNumber");
        session.removeAttribute("bankAccountName");
        session.removeAttribute("paymentTtlSeconds");

        request.getRequestDispatcher("/pages/shop/orderSuccess.jsp")
                .forward(request, response);
    }
}
