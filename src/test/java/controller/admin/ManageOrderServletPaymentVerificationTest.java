package controller.admin;

import DAO.AdminActionLogDAO;
import DAO.OrderDAO;
import Model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ManageOrderServletPaymentVerificationTest {

    @Test
    @DisplayName("updatePaymentVerification should persist review result and redirect back to detail page")
    void updatePaymentVerificationShouldRedirectToDetailPage() throws Exception {
        OrderDAO orderDAO = mock(OrderDAO.class);
        AdminActionLogDAO actionLogDAO = mock(AdminActionLogDAO.class);
        when(orderDAO.updatePaymentVerification(456, "VERIFIED", "Đã khớp sao kê"))
                .thenReturn(true);

        ManageOrderServlet servlet = new ManageOrderServlet();
        injectField(servlet, "orderDAO", orderDAO);
        injectField(servlet, "actionLog", actionLogDAO);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        User admin = new User();
        admin.setId(9);

        when(request.getParameter("action")).thenReturn("updatePaymentVerification");
        when(request.getParameter("orderId")).thenReturn("456");
        when(request.getParameter("verificationStatus")).thenReturn("VERIFIED");
        when(request.getParameter("verificationMessage")).thenReturn("Đã khớp sao kê");
        when(request.getParameter("returnTo")).thenReturn("detail");
        when(request.getContextPath()).thenReturn("/PetShop");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(admin);

        servlet.doPost(request, response);

        verify(orderDAO).updatePaymentVerification(456, "VERIFIED", "Đã khớp sao kê");
        verify(actionLogDAO).log(eq(9), eq("UPDATE_PAYMENT_VERIFICATION"), eq("order"), eq(456), contains("VERIFIED"));
        verify(session).setAttribute("messageType", "success");
        verify(response).sendRedirect("/PetShop/admin/orders?action=view&id=456");
    }

    @Test
    @DisplayName("updatePaymentVerification should reject invalid order ids")
    void updatePaymentVerificationShouldRejectInvalidOrderId() throws Exception {
        ManageOrderServlet servlet = new ManageOrderServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getParameter("action")).thenReturn("updatePaymentVerification");
        when(request.getParameter("orderId")).thenReturn("abc");
        when(request.getContextPath()).thenReturn("/PetShop");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("messageType", "error");
        verify(response).sendRedirect("/PetShop/admin/orders");
    }

    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
