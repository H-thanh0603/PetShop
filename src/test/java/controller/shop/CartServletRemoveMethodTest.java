package controller.shop;

import DAO.CartDAO;
import Model.CartItem;
import Model.Product;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CartServletRemoveMethodTest {

    @Test
    @DisplayName("GET remove should redirect without mutating cart")
    void doGetRemoveShouldOnlyRedirect() throws Exception {
        CartServlet servlet = new CartServlet();
        CartDAO cartDAO = mock(CartDAO.class);
        injectField(servlet, "cartDAO", cartDAO);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getParameter("action")).thenReturn("remove");
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/cart");
        verifyNoInteractions(cartDAO);
        verify(session, never()).setAttribute(eq("toastMessage"), any());
    }

    @Test
    @DisplayName("POST remove should delete product from cart and redirect")
    void doPostRemoveShouldMutateCart() throws Exception {
        CartServlet servlet = new CartServlet();
        CartDAO cartDAO = mock(CartDAO.class);
        injectField(servlet, "cartDAO", cartDAO);

        Product product = new Product();
        product.setId(8);
        product.setName("Hat");
        product.setPrice(new BigDecimal("10000"));

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(8, new CartItem(product, 2));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getParameter("action")).thenReturn("remove");
        when(request.getParameter("id")).thenReturn("8");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");
        when(session.getAttribute("cart")).thenReturn(cart);

        servlet.doPost(request, response);

        verify(session).setAttribute("cart", cart);
        verify(session).setAttribute("toastType", "success");
        verify(response).sendRedirect("/cart");
    }

    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
