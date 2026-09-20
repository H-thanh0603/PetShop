package com.petshop.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.PetTypeDAO;
import DAO.ProductDAO;
import DAO.WishlistDAO;
import Model.Product;

@ExtendWith(MockitoExtension.class)
class ShopControllerTest {

    @Mock
    ProductDAO productDAO;
    @Mock
    PetTypeDAO petTypeDAO;
    @Mock
    WishlistDAO wishlistDAO;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ShopController(productDAO, petTypeDAO, wishlistDAO))
                .build();
    }

    @Test
    void unfilteredRendersShopView() throws Exception {
        when(productDAO.getAllCategories()).thenReturn(List.of("Food"));
        when(productDAO.getPopularProductsPage(1, 6)).thenReturn(Collections.emptyList());
        when(productDAO.getDiscountedProductsPage(1, 12)).thenReturn(Collections.emptyList());
        when(productDAO.getAllProductsPage(1, 12)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/shop"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/shop/shop"))
                .andExpect(model().attributeExists("popularProducts", "catalogProducts"));
    }

    @Test
    void filteredUsesDaoBackedFiltering() throws Exception {
        Product product = new Product(1, "Hat cho meo", "cat.jpg", new BigDecimal("100000"), 5, "desc", "Food");
        when(productDAO.getFilteredProductsPage(any())).thenReturn(List.of(product));
        when(productDAO.countFilteredProducts(any())).thenReturn(1);
        when(productDAO.getAllCategories()).thenReturn(List.of("Food"));

        mockMvc.perform(get("/shop")
                        .param("search", "m")
                        .param("category", "Food")
                        .param("sort", "price-desc")
                        .param("priceRange", "under100")
                        .param("discountOnly", "true")
                        .param("pet", "cat")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/shop/shop-pet"))
                .andExpect(model().attributeExists("products"));

        verify(productDAO).getFilteredProductsPage(any());
        verify(productDAO).countFilteredProducts(any());
        verify(productDAO, never()).getAllProducts();
    }
}
