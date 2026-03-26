package com.assetstrack.backend.model.dto;

public record WatchlistDTO(
    Long id,
    Long userId,
    String ticker,
    String companyName
) {}