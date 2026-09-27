package com.petshop.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.petshop.repository.AdminActionLogRepository;
import com.petshop.dao.InventoryBatchDAO;
import com.petshop.dao.ProductDAO;
import com.petshop.model.User;

@ExtendWith(MockitoExtension.class)
class AdminInventoryControllerTest {

    @Mock
    ProductDAO productDAO;
    @Mock
    InventoryBatchDAO inventoryBatchDAO;
    @Mock
    AdminActionLogRepository actionLog;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AdminInventoryController(productDAO, inventoryBatchDAO, actionLog)).build();
    }

    @Test
    void inventoryRendersView() throws Exception {
        when(productDAO.getAllProducts()).thenReturn(Collections.emptyList());
        when(inventoryBatchDAO.getProductAdminInventoryViews(30)).thenReturn(new HashMap<>());

        mockMvc.perform(get("/admin/inventory"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/inventory"))
                .andExpect(model().attributeExists("products", "lowStockCount"));
    }

    @Test
    void inventoryWithProductIdLoadsBatches() throws Exception {
        when(productDAO.getAllProducts()).thenReturn(Collections.emptyList());
        when(inventoryBatchDAO.getProductAdminInventoryViews(30)).thenReturn(new HashMap<>());
        when(inventoryBatchDAO.findAllocatableBatchesForProduct(3)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/inventory").param("productId", "3"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("selectedProductBatches"));
    }

    @Test
    void addBatchImportsAndLogs() throws Exception {
        when(inventoryBatchDAO.recordImportBatch(any(), any())).thenReturn(true);
        User admin = new User();
        admin.setId(9);

        mockMvc.perform(post("/admin/inventory")
                        .param("action", "addBatch")
                        .param("productId", "3")
                        .param("quantity", "10")
                        .param("unitCost", "50000")
                        .sessionAttr("user", admin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/inventory"));

        verify(inventoryBatchDAO).recordImportBatch(any(), eq(9));
        verify(actionLog).log(eq(9), eq("IMPORT_STOCK"), eq("PRODUCT"), eq(3), any());
    }

    @Test
    void addBatchRejectsBadData() throws Exception {
        mockMvc.perform(post("/admin/inventory")
                        .param("action", "addBatch")
                        .param("productId", "abc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/inventory"));

        verify(inventoryBatchDAO, org.mockito.Mockito.never()).recordImportBatch(any(), any());
    }
}
