package com.example.bankcards.dto.api.card;

import com.example.bankcards.util.dto.PaginationRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CardListRequest {

    private String number;

    private PaginationRequest pagination;
}
