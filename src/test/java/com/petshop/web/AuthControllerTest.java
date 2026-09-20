package com.petshop.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.RememberTokenDAO;
import DAO.UserDAO;
import Model.User;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    RememberTokenDAO rememberTokenDAO;
    @Mock
    UserDAO userDAO;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(rememberTokenDAO, userDAO)).build();
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

    @Test
    void loginPageRendersView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/login"));
    }

    @Test
    void loginWrongPasswordShowsError() throws Exception {
        when(userDAO.loginByEmail(
                org.mockito.ArgumentMatchers.eq("user@example.com"),
                org.mockito.ArgumentMatchers.anyString())).thenReturn(null);

        mockMvc.perform(post("/login")
                        .param("email", "user@example.com")
                        .param("password", "Wrongpass1!"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/login"))
                .andExpect(request().attribute("error", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void loginSuccessRedirectsHome() throws Exception {
        User user = new User();
        user.setId(7);
        user.setUsername("tester");
        user.setStatus(true);
        when(userDAO.loginByEmail(
                org.mockito.ArgumentMatchers.eq("user@example.com"),
                org.mockito.ArgumentMatchers.anyString())).thenReturn(user);

        try (var mocked = org.mockito.Mockito.mockConstruction(DAO.CartDAO.class,
                (dao, ctx) -> when(dao.getCartByUserId(7))
                        .thenReturn(new java.util.HashMap<>()))) {
            mockMvc.perform(post("/login")
                            .param("email", "user@example.com")
                            .param("password", "Goodpass1!"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Test
    void registerPageRendersView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/register"));
    }

    @Test
    void registerCheckUsernameReportsAvailability() throws Exception {
        when(userDAO.checkUsernameExists("taken")).thenReturn(true);
        when(userDAO.checkUsernameExists("free_name")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("action", "checkUsername")
                        .param("username", "taken"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"available\":false")));

        mockMvc.perform(post("/register")
                        .param("action", "checkUsername")
                        .param("username", "free_name"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"available\":true")));
    }

    @Test
    void registerRejectsWeakPassword() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "new_user")
                        .param("email", "newuser@example.com")
                        .param("fullName", "Nguyen Van A")
                        .param("password", "weak")
                        .param("confirmPassword", "weak")
                        .param("otp", "123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/register"));
    }

    @Test
    void verifyEmailValidTokenMarksVerifiedAndRedirectsLogin() throws Exception {
        User user = new User();
        user.setId(9);
        when(userDAO.getUserByVerificationToken("tok")).thenReturn(user);

        mockMvc.perform(get("/verify-email").param("token", "tok"))
                .andExpect(status().is3xxRedirection());
        verify(userDAO).markEmailVerified(9);
    }

    @Test
    void verifyEmailExpiredTokenShowsExpiry() throws Exception {
        User expired = new User();
        expired.setEmail("a@b.c");
        when(userDAO.getUserByVerificationToken("tok")).thenReturn(null);
        when(userDAO.getUserByExpiredVerificationToken("tok")).thenReturn(expired);

        mockMvc.perform(get("/verify-email").param("token", "tok"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/verify-email"))
                .andExpect(model().attributeExists("verifyError", "expiredEmail"));
    }

    @Test
    void verifyOtpPageRequiresResetEmail() throws Exception {
        mockMvc.perform(get("/verify-otp"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void resetPasswordPageRequiresVerifiedOtp() throws Exception {
        mockMvc.perform(get("/reset-password"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void resetPasswordRejectsWeakPassword() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("resetEmail", "a@b.c");
        session.setAttribute("otpVerified", true);

        mockMvc.perform(post("/reset-password")
                        .param("password", "weak")
                        .param("confirmPassword", "weak")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/reset-password"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void unknownEmailShowsGenericSuccessWithoutLeakingAccountExistence() throws Exception {
        when(userDAO.getUserByEmail("missing@example.com")).thenReturn(null);

        mockMvc.perform(post("/forgot-password").param("email", "missing@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/forgot-password"))
                .andExpect(model().attributeExists("success"));
    }

    @Test
    void knownEmailStillRedirectsToOtpFlow() throws Exception {
        when(userDAO.getUserByEmail("known@example.com")).thenReturn(new User());
        try (var otpUtil = org.mockito.Mockito.mockStatic(Util.OTPUtil.class)) {
            otpUtil.when(() -> Util.OTPUtil.generateAndSendOTP("known@example.com")).thenReturn(true);

            MockHttpSession session = new MockHttpSession();
            mockMvc.perform(post("/forgot-password").param("email", "known@example.com").session(session))
                    .andExpect(status().is3xxRedirection());

            org.junit.jupiter.api.Assertions.assertEquals("known@example.com", session.getAttribute("resetEmail"));
            org.junit.jupiter.api.Assertions.assertEquals(false, session.getAttribute("otpVerified"));
        }
    }
}
