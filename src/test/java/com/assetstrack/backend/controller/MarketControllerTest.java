package com.assetstrack.backend.controller;

import com.assetstrack.backend.model.dto.TickerSearchDTO;
import com.assetstrack.backend.model.dto.WatchlistDTO;
import com.assetstrack.backend.service.MarketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Autowired
    private ObjectMapper objectMapper;

    // ── GET /search ───────────────────────────────────────────────────────────

    @Test
    void searchTicker_returnsResults() throws Exception {
        TickerSearchDTO dto = new TickerSearchDTO("AAPL", "Apple Inc.", "NASDAQ");
        when(marketService.searchTicker("AAPL")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/market/search").param("query", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].name").value("Apple Inc."));
    }

    @Test
    void searchTicker_noResults_returnsEmptyArray() throws Exception {
        when(marketService.searchTicker("UNKNOWN")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/market/search").param("query", "UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /watchlist/{id} ───────────────────────────────────────────────────

    @Test
    void getWatchlist_returnsWatchlistForUser() throws Exception {
        WatchlistDTO dto = new WatchlistDTO(1L, 1L, "AAPL", "Apple Inc.");
        when(marketService.getWatchlist(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/market/watchlist/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    // ── POST /watchlist/add ───────────────────────────────────────────────────

    @Test
    void addToWatchlist_validBody_returnsSavedDTO() throws Exception {
        WatchlistDTO input = new WatchlistDTO(null, 1L, "GOOGL", "Alphabet Inc.");
        WatchlistDTO saved = new WatchlistDTO(5L, 1L, "GOOGL", "Alphabet Inc.");
        when(marketService.addToWatchlist(any(WatchlistDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/market/watchlist/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.ticker").value("GOOGL"));
    }

    // ── DELETE /watchlist/delete ──────────────────────────────────────────────

    @Test
    void deleteWatchlist_validBody_returns200WithMessage() throws Exception {
        WatchlistDTO input = new WatchlistDTO(1L, 1L, "AAPL", "Apple Inc.");
        doNothing().when(marketService).deleteWathlist(any(WatchlistDTO.class));

        mockMvc.perform(delete("/api/v1/market/watchlist/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(content().string("Apple Inc. deleted from watchlist"));
    }

    @Test
    void deleteWatchlist_notFound_propagatesException() throws Exception {
        WatchlistDTO input = new WatchlistDTO(99L, 1L, "AAPL", "Apple Inc.");
        doThrow(new RuntimeException("Watchlist not found"))
                .when(marketService).deleteWathlist(any(WatchlistDTO.class));

        assertThatThrownBy(() ->
                mockMvc.perform(delete("/api/v1/market/watchlist/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))))
                .hasMessageContaining("not found");
    }
}
