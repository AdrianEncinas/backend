package com.assetstrack.backend.model.dto;

public record WatchlistDTO(
    Long id,
    String ticker,
    String companyName
) {}