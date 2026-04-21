package com.example.bankcards.service.admin.user;

import com.example.bankcards.dto.admin.AdminUserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.dto.PaginationRequest;
import com.example.bankcards.util.dto.PaginationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminUserService(UserRepository users, RoleRepository roles) {
        this.userRepository = users;
        this.roleRepository = roles;
    }

    public PaginationResponse<AdminUserResponse> list(PaginationRequest pagination) {
        List<User> users = userRepository.findAllWithPaginationAndRoles(pagination.getOffset(), pagination.getLimit());
        List<AdminUserResponse> userDto = users.stream().map(AdminUserService::toDto).toList();

        int total = (int)userRepository.count();

        return new PaginationResponse<>(userDto, pagination, total);
    }

    public AdminUserResponse get(UUID userId) {
        return userRepository.findById(userId).map(AdminUserService::toDto)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public void setActive(UUID userId, boolean active) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        user.setActive(active);

        userRepository.save(user);
    }

    @Transactional
    public void setRoles(UUID userId, Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new BadRequestException("Roles must not be empty");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        List<Role> newRoles = roleNames.stream()
                .map(String::trim)
                .map(name -> name.startsWith("ROLE_") ? name : "ROLE_" + name)
                .map(name -> roleRepository.findByName(name).orElseThrow(() -> new BadRequestException("Unknown role: " + name)))
                .toList();

        user.setRoles(newRoles);

        userRepository.save(user);
    }

    @Transactional
    public void delete(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(userId);
    }

    private static AdminUserResponse toDto(User u) {
        Set<String> roles = u.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        return new AdminUserResponse(
                u.getId(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.isActive(),
                roles,
                u.getCreated(),
                u.getUpdated()
        );
    }
}

