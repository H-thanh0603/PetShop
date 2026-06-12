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
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import services.InventoryService;
import services.ShippingService;

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
 * Fix-checking tests (Property 1): verify that the FIXED code always returns valid JSON
 * â€” never an empty response body â€” when exceptions are thrown at various points.
 *
 * <p>These tests assert the CORRECT (desired) behavior. On the FIXED code, all tests
 * should PASS. They validate that the top-level {@code try-catch(Throwable t)} in
 * {@code placeOrderWithStockCheck()} and the safety net in {@code doPost()} work correctly.</p>
 *
 * <p><b>Validates: Requirements 2.1, 2.2, 2.3, 2.4</b></p>
 */
class CheckoutServletFixCheckTest {

    private CartDAO mockCartDAO;
    private UserDAO mockUserDAO;
    private AddressDao mockAddressDAO;
    private ProductDAO mockProductDAO;
    private OrderDAO mockOrderDAO;
    private CouponDao mockCouponDAO;
    private InventoryService mockInventoryService;

    private User testUser;
    private Address testAddress;
    private Map<Integer, CartItem> testCart;

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

        // Cart with one product (price below free-shipping threshold to trigger ShippingService)
        Product product = new Product();
        product.setId(1);
        product.setName("Dog Food");
        product.setPrice(new BigDecimal("100000")); // below 500,000 VND threshold
        product.setStock(10);
        product.setWeight(500);

        testCart = new HashMap<>();
        testCart.put(1, new CartItem(product, 1));

