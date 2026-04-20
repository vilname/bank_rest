package com.example.bankcards.dto.admin;

import jakarta.validation.constraints.NotNull;

public record SetUserActiveRequest(
        @NotNull Boolean active
) {
}

