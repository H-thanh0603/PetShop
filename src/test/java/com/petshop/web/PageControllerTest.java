package com.petshop.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.PromotionDAO;
import Model.Product;

@ExtendWith(MockitoExtension.class)
class PageControllerTest {

    @Mock
    PromotionDAO promotionDAO;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PageController(promotionDAO)).build();
    }

    @Test
    void aboutRendersView() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/main/about"));
    }

    @Test
    void homeExposesFlashSale() throws Exception {
        Product p = new Product(1, "Hat", "hat.jpg", new BigDecimal("100000"), 0, "desc");
        when(promotionDAO.getFlashSaleProducts(8)).thenReturn(List.of(p));
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/main/home"))
                .andExpect(model().attributeExists("flashSaleProducts"));
    }

    @Test
    void policyRoutesFillModel() throws Exception {
        mockMvc.perform(get("/terms"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/main/policy"))
                .andExpect(model().attributeExists("policyTitle", "policyLead", "policySections"));
    }
}
