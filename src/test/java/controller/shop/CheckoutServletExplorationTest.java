package controller.shop;

import DAO.AddressDao;
import DAO.CartDAO;
import DAO.CouponDao;
import DAO.OrderDAO;
import DAO.ProductDAO;
import DAO.UserDAO;
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
import services.InventoryService;
import services.ShippingService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Exploratory tests to reproduce the checkout white-screen bug on UNFIXED code.
 *
 * <p>These tests assert the CORRECT (desired) behavior — i.e., that the response body
 * is never empty and always contains valid JSON. On the UNFIXED code, these tests are
 * EXPECTED TO FAIL, which confirms the bug exists.</p>
 *
 * <h2>Bug Condition</h2>
 * <p>In {@code placeOrderWithStockCheck()}, the outer {@code catch (Exception e)} only
 * covers the {@code try (Connection conn = DBContext.getConnection())} block. Any
 * exception thrown BEFORE this block (e.g., from {@code buildCheckoutSummary()} or
 * from DAO calls) that is not caught internally will escape both
 * {@code placeOrderWithStockCheck()} and {@code doPost()} — Tomcat returns HTTP 500
 * with empty body, producing a white screen.</p>
 *
 * <p>Specifically:</p>
 * <ul>
 *   <li><b>Path 1 (Task 1.1)</b>: {@code buildCheckoutSummary()} is called before the
 *       outer try-catch. Its internal {@code catch (Exception e)} handles
 *       {@code ShippingService} exceptions, but an {@code Error} (e.g.,
 *       {@code AssertionError}) escapes both the internal catch and the outer catch,
 *       propagating to Tomcat.</li>
 *   <li><b>Path 2 (Task 1.2)</b>: {@code userDAO.getUserById()} is called in
 *       {@code refreshUserSession()} at the start of {@code placeOrderWithStockCheck()},
 *       before the outer try-catch. If it throws {@code RuntimeException}, the exception
 *       escapes the method and {@code doPost()} entirely.</li>
 * </ul>
 *
 * <p><b>Validates: Requirements 1.1, 1.4, 2.1, 2.3, 2.4</b></p>
 */
class CheckoutServletExplorationTest {

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

