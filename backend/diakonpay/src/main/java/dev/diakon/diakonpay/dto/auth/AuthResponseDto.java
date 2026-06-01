package dev.diakon.diakonpay.dto.auth;

public record AuthResponseDto(
        Integer userId,
        String name,
        String email,
        String avatarUrl,
        String accessToken,
        String refreshToken
) {}
