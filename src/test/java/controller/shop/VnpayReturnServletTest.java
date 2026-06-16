package controller.shop;

import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import Model.Order;
import Util.VnpayUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VnpayReturnServletTest {

    @Test
    void successfulReturnWithMismatchedAmountDoesNotMarkOrderPaid() throws Exception {
        VnpayReturnServlet servlet = new VnpayReturnServlet();
        OrderDAO orderDAO = mock(OrderDAO.class);
        PaymentTransactionDAO paymentTransactionDAO = mock(PaymentTransactionDAO.class);
        injectField(servlet, "orderDAO", orderDAO);
        injectField(servlet, "paymentTransactionDAO", paymentTransactionDAO);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        Order order = new Order();
        order.setId(456);
        order.setTotalAmount(new BigDecimal("258000"));

        when(request.getParameter("vnp_TxnRef")).thenReturn("456");
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");
        when(request.getParameter("vnp_TransactionStatus")).thenReturn("00");
        when(request.getParameter("vnp_Amount")).thenReturn("10000");
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher("/pages/shop/payment-failed.jsp")).thenReturn(dispatcher);
        when(orderDAO.getOrderById(456)).thenReturn(order);

        try (MockedStatic<VnpayUtil> mockedVnpay = mockStatic(VnpayUtil.class)) {
            mockedVnpay.when(() -> VnpayUtil.verifyReturn(request)).thenReturn(true);

            servlet.doGet(request, response);
        }

        verify(orderDAO, never()).markOnlinePaymentPaid(456, "VNPAY");
        verify(orderDAO, never()).markOnlinePaymentPaidAndFinalize(456, "VNPAY");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void successfulReturnMarksOrderPaidAndFinalizesReservedStock() throws Exception {
        VnpayReturnServlet servlet = new VnpayReturnServlet();
        OrderDAO orderDAO = mock(OrderDAO.class);
        PaymentTransactionDAO paymentTransactionDAO = mock(PaymentTransactionDAO.class);
        injectField(servlet, "orderDAO", orderDAO);
        injectField(servlet, "paymentTransactionDAO", paymentTransactionDAO);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Order order = new Order();
        order.setId(456);
        order.setTotalAmount(new BigDecimal("258000"));

        when(request.getParameter("vnp_TxnRef")).thenReturn("456");
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");
        when(request.getParameter("vnp_TransactionStatus")).thenReturn("00");
        when(request.getParameter("vnp_TransactionNo")).thenReturn("VNPAY_TXN_789");
        when(request.getParameter("vnp_Amount")).thenReturn("25800000");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");
        when(orderDAO.getOrderById(456)).thenReturn(order);
        when(paymentTransactionDAO.updateLatestProviderResultForOrder(
                456,
                "VNPAY",
                "VNPAY_TXN_789",
                new BigDecimal("258000"),
                "responseCode=00;transactionStatus=00;bankCode=;payDate=",
                "VERIFIED",
                "VERIFIED",
                "VNPAY payment verified."
        )).thenReturn(true);
        when(orderDAO.markOnlinePaymentPaidAndFinalize(456, "VNPAY")).thenReturn(true);

        try (MockedStatic<VnpayUtil> mockedVnpay = mockStatic(VnpayUtil.class)) {
            mockedVnpay.when(() -> VnpayUtil.verifyReturn(request)).thenReturn(true);

            servlet.doGet(request, response);
        }

        verify(orderDAO).markOnlinePaymentPaidAndFinalize(456, "VNPAY");
        verify(orderDAO, never()).markOnlinePaymentPaid(456, "VNPAY");
        verify(response).sendRedirect("/order-success");
    }

    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
