package com.assetstrack.backend.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.assetstrack.backend.model.dto.ChartDTO;
import com.assetstrack.backend.service.IPortfolioApiService;
import com.assetstrack.backend.service.PortfolioApiService;


@RestController
@RequestMapping("/api/v1/chart")
public class ChartController {

    @Autowired
    private final IPortfolioApiService portfolioApiService;

    public ChartController(PortfolioApiService portfolioApiService){
        this.portfolioApiService = portfolioApiService;
    }

    @GetMapping("/{ticker}")
    public CompletableFuture<ChartDTO> getChart(
        @PathVariable String ticker,
        @RequestParam(name = "period", defaultValue = "1mo")String period
    ) {
        return portfolioApiService.getFullChart(ticker, period);
    }
    

}
