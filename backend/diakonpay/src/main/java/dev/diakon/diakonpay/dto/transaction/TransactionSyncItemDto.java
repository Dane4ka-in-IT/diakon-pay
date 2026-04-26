package dev.diakon.diakonpay.dto.transaction;

import java.util.UUID;

public record TransactionSyncItemDto(UUID id, String status) {}
