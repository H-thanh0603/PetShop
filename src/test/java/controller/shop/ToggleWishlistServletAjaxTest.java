package controller.shop;

import DAO.WishlistDAO;
import Model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ToggleWishlistServletAjaxTest {

    @Test
    @DisplayName("ajax wishlist toggle returns JSON without redirecting")
    void ajaxToggleShouldReturnJsonWithoutRedirecting() throws Exception {
        ToggleWishlistServlet servlet = new ToggleWishlistServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        StringWriter body = new StringWriter();

        User user = new User();
        user.setId(7);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getParameter("productId")).thenReturn("12");
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        try (MockedConstruction<WishlistDAO> mocked = mockConstruction(WishlistDAO.class,
                (dao, context) -> when(dao.toggleWishlistAndReturnState(7, 12)).thenReturn(true))) {
            servlet.doPost(request, response);
        }

        verify(response).setContentType("application/json;charset=UTF-8");
        verify(response, never()).sendRedirect(org.mockito.ArgumentMatchers.anyString());
        assertTrue(body.toString().contains("\"success\":true"));
        assertTrue(body.toString().contains("\"wishlisted\":true"));
    }

    @Test
    @DisplayName("ajax wishlist toggle asks unauthenticated users to login with JSON")
    void ajaxToggleShouldReturnLoginJsonWhenUnauthenticated() throws Exception {
        ToggleWishlistServlet servlet = new ToggleWishlistServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        StringWriter body = new StringWriter();

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getParameter("redirect")).thenReturn("/petshop/product-detail?id=12");
        when(request.getContextPath()).thenReturn("/petshop");
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(response, never()).sendRedirect(org.mockito.ArgumentMatchers.anyString());
        assertTrue(body.toString().contains("\"authenticated\":false"));
        assertTrue(body.toString().contains("/petshop/login?redirect="));
    }

    @Test
    @DisplayName("ajax wishlist toggle returns a validation error when product id is missing")
    void ajaxToggleShouldValidateMissingProductId() throws Exception {
        ToggleWishlistServlet servlet = new ToggleWishlistServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        StringWriter body = new StringWriter();

        User user = new User();
        user.setId(7);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getParameter("productId")).thenReturn("");
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response).setContentType("application/json;charset=UTF-8");
        assertTrue(body.toString().contains("\"success\":false"));
        assertTrue(body.toString().contains("Sản phẩm không hợp lệ"));
    }
}
