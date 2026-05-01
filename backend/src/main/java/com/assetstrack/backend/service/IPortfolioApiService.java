package com.assetstrack.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.assetstrack.backend.model.dto.ChartDTO;
import com.assetstrack.backend.model.dto.HoldingDTO;
import com.assetstrack.backend.model.dto.PortfolioPointDTO;
import com.assetstrack.backend.model.dto.StockFullDTO;
import com.assetstrack.backend.model.dto.StockPositionDTO;

public interface IPortfolioApiService {

    Map<String, Object> getPortfolioStatus(Long id);

    StockFullDTO getFullStockDetails(String ticker);

    CompletableFuture<ChartDTO> getFullChart(String ticker, String period);

    String addPosition(StockPositionDTO position,Long userId);

    String modifyPosition(StockPositionDTO position,Long userId);

    String deletePosition(String ticker, Long userId);

    HoldingDTO syncHoldingManually(Long holdingId, Long userId, BigDecimal targetShares, BigDecimal targetAvgPrice);

    List<PortfolioPointDTO> getPortfolioHistory(Long userId);

    List<PortfolioPointDTO> getTodayIntraday(Long portfolioId, String period);
    
}
