package com.example.bankcards.controller.api;

import com.example.bankcards.dto.api.card.*;
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
@Tag(name = "Card", description = "Карты пользователей")
public class CardController {

    private final CardService cardService;

    @GetMapping
    @Operation(summary = "Get user cards with search and pagination")
    public ResponseEntity<PaginationResponse<CardResponse>> getUserCards(
            Authentication authentication,
            @RequestParam(required = false) String number,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        PaginationRequest pagination = new PaginationRequest(page, limit);
        CardListRequest cardListRequest = new CardListRequest(number, pagination);
        PaginationResponse<CardResponse> response = cardService.getUserCards(
                (User) authentication.getPrincipal(), cardListRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cardId}/balance")
    @Operation(summary = "Get card balance")
    public ResponseEntity<BalanceResponse> getCardBalance(
            Authentication authentication,
            @PathVariable UUID cardId) {

        BalanceResponse response = cardService.getCardBalance((User)authentication.getPrincipal(), cardId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create a new card")
    public ResponseEntity<Void> createCard(
            Authentication authentication,
            @Valid @RequestBody CardRequest request) {

        cardService.createCard((User)authentication.getPrincipal(), request);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cardId}")
    @Operation(summary = "Update an existing card")
    public ResponseEntity<Void> updateCard(
            Authentication authentication,
            @PathVariable UUID cardId,
            @Valid @RequestBody CardRequest request) {

        cardService.updateCard((User)authentication.getPrincipal(), cardId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cardId}")
    @Operation(summary = "Delete a card")
    public ResponseEntity<Void> deleteCard(
            Authentication authentication,
            @PathVariable UUID cardId) {

        cardService.deleteCard((User)authentication.getPrincipal(), cardId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/block")
    @Operation(summary = "Request card blocking")
    public ResponseEntity<Void> blockCard(
            Authentication authentication,
            @Valid @RequestBody BlockCardRequest request) {

        cardService.blockCard((User)authentication.getPrincipal(), request.getCardId());

        return ResponseEntity.ok().build();
    }
}
