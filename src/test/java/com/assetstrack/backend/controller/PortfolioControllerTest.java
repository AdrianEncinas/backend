package com.assetstrack.backend.controller;

import com.assetstrack.backend.model.dto.ManualUpdateDTO;
import com.assetstrack.backend.model.dto.PortfolioPointDTO;
import com.assetstrack.backend.model.dto.StockFullDTO;
import com.assetstrack.backend.model.dto.StockPositionDTO;
import com.assetstrack.backend.service.IPortfolioApiService;
import com.assetstrack.backend.service.PortfolioApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
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

    @Autowired
    private ObjectMapper objectMapper;

    // ── GET /dashboard/{id} ───────────────────────────────────────────────────

    @Test
    void getDashboard_returnsPortfolioMap() throws Exception {
        Map<String, Object> dashboard = Map.of("totalValue", 5000.0, "holdings", List.of());
        when(portfolioApiService.getPortfolioStatus(1L)).thenReturn(dashboard);

        mockMvc.perform(get("/api/v1/portfolio/dashboard/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalValue").value(5000.0));
    }

    // ── GET /stock/{ticker} ───────────────────────────────────────────────────

    @Test
    void getStockFull_returnStockDetails() throws Exception {
        StockFullDTO stock = new StockFullDTO(
                "AAPL", "Apple Inc.", null, "Tech company",
                new BigDecimal("175.00"), new BigDecimal("1.5"),
                null, null, null, null, null);
        when(portfolioApiService.getFullStockDetails("AAPL")).thenReturn(stock);

        mockMvc.perform(get("/api/v1/portfolio/stock/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("AAPL"))
                .andExpect(jsonPath("$.longName").value("Apple Inc."));
    }

    // ── POST /add ─────────────────────────────────────────────────────────────

    @Test
    void addPosition_validBody_returns200() throws Exception {
        StockPositionDTO position = new StockPositionDTO(
                "AAPL", "Apple Inc.", new BigDecimal("10"), new BigDecimal("150.00"), 1L);
        when(portfolioApiService.addPosition(any(), eq(1L))).thenReturn("Position added");

        mockMvc.perform(post("/api/v1/portfolio/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(position)))
                .andExpect(status().isOk())
                .andExpect(content().string("Position added"));
    }

    @Test
    void addPosition_missingUserId_returns400() throws Exception {
        StockPositionDTO position = new StockPositionDTO(
                "AAPL", "Apple Inc.", new BigDecimal("10"), new BigDecimal("150.00"), null);

        mockMvc.perform(post("/api/v1/portfolio/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(position)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("userid es requerido"));
    }

    // ── PUT /modify ───────────────────────────────────────────────────────────

    @Test
    void modifyPosition_validBody_returns200() throws Exception {
        StockPositionDTO position = new StockPositionDTO(
                "AAPL", "Apple Inc.", new BigDecimal("20"), new BigDecimal("155.00"), 1L);
        when(portfolioApiService.modifyPosition(any(), eq(1L))).thenReturn("The position has been modified.");

        mockMvc.perform(put("/api/v1/portfolio/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(position)))
                .andExpect(status().isOk())
                .andExpect(content().string("The position has been modified."));
    }

    @Test
    void modifyPosition_missingUserId_returns400() throws Exception {
        StockPositionDTO position = new StockPositionDTO(
                "AAPL", "Apple Inc.", new BigDecimal("20"), new BigDecimal("155.00"), null);

        mockMvc.perform(put("/api/v1/portfolio/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(position)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("userid es requerido"));
    }

    // ── DELETE /delete ────────────────────────────────────────────────────────

    @Test
    void deletePosition_validBody_returns200() throws Exception {
        StockPositionDTO position = new StockPositionDTO(
                "AAPL", "Apple Inc.", BigDecimal.ZERO, BigDecimal.ZERO, 1L);
        when(portfolioApiService.deletePosition(any(), eq(1L))).thenReturn("The position has been deleted.");

        mockMvc.perform(delete("/api/v1/portfolio/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(position)))
                .andExpect(status().isOk())
                .andExpect(content().string("The position has been deleted."));
    }

    @Test
    void deletePosition_missingUserId_returns400() throws Exception {
        StockPositionDTO position = new StockPositionDTO(
                "AAPL", "Apple Inc.", BigDecimal.ZERO, BigDecimal.ZERO, null);

        mockMvc.perform(delete("/api/v1/portfolio/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(position)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /holdings/{id}/manual-update ─────────────────────────────────────

    @Test
    void manualUpdate_validRequest_returnsEntity() throws Exception {
        ManualUpdateDTO dto = new ManualUpdateDTO(new BigDecimal("25"), new BigDecimal("160.00"));
        when(portfolioApiService.syncHoldingManually(eq(10L), any(), any())).thenReturn(null);

        mockMvc.perform(put("/api/v1/portfolio/holdings/10/manual-update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("entity"));
    }

    // ── GET /{id}/graph ───────────────────────────────────────────────────────

    @Test
    void getGraph_historicMode_returnsHistoricData() throws Exception {
        PortfolioPointDTO point = new PortfolioPointDTO(
                "2024-01-01", new BigDecimal("5000"), new BigDecimal("4000"));
        when(portfolioApiService.getPortfolioHistory(1L)).thenReturn(List.of(point));

        mockMvc.perform(get("/api/v1/portfolio/1/graph")
                        .param("mode", "historic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("2024-01-01"));
    }

    @Test
    void getGraph_intradayMode_returnsIntradayData() throws Exception {
        PortfolioPointDTO point = new PortfolioPointDTO(
                "10:30", new BigDecimal("5100"), new BigDecimal("4000"));
        when(portfolioApiService.getTodayIntraday(eq(1L), eq("1d"))).thenReturn(List.of(point));

        mockMvc.perform(get("/api/v1/portfolio/1/graph")
                        .param("mode", "intraday")
                        .param("period", "1d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("10:30"));
    }
}
