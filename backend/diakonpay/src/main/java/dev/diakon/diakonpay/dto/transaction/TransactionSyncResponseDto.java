package dev.diakon.diakonpay.dto.transaction;

import java.util.List;

public record TransactionSyncResponseDto(
        int processedCount,
        int failedCount,
        String syncStatus,
        List<TransactionSyncItemDto> updatedTransactions
) {}
