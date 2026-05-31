package controller.shop;

import DAO.PetTypeDAO;
import DAO.ProductDAO;
import DAO.WishlistDAO;
import Model.Product;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ShopServletSqlFilteringTest {

    @Test
    @DisplayName("filtered shop requests should use DAO-backed SQL filtering instead of loading all products")
    void filteredShopRequestsShouldUseDaoBackedFiltering() throws Exception {
        ProductDAO productDAO = mock(ProductDAO.class);
        PetTypeDAO petTypeDAO = mock(PetTypeDAO.class);
        WishlistDAO wishlistDAO = mock(WishlistDAO.class);
        Product product = new Product(1, "Hat cho meo", "cat.jpg", new BigDecimal("100000"), 5, "desc", "Food");

        when(productDAO.getFilteredProductsPage(any())).thenReturn(List.of(product));
        when(productDAO.countFilteredProducts(any())).thenReturn(1);

        ShopServlet servlet = new ShopServlet();
        injectField(servlet, "productDao", productDAO);
        injectField(servlet, "petTypeDao", petTypeDAO);
        injectField(servlet, "wishlistDAO", wishlistDAO);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getParameter("search")).thenReturn("m");
        when(request.getParameter("category")).thenReturn("Food");
        when(request.getParameter("sort")).thenReturn("price-desc");
        when(request.getParameter("priceRange")).thenReturn("under100");
        when(request.getParameter("discountOnly")).thenReturn("true");
        when(request.getParameter("pet")).thenReturn("cat");
        when(request.getParameter("page")).thenReturn("1");
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher("/pages/shop/shop-pet.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(productDAO).getFilteredProductsPage(any());
        verify(productDAO).countFilteredProducts(any());
        verify(productDAO, never()).getAllProducts();
        verify(dispatcher).forward(request, response);
    }

    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
