package services;

import Context.DBContext;
import DAO.CartDAO;
import DAO.CouponDao;
import DAO.InventoryBatchDAO;
import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import DAO.ProductDAO;
import DAO.UserDAO;
import Model.CartItem;
import Model.CouponValidationResult;
import Model.Order;
import Model.PaymentTransaction;
import Model.Product;
import Model.User;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CheckoutServiceInventoryBatchTest {

    @Test
    void processCheckoutReservesTrackedProductStockBeforeSavingOrderItems() throws Exception {
        ProductDAO productDAO = mock(ProductDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        CouponDao couponDao = mock(CouponDao.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        PaymentTransactionDAO paymentTransactionDAO = mock(PaymentTransactionDAO.class);
        CartDAO cartDAO = mock(CartDAO.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        InventoryBatchDAO inventoryBatchDAO = mock(InventoryBatchDAO.class);

        Connection conn = mock(Connection.class);
        User user = new User();
        user.setId(7);
        user.setFullname("Nguyen Van A");
        user.setPhone("0901234567");

        Product latestProduct = new Product();
        latestProduct.setId(11);
        latestProduct.setName("Pate meo");
        latestProduct.setPrice(new BigDecimal("80000"));
        latestProduct.setStock(5);

        Product cartProduct = new Product();
        cartProduct.setId(11);
        cartProduct.setPrice(new BigDecimal("80000"));

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(11, new CartItem(cartProduct, 2));

        when(productDAO.getProductByIdForUpdate(conn, 11)).thenReturn(latestProduct);
        when(orderDAO.saveOrder(eq(conn), any(Order.class))).thenReturn(901);
        when(orderDAO.saveOrderItem(eq(conn), any())).thenReturn(true);
        when(paymentTransactionDAO.save(eq(conn), any())).thenReturn(77);
        when(productDAO.reserveStock(conn, 11, 2)).thenReturn(true);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, inventoryBatchDAO
        );

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);

            CheckoutResult result = service.processCheckout(
                    user,
                    cart,
                    "123 Nguyen Hue, Phuong Ben Nghe, Quan 1, Ho Chi Minh",
                    "",
                    CouponValidationResult.empty(),
                    "cod",
                    30000
            );

            assertTrue(result.isSuccess());
        }

        verify(productDAO).reserveStock(conn, 11, 2);
        verifyNoInteractions(inventoryBatchDAO);
    }

    @Test
    void processCheckoutReservesStockInsteadOfSellingWhilePaymentCanStillExpire() throws Exception {
        ProductDAO productDAO = mock(ProductDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        CouponDao couponDao = mock(CouponDao.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        PaymentTransactionDAO paymentTransactionDAO = mock(PaymentTransactionDAO.class);
        CartDAO cartDAO = mock(CartDAO.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        InventoryBatchDAO inventoryBatchDAO = mock(InventoryBatchDAO.class);

        Connection conn = mock(Connection.class);
        User user = new User();
        user.setId(7);
        user.setFullname("Nguyen Van A");
        user.setPhone("0901234567");

        Product latestProduct = new Product();
        latestProduct.setId(11);
        latestProduct.setName("Cat litter");
        latestProduct.setPrice(new BigDecimal("120000"));
        latestProduct.setStock(5);

        Product cartProduct = new Product();
        cartProduct.setId(11);
        cartProduct.setPrice(new BigDecimal("120000"));

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(11, new CartItem(cartProduct, 2));

        when(productDAO.getProductByIdForUpdate(conn, 11)).thenReturn(latestProduct);
        when(productDAO.reserveStock(conn, 11, 2)).thenReturn(true);
        when(orderDAO.saveOrder(eq(conn), any(Order.class))).thenReturn(901);
        when(orderDAO.saveOrderItem(eq(conn), any())).thenReturn(true);
        when(paymentTransactionDAO.save(eq(conn), any())).thenReturn(77);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, inventoryBatchDAO
        );

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);

            CheckoutResult result = service.processCheckout(
                    user,
                    cart,
                    "123 Nguyen Hue, Phuong Ben Nghe, Quan 1, Ho Chi Minh",
                    "",
                    CouponValidationResult.empty(),
                    "bank_transfer",
                    30000,
                    "PETSHOP-U7-123456"
            );

            assertTrue(result.isSuccess());
        }

        verify(productDAO).reserveStock(conn, 11, 2);
        verify(productDAO, never()).decreaseStock(conn, 11, 2);
        verifyNoInteractions(inventoryBatchDAO);
    }

    @Test
    void couponBelowMinimumOrderIsRejectedBeforeMarkingUserDiscountUsed() throws Exception {
        ProductDAO productDAO = mock(ProductDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        CouponDao couponDao = mock(CouponDao.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        PaymentTransactionDAO paymentTransactionDAO = mock(PaymentTransactionDAO.class);
        CartDAO cartDAO = mock(CartDAO.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        InventoryBatchDAO inventoryBatchDAO = mock(InventoryBatchDAO.class);

        Connection conn = mock(Connection.class);
        User user = new User();
        user.setId(7);
        user.setFullname("Nguyen Van A");
        user.setPhone("0901234567");

        Product latestProduct = new Product();
        latestProduct.setId(11);
        latestProduct.setName("Pate meo");
        latestProduct.setPrice(new BigDecimal("80000"));
        latestProduct.setStock(5);

        Product cartProduct = new Product();
        cartProduct.setId(11);
        cartProduct.setPrice(new BigDecimal("80000"));

        Model.Coupon coupon = new Model.Coupon();
        coupon.setId(3);
        coupon.setCode("SAVE20K");
        coupon.setDiscountType("fixed");
        coupon.setDiscountValue(new BigDecimal("20000"));
        coupon.setMinOrder(new BigDecimal("100000"));
        coupon.setQuantity(10);
        coupon.setUsed(0);

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(11, new CartItem(cartProduct, 1));

        when(productDAO.getProductByIdForUpdate(conn, 11)).thenReturn(latestProduct);
        when(couponDao.getValidCouponByCode(conn, "SAVE20K")).thenReturn(coupon);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, inventoryBatchDAO
        );

        CheckoutResult result;
        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);

            result = service.processCheckout(
                    user,
                    cart,
                    "123 Nguyen Hue, Phuong Ben Nghe, Quan 1, Ho Chi Minh",
                    "",
                    CouponValidationResult.valid(coupon),
                    "cod",
                    30000
            );
        }

        assertFalse(result.isSuccess());
        verify(userDAO, never()).markDiscountAsUsed(conn, 7);
        verify(couponDao, never()).increaseUsedIfAvailable(conn, 3);
    }

    @Test
    void bankTransferCheckoutUsesReservedReferenceAndExpiresInTenMinutes() throws Exception {
        ProductDAO productDAO = mock(ProductDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        CouponDao couponDao = mock(CouponDao.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        PaymentTransactionDAO paymentTransactionDAO = mock(PaymentTransactionDAO.class);
        CartDAO cartDAO = mock(CartDAO.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        InventoryBatchDAO inventoryBatchDAO = mock(InventoryBatchDAO.class);

        Connection conn = mock(Connection.class);
        User user = new User();
        user.setId(7);
        user.setFullname("Nguyen Van A");
        user.setPhone("0901234567");

        Product latestProduct = new Product();
        latestProduct.setId(11);
        latestProduct.setName("Pate meo");
        latestProduct.setPrice(new BigDecimal("80000"));
        latestProduct.setStock(5);

        Product cartProduct = new Product();
        cartProduct.setId(11);
        cartProduct.setPrice(new BigDecimal("80000"));

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(11, new CartItem(cartProduct, 2));

        when(productDAO.getProductByIdForUpdate(conn, 11)).thenReturn(latestProduct);
        when(orderDAO.saveOrder(eq(conn), any(Order.class))).thenReturn(901);
        when(orderDAO.saveOrderItem(eq(conn), any())).thenReturn(true);
        when(paymentTransactionDAO.save(eq(conn), any())).thenReturn(77);
        when(productDAO.reserveStock(conn, 11, 2)).thenReturn(true);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, inventoryBatchDAO
        );

        String reservedReference = "PETSHOP-U7-123456";
        Instant beforeCheckout = Instant.now();

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);

            CheckoutResult result = service.processCheckout(
                    user,
                    cart,
                    "123 Nguyen Hue, Phuong Ben Nghe, Quan 1, Ho Chi Minh",
                    "",
                    CouponValidationResult.empty(),
                    "bank_transfer",
                    30000,
                    reservedReference
            );

            assertTrue(result.isSuccess());
        }

        ArgumentCaptor<PaymentTransaction> transactionCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(paymentTransactionDAO).save(eq(conn), transactionCaptor.capture());

        PaymentTransaction transaction = transactionCaptor.getValue();
        assertEquals(reservedReference, transaction.getTransferReference());
        assertEquals("PENDING_VERIFICATION", transaction.getStatus());

        Timestamp expiresAt = transaction.getExpiresAt();
        assertTrue(expiresAt.toInstant().isAfter(beforeCheckout.plus(Duration.ofMinutes(9))));
        assertTrue(expiresAt.toInstant().isBefore(beforeCheckout.plus(Duration.ofMinutes(11))));
    }

    @Test
    void bankTransferCheckoutCreatesAwaitingPaymentOrder() throws Exception {
        ProductDAO productDAO = mock(ProductDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        CouponDao couponDao = mock(CouponDao.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        PaymentTransactionDAO paymentTransactionDAO = mock(PaymentTransactionDAO.class);
        CartDAO cartDAO = mock(CartDAO.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        InventoryBatchDAO inventoryBatchDAO = mock(InventoryBatchDAO.class);

        Connection conn = mock(Connection.class);
        User user = new User();
        user.setId(7);
        user.setFullname("Nguyen Van A");
        user.setPhone("0901234567");

        Product latestProduct = new Product();
        latestProduct.setId(11);
        latestProduct.setName("Pate meo");
        latestProduct.setPrice(new BigDecimal("80000"));
        latestProduct.setStock(5);

        Product cartProduct = new Product();
        cartProduct.setId(11);
        cartProduct.setPrice(new BigDecimal("80000"));

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(11, new CartItem(cartProduct, 2));

        when(productDAO.getProductByIdForUpdate(conn, 11)).thenReturn(latestProduct);
        when(productDAO.reserveStock(conn, 11, 2)).thenReturn(true);
        when(orderDAO.saveOrder(eq(conn), any(Order.class))).thenReturn(901);
        when(orderDAO.saveOrderItem(eq(conn), any())).thenReturn(true);
        when(paymentTransactionDAO.save(eq(conn), any())).thenReturn(77);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, inventoryBatchDAO
        );

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);

            CheckoutResult result = service.processCheckout(
                    user,
                    cart,
                    "123 Nguyen Hue, Phuong Ben Nghe, Quan 1, Ho Chi Minh",
                    "",
                    CouponValidationResult.empty(),
                    "bank_transfer",
                    30000,
                    "PETSHOP-U7-123456"
            );

            assertTrue(result.isSuccess());
        }

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderDAO).saveOrder(eq(conn), orderCaptor.capture());
        assertEquals("Awaiting Payment", orderCaptor.getValue().getStatus());
        assertEquals("BANK_TRANSFER", orderCaptor.getValue().getPayment_method());
        assertFalse(orderCaptor.getValue().getPayment_status());
    }

    @Test
    void vnpayCheckoutCreatesUnpaidVnpayTransaction() throws Exception {
        ProductDAO productDAO = mock(ProductDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        CouponDao couponDao = mock(CouponDao.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        PaymentTransactionDAO paymentTransactionDAO = mock(PaymentTransactionDAO.class);
        CartDAO cartDAO = mock(CartDAO.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        InventoryBatchDAO inventoryBatchDAO = mock(InventoryBatchDAO.class);

        Connection conn = mock(Connection.class);
        User user = new User();
        user.setId(7);
        user.setFullname("Nguyen Van A");
        user.setPhone("0901234567");

        Product latestProduct = new Product();
        latestProduct.setId(11);
        latestProduct.setName("Pate meo");
        latestProduct.setPrice(new BigDecimal("80000"));
        latestProduct.setStock(5);

        Product cartProduct = new Product();
        cartProduct.setId(11);
        cartProduct.setPrice(new BigDecimal("80000"));

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(11, new CartItem(cartProduct, 2));

        when(productDAO.getProductByIdForUpdate(conn, 11)).thenReturn(latestProduct);
        when(productDAO.reserveStock(conn, 11, 2)).thenReturn(true);
        when(orderDAO.saveOrder(eq(conn), any(Order.class))).thenReturn(901);
        when(orderDAO.saveOrderItem(eq(conn), any())).thenReturn(true);
        when(paymentTransactionDAO.save(eq(conn), any())).thenReturn(77);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, inventoryBatchDAO
        );

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);

            CheckoutResult result = service.processCheckout(
                    user,
                    cart,
                    "123 Nguyen Hue, Phuong Ben Nghe, Quan 1, Ho Chi Minh",
                    "",
                    CouponValidationResult.empty(),
                    "vnpay",
                    30000
            );

            assertTrue(result.isSuccess());
        }

        ArgumentCaptor<PaymentTransaction> transactionCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(paymentTransactionDAO).save(eq(conn), transactionCaptor.capture());
        PaymentTransaction transaction = transactionCaptor.getValue();
        assertEquals("VNPAY", transaction.getProviderKey());
        assertEquals("CREATED", transaction.getStatus());
        assertEquals("PENDING", transaction.getVerificationStatus());
    }

    @Test
    void processCheckoutPersistsOrderItemProductSnapshot() throws Exception {
        ProductDAO productDAO = mock(ProductDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        CouponDao couponDao = mock(CouponDao.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        PaymentTransactionDAO paymentTransactionDAO = mock(PaymentTransactionDAO.class);
        CartDAO cartDAO = mock(CartDAO.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        InventoryBatchDAO inventoryBatchDAO = mock(InventoryBatchDAO.class);

        Connection conn = mock(Connection.class);
        User user = new User();
        user.setId(7);
        user.setFullname("Nguyen Van A");
        user.setPhone("0901234567");

        Product latestProduct = new Product();
        latestProduct.setId(11);
        latestProduct.setName("Pate meo snapshot");
        latestProduct.setImage("pate.jpg");
        latestProduct.setPrice(new BigDecimal("80000"));
        latestProduct.setStock(5);

        Product cartProduct = new Product();
        cartProduct.setId(11);
        cartProduct.setPrice(new BigDecimal("80000"));

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(11, new CartItem(cartProduct, 2));

        when(productDAO.getProductByIdForUpdate(conn, 11)).thenReturn(latestProduct);
        when(productDAO.reserveStock(conn, 11, 2)).thenReturn(true);
        when(orderDAO.saveOrder(eq(conn), any(Order.class))).thenReturn(901);
        when(orderDAO.saveOrderItem(eq(conn), any())).thenReturn(true);
        when(paymentTransactionDAO.save(eq(conn), any())).thenReturn(77);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, inventoryBatchDAO
        );

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);

            CheckoutResult result = service.processCheckout(
                    user,
                    cart,
                    "123 Nguyen Hue, Phuong Ben Nghe, Quan 1, Ho Chi Minh",
                    "",
                    CouponValidationResult.empty(),
                    "cod",
                    30000
            );

            assertTrue(result.isSuccess());
        }

        ArgumentCaptor<Model.OrderItem> itemCaptor = ArgumentCaptor.forClass(Model.OrderItem.class);
        verify(orderDAO).saveOrderItem(eq(conn), itemCaptor.capture());
        assertEquals("Pate meo snapshot", itemCaptor.getValue().getProductNameSnapshot());
        assertEquals("pate.jpg", itemCaptor.getValue().getProductImageSnapshot());
    }

    @Test
    void processCheckoutPersistsRecipientSnapshotSeparateFromAccountProfile() throws Exception {
        User user = new User();
        user.setId(7);
        user.setFullname("Account Owner");
        user.setPhone("0900000000");
        user.setEmail("");

        Order savedOrder = CheckoutService.buildOrderSnapshot(
                user,
                "Nguyen Van Receiver",
                "0912345678",
                "123 Nguyen Trai",
                "",
                new BigDecimal("110000"),
                "COD",
                false
        );

        assertEquals(7, savedOrder.getUserId());
        assertEquals("Nguyen Van Receiver", savedOrder.getRecipientFullname());
        assertEquals("0912345678", savedOrder.getRecipientPhone());
        assertEquals("123 Nguyen Trai", savedOrder.getShippingAddress());
        assertEquals("Account Owner", savedOrder.getCustomerFullname());
    }
}
