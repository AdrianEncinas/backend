package com.assetstrack.backend.model.dto;

public record WatchlistAddRequest(
    String ticker,
    String companyName
) {}
