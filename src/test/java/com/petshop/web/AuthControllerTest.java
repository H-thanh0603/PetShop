package com.petshop.web;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.RememberTokenDAO;
import Model.User;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    RememberTokenDAO rememberTokenDAO;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(rememberTokenDAO)).build();
    }

    @Test
    void logoutDeletesTokensAndRedirectsHome() throws Exception {
        User user = new User();
        user.setId(7);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user);

        mockMvc.perform(get("/logout").session(session))
                .andExpect(status().is3xxRedirection());

        verify(rememberTokenDAO).deleteAllTokensForUser(7);
    }

    @Test
    void logoutAnonymousStillRedirects() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().is3xxRedirection());
    }
}
