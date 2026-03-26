package com.assetstrack.backend.model.dto;

import java.math.BigDecimal;

public record StockFullDTO(
    String ticker,
    String longName,
    String logoUrl,               // Nuevo campo para el icono
    String businessSummary,
    BigDecimal currentPrice,      // Precio actual en la raíz para acceso rápido
    BigDecimal dailyChangePct,    // Porcentaje de subida/bajada hoy
    FundamentalsDto fundamentals,
    MetricsDto metrics,
    SolvencyDto solvency,
    AnalystsDto analysts,
    DividendsDto dividends
) {
    public record FundamentalsDto(
        BigDecimal peRatio,
        BigDecimal forwardPE,
        BigDecimal pegRatio,
        BigDecimal enterpriseToEbitda
    ) {}

    public record MetricsDto(
        ProfitabilityDto profitability,
        GrowthDto growth
    ) {}

    public record ProfitabilityDto(
        BigDecimal ebitdaMargins,
        BigDecimal operatingMargins,
        BigDecimal grossMargins,
        BigDecimal returnOnAssets,
        BigDecimal returnOnEquity
    ) {}

    public record GrowthDto(
        BigDecimal revenueGrowth,
        BigDecimal earningsGrowth,
        Long freeCashflow,
        Long operatingCashflow
    ) {}

    public record SolvencyDto(
        BigDecimal debtToEquity,
        BigDecimal currentRatio,
        BigDecimal quickRatio,
        Long totalCash,
        Long totalDebt
    ) {}

    public record AnalystsDto(
        String recommendation,
        Integer numberOfAnalysts,
        TargetPriceDto targetPrice,
        BigDecimal upsidePotentialPct
    ) {}

    public record TargetPriceDto(
        BigDecimal low,
        BigDecimal high,
        BigDecimal mean,
        BigDecimal median,
        BigDecimal current
    ) {}

    public record DividendsDto(
        BigDecimal yield,
        BigDecimal payoutRatio,
        BigDecimal lastDividend
    ) {}
}