package com.example.bankcards.dto.api.card;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CardRequest {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String number;

    @NotNull(message = "Balance is required")
    @Min(value = 0, message = "Balance must be non-negative")
    private Integer balance;

    @NotBlank(message = "Owner name is required")
    private String owner;
}
