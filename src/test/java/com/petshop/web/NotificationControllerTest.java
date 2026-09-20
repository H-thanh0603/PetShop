package com.petshop.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import DAO.NotificationDAO;
import Model.User;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    NotificationDAO notificationDAO;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new NotificationController(notificationDAO)).build();
    }

    @Test
    void guestUnreadCountIsZero() throws Exception {
        mockMvc.perform(get("/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"unreadCount\":0")));
    }

    @Test
    void guestListIsEmptyArray() throws Exception {
        mockMvc.perform(get("/notifications/list"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void markReadRequiresLogin() throws Exception {
        mockMvc.perform(post("/notifications/mark-read"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUnreadCount() throws Exception {
        User user = new User();
        user.setId(7);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user);
        when(notificationDAO.getUnreadCountByUserId(7)).thenReturn(3);

        mockMvc.perform(get("/notifications/unread-count").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"unreadCount\":3")));
    }

    @Test
    void authenticatedList() throws Exception {
        User user = new User();
        user.setId(7);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user);
        when(notificationDAO.getNotificationsByUserId(7, 10)).thenReturn(List.of(Map.of("id", 1)));

        mockMvc.perform(get("/notifications/list").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"id\":1")));
    }

    @Test
    void markReadSuccess() throws Exception {
        User user = new User();
        user.setId(7);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user);
        when(notificationDAO.markAllAsRead(7)).thenReturn(true);

        mockMvc.perform(post("/notifications/mark-read").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"success\":true")));
    }
}
