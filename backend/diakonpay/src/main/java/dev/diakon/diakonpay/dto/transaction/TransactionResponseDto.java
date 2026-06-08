package dev.diakon.diakonpay.dto.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionResponseDto(
        UUID id,
        Integer userId,
        UUID accountId,
        UUID categoryId,
        BigDecimal amount,
        BigDecimal exchangeRate,
        String transactionDescription,
        OffsetDateTime transactionDate
) {}
