package com.petshop.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import DAO.ProductDAO;
import Model.Product;
import Model.User;

@ExtendWith(MockitoExtension.class)
class ShopApiControllerTest {

    @Mock
    ProductDAO productDAO;
    @Mock
    OrderDAO orderDAO;
    @Mock
    PaymentTransactionDAO paymentTransactionDAO;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ShopApiController(productDAO, orderDAO, paymentTransactionDAO))
                .build();
    }

    @Test
    void emptyQueryReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/search-autocomplete"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void autocompleteReturnsProducts() throws Exception {
        Product p = new Product(1, "Hat", "hat.jpg", new BigDecimal("100000"), 0, "desc");
        when(productDAO.searchProductsLimit("hat", 8)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/search-autocomplete").param("q", "hat"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"name\":\"Hat\"")));
    }

    @Test
    void addReviewRequiresLogin() throws Exception {
        mockMvc.perform(post("/add-review").param("productId", "1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void vnpayMismatchedAmountDoesNotMarkOrderPaid() throws Exception {
        Model.Order order = new Model.Order();
        order.setId(456);
        order.setTotalAmount(new BigDecimal("258000"));
        when(orderDAO.getOrderById(456)).thenReturn(order);

        try (var mockedVnpay = org.mockito.Mockito.mockStatic(Util.VnpayUtil.class)) {
            mockedVnpay.when(() -> Util.VnpayUtil.verifyReturn(
                    org.mockito.ArgumentMatchers.any())).thenReturn(true);

            // vnp_Amount=10000 -> 100.00 VND vs order 258000 -> mismatch page
            mockMvc.perform(get("/vnpay-return")
                            .param("vnp_TxnRef", "456")
                            .param("vnp_ResponseCode", "00")
                            .param("vnp_TransactionStatus", "00")
                            .param("vnp_Amount", "10000"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pages/shop/payment-failed"));
        }

        org.mockito.Mockito.verify(orderDAO, org.mockito.Mockito.never())
                .markOnlinePaymentPaidAndFinalize(456, "VNPAY");
    }

    @Test
    void vnpaySuccessMarksOrderPaid() throws Exception {
        Model.Order order = new Model.Order();
        order.setId(456);
        order.setTotalAmount(new BigDecimal("258000"));
        when(orderDAO.getOrderById(456)).thenReturn(order);
        when(paymentTransactionDAO.updateLatestProviderResultForOrder(
                org.mockito.ArgumentMatchers.eq(456),
                org.mockito.ArgumentMatchers.eq("VNPAY"),
                org.mockito.ArgumentMatchers.eq("VNPAY_TXN_789"),
                org.mockito.ArgumentMatchers.argThat(a -> a != null && a.compareTo(new BigDecimal("258000")) == 0),
                org.mockito.ArgumentMatchers.eq("responseCode=00;transactionStatus=00;bankCode=;payDate="),
                org.mockito.ArgumentMatchers.eq("VERIFIED"),
                org.mockito.ArgumentMatchers.eq("VERIFIED"),
                org.mockito.ArgumentMatchers.eq("VNPAY payment verified."))).thenReturn(true);
        when(orderDAO.markOnlinePaymentPaidAndFinalize(456, "VNPAY")).thenReturn(true);

        try (var mockedVnpay = org.mockito.Mockito.mockStatic(Util.VnpayUtil.class)) {
            mockedVnpay.when(() -> Util.VnpayUtil.verifyReturn(
                    org.mockito.ArgumentMatchers.any())).thenReturn(true);

            mockMvc.perform(get("/vnpay-return")
                            .param("vnp_TxnRef", "456")
                            .param("vnp_ResponseCode", "00")
                            .param("vnp_TransactionStatus", "00")
                            .param("vnp_TransactionNo", "VNPAY_TXN_789")
                            .param("vnp_Amount", "25800000"))
                    .andExpect(status().is3xxRedirection());
        }

        org.mockito.Mockito.verify(orderDAO).markOnlinePaymentPaidAndFinalize(456, "VNPAY");
    }

    @Test
    void addReviewRejectsBlankComment() throws Exception {
        User user = new User();
        user.setId(7);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user);

        try (var mocked = org.mockito.Mockito.mockConstruction(DAO.ReviewDAO.class,
                (dao, ctx) -> {
                    when(dao.hasUserPurchasedProduct(7, 1)).thenReturn(true);
                    when(dao.hasUserReviewedProduct(7, 1)).thenReturn(false);
                })) {
            mockMvc.perform(post("/add-review")
                            .param("productId", "1")
                            .param("rating", "5")
                            .param("comment", "   ")
                            .session(session))
                    .andExpect(status().is3xxRedirection());
        }
    }
}
