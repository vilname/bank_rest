package com.example.bankcards.dto.admin;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record SetUserRolesRequest(
        @NotEmpty Set<String> roles
) {
}

