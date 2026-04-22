package com.example.bankcards.controller.api;

import com.example.bankcards.dto.api.card.BalanceResponse;
import com.example.bankcards.dto.api.card.BlockCardRequest;
import com.example.bankcards.dto.api.card.CardListRequest;
import com.example.bankcards.dto.api.card.CardResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.api.card.CardService;
import com.example.bankcards.util.dto.PaginationRequest;
import com.example.bankcards.util.dto.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Card", description = "Просмотр карт, баланс и заявка на блокировку (роль USER)")
public class CardController {

    private final CardService cardService;

    @GetMapping
    @Operation(summary = "Список карт авторизованного пользователя")
    public ResponseEntity<PaginationResponse<CardResponse>> getUserCards(
            Authentication authentication,
            @RequestParam(required = false) String number,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        PaginationRequest pagination = new PaginationRequest(page, limit);
        CardListRequest cardListRequest = new CardListRequest(number, pagination);
        PaginationResponse<CardResponse> response
                = cardService.getUserCards((User) authentication.getPrincipal(), cardListRequest);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cardId}/balance")
    @Operation(summary = "Получить баланс по карте")
    public ResponseEntity<BalanceResponse> getCardBalance(
            Authentication authentication,
            @PathVariable UUID cardId) {

        BalanceResponse response = cardService.getCardBalance((User) authentication.getPrincipal(), cardId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/block")
    @Operation(summary = "Создать заявку на блокировку карты")
    public ResponseEntity<Void> blockCard(
            Authentication authentication,
            @Valid @RequestBody BlockCardRequest request) {

        cardService.blockCard((User) authentication.getPrincipal(), request.getCardId());

        return ResponseEntity.ok().build();
    }
}
