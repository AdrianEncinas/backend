package com.assetstrack.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.assetstrack.backend.mapper.Mapper;
import com.assetstrack.backend.model.dto.TickerSearchDTO;
import com.assetstrack.backend.model.dto.WatchlistDTO;
import com.assetstrack.backend.model.entity.User;
import com.assetstrack.backend.model.entity.Watchlist;
import com.assetstrack.backend.repository.UserRepository;
import com.assetstrack.backend.repository.WatchlistRepository;

@Service
public class MarketService implements IMarketService{

    private final WebClient webClient;

    private WatchlistRepository watchlistRepo;

    private UserRepository userRepository;

    public MarketService(WebClient webClient, WatchlistRepository watchlistRepo, UserRepository userRepository) {
        this.webClient = webClient;
        this.watchlistRepo = watchlistRepo;
        this.userRepository = userRepository;
    }

    @Override
    public List<TickerSearchDTO> searchTicker(String query) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/v1/market/search")
                .queryParam("query", query)
                .build())
            .retrieve()
            .bodyToFlux(TickerSearchDTO.class)
            .collectList()
            .block();
    }

    @Override
    public List<WatchlistDTO> getWatchlist (Long id){
        return watchlistRepo.findByUserId(id).stream().map(Mapper::toDTO).toList();
    }

    public WatchlistDTO addToWatchlist (WatchlistDTO watchlist){
        User user = userRepository.findById(watchlist.userId()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Watchlist newWatchlist = Watchlist.builder()
                .user(user)
                .ticker(watchlist.ticker())
                .companyName(watchlist.companyName())
                .build();

        return Mapper.toDTO(watchlistRepo.save(newWatchlist));
    }

    public void deleteWathlist (WatchlistDTO watchlist){
        Watchlist deleteWatchlist = watchlistRepo.findById(watchlist.id()).orElseThrow(() -> new RuntimeException("Whatchlist not found"));
        watchlistRepo.delete(deleteWatchlist);
    }

    

}
