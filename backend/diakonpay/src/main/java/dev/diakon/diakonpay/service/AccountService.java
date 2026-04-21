package dev.diakon.diakonpay.service;

import dev.diakon.diakonpay.dto.account.AccountRequestDto;
import dev.diakon.diakonpay.dto.account.AccountResponseDto;
import dev.diakon.diakonpay.entity.Account;
import dev.diakon.diakonpay.entity.User;
import dev.diakon.diakonpay.exception.AccessDeniedException;
import dev.diakon.diakonpay.exception.UserNotFound;
import dev.diakon.diakonpay.repository.AccountRepository;
import dev.diakon.diakonpay.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    private Account getAccountByAccountIdAndUserId(UUID accountId, int userId) {
        return accountRepository.findByIdAndUserIdAndIsDeletedFalse(accountId, userId)
                .orElseThrow(() -> new AccessDeniedException("Счет не найден или доступ запрещен"));
    }

    public List<AccountResponseDto> getUserAccounts(Integer userId) {
        return accountRepository.findAllByUserIdAndIsDeletedFalse(userId).stream()
                .map(acc -> new AccountResponseDto(
                        acc.getId(),
                        acc.getBankName(),
                        acc.getAccountNumber(),
                        acc.getBalance(),
                        acc.getCurrencyCode()
                    )
                ).toList();
    }

    @Transactional
    public void createAccount(Integer userId, AccountRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("Пользователь не найден"));

        Account account = new Account();
        account.setId(request.id() != null ? request.id() : UUID.randomUUID());
        account.setUser(user);
        account.setBankName(request.bankName());
        account.setAccountNumber(request.accountNumber());
        account.setBalance(request.balance());
        account.setCurrencyCode(request.currencyCode());
        account.setIsDeleted(false);

        accountRepository.save(account);
    }

    @Transactional
    public void updateAccount(Integer userId, UUID accountId, AccountRequestDto request) {
        Account account = getAccountByAccountIdAndUserId(accountId, userId);

        account.setBankName(request.bankName());
        account.setAccountNumber(request.accountNumber());
        account.setBalance(request.balance());
        account.setCurrencyCode(request.currencyCode());

        accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(Integer userId, UUID accountId) {
        Account account = getAccountByAccountIdAndUserId(accountId, userId);

        account.setIsDeleted(true);
        accountRepository.save(account);
    }
}