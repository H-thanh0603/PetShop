package com.petshop.web;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import DAO.AdminActionLogDAO;
import DAO.ReviewDAO;
import Model.Review;
import Model.User;

@ExtendWith(MockitoExtension.class)
class AdminReviewControllerTest {

    @Mock
    ReviewDAO reviewDAO;
    @Mock
    AdminActionLogDAO actionLog;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AdminReviewController(reviewDAO, actionLog)).build();
    }

    @Test
    void reviewsListRendersView() throws Exception {
        when(reviewDAO.getAllReviews()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/pages/admin/reviews"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/reviews"))
                .andExpect(model().attributeExists("reviews", "totalReviews"));
    }

    @Test
    void reviewsFilterByMaxRating() throws Exception {
        Review r = new Review();
        r.setRating(1);
        when(reviewDAO.getReviewsByMaxRating(2)).thenReturn(java.util.List.of(r));

        mockMvc.perform(get("/pages/admin/reviews").param("maxRating", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("lowRatingCount", 1));
    }

    @Test
    void deleteReviewLogs() throws Exception {
        when(reviewDAO.deleteReview(5)).thenReturn(true);

        mockMvc.perform(post("/pages/admin/reviews")
                        .param("action", "delete")
                        .param("reviewId", "5")
                        .sessionAttr("user", adminUser()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pages/admin/reviews"));

        verify(actionLog).log(eq(9), eq("DELETE_REVIEW"), eq("review"), eq(5), eq(null));
    }

    @Test
    void replyReviewLogs() throws Exception {
        when(reviewDAO.replyReview(5, "Cam on")).thenReturn(true);

        mockMvc.perform(post("/pages/admin/reviews")
                        .param("action", "reply")
                        .param("reviewId", "5")
                        .param("adminReply", "Cam on")
                        .sessionAttr("user", adminUser()))
                .andExpect(status().is3xxRedirection());

        verify(reviewDAO).replyReview(5, "Cam on");
    }

    private User adminUser() {
        User admin = new User();
        admin.setId(9);
        admin.setRole("admin");
        return admin;
    }
}
