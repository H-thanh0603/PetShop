package com.petshop.web;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import tools.jackson.databind.node.ObjectNode;

import DAO.AdminActionLogDAO;
import DAO.NotificationDAO;
import DAO.OrderDAO;
import Model.Order;
import Model.User;
import Util.Json;
import services.ShippingService;

@ExtendWith(MockitoExtension.class)
class AdminOrderControllerTest {

    @Mock
    AdminActionLogDAO actionLog;
    @Mock
    OrderDAO orderDAO;
    @Mock
    ShippingService shippingService;
    @Mock
    NotificationDAO notificationDAO;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AdminOrderController(actionLog, orderDAO, shippingService, notificationDAO)).build();
    }

    @Test
    void ordersListRendersView() throws Exception {
        when(orderDAO.getOrdersPage(1, 20, null, null)).thenReturn(Collections.emptyList());
        when(orderDAO.countOrders(null, null)).thenReturn(0);

        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/orders"))
                .andExpect(model().attributeExists("orders", "totalPages"));
    }

    @Test
    void orderDetailRendersView() throws Exception {
        Order order = new Order();
        order.setId(5);
        when(orderDAO.getOrderById(5)).thenReturn(order);

        mockMvc.perform(get("/admin/orders").param("action", "view").param("id", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/order-detail"))
                .andExpect(model().attributeExists("order", "statusHistory"));
    }

    @Test
    void orderDetailRejectsBadId() throws Exception {
        mockMvc.perform(get("/admin/orders").param("action", "view").param("id", "abc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"));
    }

    @Test
    void updateStatusPersistsAndLogs() throws Exception {
        Order existing = new Order();
        existing.setId(7);
        existing.setUserId(3);
        existing.setStatus("Pending");
        when(orderDAO.getOrderById(7)).thenReturn(existing);
        when(orderDAO.updateStatus(7, "Confirmed", 9)).thenReturn(true);

        mockMvc.perform(post("/admin/orders")
                        .param("action", "updateStatus")
                        .param("orderId", "7")
                        .param("status", "Confirmed")
                        .sessionAttr("user", adminUser()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"));

        verify(actionLog).log(eq(9), eq("UPDATE_ORDER_STATUS"), eq("order"), eq(7), contains("Confirmed"));
    }

    @Test
    void updatePaymentVerificationPersistsAndRedirectsToDetail() throws Exception {
        when(orderDAO.updatePaymentVerification(456, "VERIFIED", "Đã khớp sao kê")).thenReturn(true);

        mockMvc.perform(post("/admin/orders")
                        .param("action", "updatePaymentVerification")
                        .param("orderId", "456")
                        .param("verificationStatus", "VERIFIED")
                        .param("verificationMessage", "Đã khớp sao kê")
                        .param("returnTo", "detail")
                        .sessionAttr("user", adminUser()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders?action=view&id=456"));

        verify(orderDAO).updatePaymentVerification(456, "VERIFIED", "Đã khớp sao kê");
        verify(actionLog).log(eq(9), eq("UPDATE_PAYMENT_VERIFICATION"), eq("order"), eq(456), contains("VERIFIED"));
    }

    @Test
    void updatePaymentVerificationRejectsBadId() throws Exception {
        mockMvc.perform(post("/admin/orders")
                        .param("action", "updatePaymentVerification")
                        .param("orderId", "abc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"));
    }

    @Test
    void shipperCannotSetNonShippingStatus() throws Exception {
        User shipper = new User();
        shipper.setId(4);
        shipper.setRole("shipper");

        mockMvc.perform(post("/admin/orders")
                        .param("action", "updateStatus")
                        .param("orderId", "7")
                        .param("status", "Cancelled")
                        .sessionAttr("user", shipper))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"));
    }

    @Test
    void pushToGhnSuccess() throws Exception {
        Order order = new Order();
        order.setId(7);
        when(orderDAO.getOrderById(7)).thenReturn(order);
        ObjectNode ghn = Json.MAPPER.createObjectNode();
        ghn.put("order_code", "GHN1");
        ghn.put("sort_code", "S1");
        when(shippingService.createGhnOrder(order)).thenReturn(ghn);

        mockMvc.perform(post("/admin/orders")
                        .param("action", "pushToGhn")
                        .param("orderId", "7")
                        .sessionAttr("user", adminUser()))
                .andExpect(status().is3xxRedirection());

        verify(orderDAO).updateGhnInfo(eq(7), eq("GHN1"), eq("S1"), anyString(), eq(null));
    }

    @Test
    void syncGhnStatusMapsToLocal() throws Exception {
        Order order = new Order();
        order.setId(7);
        order.setStatus("Shipping");
        order.setGhnOrderId("GHN1");
        when(orderDAO.getOrderById(7)).thenReturn(order);
        when(shippingService.syncGhnStatus("GHN1")).thenReturn("delivered");

        mockMvc.perform(post("/admin/orders")
                        .param("action", "syncGhnStatus")
                        .param("orderId", "7")
                        .sessionAttr("user", adminUser()))
                .andExpect(status().is3xxRedirection());

        verify(orderDAO).updateGhnStatus(eq(7), eq("delivered"), eq(null));
    }

    private User adminUser() {
        User admin = new User();
        admin.setId(9);
        admin.setRole("admin");
        return admin;
    }
}
