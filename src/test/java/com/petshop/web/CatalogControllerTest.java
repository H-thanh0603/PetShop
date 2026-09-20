package com.petshop.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.ProductDAO;
import DAO.ReviewDAO;
import DAO.WishlistDAO;
import Model.Product;
import Model.User;

@ExtendWith(MockitoExtension.class)
class CatalogControllerTest {

    @Mock
    ProductDAO productDAO;
    @Mock
    ReviewDAO reviewDAO;
    @Mock
    WishlistDAO wishlistDAO;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CatalogController(productDAO, reviewDAO, wishlistDAO))
                .build();
    }

    @Test
    void productDetailRendersView() throws Exception {
        Product p = new Product(1, "Hat", "hat.jpg", new BigDecimal("100000"), 0, "desc");
        when(productDAO.getProductById(1)).thenReturn(p);
        when(reviewDAO.getReviewsByProductId(1)).thenReturn(Collections.emptyList());
        when(productDAO.getRelatedProducts(1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/product-detail").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/shop/product"))
                .andExpect(model().attributeExists("detail", "listReviews", "relatedProducts"));
    }

    @Test
    void productDetailMissingIdRedirectsShop() throws Exception {
        mockMvc.perform(get("/product-detail"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void productDetailMarksWishlistForLoggedInUser() throws Exception {
        Product p = new Product(1, "Hat", "hat.jpg", new BigDecimal("100000"), 0, "desc");
        when(productDAO.getProductById(1)).thenReturn(p);
        when(reviewDAO.getReviewsByProductId(1)).thenReturn(Collections.emptyList());
        when(productDAO.getRelatedProducts(1)).thenReturn(Collections.emptyList());
        when(wishlistDAO.getWishlistProductIdsByUserId(7)).thenReturn(Set.of(1));

        User user = new User();
        user.setId(7);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user);

        mockMvc.perform(get("/product-detail").param("id", "1").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("wishlistProductIds"));
    }

    @Test
    void wishlistRedirectsLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/wishlist"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void wishlistRendersForUser() throws Exception {
        User user = new User();
        user.setId(7);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user);
        when(wishlistDAO.getWishlistProductsByUserId(7)).thenReturn(List.of());

        mockMvc.perform(get("/wishlist").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/shop/wishlist"))
                .andExpect(model().attributeExists("wishlistProducts"));
    }
}
