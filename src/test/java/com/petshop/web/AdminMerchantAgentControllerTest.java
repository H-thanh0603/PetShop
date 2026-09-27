package com.petshop.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import Model.User;
import Util.Json;
import services.ai.common.MemoryService;
import services.ai.merchant.MerchantAgent;
import services.ai.merchant.MerchantChangeDAO;
import services.ai.merchant.PetShopMerchantBackend;

@ExtendWith(MockitoExtension.class)
class AdminMerchantAgentControllerTest {

    @Mock
    MerchantAgent agent;
    @Mock
    PetShopMerchantBackend backend;
    @Mock
    MerchantChangeDAO changeDAO;
    @Mock
    MemoryService memory;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AdminMerchantAgentController(agent, backend, changeDAO, memory)).build();
    }

    @Test
    void mainPageRendersView() throws Exception {
        mockMvc.perform(get("/admin/ai-merchant"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/ai-merchant"));
    }

    @Test
    void pendingReturnsJson() throws Exception {
        when(changeDAO.pendingJson()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/ai-merchant/pending"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void digestReturnsText() throws Exception {
        when(agent.digest()).thenReturn("morning digest");

        mockMvc.perform(get("/admin/ai-merchant/digest"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("morning digest")));
    }

    @Test
    void escalationsReturnsQueue() throws Exception {
        mockMvc.perform(get("/admin/ai-merchant/escalations"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("escalations")));
    }

    @Test
    void memoryRequiresSubject() throws Exception {
        mockMvc.perform(get("/admin/ai-merchant/memory"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void memoryReturnsFacts() throws Exception {
        when(memory.factsJson("user:1")).thenReturn(Json.MAPPER.createArrayNode());

        mockMvc.perform(get("/admin/ai-merchant/memory").param("subject", "user:1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("facts")));
    }

    @Test
    void chatRejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/admin/ai-merchant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chatRunsAgent() throws Exception {
        when(agent.run(eq("hello"), anyList(), anyString())).thenReturn(
                new MerchantAgent.MerchantResult("hi", "p", "m", "r", 1L, Json.MAPPER.createArrayNode()));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", adminUser());

        mockMvc.perform(post("/admin/ai-merchant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"answer\":\"hi\"")));
    }

    @Test
    void approveMarksApproved() throws Exception {
        mockMvc.perform(post("/admin/ai-merchant/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"changeId\":\"c1\"}")
                        .sessionAttr("user", adminUser()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"approved\":true")));

        verify(changeDAO).markApproved(eq("c1"), anyString());
    }

    @Test
    void applyAppliesChange() throws Exception {
        mockMvc.perform(post("/admin/ai-merchant/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"changeId\":\"c1\"}")
                        .sessionAttr("user", adminUser()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"applied\":true")));

        verify(backend).apply(eq("c1"), anyString(), eq(true));
    }

    @Test
    void discardDiscardsChange() throws Exception {
        mockMvc.perform(post("/admin/ai-merchant/discard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"changeId\":\"c1\"}")
                        .sessionAttr("user", adminUser()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"discarded\":true")));

        verify(backend).discard(eq("c1"), anyString());
    }

    @Test
    void deleteMemoryPurges() throws Exception {
        mockMvc.perform(delete("/admin/ai-merchant/memory")
                        .param("subject", "user:1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("purged")));
    }

    private User adminUser() {
        User admin = new User();
        admin.setId(9);
        admin.setRole("admin");
        return admin;
    }
}
