package dev.diakon.diakonpay.dto.analytics;

import java.math.BigDecimal;

public record AnalyticsTotalsResponseDto(
        String period,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netSavings,
        String currency
) {}
