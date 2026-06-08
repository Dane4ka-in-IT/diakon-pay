package dev.diakon.diakonpay.dto.analytics;

import java.math.BigDecimal;

public record AnalyticsByCategoryResponseDto(
        String categoryName,
        BigDecimal amount,
        BigDecimal percentage
) {}
