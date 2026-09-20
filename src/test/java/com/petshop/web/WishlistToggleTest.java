package com.petshop.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.CartDAO;
import DAO.ProductDAO;
import DAO.ReviewDAO;
import DAO.WishlistDAO;
import Model.Product;
import Model.User;
import services.InventoryService;

@ExtendWith(MockitoExtension.class)
class WishlistToggleTest {

    @Mock
    ProductDAO productDAO;
    @Mock
    ReviewDAO reviewDAO;
    @Mock
    WishlistDAO wishlistDAO;
    @Mock
    CartDAO cartDAO;
    @Mock
    InventoryService inventoryService;

    MockMvc catalogMvc;
    MockMvc cartMvc;

    @BeforeEach
    void setUp() {
        catalogMvc = MockMvcBuilders
                .standaloneSetup(new CatalogController(productDAO, reviewDAO, wishlistDAO))
                .build();
        cartMvc = MockMvcBuilders
                .standaloneSetup(new CartController(cartDAO, inventoryService))
                .build();
    }

    @Test
    void ajaxToggleReturnsJsonWithoutRedirect() throws Exception {
        User user = new User();
        user.setId(7);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user);
        when(wishlistDAO.toggleWishlistAndReturnState(7, 12)).thenReturn(true);

        catalogMvc.perform(post("/toggle-wishlist")
                        .param("productId", "12")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"success\":true")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"wishlisted\":true")));
    }

    @Test
    void ajaxToggleUnauthenticatedReturns401Json() throws Exception {
        catalogMvc.perform(post("/toggle-wishlist")
                        .param("productId", "12")
                        .param("redirect", "/product-detail?id=12")
                        .header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"authenticated\":false")));
    }

    @Test
    void ajaxToggleMissingIdReturns400() throws Exception {
        User user = new User();
        user.setId(7);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user);

        catalogMvc.perform(post("/toggle-wishlist")
                        .param("productId", "")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"success\":false")));
    }

    @Test
    void addToCartSuccessSetsToast() throws Exception {
        Product product = new Product();
        product.setId(7);
        product.setName("Bát Ăn Inox Cho Mèo Đôi");
        product.setPrice(new BigDecimal("99000"));
        product.setStock(10);

        when(inventoryService.validateAddToCart(
                org.mockito.ArgumentMatchers.anyMap(), org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.eq(1)))
                .thenReturn(InventoryService.StockValidationResult.valid(product, 1));

        MockHttpSession session = new MockHttpSession();

        cartMvc.perform(post("/add-to-cart")
                        .param("id", "7")
                        .param("quantity", "1")
                        .param("actionType", "add")
                        .header("referer", "/product?id=7")
                        .session(session))
                .andExpect(status().is3xxRedirection());

        org.junit.jupiter.api.Assertions.assertEquals(
                "Đã thêm Bát Ăn Inox Cho Mèo Đôi vào giỏ hàng!", session.getAttribute("toastMessage"));
        org.junit.jupiter.api.Assertions.assertEquals("success", session.getAttribute("toastType"));
    }
}
