package com.petshop.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CsrfFilterWebhookTest {

    @Test
    @DisplayName("server-to-server webhooks bypass CSRF (no token required)")
    void webhooksBypassCsrf() throws Exception {
        String[] uris = {
                "/PetShop/api/payment/bank-webhook",
                "/PetShop/api/payment/vnpay-ipn",
                "/PetShop/api/ghn/webhook"
        };
        for (String uri : uris) {
            CsrfFilter filter = new CsrfFilter();
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain chain = mock(FilterChain.class);

            when(request.getRequestURI()).thenReturn(uri);
            when(request.getMethod()).thenReturn("POST");

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(request, org.mockito.Mockito.never()).getSession(true);
        }
    }

    @Test
    @DisplayName("browser POST to admin AI without token is still blocked")
    void adminAiPostStillRequiresToken() throws Exception {
        CsrfFilter filter = new CsrfFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getRequestURI()).thenReturn("/PetShop/admin/ai-merchant/chat");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("csrfToken")).thenReturn("expected-token");
        when(request.getHeader("X-CSRF-Token")).thenReturn(null);
        when(request.getParameter("csrfToken")).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(chain, org.mockito.Mockito.never()).doFilter(request, response);
    }
}
