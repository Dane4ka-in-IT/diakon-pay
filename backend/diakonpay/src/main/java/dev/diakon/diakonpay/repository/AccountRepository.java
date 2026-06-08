package dev.diakon.diakonpay.repository;

import dev.diakon.diakonpay.entity.Account;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<@NonNull Account, @NonNull UUID> {
    List<Account> findAllByUserIdAndIsDeletedFalse(Integer userId);
    Optional<Account> findByIdAndUserIdAndIsDeletedFalse(UUID id, Integer userId);
}