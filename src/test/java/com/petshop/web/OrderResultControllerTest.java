package com.petshop.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OrderResultControllerTest {

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderResultController()).build();
    }

    @Test
    void redirectsShopWhenNoSessionData() throws Exception {
        mockMvc.perform(get("/order-success"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void rendersViewAndMovesSessionToModel() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("successOrderId", 42);
        session.setAttribute("paymentMethod", "COD");

        mockMvc.perform(get("/order-success").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/shop/orderSuccess"))
                .andExpect(model().attributeExists("orderId", "paymentMethod"));
    }
}
