package dev.diakon.diakonpay.controller;

import dev.diakon.diakonpay.dto.transaction.TransactionRequestDto;
import dev.diakon.diakonpay.dto.transaction.TransactionResponseDto;
import dev.diakon.diakonpay.dto.transaction.TransactionSyncResponseDto;
import dev.diakon.diakonpay.service.TransactionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<@NonNull List<TransactionResponseDto>> getTransactions(
            @AuthenticationPrincipal Integer userId,
            @RequestParam OffsetDateTime startDate,
            @RequestParam OffsetDateTime endDate,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) UUID category) {
        return ResponseEntity.ok(transactionService.getTransactions(userId, startDate, endDate, type, category));
    }

    @PostMapping
    public ResponseEntity<@NonNull Map<String, Object>> createTransaction(
            @AuthenticationPrincipal Integer userId,
            @RequestBody TransactionRequestDto request) {
        BigDecimal newBalance = transactionService.createTransaction(userId, request);
        return ResponseEntity.ok(Map.of(
                "id", request.id() != null ? request.id() : "",
                "message", "Транзакция успешно создана",
                "newTotalBalance", newBalance
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<@NonNull Map<String, Object>> updateTransaction(
            @AuthenticationPrincipal Integer userId,
            @PathVariable UUID id,
            @RequestBody TransactionRequestDto request) {
        BigDecimal newBalance = transactionService.updateTransaction(userId, id, request);
        return ResponseEntity.ok(Map.of(
                "message", "Транзакция успешно обновлена",
                "newTotalBalance", newBalance
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<@NonNull Map<String, Object>> deleteTransaction(
            @AuthenticationPrincipal Integer userId,
            @PathVariable UUID id) {
        BigDecimal newBalance = transactionService.deleteTransaction(userId, id);
        return ResponseEntity.ok(Map.of(
                "id", id,
                "message", "Транзакция успешно удалена",
                "newTotalBalance", newBalance
        ));
    }

    @PostMapping("/sync")
    public ResponseEntity<@NonNull TransactionSyncResponseDto> syncTransactions(
            @AuthenticationPrincipal Integer userId,
            @RequestBody List<TransactionRequestDto> requests) {
        return ResponseEntity.ok(transactionService.syncTransactions(userId, requests));
    }
}