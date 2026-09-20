package com.petshop.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.AiChatMessageDAO;
import DAO.AiChatSessionDAO;
import DAO.AiSupportSettingDAO;
import DAO.CustomerSupportKnowledgeDAO;
import DAO.NotificationDAO;
import Model.AiChatSession;
import Model.User;

@ExtendWith(MockitoExtension.class)
class AdminAiSupportControllerTest {

    @Mock
    AiChatSessionDAO sessionDAO;
    @Mock
    AiChatMessageDAO messageDAO;
    @Mock
    CustomerSupportKnowledgeDAO knowledgeDAO;
    @Mock
    AiSupportSettingDAO settingDAO;
    @Mock
    NotificationDAO notificationDAO;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminAiSupportController(
                sessionDAO, messageDAO, knowledgeDAO, settingDAO, notificationDAO)).build();
    }

    @Test
    void mainPageRendersView() throws Exception {
        mockMvc.perform(get("/admin/ai-support"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/ai-support"));
    }

    @Test
    void dashboardReturnsMetrics() throws Exception {
        AiChatSession s = new AiChatSession();
        s.setId(1);
        s.setStatus("OPEN");
        s.setNeedAdminSupport(false);
        s.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        when(sessionDAO.getSessionsForAdmin()).thenReturn(List.of(s));
        when(messageDAO.getMessagesBySessionId(1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/ai-support/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("totalChatsToday")));
    }

    @Test
    void sessionsListReturnsJson() throws Exception {
        when(sessionDAO.getSessionsForAdmin()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/ai-support/sessions"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void replyForbidsNonAdmin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User user = new User();
        user.setId(3);
        user.setRole("user");
        session.setAttribute("user", user);

        mockMvc.perform(post("/admin/ai-support/sessions/reply")
                        .param("sessionId", "1")
                        .param("message", "hi")
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void replyPersistsAdminMessage() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", adminUser());

        mockMvc.perform(post("/admin/ai-support/sessions/reply")
                        .param("sessionId", "1")
                        .param("message", "hi")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"success\":true")));
    }

    @Test
    void settingsRoundTrip() throws Exception {
        when(settingDAO.getAllSettings()).thenReturn(java.util.Map.of("A", "1"));
        when(settingDAO.updateSetting("A", "2")).thenReturn(true);

        mockMvc.perform(get("/admin/ai-support/settings"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"A\"")));

        mockMvc.perform(post("/admin/ai-support/settings")
                        .param("settingKey", "A")
                        .param("settingValue", "2")
                        .sessionAttr("user", adminUser()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"success\":true")));
    }

    private User adminUser() {
        User admin = new User();
        admin.setId(9);
        admin.setRole("admin");
        return admin;
    }
}
