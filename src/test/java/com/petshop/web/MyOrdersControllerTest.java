package com.petshop.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
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
import Model.Order;
import Model.User;
import services.ReorderService;

@ExtendWith(MockitoExtension.class)
class MyOrdersControllerTest {

    @Mock
    OrderDAO orderDAO;
    @Mock
    ReorderService reorderService;

    MockMvc mockMvc;
    MockHttpSession authed;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MyOrdersController(orderDAO, reorderService))
                .build();
        User user = new User();
        user.setId(7);
        authed = new MockHttpSession();
        authed.setAttribute("user", user);
    }

    @Test
    void redirectsLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/my-orders"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void rendersOrderList() throws Exception {
        when(orderDAO.getOrdersByUserId(7)).thenReturn(List.of());
        when(orderDAO.getRepurchaseSuggestions(7, 30, 5)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/my-orders").session(authed))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/shop/my-orders"))
                .andExpect(model().attributeExists("orders", "countPending", "countCompleted"));
    }

    @Test
    void viewOwnOrderRendersDetail() throws Exception {
        Order order = new Order();
        order.setId(42);
        order.setUserId(7);
        when(orderDAO.getOrderById(42)).thenReturn(order);

        mockMvc.perform(get("/my-orders").param("action", "view").param("id", "42").session(authed))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/shop/order-detail"));
    }

    @Test
    void viewForeignOrderRedirectsList() throws Exception {
        Order order = new Order();
        order.setId(42);
        order.setUserId(999);
        when(orderDAO.getOrderById(42)).thenReturn(order);

        mockMvc.perform(get("/my-orders").param("action", "view").param("id", "42").session(authed))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void cancelOutsideWindowKeepsOrderAndRedirects() throws Exception {
        when(orderDAO.isWithinCancellationWindow(42)).thenReturn(false);

        mockMvc.perform(post("/my-orders").param("action", "cancel").param("orderId", "42").session(authed))
                .andExpect(status().is3xxRedirection());
    }
}
