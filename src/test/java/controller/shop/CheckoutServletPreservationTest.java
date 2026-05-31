package controller.shop;

import DAO.AddressDao;
import DAO.CartDAO;
import DAO.CouponDao;
import DAO.OrderDAO;
import DAO.ProductDAO;
import DAO.UserDAO;
import Context.DBContext;
import Model.Address;
import Model.CartItem;
import Model.Product;
import Model.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import services.InventoryService;
import services.payment.PaymentRegistry;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Preservation-checking tests (Property 2): verify that all existing business logic
 * paths still work correctly after the fix.
 *
 * <p>These tests assert that the FIXED code produces the same results as the original
 * code for all inputs that do NOT trigger the bug condition (i.e., no exception escapes
 * {@code placeOrderWithStockCheck()}).</p>
 *
 * <p><b>Validates: Requirements 2.5, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8</b></p>
 */
class CheckoutServletPreservationTest {

    private CartDAO mockCartDAO;
    private UserDAO mockUserDAO;
    private AddressDao mockAddressDAO;
    private ProductDAO mockProductDAO;
    private OrderDAO mockOrderDAO;
    private CouponDao mockCouponDAO;
    private InventoryService mockInventoryService;

    private User testUser;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        mockCartDAO = mock(CartDAO.class);
        mockUserDAO = mock(UserDAO.class);
        mockAddressDAO = mock(AddressDao.class);
        mockProductDAO = mock(ProductDAO.class);
        mockOrderDAO = mock(OrderDAO.class);
        mockCouponDAO = mock(CouponDao.class);
        mockInventoryService = mock(InventoryService.class);

        // Valid test user
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setFullname("Nguyen Van A");
        testUser.setPhone("0901234567");
        testUser.setEmail("test@example.com");
        testUser.setDiscountUsed(false);

        // Valid default address
        testAddress = new Address();
        testAddress.setId(1);
        testAddress.setUserId(1);
        testAddress.setDefaultt(true);
        testAddress.setAddress("123 Nguyen Hue");
        testAddress.setProvince("Ho Chi Minh");
        testAddress.setDistrict("Quan 1");
        testAddress.setWard("Phuong Ben Nghe");

        // Default stubs
        when(mockUserDAO.getUserById(1)).thenReturn(testUser);
        when(mockAddressDAO.getDefaultAddressByUserId(1)).thenReturn(testAddress);
        when(mockInventoryService.refreshCartProductsWithNotification(any())).thenReturn(new ArrayList<>());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 4.1 — Empty cart
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Task 4.1: Empty cart → assert response is
     * {@code {"success": false, "message": "Giỏ hàng đang trống."}}
     *
     * <p><b>Validates: Requirement 3.1</b></p>
     */
    @Test
    @DisplayName("4.1 Empty cart: response must be {success: false, message: 'Giỏ hàng đang trống.'}")
    void emptyCart_returnsEmptyCartError() throws Exception {
        // Arrange: empty cart
        when(mockCartDAO.getCartByUserId(1)).thenReturn(new HashMap<>());

        CheckoutServlet servlet = buildServlet();
        HttpServletRequest request = buildPostRequest();
        StringWriter responseBody = new StringWriter();
        HttpServletResponse response = buildMockResponse(responseBody);

        // Act
        servlet.doPost(request, response);

        // Assert
        String body = responseBody.toString();
        assertFalse(body.isEmpty(), "Response body must not be empty");

        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean(), "success must be false");
        assertEquals("Giỏ hàng đang trống.", json.get("message").getAsString(),
                "message must be 'Giỏ hàng đang trống.'");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 4.2 — Product stock < requested quantity
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Task 4.2: Product stock < requested quantity → assert response contains product
     * name and success=false.
     *
     * <p>This test mocks {@code DBContext.getConnection()} using {@code mockStatic} to
     * return a mock Connection, and mocks {@code productDAO.getProductByIdForUpdate()}
     * to return a product with insufficient stock.</p>
     *
     * <p><b>Validates: Requirement 3.2</b></p>
     */
    @Test
    @DisplayName("4.2 Product stock < requested quantity: response must contain product name and success=false")
    void insufficientStock_returnsStockError() throws Exception {
        // Arrange: cart with 5 items requested
        Product cartProduct = new Product();
        cartProduct.setId(1);
        cartProduct.setName("Dog Food");
        cartProduct.setPrice(new BigDecimal("600000")); // above threshold to skip ShippingService
        cartProduct.setStock(10);
        cartProduct.setWeight(500);

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(1, new CartItem(cartProduct, 5)); // requesting 5

        when(mockCartDAO.getCartByUserId(1)).thenReturn(cart);

        // Product returned by getProductByIdForUpdate has only 2 in stock
        Product lowStockProduct = new Product();
        lowStockProduct.setId(1);
        lowStockProduct.setName("Dog Food");
        lowStockProduct.setPrice(new BigDecimal("600000"));
        lowStockProduct.setStock(2); // only 2 available, but 5 requested

        // Mock DBContext.getConnection() to return a mock Connection
        Connection mockConn = mock(Connection.class);
        when(mockProductDAO.getProductByIdForUpdate(eq(mockConn), eq(1))).thenReturn(lowStockProduct);

        CheckoutServlet servlet = buildServlet();
        HttpServletRequest request = buildPostRequest();
        StringWriter responseBody = new StringWriter();
        HttpServletResponse response = buildMockResponse(responseBody);

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(mockConn);

            // Act
            servlet.doPost(request, response);
        }