    // ─────────────────────────────────────────────────────────────────────────
    // Task 1.1 — Circuit Breaker / buildCheckoutSummary() throws Error
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Task 1.1: ShippingService throws an Error (not Exception) from within
     * {@code buildCheckoutSummary()}.
     *
     * <p><b>Why Error and not RuntimeException?</b> {@code buildCheckoutSummary()} wraps
     * the {@code ShippingService} call in {@code catch (Exception e)}, so a
     * {@code RuntimeException} is caught internally and falls back to the default fee.
     * An {@code Error} (e.g., {@code AssertionError}) is NOT a subclass of
     * {@code Exception}, so it escapes the internal catch. It then escapes the outer
     * {@code catch (Exception e)} in {@code placeOrderWithStockCheck()} (which only
     * covers the try-with-resources block), and finally escapes {@code doPost()} —
     * Tomcat returns HTTP 500 with empty body.</p>
     *
     * <p>This test asserts the CORRECT behavior (response body is NOT empty).
     * It is EXPECTED TO FAIL on unfixed code, confirming the bug exists.</p>
     *
     * <p><b>Validates: Requirements 1.4, 2.3, 2.4</b></p>
     */
    @Test
    @DisplayName("1.1 ShippingService throws Error from buildCheckoutSummary: response body must NOT be empty (expected to FAIL on unfixed code)")
    void shippingServiceThrowsError_responseMustNotBeEmpty() throws Exception {
        CheckoutServlet servlet = buildServlet();

        // Arrange: mock ShippingService constructor so calculateShippingFee() throws AssertionError.
        // AssertionError extends Error, NOT Exception — it escapes buildCheckoutSummary()'s
        // catch (Exception e) and propagates up through placeOrderWithStockCheck() and doPost().
        try (MockedConstruction<ShippingService> mockedShipping = mockConstruction(
                ShippingService.class,
                (mock, context) -> {
                    when(mock.calculateShippingFee(anyString(), anyString(), anyString(),
                            anyInt(), anyInt(), anyInt(), anyInt()))
                            .thenThrow(new AssertionError(
                                    "GHN API circuit breaker is open — Error escapes catch(Exception e)"));
                })) {

            HttpServletRequest request = buildPostRequest();
            StringWriter responseBody = new StringWriter();
            HttpServletResponse response = buildMockResponse(responseBody);

            // Act: call doPost()
            // On unfixed code: AssertionError escapes buildCheckoutSummary()'s catch(Exception e),
            // then escapes the outer catch(Exception e) in placeOrderWithStockCheck() (which only
            // covers the try-with-resources block), then escapes doPost() — response body is empty.
            servlet.doPost(request, response);

            // Assert: response body must NOT be empty (correct behavior)
            String body = responseBody.toString();
            assertFalse(body.isEmpty(),
                    "BUG CONFIRMED: Response body is empty when ShippingService throws Error. " +
                    "buildCheckoutSummary() is called outside the outer try-catch in " +
                    "placeOrderWithStockCheck(), and catch(Exception e) does not catch Error. " +
                    "Counterexample: body='" + body + "'");

            // Also assert it is valid JSON with success=false
            assertDoesNotThrow(() -> JsonParser.parseString(body),
                    "Response body must be valid JSON, got: " + body);
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            assertFalse(json.get("success").getAsBoolean(),
                    "success must be false when ShippingService throws Error");
            assertFalse(json.get("message").getAsString().isEmpty(),
                    "message must not be empty");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 1.2 — DAO call before outer try-catch throws RuntimeException
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Task 1.2: {@code userDAO.getUserById()} throws RuntimeException before the outer
     * try-catch in {@code placeOrderWithStockCheck()}.
     *
     * <p>{@code refreshUserSession()} calls {@code userDAO.getUserById()} at the very
     * start of {@code placeOrderWithStockCheck()}, BEFORE the
     * {@code try (Connection conn = DBContext.getConnection())} block. The outer
     * {@code catch (Exception e)} does NOT cover this call site. A
     * {@code RuntimeException} thrown here escapes both {@code placeOrderWithStockCheck()}
     * and {@code doPost()} — Tomcat returns HTTP 500 with empty body.</p>
     *
     * <p>This test asserts the CORRECT behavior (response body is NOT empty).
     * It is EXPECTED TO FAIL on unfixed code, confirming the bug exists.</p>
     *
     * <p><b>Validates: Requirements 1.1, 2.1, 2.4</b></p>
     */
    @Test
    @DisplayName("1.2 userDAO.getUserById() throws RuntimeException before outer try-catch: response body must NOT be empty (expected to FAIL on unfixed code)")
    void userDaoThrowsRuntimeException_responseMustNotBeEmpty() throws Exception {
        // Arrange: userDAO.getUserById() throws RuntimeException
        // This is called in refreshUserSession() at the start of placeOrderWithStockCheck(),
        // BEFORE the outer try (Connection conn = DBContext.getConnection()) block.
        // The outer catch(Exception e) does NOT cover this call site.
        when(mockUserDAO.getUserById(1))
                .thenThrow(new RuntimeException("DB connection pool exhausted — RuntimeException before outer try-catch"));

        CheckoutServlet servlet = buildServlet();

        HttpServletRequest request = buildPostRequest();
        StringWriter responseBody = new StringWriter();
        HttpServletResponse response = buildMockResponse(responseBody);

        // Act: call doPost()
        // On unfixed code: RuntimeException from userDAO.getUserById() escapes
        // placeOrderWithStockCheck() (no catch covers it) and then escapes doPost()
        // (no top-level catch) — response body is empty.
        servlet.doPost(request, response);

        // Assert: response body must NOT be empty (correct behavior)
        String body = responseBody.toString();
        assertFalse(body.isEmpty(),
                "BUG CONFIRMED: Response body is empty when userDAO.getUserById() throws RuntimeException. " +
                "refreshUserSession() is called before the outer try-catch in placeOrderWithStockCheck(). " +
                "doPost() has no top-level catch. " +
                "Counterexample: body='" + body + "'");

        // Also assert it is valid JSON with success=false
        assertDoesNotThrow(() -> JsonParser.parseString(body),
                "Response body must be valid JSON, got: " + body);
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean(),
                "success must be false when DAO throws RuntimeException");
        assertFalse(json.get("message").getAsString().isEmpty(),
                "message must not be empty");
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
