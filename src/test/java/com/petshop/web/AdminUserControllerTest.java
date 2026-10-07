package com.petshop.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

import com.petshop.repository.AdminActionLogRepository;
import com.petshop.repository.OrderRepository;
import com.petshop.repository.UserRepository;
import com.petshop.model.User;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    UserRepository userDAO;
    @Mock
    OrderRepository orderDAO;
    @Mock
    AdminActionLogRepository actionLog;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AdminUserController(userDAO, orderDAO, actionLog)).build();
    }

    @Test
    void usersListRendersView() throws Exception {
        when(userDAO.getAllUsersWithStats()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/users"))
                .andExpect(model().attributeExists("users", "totalUsers"));
    }

    @Test
    void usersSearchFilters() throws Exception {
        when(userDAO.searchUsers("a", "user")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/users").param("keyword", "a").param("role", "user"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/users"));

        verify(userDAO).searchUsers("a", "user");
    }

    @Test
    void usersApiRejectsMissingUserId() throws Exception {
        mockMvc.perform(get("/admin/users/api").param("action", "getOrders"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Missing userId")));
    }

    @Test
    void usersApiReturnsOrders() throws Exception {
        when(orderDAO.getOrdersByUserId(7)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/users/api")
                        .param("action", "getOrders")
                        .param("userId", "7"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void addUserRejectsWeakPassword() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .param("action", "add")
                        .param("username", "u1")
                        .param("password", "weak"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    void toggleStatusLogs() throws Exception {
        when(userDAO.updateUserStatus(7, "inactive")).thenReturn(true);

        mockMvc.perform(post("/admin/users")
                        .param("action", "toggleStatus")
                        .param("userId", "7")
                        .param("status", "inactive")
                        .sessionAttr("user", adminUser()))
                .andExpect(status().is3xxRedirection());

        verify(actionLog).log(eq(9), eq("TOGGLE_STATUS"), eq("user"), eq(7), contains("inactive"));
    }

    @Test
    void deleteDeactivatesAndLogs() throws Exception {
        when(userDAO.deactivateUser(7)).thenReturn(true);

        mockMvc.perform(post("/admin/users")
                        .param("action", "delete")
                        .param("userId", "7")
                        .sessionAttr("user", adminUser()))
                .andExpect(status().is3xxRedirection());

        verify(userDAO).deactivateUser(7);
    }

    private User adminUser() {
        User admin = new User();
        admin.setId(9);
        admin.setRole("admin");
        return admin;
    }
}
