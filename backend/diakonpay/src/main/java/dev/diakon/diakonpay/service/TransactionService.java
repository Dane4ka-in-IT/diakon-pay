package dev.diakon.diakonpay.service;

import dev.diakon.diakonpay.dto.transaction.*;
import dev.diakon.diakonpay.entity.Account;
import dev.diakon.diakonpay.entity.Category;
import dev.diakon.diakonpay.entity.Transaction;
import dev.diakon.diakonpay.exception.AccessDeniedException;
import dev.diakon.diakonpay.exception.CategoryNotFoundException;
import dev.diakon.diakonpay.repository.AccountRepository;
import dev.diakon.diakonpay.repository.CategoryRepository;
import dev.diakon.diakonpay.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    private Transaction getTransactionByIdAndUserId(UUID txId, Integer userId) {
        return transactionRepository.findByIdAndAccountUserId(txId, userId)
                .orElseThrow(() -> new AccessDeniedException("Транзакция не найдена или доступ запрещен"));
    }

    private Account getAccountByIdAndUserId(UUID accountId, Integer userId) {
        return accountRepository.findByIdAndUserIdAndIsDeletedFalse(accountId, userId)
                .orElseThrow(() -> new AccessDeniedException("Счет не найден или доступ запрещен"));
    }

    private Category getCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));
    }

    private void applyRequestToTransaction(Transaction transaction, Account account,
                                           Category category, TransactionRequestDto request) {
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setAmount(request.amount().abs());
        transaction.setExchangeRate(request.exchangeRate());
        transaction.setTransactionsDescription(request.transactionDescription());
        transaction.setTransactionsDate(request.transactionDate() != null
                ? request.transactionDate() : OffsetDateTime.now());
    }

    private BigDecimal getAccountBalance(UUID accountId) {
        return accountRepository.findById(accountId)
                .map(Account::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    public List<TransactionResponseDto> getTransactions(Integer userId,
                                                        OffsetDateTime startDate,
                                                        OffsetDateTime endDate,
                                                        String type,
                                                        UUID categoryId) {
        return transactionRepository.findAllByAccountUserIdAndTransactionsDateBetween(userId, startDate, endDate).stream()
                .filter(tx -> type == null || tx.getCategory().getType().equals(type))
                .filter(tx -> categoryId == null || tx.getCategory().getId().equals(categoryId))
                .map(tx -> new TransactionResponseDto(
                        tx.getId(),
                        tx.getAccount().getUser().getId(),
                        tx.getAccount().getId(),
                        tx.getCategory().getId(),
                        tx.getCategory().getType().equals("EXPENSE")
                                ? tx.getAmount().negate()
                                : tx.getAmount(),
                        tx.getExchangeRate(),
                        tx.getTransactionsDescription(),
                        tx.getTransactionsDate()
                )).toList();
    }

    @Transactional
    public BigDecimal createTransaction(Integer userId, TransactionRequestDto request) {
        Account account = getAccountByIdAndUserId(request.accountId(), userId);
        Category category = getCategoryById(request.categoryId());

        Transaction transaction = new Transaction();
        transaction.setId(request.id() != null ? request.id() : UUID.randomUUID());
        applyRequestToTransaction(transaction, account, category, request);

        transactionRepository.save(transaction);
        transactionRepository.flush();

        return getAccountBalance(account.getId());
    }

    @Transactional
    public BigDecimal updateTransaction(Integer userId, UUID txId, TransactionRequestDto request) {
        Transaction transaction = getTransactionByIdAndUserId(txId, userId);
        Account account = getAccountByIdAndUserId(request.accountId(), userId);
        Category category = getCategoryById(request.categoryId());

        applyRequestToTransaction(transaction, account, category, request);
        transaction.setTransactionsDate(request.transactionDate() != null
                ? request.transactionDate() : transaction.getTransactionsDate());

        transactionRepository.save(transaction);
        transactionRepository.flush();

        return getAccountBalance(account.getId());
    }

    @Transactional
    public BigDecimal deleteTransaction(Integer userId, UUID txId) {
        Transaction transaction = getTransactionByIdAndUserId(txId, userId);
        UUID accountId = transaction.getAccount().getId();

        transactionRepository.delete(transaction);
        transactionRepository.flush();

        return getAccountBalance(accountId);
    }

    @Transactional
    public TransactionSyncResponseDto syncTransactions(Integer userId, List<TransactionRequestDto> requests) {
        List<TransactionSyncItemDto> results = new ArrayList<>();
        int processedCount = 0;
        int failedCount = 0;

        for (TransactionRequestDto request : requests) {
            try {
                Account account = getAccountByIdAndUserId(request.accountId(), userId);
                Category category = getCategoryById(request.categoryId());

                Transaction transaction = new Transaction();
                transaction.setId(request.id() != null ? request.id() : UUID.randomUUID());
                applyRequestToTransaction(transaction, account, category, request);

                transactionRepository.save(transaction);
                results.add(new TransactionSyncItemDto(transaction.getId(), "SYNCED"));
                processedCount++;
            } catch (Exception e) {
                results.add(new TransactionSyncItemDto(request.id(), "FAILED"));
                failedCount++;
            }
        }

        String syncStatus = failedCount == 0 ? "SUCCESS" : (processedCount == 0 ? "FAILED" : "PARTIAL");
        return new TransactionSyncResponseDto(processedCount, failedCount, syncStatus, results);
    }
}
