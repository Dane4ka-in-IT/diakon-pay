package dev.diakon.diakonpay.controller;

import dev.diakon.diakonpay.dto.account.AccountRequestDto;
import dev.diakon.diakonpay.dto.account.AccountResponseDto;
import dev.diakon.diakonpay.service.AccountService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<@NonNull List<AccountResponseDto>> getAccounts(@RequestParam Integer userId) {
        return ResponseEntity.ok(accountService.getUserAccounts(userId));
    }

    @PostMapping
    public ResponseEntity<@NonNull Map<String, String>> createAccount(
            @RequestParam Integer userId,
            @RequestBody AccountRequestDto request) {
        accountService.createAccount(userId, request);
        return ResponseEntity.ok(Map.of("message", "Счет успешно создан"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<@NonNull Map<String, String>> updateAccount(
            @RequestParam Integer userId,
            @PathVariable UUID id,
            @RequestBody AccountRequestDto request) {
        accountService.updateAccount(userId, id, request);
        return ResponseEntity.ok(Map.of("message", "Данные счета успешно обновлены"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<@NonNull Map<String, String>> deleteAccount(
            @RequestParam Integer userId,
            @PathVariable UUID id) {
        accountService.deleteAccount(userId, id);
        return ResponseEntity.ok(Map.of("message", "Счет отправлен в архив, история транзакций сохранена"));
    }
}