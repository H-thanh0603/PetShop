package com.petshop.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.petshop.repository.CertificateRepository;
import com.petshop.dao.OrderDAO;
import com.petshop.repository.OrderSignRepository;
import com.petshop.repository.OrderSignatureRepository;
import com.petshop.model.OrderSign;
import com.petshop.model.User;

@ExtendWith(MockitoExtension.class)
class SignatureControllerTest {

    @Mock
    OrderSignRepository orderSignDAO;
    @Mock
    OrderSignatureRepository orderSignatureDAO;
    @Mock
    CertificateRepository certificateDAO;
    @Mock
    OrderDAO orderDAO;

    MockMvc mockMvc;
    MockHttpSession authed;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new SignatureController(orderSignDAO, orderSignatureDAO, certificateDAO, orderDAO)).build();
        User user = new User();
        user.setId(7);
        authed = new MockHttpSession();
        authed.setAttribute("user", user);
    }

    @Test
    void uploadRequiresLogin() throws Exception {
        mockMvc.perform(post("/user/upload-signature").param("orderId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"success\":false")));
    }

    @Test
    void uploadRejectsMissingOrderId() throws Exception {
        mockMvc.perform(post("/user/upload-signature").session(authed))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Thiếu orderId")));
    }

    @Test
    void uploadRejectsUnknownOrder() throws Exception {
        when(orderSignDAO.findByOrderId(99)).thenReturn(null);

        mockMvc.perform(post("/user/upload-signature")
                        .param("orderId", "99")
                        .param("signature", "abc")
                        .session(authed))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Không tìm thấy dữ liệu ký")));
    }

    @Test
    void downloadRequiresLogin() throws Exception {
        mockMvc.perform(get("/user/download-private-key").param("orderId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void downloadRejectsForeignOrder() throws Exception {
        OrderSign sign = org.mockito.Mockito.mock(OrderSign.class);
        when(sign.getUserId()).thenReturn(999);
        when(orderSignDAO.findByOrderId(1)).thenReturn(sign);

        mockMvc.perform(get("/user/download-private-key").param("orderId", "1").session(authed))
                .andExpect(status().isForbidden());
    }

    @Test
    void downloadReturnsKeyBytes() throws Exception {
        OrderSign sign = org.mockito.Mockito.mock(OrderSign.class);
        when(sign.getUserId()).thenReturn(7);
        when(sign.getPrivateKey()).thenReturn(Base64.getEncoder().encodeToString(new byte[]{1, 2, 3}));
        when(orderSignDAO.findByOrderId(1)).thenReturn(sign);

        mockMvc.perform(get("/user/download-private-key").param("orderId", "1").session(authed))
                .andExpect(status().isOk());
    }
}
