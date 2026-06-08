package dev.diakon.diakonpay.controller;

import dev.diakon.diakonpay.dto.auth.AuthResponseDto;
import dev.diakon.diakonpay.dto.auth.RefreshRequestDto;
import dev.diakon.diakonpay.dto.user.UserLoginRequestDto;
import dev.diakon.diakonpay.dto.user.PasswordRecoveryRequestDto;
import dev.diakon.diakonpay.dto.user.PasswordUpdateRequestDto;
import dev.diakon.diakonpay.entity.User;
import dev.diakon.diakonpay.repository.TarantoolTokenRepository;
import dev.diakon.diakonpay.security.JwtService;
import dev.diakon.diakonpay.service.UserService;
import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final TarantoolTokenRepository tokenRepository;

    @PostMapping("/login")
    public ResponseEntity<@NonNull AuthResponseDto> login(@RequestBody UserLoginRequestDto request) {
        User user = userService.loginUser(request.email(), request.password());
        return ResponseEntity.ok(issueTokens(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<@NonNull AuthResponseDto> refresh(@RequestBody RefreshRequestDto request) {
        String refreshToken = request.refreshToken();
        try {
            if (jwtService.isTokenExpired(refreshToken)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token истёк");
            }
            String jti = jwtService.extractJti(refreshToken);
            String type = jwtService.extractType(refreshToken);

            if (!"refresh".equals(type) || !tokenRepository.isValid(jti)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token недействителен");
            }

            Integer userId = jwtService.extractUserId(refreshToken);
            tokenRepository.revokeToken(jti);

            User user = userService.getUserById(userId);
            return ResponseEntity.ok(issueTokens(user));

        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Невалидный токен");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<@NonNull Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Не авторизован");
        }

        String token = authHeader.substring(7);
        String jti = jwtService.extractJti(token);
        Integer userId = jwtService.extractUserId(token);

        tokenRepository.revokeToken(jti);
        tokenRepository.revokeAllUserTokens(userId, "refresh");

        return ResponseEntity.ok(Map.of("message", "Выход выполнен успешно"));
    }

    @PostMapping("/password-recovery/request")
    public ResponseEntity<@NonNull Integer> requestPasswordRecovery(@RequestBody PasswordRecoveryRequestDto request) {
        return ResponseEntity.ok(userService.requestPasswordRecovery(request.email()));
    }

    @PutMapping("/password-recovery/confirm")
    public ResponseEntity<@NonNull Map<String, String>> confirmPasswordRecovery(@RequestBody PasswordUpdateRequestDto request) {
        userService.updatePassword(request.email(), request.newPassword(), request.code());
        return ResponseEntity.ok(Map.of("message", "Пароль успешно изменен"));
    }

    private AuthResponseDto issueTokens(User user) {
        String accessToken  = jwtService.generateAccessToken(user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        tokenRepository.saveToken(
                jwtService.extractJti(accessToken),
                user.getId(), "access",
                jwtService.extractExpirationEpochSeconds(accessToken)
        );
        tokenRepository.saveToken(
                jwtService.extractJti(refreshToken),
                user.getId(), "refresh",
                jwtService.extractExpirationEpochSeconds(refreshToken)
        );

        return new AuthResponseDto(
                user.getId(),
                user.getName(),
                user.getUserEmail(),
                user.getAvatarUrl(),
                accessToken,
                refreshToken
        );
    }
}