package com.example.bankcards.dto.api.card;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BlockCardRequest {
    @NotNull(message = "Card ID is required")
    private UUID cardId;
}
