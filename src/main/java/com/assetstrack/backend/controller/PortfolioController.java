package com.assetstrack.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.assetstrack.backend.model.dto.ManualUpdateDTO;
import com.assetstrack.backend.model.dto.PortfolioPointDTO;
import com.assetstrack.backend.model.dto.StockFullDTO;
import com.assetstrack.backend.model.dto.StockPositionDTO;
import com.assetstrack.backend.service.IPortfolioApiService;
import com.assetstrack.backend.service.PortfolioApiService;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController{

    @Autowired
    private final IPortfolioApiService portfolioApiService;

    public PortfolioController(PortfolioApiService portfolioApiService){
        this.portfolioApiService = portfolioApiService;
    }
    
    @GetMapping("/dashboard/{id}")
    public Map<String, Object> getDashboard(@PathVariable Long id) {
        return portfolioApiService.getPortfolioStatus(id);
    }

    @GetMapping("/stock/{ticker}")
    public StockFullDTO getStockFull(@PathVariable String ticker) {
        return portfolioApiService.getFullStockDetails(ticker);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addPosition(@RequestBody StockPositionDTO position) {
        if (position.userid() == null) {
            return ResponseEntity.badRequest().body("userid es requerido");
        }
        return ResponseEntity.ok(portfolioApiService.addPosition(position, position.userid()));
    }

    @PutMapping("/modify")
    public ResponseEntity<String> modifyPosition(@RequestBody StockPositionDTO position) {
        if (position.userid() == null) {
            return ResponseEntity.badRequest().body("userid es requerido");
        }
        return ResponseEntity.ok(portfolioApiService.modifyPosition(position, position.userid()));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deletePosition(@RequestBody StockPositionDTO position) {
        if (position.userid() == null) {
            return ResponseEntity.badRequest().body("userid es requerido");
        }
        return ResponseEntity.ok(portfolioApiService.deletePosition(position, position.userid()));
    }

    @PutMapping("/holdings/{id}/manual-update")
    public String putMethodName(@PathVariable Long id, @RequestBody ManualUpdateDTO dto) {
        portfolioApiService.syncHoldingManually(id, dto.totalShares(),dto.avgPrice());
        
        return "entity";
    }
    
    @GetMapping("/{id}/graph")
    public List<PortfolioPointDTO> getGraph(
            @PathVariable Long id,
            @RequestParam(defaultValue = "historic") String mode,
            @RequestParam(defaultValue = "1d") String period) {

        if ("intraday".equals(mode)) {
            return portfolioApiService.getTodayIntraday(id, period); 
        }
        return portfolioApiService.getPortfolioHistory(id);
    }
    
}
