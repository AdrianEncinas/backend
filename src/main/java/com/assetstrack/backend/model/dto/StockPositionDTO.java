package com.assetstrack.backend.model.dto;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StockPositionDTO(
    String ticker,
    String companyName,
    BigDecimal shares,
    
    @JsonProperty("avg_price") 
    BigDecimal avgPrice,
    
    Long userid
) {}