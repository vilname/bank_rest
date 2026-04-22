package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.admin.AdminCardResponse;
import com.example.bankcards.dto.admin.AdminCardBlockRequestResponse;
import com.example.bankcards.dto.admin.CreateCardRequest;
import com.example.bankcards.service.admin.card.AdminCardService;
import com.example.bankcards.util.dto.PaginationRequest;
import com.example.bankcards.util.dto.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardsController {
    private final AdminCardService service;

    public AdminCardsController(AdminCardService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Список карт")
    public ResponseEntity<PaginationResponse<AdminCardResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PaginationRequest pagination = new PaginationRequest(page, limit);

        return ResponseEntity.ok(service.list(pagination));
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Детальная карты")
    public ResponseEntity<AdminCardResponse> get(@PathVariable UUID cardId) {
        return ResponseEntity.ok(service.get(cardId));
    }

    @PostMapping
    @Operation(summary = "Создание карты")
    public ResponseEntity<Void> create(@Valid @RequestBody CreateCardRequest req) {
        service.create(req);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{cardId}/block")
    @Operation(summary = "Блокировка карты")
    public ResponseEntity<Void> block(@PathVariable UUID cardId) {
        service.block(cardId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{cardId}/activate")
    @Operation(summary = "Активация карты")
    public ResponseEntity<Void> activate(@PathVariable UUID cardId) {
        service.activate(cardId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cardId}")
    @Operation(summary = "Удаление карты")
    public ResponseEntity<Void> delete(@PathVariable UUID cardId) {
        service.delete(cardId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/block-requests")
    @Operation(summary = "Список запросов от пользователей на блокировку карты")
    public ResponseEntity<PaginationResponse<AdminCardBlockRequestResponse>> listPendingBlockRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PaginationRequest pagination = new PaginationRequest(page, limit);
        return ResponseEntity.ok(service.listPendingBlockRequests(pagination));
    }

    @PostMapping("/block-requests/{requestId}/approve")
    @Operation(summary = "Удовлетворение заявки по блокированию карты")
    public ResponseEntity<Void> approveBlockRequest(@PathVariable UUID requestId) {
        service.approveBlockRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/block-requests/{requestId}/reject")
    @Operation(summary = "Отказ заявки в блокирование карты")
    public ResponseEntity<Void> rejectBlockRequest(@PathVariable UUID requestId) {
        service.rejectBlockRequest(requestId);
        return ResponseEntity.ok().build();
    }
}

