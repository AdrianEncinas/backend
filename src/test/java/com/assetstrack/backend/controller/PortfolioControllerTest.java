package com.assetstrack.backend.controller;

import com.assetstrack.backend.config.JwtUtil;
import com.assetstrack.backend.config.SecurityUtils;
import com.assetstrack.backend.config.UserDetailsServiceImpl;
import com.assetstrack.backend.model.dto.ManualUpdateDTO;
import com.assetstrack.backend.model.dto.PortfolioPointDTO;
import com.assetstrack.backend.model.dto.StockFullDTO;
import com.assetstrack.backend.model.dto.StockPositionDTO;
import com.assetstrack.backend.service.PortfolioApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioController.class)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortfolioApiService portfolioApiService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

        @MockitoBean
        private SecurityUtils securityUtils;

    @Autowired
    private ObjectMapper objectMapper;

        // ── GET /dashboard ────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getDashboard_returnsPortfolioMap() throws Exception {
        Map<String, Object> dashboard = Map.of("totalValue", 5000.0, "holdings", List.of());
                when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(portfolioApiService.getPortfolioStatus(1L)).thenReturn(dashboard);

                mockMvc.perform(get("/api/v1/portfolio/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalValue").value(5000.0));
    }

        // ── GET /stocks/{ticker} ──────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getStockFull_returnStockDetails() throws Exception {
        StockFullDTO stock = new StockFullDTO(
                "AAPL", "Apple Inc.", null, "Tech company",
                new BigDecimal("175.00"), new BigDecimal("1.5"),
                null, null, null, null, null);
        when(portfolioApiService.getFullStockDetails("AAPL")).thenReturn(stock);

                mockMvc.perform(get("/api/v1/portfolio/stocks/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("AAPL"))
                .andExpect(jsonPath("$.longName").value("Apple Inc."));
    }

        // ── POST /positions ───────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void addPosition_validBody_returns201() throws Exception {
        StockPositionDTO position = new StockPositionDTO(
                "AAPL", "Apple Inc.", new BigDecimal("10"), new BigDecimal("150.00"), 1L);
        when(portfolioApiService.addPosition(any(), eq(1L))).thenReturn("Position added");
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);

        mockMvc.perform(post("/api/v1/portfolio/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(position)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Position added"));
    }

    @Test
    @WithMockUser
    void addPosition_missingUserId_stillUsesAuthenticatedUser_returns201() throws Exception {
        StockPositionDTO position = new StockPositionDTO(
                "AAPL", "Apple Inc.", new BigDecimal("10"), new BigDecimal("150.00"), null);
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(portfolioApiService.addPosition(any(), eq(1L))).thenReturn("Position added");

        mockMvc.perform(post("/api/v1/portfolio/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(position)))
                .andExpect(status().isCreated());
    }

    // ── PUT /positions ────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void modifyPosition_validBody_returns200() throws Exception {
        StockPositionDTO position = new StockPositionDTO(
                "AAPL", "Apple Inc.", new BigDecimal("20"), new BigDecimal("155.00"), 1L);
        when(portfolioApiService.modifyPosition(any(), eq(1L))).thenReturn("The position has been modified.");
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);

        mockMvc.perform(put("/api/v1/portfolio/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(position)))
                .andExpect(status().isOk())
                .andExpect(content().string("The position has been modified."));
    }

    @Test
    @WithMockUser
    void modifyPosition_missingUserId_stillUsesAuthenticatedUser_returns200() throws Exception {
        StockPositionDTO position = new StockPositionDTO(
                "AAPL", "Apple Inc.", new BigDecimal("20"), new BigDecimal("155.00"), null);
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(portfolioApiService.modifyPosition(any(), eq(1L))).thenReturn("The position has been modified.");

        mockMvc.perform(put("/api/v1/portfolio/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(position)))
                .andExpect(status().isOk());
    }

    // ── DELETE /positions/{ticker} ────────────────────────────────────────────

    @Test
    @WithMockUser
    void deletePosition_validTicker_returns200() throws Exception {
        when(portfolioApiService.deletePosition(eq("AAPL"), eq(1L))).thenReturn("The position has been deleted.");
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);

        mockMvc.perform(delete("/api/v1/portfolio/positions/AAPL"))
                .andExpect(status().isOk())
                .andExpect(content().string("The position has been deleted."));
    }

    @Test
    @WithMockUser
    void deletePosition_withoutAuthUser_returns500IfSecurityUtilsFails() throws Exception {
        when(securityUtils.getAuthenticatedUserId()).thenThrow(new RuntimeException("No auth user"));

        mockMvc.perform(delete("/api/v1/portfolio/positions/AAPL"))
                .andExpect(status().isInternalServerError());
    }

    // ── PATCH /holdings/{id} ──────────────────────────────────────────────────

    @Test
    @WithMockUser
        void manualUpdate_validRequest_returnsNoContent() throws Exception {
        ManualUpdateDTO dto = new ManualUpdateDTO(new BigDecimal("25"), new BigDecimal("160.00"));
                when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
                when(portfolioApiService.syncHoldingManually(eq(10L), eq(1L), any(), any())).thenReturn(null);

                mockMvc.perform(patch("/api/v1/portfolio/holdings/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isNoContent());
    }

        // ── GET /graph ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getGraph_historicMode_returnsHistoricData() throws Exception {
        PortfolioPointDTO point = new PortfolioPointDTO(
                "2024-01-01", new BigDecimal("5000"), new BigDecimal("4000"));
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(portfolioApiService.getPortfolioHistory(1L)).thenReturn(List.of(point));

        mockMvc.perform(get("/api/v1/portfolio/graph")
                        .param("mode", "historic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("2024-01-01"));
    }

    @Test
    @WithMockUser
    void getGraph_intradayMode_returnsIntradayData() throws Exception {
        PortfolioPointDTO point = new PortfolioPointDTO(
                "10:30", new BigDecimal("5100"), new BigDecimal("4000"));
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(portfolioApiService.getTodayIntraday(eq(1L), eq("1d"))).thenReturn(List.of(point));

        mockMvc.perform(get("/api/v1/portfolio/graph")
                        .param("mode", "intraday")
                        .param("period", "1d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("10:30"));
    }
}
