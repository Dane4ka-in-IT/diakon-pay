package dev.diakon.diakonpay.dto.user;

import dev.diakon.diakonpay.dto.account.AccountResponseDto;

import java.util.List;

public record UserProfileResponseDto(
        Integer id,
        String name,
        String email,
        String avatarUrl,
        List<AccountResponseDto> accounts
) {}