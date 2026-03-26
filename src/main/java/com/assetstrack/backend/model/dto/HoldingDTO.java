package com.assetstrack.backend.model.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldingDTO {

    private Long id;
    private Long userid;
    private String currency;
    private String ticker;
    private String companyName;

    @JsonProperty("logo_url")
    private String logoUrl;

    @JsonProperty("shares")
    private BigDecimal totalShares;
    
    @JsonProperty("avg_price")
    private BigDecimal avgPrice;
}