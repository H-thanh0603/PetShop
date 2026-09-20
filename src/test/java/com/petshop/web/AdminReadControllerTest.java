package com.petshop.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.PetTypeDAO;
import DAO.ReportDAO;

@ExtendWith(MockitoExtension.class)
class AdminReadControllerTest {

    @Mock
    ReportDAO reportDAO;
    @Mock
    PetTypeDAO petTypeDAO;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminReadController(reportDAO, petTypeDAO)).build();
    }

    @Test
    void dashboardRendersView() throws Exception {
        when(reportDAO.getOverviewStats()).thenReturn(Map.of("orders", 1));
        when(reportDAO.getRecentOrders(5)).thenReturn(Collections.emptyList());
        when(reportDAO.getLowStockProducts(10, 5)).thenReturn(Collections.emptyList());
        when(reportDAO.getRecentReviews(5)).thenReturn(Collections.emptyList());
        when(reportDAO.getTopSellingProducts(5)).thenReturn(Collections.emptyList());
        when(reportDAO.getRevenueByMonth(org.mockito.ArgumentMatchers.anyInt())).thenReturn(Collections.emptyList());
        when(reportDAO.getOrdersByStatus()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/pages/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/dashboard"))
                .andExpect(model().attributeExists("overview", "revenueByMonthJson"));
    }

    @Test
    void categoriesRendersView() throws Exception {
        when(petTypeDAO.getAllPetTypes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/pages/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/categories"))
                .andExpect(model().attributeExists("categories", "petTypes"));
    }

    @Test
    void categoriesRejectsInvalidRename() throws Exception {
        mockMvc.perform(post("/pages/admin/categories")
                        .param("action", "rename")
                        .param("oldName", "A")
                        .param("newName", ""))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void petTypesRendersView() throws Exception {
        when(petTypeDAO.getAllPetTypes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/pages/admin/pet-types"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/pet-types"))
                .andExpect(model().attributeExists("petTypes"));
    }

    @Test
    void petTypesRejectsBlankAdd() throws Exception {
        mockMvc.perform(post("/pages/admin/pet-types")
                        .param("action", "add")
                        .param("code", "")
                        .param("name", ""))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void reportsRendersView() throws Exception {
        when(reportDAO.getOverviewStats()).thenReturn(Map.of("orders", 1));
        when(reportDAO.getTopSellingProducts(10)).thenReturn(Collections.emptyList());
        when(reportDAO.getTopCustomers(10)).thenReturn(Collections.emptyList());
        when(reportDAO.getCouponUsage(10)).thenReturn(Collections.emptyList());
        when(reportDAO.getOrdersByStatus()).thenReturn(Collections.emptyList());
        when(reportDAO.getRevenueByMonth(org.mockito.ArgumentMatchers.anyInt())).thenReturn(Collections.emptyList());
        when(reportDAO.getLowStockProducts(10, 10)).thenReturn(Collections.emptyList());
        when(reportDAO.getRecentLowRatingReviews(10)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/reports"))
                .andExpect(model().attributeExists("overview", "selectedYear"));
    }

    @Test
    void statisticsRendersView() throws Exception {
        when(reportDAO.getOverviewStats()).thenReturn(Map.of("orders", 1));
        when(reportDAO.getRevenueByMonth(org.mockito.ArgumentMatchers.anyInt())).thenReturn(Collections.emptyList());
        when(reportDAO.getTopSellingProducts(5)).thenReturn(Collections.emptyList());
        when(reportDAO.getOrdersByStatus()).thenReturn(Collections.emptyList());
        when(reportDAO.getOrdersByMonthWithStatus(org.mockito.ArgumentMatchers.anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/statistics"))
                .andExpect(model().attributeExists("revenueByMonthJson", "ordersByMonthJson"));
    }

    @Test
    void adminNotificationsRendersView() throws Exception {
        when(reportDAO.getRecentOrders(10)).thenReturn(List.of());
        when(reportDAO.getLowStockProducts(10, 10)).thenReturn(Collections.emptyList());
        when(reportDAO.getRecentLowRatingReviews(10)).thenReturn(Collections.emptyList());
        when(reportDAO.getStoredNotifications(10)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/notifications"))
                .andExpect(model().attributeExists("pendingOrders", "pendingOrderCount"));
    }
}
