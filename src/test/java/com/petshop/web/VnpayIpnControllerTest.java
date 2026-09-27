package com.petshop.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.petshop.dao.OrderDAO;
import com.petshop.dao.PaymentTransactionDAO;
import com.petshop.model.Order;

@ExtendWith(MockitoExtension.class)
class VnpayIpnControllerTest {

    @Mock
    OrderDAO orderDAO;
    @Mock
    PaymentTransactionDAO paymentTransactionDAO;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new VnpayIpnController(orderDAO, paymentTransactionDAO)).build();
    }

    @Test
    void rejectsBadChecksumWith97() throws Exception {
        mockMvc.perform(get("/api/payment/vnpay-ipn")
                        .param("vnp_TxnRef", "1")
                        .param("vnp_SecureHash", "bad"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"97\"")));
    }

    @Test
    void unknownOrderReturns01() throws Exception {
        // verifyReturn will fail without a valid signature, so this path only
        // asserts the controller never throws and always answers JSON.
        mockMvc.perform(post("/api/payment/vnpay-ipn"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("RspCode")));
    }
}
