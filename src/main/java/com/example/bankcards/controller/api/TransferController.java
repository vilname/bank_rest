package com.example.bankcards.controller.api;

import com.example.bankcards.dto.api.transfer.TransferRequest;
import com.example.bankcards.dto.api.transfer.TransferResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.api.transfer.TransferService;
import com.example.bankcards.util.dto.PaginationRequest;
import com.example.bankcards.util.dto.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfer", description = "Перевод средств с карты на карту")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @Operation(summary = "Make a transfer between own cards")
    public ResponseEntity<TransferResponse> makeTransfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request) {

        TransferResponse response = transferService.makeTransfer((User)authentication.getPrincipal(), request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get user transfers with pagination")
    public ResponseEntity<PaginationResponse<TransferResponse>> getUserTransfers(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        PaginationRequest pagination = new PaginationRequest(page, limit);
        var response = transferService.getUserTransfers((User)authentication.getPrincipal(), pagination);

        return ResponseEntity.ok(response);
    }
}
