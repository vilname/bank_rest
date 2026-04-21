package com.example.bankcards.service.admin.user;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void setRolesThrowsWhenEmptyRoles() {
        UUID userId = UUID.randomUUID();

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> adminUserService.setRoles(userId, Set.of())
        );

        assertEquals("Roles must not be empty", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void setRolesAddsRolePrefixAndSavesUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        adminUserService.setRoles(userId, Set.of("ADMIN"));

        assertEquals(1, user.getRoles().size());
        assertEquals("ROLE_ADMIN", user.getRoles().get(0).getName());
        verify(userRepository).save(user);
    }

    @Test
    void deleteThrowsWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> adminUserService.delete(userId));

        assertEquals("User not found", ex.getMessage());
        verify(userRepository, never()).deleteById(userId);
    }
}
