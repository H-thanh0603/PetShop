package com.petshop.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Upload lands under a temp app.upload-dir so the test never touches the
 * real ./uploads folder.
 */
@ExtendWith(MockitoExtension.class)
class AdminUploadControllerTest {

    MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        java.nio.file.Path tmp = java.nio.file.Files.createTempDirectory("petshop-upload-test");
        System.setProperty("app.upload-dir", tmp.toAbsolutePath().toString());
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminUploadController()).build();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.clearProperty("app.upload-dir");
    }

    @Test
    void uploadRejectsEmptyFile() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/admin/upload").file(empty))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"success\":false")));
    }

    @Test
    void uploadAcceptsValidImage() throws Exception {
        MockMultipartFile image = new MockMultipartFile("file", "cat.jpg",
                "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, 1, 2});

        mockMvc.perform(multipart("/admin/upload").file(image).param("type", "product"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"success\":true")))
                .andExpect(content().string(containsString("shop_pic")));
    }

    @Test
    void uploadGetRedirectsToProducts() throws Exception {
        mockMvc.perform(get("/admin/upload"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pages/admin/products"));
    }
}
