package com.assetstrack.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.assetstrack.backend.model.dto.TickerSearchDTO;
import com.assetstrack.backend.model.dto.WatchlistAddRequest;
import com.assetstrack.backend.model.dto.WatchlistDTO;
import com.assetstrack.backend.config.SecurityUtils;
import com.assetstrack.backend.service.MarketService;



@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    private final MarketService marketService;
    private final SecurityUtils securityUtils;

    public MarketController(MarketService marketService, SecurityUtils securityUtils){
        this.marketService = marketService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/search")
    public List<TickerSearchDTO> getMethodName(@RequestParam String query) {
        return marketService.searchTicker(query);
    }
    
    @GetMapping("/watchlist")
    public List<WatchlistDTO> getWatchlist() {
        Long userId = securityUtils.getAuthenticatedUserId();
        return marketService.getWatchlist(userId);
    }

    @PostMapping("/watchlist")
    public WatchlistDTO addToWatchlist(@RequestBody WatchlistAddRequest request) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return marketService.addToWatchlist(request, userId);
    }

    @DeleteMapping("/watchlist/{watchlistItemId}")
    public ResponseEntity<String> deleteWatchlist(@PathVariable Long watchlistItemId) {
        Long userId = securityUtils.getAuthenticatedUserId();
        marketService.deleteFromWatchlist(watchlistItemId, userId);
        return ResponseEntity.noContent().build();
    }
    
    
    


}
