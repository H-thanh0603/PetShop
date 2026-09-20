package com.petshop.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.CartDAO;
import Model.CartItem;
import Model.Product;
import services.InventoryService;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    CartDAO cartDAO;
    @Mock
    InventoryService inventoryService;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CartController(cartDAO, inventoryService))
                .build();
    }

    @Test
    void getRemoveOnlyRedirectsWithoutMutating() throws Exception {
        mockMvc.perform(get("/cart").param("action", "remove"))
                .andExpect(status().is3xxRedirection());
        verifyNoInteractions(cartDAO);
    }

    @Test
    void postRemoveDeletesProductAndRedirects() throws Exception {
        Product product = new Product();
        product.setId(8);
        product.setName("Hat");
        product.setPrice(new BigDecimal("10000"));

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(8, new CartItem(product, 2));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("cart", cart);

        mockMvc.perform(post("/cart").param("action", "remove").param("id", "8").session(session))
                .andExpect(status().is3xxRedirection());

        assertTrue(((Map<?, ?>) session.getAttribute("cart")).isEmpty());
    }

    @Test
    void cartStateReturnsJson() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("cart", new HashMap<Integer, CartItem>());

        mockMvc.perform(get("/cart").param("action", "state").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void showCartRendersView() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/shop/cart"));
        verify(inventoryService).refreshCartProductsWithNotification(org.mockito.ArgumentMatchers.anyMap());
    }
}