        // Default stubs
        when(mockUserDAO.getUserById(1)).thenReturn(testUser);
        when(mockCartDAO.getCartByUserId(1)).thenReturn(testCart);
        when(mockAddressDAO.getDefaultAddressByUserId(1)).thenReturn(testAddress);
        when(mockInventoryService.refreshCartProductsWithNotification(any())).thenReturn(new ArrayList<>());
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Task 3.1 â€” ShippingService throws AssertionError (Error subclass)
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Task 3.1: ShippingService throws an AssertionError (Error subclass) from within
     * {@code buildCheckoutSummary()}.
     *
     * <p>An {@code AssertionError} extends {@code Error}, NOT {@code Exception}. The
     * internal {@code catch (Exception e)} in {@code buildCheckoutSummary()} does NOT
     * catch it. The fix wraps the entire body of {@code placeOrderWithStockCheck()} in
     * {@code catch (Throwable t)}, which catches both {@code Exception} and {@code Error}
     * subclasses â€” ensuring the response is always valid JSON.</p>
     *
     * <p><b>Validates: Requirements 2.3, 2.4</b></p>
     */
    @Test
    @DisplayName("3.1 ShippingService throws AssertionError (Error subclass): response must be valid JSON with success=false")
    void shippingServiceThrowsAssertionError_responseIsValidJson() throws Exception {
        CheckoutServlet servlet = buildServlet();

        // Arrange: mock ShippingService constructor so calculateShippingFee() throws AssertionError.
        // AssertionError extends Error, NOT Exception â€” it escapes buildCheckoutSummary()'s
        // catch (Exception e). The fix's top-level catch(Throwable t) must catch it.
        try (MockedConstruction<ShippingService> mockedShipping = mockConstruction(
                ShippingService.class,
                (mock, context) -> {
                    when(mock.calculateShippingFee(anyString(), anyString(), anyString(),
                            anyInt(), anyInt(), anyInt(), anyInt()))
                            .thenThrow(new AssertionError(
                                    "GHN API circuit breaker is open â€” Error escapes catch(Exception e)"));
                })) {

            HttpServletRequest request = buildPostRequest();
            StringWriter responseBody = new StringWriter();
            HttpServletResponse response = buildMockResponse(responseBody);

            // Act
            servlet.doPost(request, response);

            // Assert: response body must NOT be empty
            String body = responseBody.toString();
            assertFalse(body.isEmpty(),
                    "Response body must not be empty when ShippingService throws AssertionError. " +
                    "The fix's catch(Throwable t) must catch Error subclasses. Got: '" + body + "'");

            // Assert: valid JSON with success=false and non-empty message
            assertDoesNotThrow(() -> new JsonParser().parse(body),
                    "Response body must be valid JSON, got: " + body);
            JsonObject json = new JsonParser().parse(body).getAsJsonObject();
            assertFalse(json.get("success").getAsBoolean(),
                    "success must be false when ShippingService throws AssertionError");
            assertNotNull(json.get("message"),
                    "message field must be present");
            assertFalse(json.get("message").getAsString().isEmpty(),
                    "message must not be empty");
        }
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Task 3.2 â€” userDAO.getUserById() throws RuntimeException
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Task 3.2: {@code userDAO.getUserById()} throws RuntimeException before the outer
     * try-catch in {@code placeOrderWithStockCheck()}.
     *
     * <p>{@code refreshUserSession()} calls {@code userDAO.getUserById()} at the very
     * start of {@code placeOrderWithStockCheck()}, before the
     * {@code try (Connection conn = DBContext.getConnection())} block. The fix wraps
     * the entire method body in {@code catch (Throwable t)}, ensuring this exception
     * is caught and a JSON error response is returned.</p>
     *
     * <p><b>Validates: Requirements 2.1, 2.4</b></p>
     */
    @Test
    @DisplayName("3.2 userDAO.getUserById() throws RuntimeException: response must be valid JSON with success=false")
    void userDaoThrowsRuntimeException_responseIsValidJson() throws Exception {
        // Arrange: userDAO.getUserById() throws RuntimeException
        when(mockUserDAO.getUserById(1))
                .thenThrow(new RuntimeException("DB connection pool exhausted â€” RuntimeException before outer try-catch"));

        CheckoutServlet servlet = buildServlet();

        HttpServletRequest request = buildPostRequest();
        StringWriter responseBody = new StringWriter();
        HttpServletResponse response = buildMockResponse(responseBody);

        // Act
        servlet.doPost(request, response);

        // Assert: response body must NOT be empty
        String body = responseBody.toString();
        assertFalse(body.isEmpty(),
                "Response body must not be empty when userDAO.getUserById() throws RuntimeException. " +
                "The fix's catch(Throwable t) must catch it. Got: '" + body + "'");

        // Assert: valid JSON with success=false
        assertDoesNotThrow(() -> new JsonParser().parse(body),
                "Response body must be valid JSON, got: " + body);
        JsonObject json = new JsonParser().parse(body).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean(),
                "success must be false when DAO throws RuntimeException");
        assertNotNull(json.get("message"),
                "message field must be present");
        assertFalse(json.get("message").getAsString().isEmpty(),
                "message must not be empty");
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Task 3.3 â€” cartDAO.getCartByUserId() throws RuntimeException
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Task 3.3: {@code cartDAO.getCartByUserId()} throws RuntimeException â€” another
     * point before the DB connection try-with-resources block.
     *
     * <p>The cart is loaded via {@code loadLatestCartForUser()} which calls
     * {@code cartDAO.getCartByUserId()}. This happens before the
     * {@code try (Connection conn = DBContext.getConnection())} block. The fix's
     * top-level {@code catch (Throwable t)} must catch this and return JSON.</p>
     *
     * <p><b>Validates: Requirements 2.1, 2.4</b></p>
     */
    @Test
    @DisplayName("3.3 cartDAO.getCartByUserId() throws RuntimeException: response must be valid JSON with success=false")
    void cartDaoThrowsRuntimeException_responseIsValidJson() throws Exception {
        // Arrange: cartDAO.getCartByUserId() throws RuntimeException
        when(mockCartDAO.getCartByUserId(1))
                .thenThrow(new RuntimeException("Cart DB error â€” RuntimeException before DB connection block"));

        CheckoutServlet servlet = buildServlet();

        HttpServletRequest request = buildPostRequest();
        StringWriter responseBody = new StringWriter();
        HttpServletResponse response = buildMockResponse(responseBody);

        // Act
        servlet.doPost(request, response);

        // Assert: response body must NOT be empty
        String body = responseBody.toString();
        assertFalse(body.isEmpty(),
                "Response body must not be empty when cartDAO.getCartByUserId() throws RuntimeException. " +
                "The fix's catch(Throwable t) must catch it. Got: '" + body + "'");

        // Assert: valid JSON with success=false
        assertDoesNotThrow(() -> new JsonParser().parse(body),
                "Response body must be valid JSON, got: " + body);
        JsonObject json = new JsonParser().parse(body).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean(),
                "success must be false when cartDAO throws RuntimeException");
        assertNotNull(json.get("message"),
                "message field must be present");
        assertFalse(json.get("message").getAsString().isEmpty(),
                "message must not be empty");
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Task 3.4 â€” doPost() top-level catch handles IOException from placeOrderWithStockCheck
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Task 3.4: Verify that {@code doPost()}'s top-level catch handles the case where
     * {@code placeOrderWithStockCheck()} throws an {@code IOException}.
     *
     * <p>The fix adds a top-level {@code try-catch(Exception e)} in {@code doPost()}
     * around the call to {@code placeOrderWithStockCheck()}. This test verifies that
     * even if {@code placeOrderWithStockCheck()} itself throws an {@code IOException}
     * (e.g., from {@code write()}), the {@code doPost()} safety net catches it and
     * returns a JSON error response.</p>
     *
     * <p>We simulate this by making {@code response.getWriter()} throw {@code IOException}
     * on the first call (inside {@code placeOrderWithStockCheck()}) but succeed on the
     * second call (inside {@code doPost()}'s catch block).</p>
     *
     * <p><b>Validates: Requirements 2.2, 2.4</b></p>
     */
    @Test
    @DisplayName("3.4 placeOrderWithStockCheck throws IOException: doPost() top-level catch returns JSON error")
    void placeOrderThrowsIOException_doPostSafetyNetReturnsJson() throws Exception {
        // Arrange: make addressDAO throw RuntimeException to trigger the top-level catch in placeOrderWithStockCheck.
        // Then the catch(Throwable t) in placeOrderWithStockCheck will try to call write().
        // If write() also fails (IOException from getWriter()), the doPost() safety net must catch it.
        when(mockAddressDAO.getDefaultAddressByUserId(1))
                .thenThrow(new RuntimeException("Simulated error to trigger top-level catch"));

        CheckoutServlet servlet = buildServlet();

        HttpServletRequest request = buildPostRequest();

        // First call to getWriter() throws IOException (simulating write() failure inside placeOrderWithStockCheck)
        // Second call to getWriter() succeeds (doPost() safety net)
        StringWriter safetyNetBody = new StringWriter();
        PrintWriter safetyNetWriter = new PrintWriter(safetyNetBody);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter())
                .thenThrow(new java.io.IOException("Simulated IOException from write() inside placeOrderWithStockCheck"))
                .thenReturn(safetyNetWriter);

        // Act
        servlet.doPost(request, response);

        // Assert: doPost() safety net must have written a JSON response
        String body = safetyNetBody.toString();
        assertFalse(body.isEmpty(),
                "doPost() safety net must write JSON when placeOrderWithStockCheck throws IOException. " +
                "Got: '" + body + "'");

        assertDoesNotThrow(() -> new JsonParser().parse(body),
                "doPost() safety net response must be valid JSON, got: " + body);
        JsonObject json = new JsonParser().parse(body).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean(),
                "success must be false in doPost() safety net response");
        assertNotNull(json.get("message"),
                "message field must be present in doPost() safety net response");
        assertFalse(json.get("message").getAsString().isEmpty(),
                "message must not be empty in doPost() safety net response");
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Helper methods
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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
     * Builds a mock POST HttpServletRequest for the place-order flow.
     */
    private HttpServletRequest buildPostRequest() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("paymentMethod")).thenReturn("cod");
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
