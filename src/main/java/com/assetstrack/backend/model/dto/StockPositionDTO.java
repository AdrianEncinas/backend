package com.assetstrack.backend.model.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockPositionDTO(

    @NotBlank(message = "Ticker is required")
    @JsonAlias("symbol")
    String ticker,

    @NotBlank(message = "Company name is required")
    @JsonAlias("name")
    String companyName,

    @NotNull(message = "Shares are required")
    @DecimalMin(value = "0.0001", message = "Shares must be greater than zero")
    BigDecimal shares,

    @NotNull(message = "Average price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Average price must be greater than zero")
    @JsonProperty("avg_price")
    @JsonAlias("avgPrice")
    BigDecimal avgPrice,

    Long userid
) {}