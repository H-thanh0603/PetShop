package controller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CsrfFilterAjaxTest {

    @Test
    @DisplayName("ajax request with invalid csrf token receives JSON instead of an HTML error page")
    void invalidAjaxTokenShouldReturnJson() throws Exception {
        CsrfFilter filter = new CsrfFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        FilterChain chain = mock(FilterChain.class);
        StringWriter body = new StringWriter();

        when(request.getRequestURI()).thenReturn("/petshop/toggle-wishlist");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("csrfToken")).thenReturn("expected-token");
        when(request.getHeader("X-CSRF-Token")).thenReturn("wrong-token");
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(response, never()).sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
        verify(chain, never()).doFilter(request, response);
        assertTrue(body.toString().contains("\"success\":false"));
    }
}
