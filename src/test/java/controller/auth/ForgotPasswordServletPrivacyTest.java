package controller.auth;

import DAO.UserDAO;
import Model.User;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ForgotPasswordServletPrivacyTest {

    @Test
    void unknownEmailShowsGenericSuccessWithoutLeakingAccountExistence() throws Exception {
        ForgotPasswordServlet servlet = new ForgotPasswordServlet();
        UserDAO userDAO = mock(UserDAO.class);
        inject(servlet, "userDAO", userDAO);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getServletPath()).thenReturn("/forgot-password");
        when(request.getParameter("email")).thenReturn("missing@example.com");
        when(request.getRequestDispatcher("/pages/auth/forgot-password.jsp")).thenReturn(dispatcher);
        when(userDAO.getUserByEmail("missing@example.com")).thenReturn(null);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("success"), contains("Nếu email tồn tại trong hệ thống"));
        verify(request, never()).setAttribute(eq("error"), contains("Email không tồn tại"));
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void knownEmailStillRedirectsToOtpFlow() throws Exception {
        ForgotPasswordServlet servlet = new ForgotPasswordServlet();
        UserDAO userDAO = mock(UserDAO.class);
        inject(servlet, "userDAO", userDAO);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getServletPath()).thenReturn("/forgot-password");
        when(request.getParameter("email")).thenReturn("known@example.com");
        when(request.getContextPath()).thenReturn("/PetShop");
        when(request.getSession()).thenReturn(session);
        when(userDAO.getUserByEmail("known@example.com")).thenReturn(new User());

        try (var otpUtil = mockStatic(Util.OTPUtil.class)) {
            otpUtil.when(() -> Util.OTPUtil.generateAndSendOTP("known@example.com")).thenReturn(true);

            servlet.doPost(request, response);

            verify(session).setAttribute("resetEmail", "known@example.com");
            verify(session).setAttribute("otpVerified", false);
            verify(response).sendRedirect("/PetShop/verify-otp");
        }
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
