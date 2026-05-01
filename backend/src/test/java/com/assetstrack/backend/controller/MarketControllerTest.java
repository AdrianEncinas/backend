package com.assetstrack.backend.controller;

import com.assetstrack.backend.config.JwtUtil;
import com.assetstrack.backend.config.SecurityUtils;
import com.assetstrack.backend.config.UserDetailsServiceImpl;
import com.assetstrack.backend.model.dto.TickerSearchDTO;
import com.assetstrack.backend.model.dto.WatchlistAddRequest;
import com.assetstrack.backend.model.dto.WatchlistDTO;
import com.assetstrack.backend.service.MarketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MarketController.class)
class MarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MarketService marketService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private SecurityUtils securityUtils;

    @Autowired
    private ObjectMapper objectMapper;

    // ── GET /search ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void searchTicker_returnsResults() throws Exception {
        TickerSearchDTO dto = new TickerSearchDTO("AAPL", "Apple Inc.", "NASDAQ");
        when(marketService.searchTicker("AAPL")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/market/search").param("query", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].name").value("Apple Inc."));
    }

    @Test
    @WithMockUser
    void searchTicker_noResults_returnsEmptyArray() throws Exception {
        when(marketService.searchTicker("UNKNOWN")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/market/search").param("query", "UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /watchlist ────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getWatchlist_returnsWatchlistForUser() throws Exception {
        WatchlistDTO dto = new WatchlistDTO(1L, "AAPL", "Apple Inc.");
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(marketService.getWatchlist(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/market/watchlist"))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].ticker").value("AAPL"));
    }

        // ── POST /watchlist ───────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void addToWatchlist_validBody_returnsSavedDTO() throws Exception {
        WatchlistAddRequest input = new WatchlistAddRequest("GOOGL", "Alphabet Inc.");
        WatchlistDTO saved = new WatchlistDTO(5L, "GOOGL", "Alphabet Inc.");
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(marketService.addToWatchlist(any(WatchlistAddRequest.class), eq(1L))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/market/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.ticker").value("GOOGL"));
    }

    // ── DELETE /watchlist/{watchlistItemId} ───────────────────────────────────

    @Test
    @WithMockUser
        void deleteWatchlist_validId_returns204() throws Exception {
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        doNothing().when(marketService).deleteFromWatchlist(1L, 1L);

        mockMvc.perform(delete("/api/v1/market/watchlist/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteWatchlist_notFound_returns500() throws Exception {
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        doThrow(new RuntimeException("Watchlist not found"))
            .when(marketService).deleteFromWatchlist(99L, 1L);

        mockMvc.perform(delete("/api/v1/market/watchlist/99"))
                .andExpect(status().isInternalServerError());
    }
}
