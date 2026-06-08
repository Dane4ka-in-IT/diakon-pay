package dev.diakon.diakonpay.dto.user;

public record PasswordUpdateRequestDto(String email, String newPassword, int code) {}