package dev.diakon.diakonpay.repository;

import dev.diakon.diakonpay.entity.Transaction;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<@NonNull Transaction, @NonNull UUID> {
    List<Transaction> findAllByAccountUserIdAndTransactionsDateBetween(Integer userId, OffsetDateTime start, OffsetDateTime end);
    Optional<Transaction> findByIdAndAccountUserId(UUID id, Integer userId);

    @Query("SELECT t.category.nameCategory, SUM(t.amount) FROM Transaction t " +
           "WHERE t.account.user.id = :userId " +
           "AND t.transactionsDate BETWEEN :start AND :end " +
           "GROUP BY t.category.nameCategory")
    List<Object[]> sumAmountGroupedByCategory(@Param("userId") Integer userId,
                                              @Param("start") OffsetDateTime start,
                                              @Param("end") OffsetDateTime end);
}
