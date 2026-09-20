package com.petshop.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.AddressDao;
import DAO.OrderDAO;
import DAO.OrderSignDAO;
import DAO.OrderSignatureDAO;
import DAO.UserDAO;
import Model.User;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    AddressDao addressDao;
    @Mock
    OrderDAO orderDAO;
    @Mock
    UserDAO userDAO;
    @Mock
    OrderSignDAO orderSignDAO;
    @Mock
    OrderSignatureDAO orderSignatureDAO;

    MockMvc mockMvc;
    MockHttpSession authed;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AccountController(addressDao, orderDAO, userDAO, orderSignDAO, orderSignatureDAO)).build();
        User user = new User();
        user.setId(7);
        user.setEmail("user@example.com");
        authed = new MockHttpSession();
        authed.setAttribute("user", user);
    }

    @Test
    void myAccountRedirectsLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/my-account"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void myAccountRendersView() throws Exception {
        when(addressDao.getAddressesByUserId(7)).thenReturn(Collections.emptyList());
        when(orderDAO.getOrdersByUserId(7)).thenReturn(List.of());
        when(orderSignDAO.findPendingByUserId(7)).thenReturn(Collections.emptyList());
        when(orderSignatureDAO.findByUserId(7)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/my-account").session(authed))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/shop/my-account"))
                .andExpect(model().attributeExists("addressList", "recentOrders"));
    }

    @Test
    void myAccountRejectsInvalidFullname() throws Exception {
        mockMvc.perform(post("/my-account")
                        .param("fullname", "X")
                        .param("email", "user@example.com")
                        .session(authed))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void changePasswordRejectsWrongCurrent() throws Exception {
        User dbUser = new User();
        dbUser.setId(7);
        dbUser.setPassword("$2a$12$abcdefghijklmnopqrstuuXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        when(userDAO.getUserById(7)).thenReturn(dbUser);

        mockMvc.perform(post("/my-account")
                        .param("action", "changePassword")
                        .param("currentPassword", "Wrongpass1!")
                        .param("newPassword", "Newpass1!")
                        .param("confirmPassword", "Newpass1!")
                        .session(authed))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void addressesRequireLogin() throws Exception {
        mockMvc.perform(post("/addresses").param("addressDetail", "123 ABC"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void addressDeleteWithBadIdRedirects() throws Exception {
        mockMvc.perform(post("/addresses")
                        .param("_method", "delete")
                        .session(authed))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void updateProfileCheckoutRejectsBadName() throws Exception {
        mockMvc.perform(post("/update-profile-checkout")
                        .param("fullname", "X")
                        .session(authed))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void addFromMyAccountShouldForceDefault() throws Exception {
        when(addressDao.hasAnyAddress(7)).thenReturn(true);
        when(addressDao.addAddress(
                org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.eq(true),
                org.mockito.ArgumentMatchers.any(java.sql.Timestamp.class),
                org.mockito.ArgumentMatchers.eq("12 Tran Hung Dao"),
                org.mockito.ArgumentMatchers.eq("Ho Chi Minh"),
                org.mockito.ArgumentMatchers.eq("Quan 5"),
                org.mockito.ArgumentMatchers.eq("Phuong 2"))).thenReturn(true);

        mockMvc.perform(post("/addresses")
                        .param("addressDetail", "12 Tran Hung Dao")
                        .param("province", "Ho Chi Minh")
                        .param("district", "Quan 5")
                        .param("ward", "Phuong 2")
                        .param("source", "account")
                        .param("redirect", "account")
                        .session(authed))
                .andExpect(status().is3xxRedirection());

        org.mockito.Mockito.verify(addressDao).addAddress(
                org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.eq(true),
                org.mockito.ArgumentMatchers.any(java.sql.Timestamp.class),
                org.mockito.ArgumentMatchers.eq("12 Tran Hung Dao"),
                org.mockito.ArgumentMatchers.eq("Ho Chi Minh"),
                org.mockito.ArgumentMatchers.eq("Quan 5"),
                org.mockito.ArgumentMatchers.eq("Phuong 2"));
    }

    @Test
    void addFromCheckoutShouldForceDefault() throws Exception {
        when(addressDao.hasAnyAddress(7)).thenReturn(true);
        when(addressDao.addAddress(
                org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.eq(true),
                org.mockito.ArgumentMatchers.any(java.sql.Timestamp.class),
                org.mockito.ArgumentMatchers.eq("123 Nguyen Hue"),
                org.mockito.ArgumentMatchers.eq("Ho Chi Minh"),
                org.mockito.ArgumentMatchers.eq("Quan 1"),
                org.mockito.ArgumentMatchers.eq("Ben Nghe"))).thenReturn(true);

        mockMvc.perform(post("/addresses")
                        .param("addressDetail", "123 Nguyen Hue")
                        .param("province", "Ho Chi Minh")
                        .param("district", "Quan 1")
                        .param("ward", "Ben Nghe")
                        .param("source", "checkout")
                        .param("redirect", "checkout")
                        .session(authed))
                .andExpect(status().is3xxRedirection());

        org.mockito.Mockito.verify(addressDao).addAddress(
                org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.eq(true),
                org.mockito.ArgumentMatchers.any(java.sql.Timestamp.class),
                org.mockito.ArgumentMatchers.eq("123 Nguyen Hue"),
                org.mockito.ArgumentMatchers.eq("Ho Chi Minh"),
                org.mockito.ArgumentMatchers.eq("Quan 1"),
                org.mockito.ArgumentMatchers.eq("Ben Nghe"));
    }

    @Test
    void setDefaultAddressShouldUsePostFlow() throws Exception {
        when(addressDao.setDefaultAddress(7, 13)).thenReturn(true);

        mockMvc.perform(post("/addresses")
                        .param("_method", "patch")
                        .param("action", "setDefault")
                        .param("id", "13")
                        .param("redirect", "account")
                        .session(authed))
                .andExpect(status().is3xxRedirection());

        org.mockito.Mockito.verify(addressDao).setDefaultAddress(7, 13);
    }

    @Test
    void updateFromCheckoutShouldForceDefault() throws Exception {
        when(addressDao.updateAddress(
                org.mockito.ArgumentMatchers.eq(11),
                org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.eq(true),
                org.mockito.ArgumentMatchers.any(java.sql.Timestamp.class),
                org.mockito.ArgumentMatchers.eq("456 Le Loi"),
                org.mockito.ArgumentMatchers.eq("Ho Chi Minh"),
                org.mockito.ArgumentMatchers.eq("Quan 1"),
                org.mockito.ArgumentMatchers.eq("Ben Thanh"))).thenReturn(true);

        mockMvc.perform(post("/addresses")
                        .param("_method", "put")
                        .param("id", "11")
                        .param("addressDetail", "456 Le Loi")
                        .param("province", "Ho Chi Minh")
                        .param("district", "Quan 1")
                        .param("ward", "Ben Thanh")
                        .param("source", "checkout")
                        .param("redirect", "checkout")
                        .session(authed))
                .andExpect(status().is3xxRedirection());

        org.mockito.Mockito.verify(addressDao).updateAddress(
                org.mockito.ArgumentMatchers.eq(11),
                org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.eq(true),
                org.mockito.ArgumentMatchers.any(java.sql.Timestamp.class),
                org.mockito.ArgumentMatchers.eq("456 Le Loi"),
                org.mockito.ArgumentMatchers.eq("Ho Chi Minh"),
                org.mockito.ArgumentMatchers.eq("Quan 1"),
                org.mockito.ArgumentMatchers.eq("Ben Thanh"));
    }
}
