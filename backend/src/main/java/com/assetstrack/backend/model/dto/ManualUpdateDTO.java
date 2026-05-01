package com.assetstrack.backend.model.dto;

import java.math.BigDecimal;

public record ManualUpdateDTO(
    BigDecimal totalShares,
    BigDecimal avgPrice
) {}
