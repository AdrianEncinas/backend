package com.assetstrack.backend.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.assetstrack.backend.exception.NotFoundException;
import com.assetstrack.backend.mapper.Mapper;
import com.assetstrack.backend.model.dto.TickerSearchDTO;
import com.assetstrack.backend.model.dto.WatchlistAddRequest;
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

    @Override
    public WatchlistDTO addToWatchlist(WatchlistAddRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Watchlist newWatchlist = Watchlist.builder()
                .user(user)
                .ticker(request.ticker())
                .companyName(request.companyName())
                .build();
        return Mapper.toDTO(watchlistRepo.save(newWatchlist));
    }

    @Override
    public void deleteFromWatchlist(Long watchlistItemId, Long userId) {
        Watchlist item = watchlistRepo.findById(watchlistItemId)
                .orElseThrow(() -> new NotFoundException("Watchlist item not found"));
        if (!item.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied: this watchlist item does not belong to you");
        }
        watchlistRepo.delete(item);
    }

    

}
