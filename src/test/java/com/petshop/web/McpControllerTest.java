package com.petshop.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class McpControllerTest {

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new McpController(
                org.mockito.Mockito.mock(services.ai.PetShopCommerceBackend.class),
                org.mockito.Mockito.mock(com.petshop.repository.PromotionRepository.class),
                org.mockito.Mockito.mock(com.petshop.repository.OrderRepository.class),
                org.mockito.Mockito.mock(com.petshop.repository.ReportRepository.class),
                org.mockito.Mockito.mock(com.petshop.repository.ProductRepository.class))).build();
    }

    @Test
    void toolsListReturnsJsonRpcEnvelope() throws Exception {
        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"jsonrpc\":\"2.0\"")))
                .andExpect(content().string(containsString("\"tools\"")));
    }

    @Test
    void unknownMethodReturns32601() throws Exception {
        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"nope\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("-32601")));
    }

    @Test
    void merchantToolWithoutAdminIsRefused() throws Exception {
        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\","
                                + "\"params\":{\"name\":\"get_business_snapshot\",\"arguments\":{}}}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin session required")));
    }
}
