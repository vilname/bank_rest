package com.example.bankcards.dto.admin;

import com.example.bankcards.util.enums.CardBlockRequestStatusEnum;

import java.time.Instant;
import java.util.UUID;

public record AdminCardBlockRequestResponse(
        UUID requestId,
        UUID cardId,
        String maskedCardNumber,
        UUID userId,
        CardBlockRequestStatusEnum status,
        Instant created
) {
}
