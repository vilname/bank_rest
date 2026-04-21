package com.example.bankcards.dto.admin;

import com.example.bankcards.util.enums.CardStatusEnum;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AdminCardResponse(
        UUID id,
        String maskedNumber,
        String owner,
        LocalDate expiryDate,
        CardStatusEnum status,
        Integer balance,
        UUID userId,
        Instant created,
        Instant updated
) {
}

