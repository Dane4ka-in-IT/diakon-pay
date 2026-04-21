package dev.diakon.diakonpay.dto.category;

import java.util.UUID;

public record CategoryRequestDto(
        UUID id,
        String nameCategory,
        String categoryDescription,
        String type
) {}