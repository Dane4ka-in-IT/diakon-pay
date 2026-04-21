package dev.diakon.diakonpay.service;

import dev.diakon.diakonpay.entity.User;
import dev.diakon.diakonpay.exception.DuplicateEmailException;
import dev.diakon.diakonpay.exception.InvalidPassword;
import dev.diakon.diakonpay.exception.UserNotFound;
import dev.diakon.diakonpay.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SMTPService smtpService;


    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("Пользователь не найден"));
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
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UserNotFound("Юзер отсутствует в базе данных."));

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
    public void updateEmail(Integer userId, String newEmail) {
        User user = getUserById(userId);

        user.setUserEmail(newEmail);
        userRepository.save(user);
    }

    public int requestPasswordRecovery(String email) {
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UserNotFound("Пользователь не найден."));

        return smtpService.sendMessageAboutRecoveryPass(user.getName(), user.getUserEmail());
    }

    @Transactional
    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UserNotFound("Пользователь не найден."));

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