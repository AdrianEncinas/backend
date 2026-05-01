package com.assetstrack.backend.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record ChartDTO(
    String ticker,
    String period,
    List<HistoryDTO> history
){
    public record HistoryDTO(
        String time,
        BigDecimal price,
        Long volume
    ) {}
}
