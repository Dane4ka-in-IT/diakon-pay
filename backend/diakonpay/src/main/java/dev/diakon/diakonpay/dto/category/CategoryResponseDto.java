package dev.diakon.diakonpay.dto.category;

import java.util.UUID;

public record CategoryResponseDto(
        UUID id,
        Integer userId,
        String nameCategory,
        String categoryDescription,
        String type
) {}