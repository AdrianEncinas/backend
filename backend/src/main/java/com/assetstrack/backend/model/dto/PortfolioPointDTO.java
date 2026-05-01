package com.assetstrack.backend.model.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PortfolioPointDTO(
    String label,
    @JsonProperty("date")
    String date,
    @JsonProperty("time")
    String time,
    @JsonProperty("timestamp")
    String timestamp,
    @JsonProperty("totalValue")
    BigDecimal totalValue,
    @JsonProperty("portfolioValue")
    BigDecimal portfolioValue,
    @JsonProperty("totalCost")
    BigDecimal totalCost,
    @JsonProperty("investedCapital")
    BigDecimal investedCapital,
    @JsonProperty("dayIntraChange")
    BigDecimal dayIntraChange,
    @JsonProperty("totalChangePercentage")
    BigDecimal totalChangePercentage
) {
    
    public PortfolioPointDTO(String label, BigDecimal totalValue, BigDecimal totalCost) {
        this(label, null, null, null, totalValue, totalValue, totalCost, totalCost, null, null);
    }
}