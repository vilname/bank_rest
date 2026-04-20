package com.example.bankcards.dto.admin;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AdminUserDto(
        UUID id,
        String email,
        String firstName,
        String lastName,
        boolean active,
        Set<String> roles,
        Instant created,
        Instant updated
) {
}

