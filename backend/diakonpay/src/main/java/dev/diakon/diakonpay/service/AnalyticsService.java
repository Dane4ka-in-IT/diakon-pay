package dev.diakon.diakonpay.service;

import dev.diakon.diakonpay.dto.analytics.AnalyticsByCategoryResponseDto;
import dev.diakon.diakonpay.dto.analytics.AnalyticsTotalBalanceResponseDto;
import dev.diakon.diakonpay.dto.analytics.AnalyticsTotalsResponseDto;
import dev.diakon.diakonpay.entity.Account;
import dev.diakon.diakonpay.entity.Transaction;
import dev.diakon.diakonpay.repository.AccountRepository;
import dev.diakon.diakonpay.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    private OffsetDateTime periodStart(String period) {
        return YearMonth.parse(period).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    private OffsetDateTime periodEnd(String period) {
        return YearMonth.parse(period).atEndOfMonth().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
    }

    private List<Transaction> getTransactionsByPeriod(Integer userId, String period) {
        return transactionRepository
                .findAllByAccountUserIdAndTransactionsDateBetween(userId, periodStart(period), periodEnd(period));
    }

    public AnalyticsTotalsResponseDto getTotals(Integer userId, String period) {
        List<Transaction> transactions = getTransactionsByPeriod(userId, period);

        BigDecimal totalIncome = transactions.stream()
                .filter(tx -> tx.getCategory().getType().equals("INCOME"))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(tx -> tx.getCategory().getType().equals("EXPENSE"))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        return new AnalyticsTotalsResponseDto(period, totalIncome, totalExpense, netSavings, "RUB");
    }

    public List<AnalyticsByCategoryResponseDto> getByCategory(Integer userId, String period) {
        List<Object[]> grouped = transactionRepository
                .sumAmountGroupedByCategory(userId, periodStart(period), periodEnd(period));

        BigDecimal totalAmount = grouped.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return grouped.stream()
                .map(row -> {
                    String categoryName = (String) row[0];
                    BigDecimal amount = (BigDecimal) row[1];
                    BigDecimal percentage = totalAmount.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : amount.multiply(BigDecimal.valueOf(100))
                                .divide(totalAmount, 1, RoundingMode.HALF_UP);
                    return new AnalyticsByCategoryResponseDto(categoryName, amount, percentage);
                }).toList();
    }

    public AnalyticsTotalBalanceResponseDto getTotalBalance(Integer userId) {
        BigDecimal totalBalance = accountRepository.findAllByUserIdAndIsDeletedFalse(userId).stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AnalyticsTotalBalanceResponseDto(totalBalance, "RUB");
    }
}
