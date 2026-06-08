package dev.diakon.diakonpay.dto.analytics;

import java.math.BigDecimal;

public record AnalyticsTotalBalanceResponseDto(
        BigDecimal totalBalance,
        String currencyCode
) {}
