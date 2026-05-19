package controller.shop;

import Context.DBContext;
import DAO.AddressDao;
import DAO.CartDAO;
import DAO.CouponDao;
import DAO.InventoryBatchDAO;
import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import DAO.ProductDAO;
import DAO.UserDAO;
import Model.Address;
import Model.CartItem;
import Model.Order;
import Model.Product;
import Model.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import services.InventoryService;
import services.OrderEmailService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CheckoutServletPaymentMethodFallbackTest {

    @Test
    @DisplayName("checkout should accept the plain payment parameter used by the page")
    void checkoutShouldAcceptFallbackPaymentParameter() throws Exception {
        CartDAO mockCartDAO = mock(CartDAO.class);
        UserDAO mockUserDAO = mock(UserDAO.class);
        AddressDao mockAddressDAO = mock(AddressDao.class);
        ProductDAO mockProductDAO = mock(ProductDAO.class);
        OrderDAO mockOrderDAO = mock(OrderDAO.class);
        PaymentTransactionDAO mockPaymentTransactionDAO = mock(PaymentTransactionDAO.class);
        CouponDao mockCouponDAO = mock(CouponDao.class);
        InventoryService mockInventoryService = mock(InventoryService.class);
        OrderEmailService mockOrderEmailService = mock(OrderEmailService.class);
        InventoryBatchDAO mockInventoryBatchDAO = mock(InventoryBatchDAO.class);

        User testUser = new User();
        testUser.setId(1);
        testUser.setFullname("Nguyen Van A");
        testUser.setPhone("0901234567");
        testUser.setEmail("");

        Address defaultAddress = new Address();
        defaultAddress.setUserId(1);
        defaultAddress.setAddress("123 Nguyen Hue");
        defaultAddress.setWard("Phuong Ben Nghe");
        defaultAddress.setDistrict("Quan 1");
        defaultAddress.setProvince("Ho Chi Minh");

        Product cartProduct = new Product();
        cartProduct.setId(10);
        cartProduct.setName("Cat Tree");
        cartProduct.setPrice(new BigDecimal("600000"));
        cartProduct.setWeight(500);
        cartProduct.setStock(5);

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(10, new CartItem(cartProduct, 1));

        when(mockUserDAO.getUserById(1)).thenReturn(testUser);
        when(mockCartDAO.getCartByUserId(1)).thenReturn(cart);
        when(mockAddressDAO.getDefaultAddressByUserId(1)).thenReturn(defaultAddress);
        when(mockInventoryService.refreshCartProductsWithNotification(any())).thenReturn(new ArrayList<>());

        Product lockedProduct = new Product();
        lockedProduct.setId(10);
        lockedProduct.setName("Cat Tree");
        lockedProduct.setPrice(new BigDecimal("600000"));
        lockedProduct.setWeight(500);
        lockedProduct.setStock(5);

        Connection mockConn = mock(Connection.class);
        when(mockProductDAO.getProductByIdForUpdate(eq(mockConn), eq(10))).thenReturn(lockedProduct);
        when(mockProductDAO.decreaseStock(eq(mockConn), eq(10), eq(1))).thenReturn(true);
        when(mockOrderDAO.saveOrder(eq(mockConn), any(Order.class))).thenReturn(123);
        when(mockOrderDAO.saveOrderItem(eq(mockConn), any())).thenReturn(true);
        when(mockPaymentTransactionDAO.save(eq(mockConn), any())).thenReturn(321);
        when(mockInventoryBatchDAO.hasTrackedBatchesForProduct(any(), eq(10))).thenReturn(false);

        CheckoutServlet servlet = new CheckoutServlet();
        injectField(servlet, "cartDAO", mockCartDAO);
        injectField(servlet, "userDAO", mockUserDAO);
        injectField(servlet, "addressDAO", mockAddressDAO);
        injectField(servlet, "productDAO", mockProductDAO);
        injectField(servlet, "orderDAO", mockOrderDAO);
        injectField(servlet, "paymentTransactionDAO", mockPaymentTransactionDAO);
        injectField(servlet, "couponDao", mockCouponDAO);
        injectField(servlet, "inventoryService", mockInventoryService);
        injectField(servlet, "orderEmailService", mockOrderEmailService);
        injectField(servlet, "inventoryBatchDAO", mockInventoryBatchDAO);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();

        when(request.getSession()).thenReturn(session);
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("paymentMethod")).thenReturn(null);
        when(request.getParameter("payment")).thenReturn("cod");
        when(request.getParameter("note")).thenReturn("");
        when(request.getContextPath()).thenReturn("");
        when(session.getAttribute("user")).thenReturn(testUser);
        when(session.getAttribute("appliedCoupon")).thenReturn(null);
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(mockConn);

            servlet.doPost(request, response);
        }

        String responseJson = body.toString();
        assertFalse(responseJson.isEmpty(), "checkout should always return JSON");

        JsonObject json = JsonParser.parseString(responseJson).getAsJsonObject();
        assertTrue(json.get("success").getAsBoolean(), "fallback payment parameter should still place the order");
        assertEquals("Đặt hàng thành công!", json.get("message").getAsString());
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
