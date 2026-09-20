package com.petshop.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.AdminActionLogDAO;
import DAO.PetTypeDAO;
import DAO.ProductDAO;

@ExtendWith(MockitoExtension.class)
class AdminProductControllerTest {

    @Mock
    ProductDAO productDAO;
    @Mock
    PetTypeDAO petTypeDAO;
    @Mock
    AdminActionLogDAO actionLog;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AdminProductController(productDAO, petTypeDAO, actionLog)).build();
    }

    @Test
    void productsListRendersView() throws Exception {
        when(productDAO.getAllProducts()).thenReturn(Collections.emptyList());
        when(petTypeDAO.getAllPetTypes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/pages/admin/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/products"))
                .andExpect(model().attributeExists("products", "petTypes"));
    }

    @Test
    void addRejectsBlankName() throws Exception {
        mockMvc.perform(post("/pages/admin/products")
                        .param("action", "add")
                        .param("name", "")
                        .param("price", "10000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pages/admin/products"));

        verify(productDAO, never()).addProductAndReturnId(anyString(), any(), any(),
                anyInt(), any(), anyInt(), anyString(), anyInt());
    }

    @Test
    void addPersistsValidProduct() throws Exception {
        when(productDAO.addProductAndReturnId(anyString(), any(), any(),
                anyInt(), any(), anyInt(), anyString(), anyInt())).thenReturn(1);

        mockMvc.perform(post("/pages/admin/products")
                        .param("action", "add")
                        .param("name", "Hat meo")
                        .param("price", "10000")
                        .param("discount", "0")
                        .param("weight", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pages/admin/products"));

        verify(productDAO).addProductAndReturnId(anyString(), any(), any(),
                anyInt(), any(), anyInt(), anyString(), anyInt());
    }

    @Test
    void addWithImageFileUploads() throws Exception {
        when(productDAO.addProductAndReturnId(anyString(), anyString(), any(),
                anyInt(), any(), anyInt(), anyString(), anyInt())).thenReturn(2);
        MockMultipartFile image = new MockMultipartFile("imageFile", "cat.jpg",
                "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, 1, 2});

        mockMvc.perform(multipart("/pages/admin/products")
                        .file(image)
                        .param("action", "add")
                        .param("name", "Hat meo")
                        .param("price", "10000"))
                .andExpect(status().is3xxRedirection());

        verify(productDAO).addProductAndReturnId(anyString(), anyString(), any(),
                anyInt(), any(), anyInt(), anyString(), anyInt());
    }

    @Test
    void deleteRejectsBadId() throws Exception {
        mockMvc.perform(post("/pages/admin/products")
                        .param("action", "delete")
                        .param("id", "abc"))
                .andExpect(status().is3xxRedirection());

        verify(productDAO, never()).softDeleteProduct(anyInt());
    }

    @Test
    void deleteSoftDeletesAndLogs() throws Exception {
        when(productDAO.softDeleteProduct(3)).thenReturn(true);

        mockMvc.perform(post("/pages/admin/products")
                        .param("action", "delete")
                        .param("id", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pages/admin/products"));

        verify(actionLog).log(anyInt(), org.mockito.ArgumentMatchers.eq("DELETE_PRODUCT"),
                org.mockito.ArgumentMatchers.eq("product"),
                org.mockito.ArgumentMatchers.eq(3),
                org.mockito.ArgumentMatchers.isNull());
    }
}
