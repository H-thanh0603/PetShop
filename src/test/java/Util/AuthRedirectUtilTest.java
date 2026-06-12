package Util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthRedirectUtilTest {

    @Test
    @DisplayName("store and consume an internal product redirect once")
    void storeAndConsumeInternalRedirectOnce() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getParameter("redirect")).thenReturn("/petshop/product-detail?id=12");
        when(request.getContextPath()).thenReturn("/petshop");
        when(request.getSession()).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("redirectAfterLogin")).thenReturn("/petshop/product-detail?id=12");

        AuthRedirectUtil.storeRedirectAfterLogin(request);
        String redirectUrl = AuthRedirectUtil.consumeRedirectAfterLogin(request);

        verify(session).setAttribute("redirectAfterLogin", "/petshop/product-detail?id=12");
        verify(session).removeAttribute("redirectAfterLogin");
        assertEquals("/petshop/product-detail?id=12", redirectUrl);
    }

    @Test
    @DisplayName("reject external and auth-page redirects")
    void rejectUnsafeRedirects() {
        HttpServletRequest externalRequest = mock(HttpServletRequest.class);
        HttpSession externalSession = mock(HttpSession.class);
        when(externalRequest.getContextPath()).thenReturn("/petshop");
        when(externalRequest.getSession(false)).thenReturn(externalSession);
        when(externalSession.getAttribute("redirectAfterLogin")).thenReturn("https://example.com/shop");

        HttpServletRequest loginRequest = mock(HttpServletRequest.class);
        HttpSession loginSession = mock(HttpSession.class);
        when(loginRequest.getContextPath()).thenReturn("/petshop");
        when(loginRequest.getSession(false)).thenReturn(loginSession);
        when(loginSession.getAttribute("redirectAfterLogin")).thenReturn("/petshop/login");

        assertNull(AuthRedirectUtil.consumeRedirectAfterLogin(externalRequest));
        assertNull(AuthRedirectUtil.consumeRedirectAfterLogin(loginRequest));
    }
}
