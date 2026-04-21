package com.example.bankcards.dto.api.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

    private UUID cardId;
    private String cardNumber;
    private Integer balance;
    private String status;
}
