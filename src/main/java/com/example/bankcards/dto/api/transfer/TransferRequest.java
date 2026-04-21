package com.example.bankcards.dto.api.transfer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TransferRequest {

    @NotNull(message = "Source card ID is required")
    private UUID fromCardId;

    @NotNull(message = "Destination card ID is required")
    private UUID toCardId;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1")
    private Integer amount;
}
