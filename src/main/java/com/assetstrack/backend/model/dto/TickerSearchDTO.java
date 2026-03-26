package com.assetstrack.backend.model.dto;

public record TickerSearchDTO(
    String symbol,
    String name,
    String exch
) {}
