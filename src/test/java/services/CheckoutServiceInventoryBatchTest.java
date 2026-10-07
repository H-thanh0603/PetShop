package services;

import com.petshop.repository.CartRepository;
import com.petshop.repository.CouponRepository;
import com.petshop.repository.OrderSignRepository;
import com.petshop.repository.CertificateRepository;
import com.petshop.repository.PromotionRepository;
import com.petshop.repository.OrderRepository;
import com.petshop.repository.PaymentTransactionRepository;
import com.petshop.repository.ProductRepository;
import com.petshop.repository.UserRepository;
import com.petshop.model.CartItem;
import com.petshop.model.CouponValidationResult;
import com.petshop.model.Order;
import com.petshop.model.PaymentTransaction;
import com.petshop.model.Product;
import com.petshop.model.User;
import org.junit.jupiter.api.Test;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CheckoutServiceInventoryBatchTest {

    @Test
    void processCheckoutReservesTrackedProductStockBeforeSavingOrderItems() throws Exception {
        ProductRepository productDAO = mock(ProductRepository.class);
        UserRepository userDAO = mock(UserRepository.class);
        CouponRepository couponDao = mock(CouponRepository.class);
        OrderRepository orderDAO = mock(OrderRepository.class);
        PaymentTransactionRepository paymentTransactionDAO = mock(PaymentTransactionRepository.class);
        CartRepository cartDAO = mock(CartRepository.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        OrderSignRepository orderSignDAO = mock(OrderSignRepository.class);
        CertificateRepository certificateDAO = mock(CertificateRepository.class);
        PromotionRepository promotionDAO = mock(PromotionRepository.class);

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

        when(productDAO.findForUpdateById(11)).thenReturn(latestProduct);
        when(orderDAO.saveOrder(any(Order.class))).thenReturn(901);
        when(orderDAO.saveOrderItem(any())).thenReturn(true);
        when(paymentTransactionDAO.saveTx(any())).thenReturn(77);
        when(productDAO.reserveStockAmbient(11, 2)).thenReturn(true);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, orderSignDAO, certificateDAO, promotionDAO
        );

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

        verify(productDAO).reserveStockAmbient(11, 2);
    }

    @Test
    void processCheckoutReservesStockInsteadOfSellingWhilePaymentCanStillExpire() throws Exception {
        ProductRepository productDAO = mock(ProductRepository.class);
        UserRepository userDAO = mock(UserRepository.class);
        CouponRepository couponDao = mock(CouponRepository.class);
        OrderRepository orderDAO = mock(OrderRepository.class);
        PaymentTransactionRepository paymentTransactionDAO = mock(PaymentTransactionRepository.class);
        CartRepository cartDAO = mock(CartRepository.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        OrderSignRepository orderSignDAO = mock(OrderSignRepository.class);
        CertificateRepository certificateDAO = mock(CertificateRepository.class);
        PromotionRepository promotionDAO = mock(PromotionRepository.class);

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

        when(productDAO.findForUpdateById(11)).thenReturn(latestProduct);
        when(productDAO.reserveStockAmbient(11, 2)).thenReturn(true);
        when(orderDAO.saveOrder(any(Order.class))).thenReturn(901);
        when(orderDAO.saveOrderItem(any())).thenReturn(true);
        when(paymentTransactionDAO.saveTx(any())).thenReturn(77);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, orderSignDAO, certificateDAO, promotionDAO
        );

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

        verify(productDAO).reserveStockAmbient(11, 2);
        verify(productDAO, never()).decreaseStock(11, 2);
    }

    @Test
    void couponBelowMinimumOrderIsRejectedBeforeMarkingUserDiscountUsed() throws Exception {
        ProductRepository productDAO = mock(ProductRepository.class);
        UserRepository userDAO = mock(UserRepository.class);
        CouponRepository couponDao = mock(CouponRepository.class);
        OrderRepository orderDAO = mock(OrderRepository.class);
        PaymentTransactionRepository paymentTransactionDAO = mock(PaymentTransactionRepository.class);
        CartRepository cartDAO = mock(CartRepository.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        OrderSignRepository orderSignDAO = mock(OrderSignRepository.class);
        CertificateRepository certificateDAO = mock(CertificateRepository.class);
        PromotionRepository promotionDAO = mock(PromotionRepository.class);

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

        com.petshop.model.Coupon coupon = new com.petshop.model.Coupon();
        coupon.setId(3);
        coupon.setCode("SAVE20K");
        coupon.setDiscountType("fixed");
        coupon.setDiscountValue(new BigDecimal("20000"));
        coupon.setMinOrder(new BigDecimal("100000"));
        coupon.setQuantity(10);
        coupon.setUsed(0);

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(11, new CartItem(cartProduct, 1));

        when(productDAO.findForUpdateById(11)).thenReturn(latestProduct);
        when(couponDao.getValidCouponByCode("SAVE20K")).thenReturn(coupon);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, orderSignDAO, certificateDAO, promotionDAO
        );

        CheckoutResult result;
            result = service.processCheckout(
                    user,
                    cart,
                    "123 Nguyen Hue, Phuong Ben Nghe, Quan 1, Ho Chi Minh",
                    "",
                    CouponValidationResult.valid(coupon),
                    "cod",
                    30000
            );

        assertFalse(result.isSuccess());
        verify(userDAO, never()).markDiscountAsUsed(7);
        verify(couponDao, never()).increaseUsedIfAvailable(3);
    }

    @Test
    void bankTransferCheckoutUsesReservedReferenceAndExpiresInTenMinutes() throws Exception {
        ProductRepository productDAO = mock(ProductRepository.class);
        UserRepository userDAO = mock(UserRepository.class);
        CouponRepository couponDao = mock(CouponRepository.class);
        OrderRepository orderDAO = mock(OrderRepository.class);
        PaymentTransactionRepository paymentTransactionDAO = mock(PaymentTransactionRepository.class);
        CartRepository cartDAO = mock(CartRepository.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        OrderSignRepository orderSignDAO = mock(OrderSignRepository.class);
        CertificateRepository certificateDAO = mock(CertificateRepository.class);
        PromotionRepository promotionDAO = mock(PromotionRepository.class);

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

        when(productDAO.findForUpdateById(11)).thenReturn(latestProduct);
        when(orderDAO.saveOrder(any(Order.class))).thenReturn(901);
        when(orderDAO.saveOrderItem(any())).thenReturn(true);
        when(paymentTransactionDAO.saveTx(any())).thenReturn(77);
        when(productDAO.reserveStockAmbient(11, 2)).thenReturn(true);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, orderSignDAO, certificateDAO, promotionDAO
        );

        String reservedReference = "PETSHOP-U7-123456";
        Instant beforeCheckout = Instant.now();

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

        ArgumentCaptor<PaymentTransaction> transactionCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(paymentTransactionDAO).saveTx(transactionCaptor.capture());

        PaymentTransaction transaction = transactionCaptor.getValue();
        assertEquals(reservedReference, transaction.getTransferReference());
        assertEquals("PENDING_VERIFICATION", transaction.getStatus());

        Timestamp expiresAt = transaction.getExpiresAt();
        assertTrue(expiresAt.toInstant().isAfter(beforeCheckout.plus(Duration.ofMinutes(9))));
        assertTrue(expiresAt.toInstant().isBefore(beforeCheckout.plus(Duration.ofMinutes(11))));
    }

    @Test
    void bankTransferCheckoutCreatesAwaitingPaymentOrder() throws Exception {
        ProductRepository productDAO = mock(ProductRepository.class);
        UserRepository userDAO = mock(UserRepository.class);
        CouponRepository couponDao = mock(CouponRepository.class);
        OrderRepository orderDAO = mock(OrderRepository.class);
        PaymentTransactionRepository paymentTransactionDAO = mock(PaymentTransactionRepository.class);
        CartRepository cartDAO = mock(CartRepository.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        OrderSignRepository orderSignDAO = mock(OrderSignRepository.class);
        CertificateRepository certificateDAO = mock(CertificateRepository.class);
        PromotionRepository promotionDAO = mock(PromotionRepository.class);

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

        when(productDAO.findForUpdateById(11)).thenReturn(latestProduct);
        when(productDAO.reserveStockAmbient(11, 2)).thenReturn(true);
        when(orderDAO.saveOrder(any(Order.class))).thenReturn(901);
        when(orderDAO.saveOrderItem(any())).thenReturn(true);
        when(paymentTransactionDAO.saveTx(any())).thenReturn(77);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, orderSignDAO, certificateDAO, promotionDAO
        );

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

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderDAO).saveOrder(orderCaptor.capture());
        assertEquals("Awaiting Payment", orderCaptor.getValue().getStatus());
        assertEquals("BANK_TRANSFER", orderCaptor.getValue().getPayment_method());
        assertFalse(orderCaptor.getValue().getPayment_status());
    }

    @Test
    void vnpayCheckoutCreatesUnpaidVnpayTransaction() throws Exception {
        ProductRepository productDAO = mock(ProductRepository.class);
        UserRepository userDAO = mock(UserRepository.class);
        CouponRepository couponDao = mock(CouponRepository.class);
        OrderRepository orderDAO = mock(OrderRepository.class);
        PaymentTransactionRepository paymentTransactionDAO = mock(PaymentTransactionRepository.class);
        CartRepository cartDAO = mock(CartRepository.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        OrderSignRepository orderSignDAO = mock(OrderSignRepository.class);
        CertificateRepository certificateDAO = mock(CertificateRepository.class);
        PromotionRepository promotionDAO = mock(PromotionRepository.class);

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

        when(productDAO.findForUpdateById(11)).thenReturn(latestProduct);
        when(productDAO.reserveStockAmbient(11, 2)).thenReturn(true);
        when(orderDAO.saveOrder(any(Order.class))).thenReturn(901);
        when(orderDAO.saveOrderItem(any())).thenReturn(true);
        when(paymentTransactionDAO.saveTx(any())).thenReturn(77);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, orderSignDAO, certificateDAO, promotionDAO
        );

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

        ArgumentCaptor<PaymentTransaction> transactionCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(paymentTransactionDAO).saveTx(transactionCaptor.capture());
        PaymentTransaction transaction = transactionCaptor.getValue();
        assertEquals("VNPAY", transaction.getProviderKey());
        assertEquals("CREATED", transaction.getStatus());
        assertEquals("PENDING", transaction.getVerificationStatus());
    }

    @Test
    void processCheckoutPersistsOrderItemProductSnapshot() throws Exception {
        ProductRepository productDAO = mock(ProductRepository.class);
        UserRepository userDAO = mock(UserRepository.class);
        CouponRepository couponDao = mock(CouponRepository.class);
        OrderRepository orderDAO = mock(OrderRepository.class);
        PaymentTransactionRepository paymentTransactionDAO = mock(PaymentTransactionRepository.class);
        CartRepository cartDAO = mock(CartRepository.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        OrderSignRepository orderSignDAO = mock(OrderSignRepository.class);
        CertificateRepository certificateDAO = mock(CertificateRepository.class);
        PromotionRepository promotionDAO = mock(PromotionRepository.class);

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

        when(productDAO.findForUpdateById(11)).thenReturn(latestProduct);
        when(productDAO.reserveStockAmbient(11, 2)).thenReturn(true);
        when(orderDAO.saveOrder(any(Order.class))).thenReturn(901);
        when(orderDAO.saveOrderItem(any())).thenReturn(true);
        when(paymentTransactionDAO.saveTx(any())).thenReturn(77);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, orderSignDAO, certificateDAO, promotionDAO
        );

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

        ArgumentCaptor<com.petshop.model.OrderItem> itemCaptor = ArgumentCaptor.forClass(com.petshop.model.OrderItem.class);
        verify(orderDAO).saveOrderItem(itemCaptor.capture());
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
