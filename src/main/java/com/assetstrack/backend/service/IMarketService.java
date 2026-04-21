package com.assetstrack.backend.service;

import java.util.List;

import com.assetstrack.backend.model.dto.TickerSearchDTO;
import com.assetstrack.backend.model.dto.WatchlistAddRequest;
import com.assetstrack.backend.model.dto.WatchlistDTO;

public interface IMarketService {

    List<TickerSearchDTO> searchTicker(String ticker);

    List<WatchlistDTO> getWatchlist(Long userId);

    WatchlistDTO addToWatchlist(WatchlistAddRequest request, Long userId);

    void deleteFromWatchlist(Long watchlistItemId, Long userId);
}
