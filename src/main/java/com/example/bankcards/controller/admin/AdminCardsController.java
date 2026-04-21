package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.admin.AdminCardResponse;
import com.example.bankcards.dto.admin.CreateCardRequest;
import com.example.bankcards.service.admin.card.AdminCardService;
import com.example.bankcards.util.dto.PaginationRequest;
import com.example.bankcards.util.dto.PaginationResponse;
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
    public ResponseEntity<PaginationResponse<AdminCardResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PaginationRequest pagination = new PaginationRequest(page, limit);

        return ResponseEntity.ok(service.list(pagination));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<AdminCardResponse> get(@PathVariable UUID cardId) {
        return ResponseEntity.ok(service.get(cardId));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateCardRequest req) {
        service.create(req);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{cardId}/block")
    public ResponseEntity<Void> block(@PathVariable UUID cardId) {
        service.block(cardId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{cardId}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID cardId) {
        service.activate(cardId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> delete(@PathVariable UUID cardId) {
        service.delete(cardId);
        return ResponseEntity.ok().build();
    }
}

