package com.example.bankcards.dto.api.card;

import com.example.bankcards.util.enums.CardStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {

    private UUID id;
    private String number;
    private Integer balance;
    private String owner;
    private LocalDate expiryDate;
    private CardStatusEnum status;
}
