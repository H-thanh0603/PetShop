package controller.shop;

import Context.DBContext;
import DAO.AddressDao;
import DAO.CartDAO;
import DAO.CouponDao;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CheckoutServletBankTransferPendingTest {

    @Test
    @DisplayName("bank transfer checkout should create an unpaid pending-verification flow")
    void bankTransferCheckoutShouldBePendingVerification() throws Exception {
        CartDAO mockCartDAO = mock(CartDAO.class);
        UserDAO mockUserDAO = mock(UserDAO.class);
        AddressDao mockAddressDAO = mock(AddressDao.class);
        ProductDAO mockProductDAO = mock(ProductDAO.class);
        OrderDAO mockOrderDAO = mock(OrderDAO.class);
        PaymentTransactionDAO mockPaymentTransactionDAO = mock(PaymentTransactionDAO.class);
        CouponDao mockCouponDAO = mock(CouponDao.class);
        InventoryService mockInventoryService = mock(InventoryService.class);
        OrderEmailService mockOrderEmailService = mock(OrderEmailService.class);

        User user = new User();
        user.setId(1);
        user.setFullname("Nguyen Van A");
        user.setPhone("0901234567");
        user.setEmail("");

        Address address = new Address();
        address.setUserId(1);
        address.setAddress("123 Nguyen Hue");
        address.setWard("Phuong Ben Nghe");
        address.setDistrict("Quan 1");
        address.setProvince("Ho Chi Minh");

        Product cartProduct = new Product();
        cartProduct.setId(12);
        cartProduct.setName("Hạt mèo");
        cartProduct.setPrice(new BigDecimal("600000"));
        cartProduct.setWeight(300);
        cartProduct.setStock(8);

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(12, new CartItem(cartProduct, 1));

        when(mockUserDAO.getUserById(1)).thenReturn(user);
        when(mockCartDAO.getCartByUserId(1)).thenReturn(cart);
        when(mockAddressDAO.getDefaultAddressByUserId(1)).thenReturn(address);
        when(mockInventoryService.refreshCartProductsWithNotification(any())).thenReturn(new ArrayList<>());

        Product lockedProduct = new Product();
        lockedProduct.setId(12);
        lockedProduct.setName("Hạt mèo");
        lockedProduct.setPrice(new BigDecimal("600000"));
        lockedProduct.setWeight(300);
        lockedProduct.setStock(8);

        Connection mockConn = mock(Connection.class);
        when(mockProductDAO.getProductByIdForUpdate(eq(mockConn), eq(12))).thenReturn(lockedProduct);
        when(mockProductDAO.decreaseStock(eq(mockConn), eq(12), eq(1))).thenReturn(true);
        when(mockOrderDAO.saveOrder(eq(mockConn), any(Order.class))).thenReturn(456);
        when(mockOrderDAO.saveOrderItem(eq(mockConn), any())).thenReturn(true);
        when(mockPaymentTransactionDAO.save(eq(mockConn), any())).thenReturn(999);

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

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseBody = new StringWriter();

        when(request.getSession()).thenReturn(session);
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("paymentMethod")).thenReturn("bank_transfer");
        when(request.getParameter("payment")).thenReturn("bank_transfer");
        when(request.getParameter("note")).thenReturn("");
        when(request.getContextPath()).thenReturn("");
        when(session.getAttribute("user")).thenReturn(user);
        when(session.getAttribute("appliedCoupon")).thenReturn(null);
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(mockConn);
            servlet.doPost(request, response);
        }

        String body = responseBody.toString();
        assertFalse(body.isEmpty());

        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        assertTrue(json.get("success").getAsBoolean());
        assertTrue(json.get("pendingVerification").getAsBoolean());
        assertTrue(json.has("transferReference"));
        assertFalse(json.get("transferReference").getAsString().isBlank());
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
