package dev.diakon.diakonpay.dto.account;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponseDto(
        UUID id,
        String bankName,
        String accountNumber,
        BigDecimal balance,
        String currencyCode
) {}