package com.example.bankcards.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateCardRequest(
        @NotNull UUID userId,
        @NotBlank String number,
        @NotBlank String owner,
        @NotNull LocalDate expiryDate,
        @NotNull @Min(0) Integer balance
) {
}

