package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.admin.AdminUserResponse;
import com.example.bankcards.dto.admin.SetUserActiveRequest;
import com.example.bankcards.dto.admin.SetUserRolesRequest;
import com.example.bankcards.service.admin.user.AdminUserService;
import com.example.bankcards.util.dto.PaginationRequest;
import com.example.bankcards.util.dto.PaginationResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsersController {
    private final AdminUserService service;

    public AdminUsersController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<AdminUserResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PaginationRequest pagination = new PaginationRequest(page, limit);

        return ResponseEntity.ok(service.list(pagination));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponse> get(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.get(userId));
    }

    @PatchMapping("/{userId}/active")
    public ResponseEntity<Void> setActive(@PathVariable UUID userId, @Valid @RequestBody SetUserActiveRequest req) {
        service.setActive(userId, req.active());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<Void> setRoles(@PathVariable UUID userId, @Valid @RequestBody SetUserRolesRequest req) {
        service.setRoles(userId, req.roles());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        service.delete(userId);
        return ResponseEntity.noContent().build();
    }
}

