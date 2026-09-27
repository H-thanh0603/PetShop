package com.petshop.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.petshop.repository.AiChatMessageRepository;
import com.petshop.repository.AiChatSessionRepository;
import com.petshop.repository.AiSupportSettingRepository;
import com.petshop.model.AiChatSession;
import com.petshop.model.User;
import services.DeepSeekService;

@ExtendWith(MockitoExtension.class)
class UserAiSupportControllerTest {

    @Mock
    AiChatSessionRepository sessionDAO;
    @Mock
    AiChatMessageRepository messageDAO;
    @Mock
    AiSupportSettingRepository settingDAO;
    @Mock
    DeepSeekService deepSeekService;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserAiSupportController(
                sessionDAO, messageDAO, settingDAO, deepSeekService)).build();
    }

    @Test
    void historyReturnsEmptyForGuestWithoutSession() throws Exception {
        mockMvc.perform(get("/ai-support/history"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void historyReturnsUserSessions() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User user = new User();
        user.setId(7);
        session.setAttribute("user", user);

        AiChatSession s = new AiChatSession();
        s.setId(1);
        s.setUserId(7);
        s.setStatus("OPEN");
        s.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        when(sessionDAO.getSessionsByUserId(7)).thenReturn(List.of(s));
        when(messageDAO.getRecentMessagesBySessionId(1, 1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/ai-support/history").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"sessionId\":1")));
    }

    @Test
    void messagesRejectsMissingSessionId() throws Exception {
        mockMvc.perform(get("/ai-support/messages"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void messagesForbidsForeignSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User user = new User();
        user.setId(7);
        session.setAttribute("user", user);

        AiChatSession foreign = new AiChatSession();
        foreign.setId(9);
        foreign.setUserId(999);
        when(sessionDAO.findById(9)).thenReturn(java.util.Optional.of(foreign));

        mockMvc.perform(get("/ai-support/messages").param("sessionId", "9").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void chatRejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/ai-support/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chatCreatesSessionAndAnswers() throws Exception {
        when(settingDAO.getSetting("MAX_MESSAGE_LENGTH", "1000")).thenReturn("1000");
        when(settingDAO.getSetting("AI_MAX_MSGS_PER_SESSION", "60")).thenReturn("60");
        when(settingDAO.getSetting("AUTO_ESCALATE_TO_ADMIN", "true")).thenReturn("true");
        when(sessionDAO.create(any())).thenReturn(42);
        when(messageDAO.getRecentMessagesBySessionId(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        DeepSeekService.AiResponse aiRes = new DeepSeekService.AiResponse();
        aiRes.setAnswer("Xin chao");
        aiRes.setIntent("GREETING");
        aiRes.setConfidence(0.9);
        when(deepSeekService.getChatResponse(any(), any(), any(), any())).thenReturn(aiRes);

        mockMvc.perform(post("/ai-support/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"sessionId\":42")))
                .andExpect(content().string(containsString("Xin chao")));
    }
}
