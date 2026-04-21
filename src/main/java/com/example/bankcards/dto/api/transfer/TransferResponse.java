package com.example.bankcards.dto.api.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private UUID id;
    private Integer balance;
    private Instant created;
    private CardResponse fromCard;
    private String fromCardNumber;
    private CardResponse toCard;
    private String toCardNumber;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardResponse {
        private UUID id;
        private Integer balance;
        private String number;
    }
}
