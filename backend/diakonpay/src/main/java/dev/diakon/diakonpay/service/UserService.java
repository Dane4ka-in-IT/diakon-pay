package dev.diakon.diakonpay.service;

import dev.diakon.diakonpay.entity.User;
import dev.diakon.diakonpay.exception.DuplicateEmailException;
import dev.diakon.diakonpay.exception.InvalidPassword;
import dev.diakon.diakonpay.exception.UserNotFound;
import dev.diakon.diakonpay.repository.TarantoolTokenRepository;
import dev.diakon.diakonpay.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SMTPService smtpService;
    private final TarantoolTokenRepository tokenRepository;

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("Пользователь не найден"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UserNotFound("Пользователь не найден."));
    }

    @Transactional
    public void registerUser(User user) {
        userRepository.findByUserEmail(user.getUserEmail())
                .ifPresentOrElse(
                        i -> { throw new DuplicateEmailException("Этот email уже используется"); },
                        () -> {
                            String pass = passwordEncoder.encode(user.getUserPassword());
                            user.setUserPassword(pass);
                            userRepository.save(user);
                            log.info("Юзер успешно добавлен!");
                            smtpService.sendMessageAboutRegistry(user.getName(), user.getUserEmail());
                        });
    }

    public User loginUser(String email, String password) {
        User user = getUserByEmail(email);

        if (!passwordEncoder.matches(password, user.getUserPassword())) {
            throw new InvalidPassword("Невалидный логин или пароль");
        }
        return user;
    }

    public int requestEmailChange(Integer userId, String newEmail) {
        User user = getUserById(userId);

        userRepository.findByUserEmail(newEmail).ifPresent(u -> {
            throw new DuplicateEmailException("Этот email уже используется");
        });

        return smtpService.sendMessageAboutChangedEmail(user.getName(), user.getUserEmail(), newEmail);
    }

    @Transactional
    public void updateEmail(Integer userId, String newEmail, int code) {
        User user = getUserById(userId);

        boolean isOtpValid = tokenRepository.verifyOtp(newEmail, code, "email_change");
        if (!isOtpValid) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Неверный или просроченный код подтверждения"
            );
        }

        user.setUserEmail(newEmail);
        userRepository.save(user);
    }

    public int requestPasswordRecovery(String email) {
        User user = getUserByEmail(email);

        return smtpService.sendMessageAboutRecoveryPass(user.getName(), user.getUserEmail());
    }

    @Transactional
    public void updatePassword(String email, String newPassword, int code) {
        User user = getUserByEmail(email);

        boolean isOtpValid = tokenRepository.verifyOtp(email, code, "password_recovery");
        if (!isOtpValid) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Неверный или просроченный код подтверждения"
            );
        }

        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public User updateProfile(Integer userId, String name, String avatarUrl) {
        User user = getUserById(userId);
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            user.setAvatarUrl(avatarUrl);
        }
        return userRepository.save(user);
    }
}