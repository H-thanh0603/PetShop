package com.petshop.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.petshop.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class GhnWebhookControllerTest {

    @Mock
    OrderRepository orderDAO;

    MockMvc mockMvc;
    GhnWebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new GhnWebhookController(orderDAO);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void headerSecretIsAccepted() {
        System.setProperty("payment.ghn.webhook-secret", "ghn-secret");
        try {
            assertTrue(controller.isAuthorized("ghn-secret", null));
            assertTrue(controller.isAuthorized(null, "ghn-secret"));
            assertFalse(controller.isAuthorized(null, null));
            assertFalse(controller.isAuthorized("wrong", null));
        } finally {
            System.clearProperty("payment.ghn.webhook-secret");
        }
    }

    @Test
    void blankConfiguredSecretRejectsAll() {
        System.clearProperty("payment.ghn.webhook-secret");
        assertFalse(controller.isAuthorized("anything", null));
    }

    @Test
    void unauthorizedWebhookReturns401() throws Exception {
        System.clearProperty("payment.ghn.webhook-secret");
        mockMvc.perform(post("/api/ghn/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"order_code\":\"A\",\"status\":\"delivering\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingFieldsReturns400() throws Exception {
        System.setProperty("payment.ghn.webhook-secret", "s");
        try {
            mockMvc.perform(post("/api/ghn/webhook")
                            .param("secret", "s")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"order_code\":\"A\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(Matchers.containsString("Missing order_code")));
        } finally {
            System.clearProperty("payment.ghn.webhook-secret");
        }
    }
}
