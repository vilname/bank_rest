package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.admin.AdminCardRequest;
import com.example.bankcards.dto.admin.CreateCardRequest;
import com.example.bankcards.service.admin.card.AdminCardService;
import com.example.bankcards.util.dto.PaginationResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardsController {
    private final AdminCardService service;

    public AdminCardsController(AdminCardService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<AdminCardRequest>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<AdminCardRequest> get(@PathVariable UUID cardId) {
        return ResponseEntity.ok(service.get(cardId));
    }

    @PostMapping
    public ResponseEntity<AdminCardRequest> create(@Valid @RequestBody CreateCardRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PostMapping("/{cardId}/block")
    public ResponseEntity<AdminCardRequest> block(@PathVariable UUID cardId) {
        return ResponseEntity.ok(service.block(cardId));
    }

    @PostMapping("/{cardId}/activate")
    public ResponseEntity<AdminCardRequest> activate(@PathVariable UUID cardId) {
        return ResponseEntity.ok(service.activate(cardId));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> delete(@PathVariable UUID cardId) {
        service.delete(cardId);
        return ResponseEntity.noContent().build();
    }
}

