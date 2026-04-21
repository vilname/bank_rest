package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.admin.AdminUserRequest;
import com.example.bankcards.dto.admin.SetUserActiveRequest;
import com.example.bankcards.dto.admin.SetUserRolesRequest;
import com.example.bankcards.service.admin.user.AdminUserService;
import com.example.bankcards.util.dto.PaginationResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsersController {
    private final AdminUserService service;

    public AdminUsersController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<AdminUserRequest>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserRequest> get(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.get(userId));
    }

    @PatchMapping("/{userId}/active")
    public ResponseEntity<AdminUserRequest> setActive(@PathVariable UUID userId, @Valid @RequestBody SetUserActiveRequest req) {
        return ResponseEntity.ok(service.setActive(userId, req.active()));
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<AdminUserRequest> setRoles(@PathVariable UUID userId, @Valid @RequestBody SetUserRolesRequest req) {
        return ResponseEntity.ok(service.setRoles(userId, req.roles()));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        service.delete(userId);
        return ResponseEntity.noContent().build();
    }
}

