package controller.shop;

import DAO.CartDAO;
import Model.Product;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import services.InventoryService;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AddToCartServletToastMessageTest {

    @Test
    @DisplayName("add-to-cart success message should not include raw bold tags")
    void addToCartSuccessMessage_shouldNotIncludeRawBoldTags() throws Exception {
        InventoryService mockInventoryService = mock(InventoryService.class);
        CartDAO mockCartDao = mock(CartDAO.class);

        Product product = new Product();
        product.setId(7);
        product.setName("Bát Ăn Inox Cho Mèo Đôi");
        product.setPrice(new BigDecimal("99000"));
        product.setStock(10);

        when(mockInventoryService.validateAddToCart(anyMap(), eq(7), eq(1)))
                .thenReturn(InventoryService.StockValidationResult.valid(product, 1));

        AddToCartServlet servlet = new AddToCartServlet();
        injectField(servlet, "inventoryService", mockInventoryService);
        injectField(servlet, "cartDAO", mockCartDao);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(request.getParameter("id")).thenReturn("7");
        when(request.getParameter("quantity")).thenReturn("1");
        when(request.getParameter("actionType")).thenReturn("add");
        when(request.getHeader("referer")).thenReturn("/product?id=7");
        when(request.getContextPath()).thenReturn("");

        servlet.doPost(request, response);

        verify(session).setAttribute("toastMessage", "Đã thêm Bát Ăn Inox Cho Mèo Đôi vào giỏ hàng!");
        verify(session).setAttribute("toastType", "success");
        verify(response).sendRedirect("/product?id=7");
    }

    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
