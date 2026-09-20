package com.petshop.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.ProductDAO;
import DAO.PromotionDAO;

@ExtendWith(MockitoExtension.class)
class AdminPromotionControllerTest {

    @Mock
    PromotionDAO promotionDAO;
    @Mock
    ProductDAO productDAO;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AdminPromotionController(promotionDAO, productDAO)).build();
    }

    @Test
    void promotionsListRendersView() throws Exception {
        when(promotionDAO.getAllPromotions()).thenReturn(Collections.emptyList());
        when(productDAO.getAllProducts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/promotions"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/promotions"))
                .andExpect(model().attributeExists("promotions", "products"));
    }

    @Test
    void saveRejectsBlankName() throws Exception {
        mockMvc.perform(post("/admin/promotions")
                        .param("name", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/promotions"));

        verify(promotionDAO, org.mockito.Mockito.never()).savePromotion(any());
    }

    @Test
    void savePersistsValidPromotion() throws Exception {
        when(promotionDAO.savePromotion(any())).thenReturn(1);

        mockMvc.perform(post("/admin/promotions")
                        .param("name", "Sale 9.9")
                        .param("discountType", "PERCENT")
                        .param("discountValue", "10")
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-02-01T00:00:00")
                        .param("productIds", "1", "2"))
                .andExpect(status().is3xxRedirection());

        verify(promotionDAO).savePromotion(any());
    }

    @Test
    void toggleFlipsStatus() throws Exception {
        when(promotionDAO.updatePromotionStatus(5, "INACTIVE")).thenReturn(true);

        mockMvc.perform(post("/admin/promotions")
                        .param("action", "toggle")
                        .param("id", "5")
                        .param("currentStatus", "ACTIVE"))
                .andExpect(status().is3xxRedirection());

        verify(promotionDAO).updatePromotionStatus(5, "INACTIVE");
    }

    @Test
    void deleteRemovesPromotion() throws Exception {
        when(promotionDAO.deletePromotion(5)).thenReturn(true);

        mockMvc.perform(post("/admin/promotions")
                        .param("action", "delete")
                        .param("id", "5"))
                .andExpect(status().is3xxRedirection());

        verify(promotionDAO).deletePromotion(5);
    }
}
