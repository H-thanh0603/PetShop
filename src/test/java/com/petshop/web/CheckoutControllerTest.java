package com.petshop.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
import Model.Coupon;
import Model.Product;
import Model.User;
import services.InventoryService;
import services.OrderEmailService;

/**
 * Key-path coverage for CheckoutController (ported from the servlet-era
 * fix-check + preservation tests): never empty body, empty cart, coupon,
 * guards.
 */
@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    CouponDao couponDao;
    @Mock
    AddressDao addressDAO;
    @Mock
    InventoryService inventoryService;
    @Mock
    CartDAO cartDAO;
    @Mock
    ProductDAO productDAO;
    @Mock
    OrderDAO orderDAO;
    @Mock
    PaymentTransactionDAO paymentTransactionDAO;
    @Mock
    UserDAO userDAO;
    @Mock
    OrderEmailService orderEmailService;
    @Mock
    InventoryBatchDAO inventoryBatchDAO;

    MockMvc mockMvc;
    MockHttpSession authed;
    User testUser;
    Address testAddress;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CheckoutController(
                couponDao, addressDAO, inventoryService, cartDAO, productDAO,
                orderDAO, paymentTransactionDAO, userDAO, orderEmailService, inventoryBatchDAO)).build();

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setFullname("Nguyen Van A");
        testUser.setPhone("0901234567");
        testUser.setDiscountUsed(false);

        testAddress = new Address();
        testAddress.setId(1);
        testAddress.setUserId(1);
        testAddress.setDefaultt(true);
        testAddress.setAddress("123 Nguyen Hue");
        testAddress.setProvince("Ho Chi Minh");
        testAddress.setDistrict("Quan 1");
        testAddress.setWard("Phuong Ben Nghe");

        authed = new MockHttpSession();
        authed.setAttribute("user", testUser);

        org.mockito.Mockito.lenient().when(userDAO.getUserById(1)).thenReturn(testUser);
    }

    @Test
    void redirectsLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/checkout"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void emptyCartRedirectsShop() throws Exception {
        when(cartDAO.getCartByUserId(1)).thenReturn(new HashMap<>());
        when(inventoryService.refreshCartProductsWithNotification(any())).thenReturn(List.of());

        mockMvc.perform(get("/checkout").session(authed))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void renderCheckoutExposesModel() throws Exception {
        Product p = new Product(10, "Cat Tree", "cat.jpg", new BigDecimal("600000"), 0, "desc");
        p.setWeight(500);
        p.setStock(5);
        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(10, new CartItem(p, 1));

        when(cartDAO.getCartByUserId(1)).thenReturn(cart);
        when(inventoryService.refreshCartProductsWithNotification(any())).thenReturn(List.of());
        when(inventoryService.validateCartForCheckout(any())).thenReturn(List.of());
        when(addressDAO.getAddressesByUserId(1)).thenReturn(List.of(testAddress));
        when(addressDAO.getDefaultAddressByUserId(1)).thenReturn(testAddress);

        mockMvc.perform(get("/checkout").session(authed))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/shop/checkout"))
                .andExpect(model().attributeExists("cartItems", "finalTotal"));
    }

    @Test
    void applyCouponInvalidKeepsMessage() throws Exception {
        when(couponDao.getValidCouponByCode("BAD")).thenReturn(null);

        mockMvc.perform(post("/checkout")
                        .param("action", "applyCoupon")
                        .param("couponCode", "BAD")
                        .session(authed))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void applyCouponValidStoresCoupon() throws Exception {
        Coupon coupon = new Coupon();
        coupon.setCode("SALE10");
        coupon.setDiscountPercent(10);
        coupon.setQuantity(100);
        coupon.setUsed(0);
        when(couponDao.getValidCouponByCode("SALE10")).thenReturn(coupon);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", testUser);

        mockMvc.perform(post("/checkout")
                        .param("action", "applyCoupon")
                        .param("couponCode", "SALE10")
                        .session(session))
                .andExpect(status().is3xxRedirection());
        org.junit.jupiter.api.Assertions.assertNotNull(session.getAttribute("appliedCoupon"));
    }

    @Test
    void placeOrderEmptyCartReturnsJsonError() throws Exception {
        when(cartDAO.getCartByUserId(1)).thenReturn(new HashMap<>());
        when(inventoryService.refreshCartProductsWithNotification(any())).thenReturn(List.of());

        mockMvc.perform(post("/checkout").session(authed))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"success\":false")));
    }

    @Test
    void placeOrderNeverReturnsEmptyBodyOnError() throws Exception {
        when(cartDAO.getCartByUserId(1)).thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(post("/checkout").session(authed))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"success\":false")));
    }
}
