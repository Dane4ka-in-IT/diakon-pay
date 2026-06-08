package dev.diakon.diakonpay.controller;

import dev.diakon.diakonpay.dto.account.AccountResponseDto;
import dev.diakon.diakonpay.dto.user.*;
import dev.diakon.diakonpay.entity.User;
import dev.diakon.diakonpay.service.AccountService;
import dev.diakon.diakonpay.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<@NonNull Map<String, String>> register(@RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.ok(Map.of("message", "Пользователь успешно зарегистрирован"));
    }

    @GetMapping("/me")
    public ResponseEntity<@NonNull UserProfileResponseDto> getMe(@AuthenticationPrincipal Integer userId) {
        User user = userService.getUserById(userId);
        List<AccountResponseDto> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(new UserProfileResponseDto(
                user.getId(),
                user.getName(),
                user.getUserEmail(),
                user.getAvatarUrl(),
                accounts
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<@NonNull Map<String, Object>> updateProfile(
            @AuthenticationPrincipal Integer userId,
            @RequestBody UserUpdateRequestDto request) {
        User user = userService.updateProfile(userId, request.name(), request.avatarUrl());
        return ResponseEntity.ok(Map.of(
                "message", "Профиль успешно обновлен",
                "user", new UserResponseDto(user.getId(), user.getName(), user.getUserEmail(), user.getAvatarUrl())
        ));
    }

    @PostMapping("/email-change/request")
    public ResponseEntity<@NonNull Integer> requestEmailChange(
            @AuthenticationPrincipal Integer userId,
            @RequestBody EmailChangeRequestDto request) {
        return ResponseEntity.ok(userService.requestEmailChange(userId, request.newEmail()));
    }

    @PutMapping("/email-change/confirm")
    public ResponseEntity<@NonNull Map<String, String>> confirmEmailChange(
            @AuthenticationPrincipal Integer userId,
            @RequestBody EmailChangeRequestDto request) {
        userService.updateEmail(userId, request.newEmail(), request.code());

        return ResponseEntity.ok(Map.of(
                "message", "Email успешно изменен",
                "newEmail", request.newEmail()
        ));
    }
}