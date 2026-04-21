package com.assetstrack.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.assetstrack.backend.model.dto.ManualUpdateDTO;
import com.assetstrack.backend.model.dto.PortfolioPointDTO;
import com.assetstrack.backend.model.dto.StockFullDTO;
import com.assetstrack.backend.model.dto.StockPositionDTO;
import com.assetstrack.backend.config.SecurityUtils;
import com.assetstrack.backend.service.IPortfolioApiService;
import com.assetstrack.backend.service.PortfolioApiService;

@RestController
@RequestMapping("/api/v1/portfolio")
@Validated
public class PortfolioController {

    private final IPortfolioApiService portfolioApiService;
    private final SecurityUtils securityUtils;

    public PortfolioController(PortfolioApiService portfolioApiService, SecurityUtils securityUtils) {
        this.portfolioApiService = portfolioApiService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Long userId = securityUtils.getAuthenticatedUserId();
        return portfolioApiService.getPortfolioStatus(userId);
    }

    @GetMapping("/stocks/{ticker}")
    public StockFullDTO getStockDetails(@PathVariable String ticker) {
        return portfolioApiService.getFullStockDetails(ticker);
    }

    @PostMapping("/positions")
    public ResponseEntity<String> addPosition(@Valid @RequestBody StockPositionDTO position) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioApiService.addPosition(position, userId));
    }

    @PutMapping("/positions")
    public ResponseEntity<String> modifyPosition(@Valid @RequestBody StockPositionDTO position) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(portfolioApiService.modifyPosition(position, userId));
    }

    @DeleteMapping("/positions/{ticker}")
    public ResponseEntity<String> deletePosition(@PathVariable String ticker) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(portfolioApiService.deletePosition(ticker, userId));
    }

    @PatchMapping("/holdings/{holdingId}")
    public ResponseEntity<Void> updateHolding(@PathVariable Long holdingId, @RequestBody ManualUpdateDTO dto) {
        Long userId = securityUtils.getAuthenticatedUserId();
        portfolioApiService.syncHoldingManually(holdingId, userId, dto.totalShares(), dto.avgPrice());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/graph")
    public List<PortfolioPointDTO> getGraph(
            @RequestParam(defaultValue = "historic") String mode,
            @RequestParam(defaultValue = "1d") String period) {
        Long userId = securityUtils.getAuthenticatedUserId();
        if ("intraday".equals(mode)) {
            return portfolioApiService.getTodayIntraday(userId, period);
        }
        return portfolioApiService.getPortfolioHistory(userId);
    }
}

