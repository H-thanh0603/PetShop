package controller.shop;

import DAO.AddressDao;
import DAO.CartDAO;
import DAO.CouponDao;
import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import DAO.ProductDAO;
import DAO.UserDAO;
import Model.Address;
import Model.CartItem;
import Model.Product;
import Model.User;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import services.InventoryService;
import services.OrderEmailService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutServletAddressFallbackTest {

    @Test
    @DisplayName("checkout page should show the newest saved address when no default address is marked")
    void doGetShouldExposeSavedAddressWhenDefaultMissing() throws Exception {
        CartDAO cartDAO = mock(CartDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        AddressDao addressDAO = mock(AddressDao.class);
        InventoryService inventoryService = mock(InventoryService.class);
        ProductDAO productDAO = mock(ProductDAO.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        PaymentTransactionDAO paymentTransactionDAO = mock(PaymentTransactionDAO.class);
        CouponDao couponDao = mock(CouponDao.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);

        User user = new User();
        user.setId(1);
        user.setFullname("Nguyen Van A");
        user.setPhone("0901234567");
        user.setEmail("a@example.com");

        Product product = new Product();
        product.setId(5);
        product.setName("Cat Tree");
        product.setPrice(new BigDecimal("600000"));
        product.setWeight(300);

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(5, new CartItem(product, 1));

        Address savedAddress = new Address();
        savedAddress.setId(10);
        savedAddress.setUserId(1);
        savedAddress.setAddress("123 Nguyen Hue");
        savedAddress.setWard("Ben Nghe");
        savedAddress.setDistrict("Quan 1");
        savedAddress.setProvince("Ho Chi Minh");

        when(userDAO.getUserById(1)).thenReturn(user);
        when(cartDAO.getCartByUserId(1)).thenReturn(cart);
        when(inventoryService.refreshCartProductsWithNotification(any())).thenReturn(new ArrayList<>());
        when(inventoryService.validateCartForCheckout(any())).thenReturn(new ArrayList<>());
        when(addressDAO.getAddressesByUserId(1)).thenReturn(List.of(savedAddress));
        when(addressDAO.getDefaultAddressByUserId(1)).thenReturn(null);

        CheckoutServlet servlet = new CheckoutServlet();
        injectField(servlet, "cartDAO", cartDAO);
        injectField(servlet, "userDAO", userDAO);
        injectField(servlet, "addressDAO", addressDAO);
        injectField(servlet, "inventoryService", inventoryService);
        injectField(servlet, "productDAO", productDAO);
        injectField(servlet, "orderDAO", orderDAO);
        injectField(servlet, "paymentTransactionDAO", paymentTransactionDAO);
        injectField(servlet, "couponDao", couponDao);
        injectField(servlet, "orderEmailService", orderEmailService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getRequestDispatcher("/pages/shop/checkout.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("defaultAddress", savedAddress);
        verify(request).setAttribute("selectedAddressId", savedAddress.getId());
        verify(dispatcher).forward(request, response);
    }

    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