        // Assert
        String body = responseBody.toString();
        assertFalse(body.isEmpty(), "Response body must not be empty");

        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean(), "success must be false");
        String message = json.get("message").getAsString();
        assertTrue(message.contains("Dog Food"),
                "message must contain the product name 'Dog Food', got: " + message);
        assertTrue(message.contains("2"),
                "message must contain the available stock count '2', got: " + message);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 4.3 — No default address
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Task 4.3: No default address → assert response is
     * {@code {"success": false, "message": "Bạn chưa có địa chỉ mặc định."}}
     *
     * <p><b>Validates: Requirement 3.4</b></p>
     */
    @Test
    @DisplayName("4.3 No default address: response must be {success: false, message: 'Bạn chưa có địa chỉ mặc định.'}")
    void noDefaultAddress_returnsAddressError() throws Exception {
        // Arrange: cart with product above free-shipping threshold (no ShippingService call)
        Product product = new Product();
        product.setId(1);
        product.setName("Dog Food");
        product.setPrice(new BigDecimal("600000")); // above 500,000 VND threshold
        product.setStock(10);
        product.setWeight(500);

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(1, new CartItem(product, 1));
        when(mockCartDAO.getCartByUserId(1)).thenReturn(cart);

        // No default address
        when(mockAddressDAO.getDefaultAddressByUserId(1)).thenReturn(null);

        CheckoutServlet servlet = buildServlet();
        HttpServletRequest request = buildPostRequest();
        StringWriter responseBody = new StringWriter();
        HttpServletResponse response = buildMockResponse(responseBody);

        // Act
        servlet.doPost(request, response);

        // Assert
        String body = responseBody.toString();
        assertFalse(body.isEmpty(), "Response body must not be empty");

        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean(), "success must be false");
        assertEquals("Bạn chưa có địa chỉ mặc định.", json.get("message").getAsString(),
                "message must be 'Bạn chưa có địa chỉ mặc định.'");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 4.4 — Invalid payment method (null from PaymentRegistry)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Task 4.4: Invalid payment method (null from PaymentRegistry) → assert response is
     * {@code {"success": false, "message": "Phương thức thanh toán không hợp lệ."}}
     *
     * <p>This test uses a product with price above the free-shipping threshold to avoid
     * calling ShippingService. It mocks {@code DBContext.getConnection()} to return a
     * mock Connection, and mocks {@code productDAO.getProductByIdForUpdate()} to return
     * a product with sufficient stock. The payment method parameter is set to an invalid
     * value that PaymentRegistry does not recognize.</p>
     *
     * <p><b>Validates: Requirement 3.5</b></p>
     */
    @Test
    @DisplayName("4.4 Invalid payment method: response must be {success: false, message: 'Phương thức thanh toán không hợp lệ.'}")
    void invalidPaymentMethod_returnsPaymentError() throws Exception {
        // Arrange: cart with product above free-shipping threshold
        Product cartProduct = new Product();
        cartProduct.setId(1);
        cartProduct.setName("Dog Food");
        cartProduct.setPrice(new BigDecimal("600000")); // above 500,000 VND threshold
        cartProduct.setStock(10);
        cartProduct.setWeight(500);

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(1, new CartItem(cartProduct, 1));
        when(mockCartDAO.getCartByUserId(1)).thenReturn(cart);

        // Product with sufficient stock
        Product stockProduct = new Product();
        stockProduct.setId(1);
        stockProduct.setName("Dog Food");
        stockProduct.setPrice(new BigDecimal("600000"));
        stockProduct.setStock(10); // sufficient stock

        Connection mockConn = mock(Connection.class);
        when(mockProductDAO.getProductByIdForUpdate(eq(mockConn), eq(1))).thenReturn(stockProduct);

        CheckoutServlet servlet = buildServlet();

        // Request with invalid payment method
        HttpServletRequest request = buildPostRequestWithPayment("invalid_payment_method_xyz");
        StringWriter responseBody = new StringWriter();
        HttpServletResponse response = buildMockResponse(responseBody);

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(mockConn);

            // Act
            servlet.doPost(request, response);
        }

        // Assert
        String body = responseBody.toString();
        assertFalse(body.isEmpty(), "Response body must not be empty");

        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean(), "success must be false");
        assertEquals("Phương thức thanh toán không hợp lệ.", json.get("message").getAsString(),
                "message must be 'Phương thức thanh toán không hợp lệ.'");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper methods
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a CheckoutServlet with all DAOs injected as mocks via reflection.
     */
    private CheckoutServlet buildServlet() throws Exception {
        CheckoutServlet servlet = new CheckoutServlet();
        injectField(servlet, "cartDAO", mockCartDAO);
        injectField(servlet, "userDAO", mockUserDAO);
        injectField(servlet, "addressDAO", mockAddressDAO);
        injectField(servlet, "productDAO", mockProductDAO);
        injectField(servlet, "orderDAO", mockOrderDAO);
        injectField(servlet, "couponDao", mockCouponDAO);
        injectField(servlet, "inventoryService", mockInventoryService);
        return servlet;
    }

    /**
     * Builds a mock POST HttpServletRequest for the place-order flow with "cod" payment.
     */
    private HttpServletRequest buildPostRequest() throws Exception {
        return buildPostRequestWithPayment("cod");
    }

    /**
     * Builds a mock POST HttpServletRequest for the place-order flow with a specific payment method.
     */
    private HttpServletRequest buildPostRequestWithPayment(String paymentMethod) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("paymentMethod")).thenReturn(paymentMethod);
        when(request.getParameter("note")).thenReturn("");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");

        when(session.getAttribute("user")).thenReturn(testUser);
        when(session.getAttribute("appliedCoupon")).thenReturn(null);

        return request;
    }

    /**
     * Builds a mock HttpServletResponse that captures the response body in a StringWriter.
     */
    private HttpServletResponse buildMockResponse(StringWriter responseBody) throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = new PrintWriter(responseBody);
        when(response.getWriter()).thenReturn(writer);
        return response;
    }

    /**
     * Injects a value into a private field of the target object via reflection.
     */
    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Searches for a field by name in the class hierarchy.
     */
    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(
                "Field '" + fieldName + "' not found in class hierarchy of " + clazz.getName());
    }
}
