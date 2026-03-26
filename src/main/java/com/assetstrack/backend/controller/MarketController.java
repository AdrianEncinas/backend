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
import com.assetstrack.backend.model.dto.WatchlistDTO;
import com.assetstrack.backend.service.MarketService;



@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService){
        this.marketService = marketService;
    }

    @GetMapping("/search")
    public List<TickerSearchDTO> getMethodName(@RequestParam String query) {
        return marketService.searchTicker(query);
    }
    
    @GetMapping("/watchlist/{id}")
    public List<WatchlistDTO> getWatchlist(@PathVariable Long id) {
        return marketService.getWatchlist(id);
    }

    @PostMapping("/watchlist/add")
    public WatchlistDTO addToWatchlist(@RequestBody WatchlistDTO entity) {
        System.out.println("entity: " + entity);
        return marketService.addToWatchlist(entity);
    }

    @DeleteMapping("/watchlist/delete")
    public ResponseEntity<String> deleteWatchlist(@RequestBody WatchlistDTO entity) {
        marketService.deleteWathlist(entity);
        return ResponseEntity.ok(entity.companyName() + " deleted from watchlist");
    }
    
    
    


}
