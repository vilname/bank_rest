package com.example.bankcards.controller.admin;

import com.example.bankcards.controller.TestSecurityConfig;
import com.example.bankcards.dto.admin.AdminCardResponse;
import com.example.bankcards.service.admin.card.AdminCardService;
import com.example.bankcards.util.dto.PaginationRequest;
import com.example.bankcards.util.dto.PaginationResponse;
import com.example.bankcards.util.enums.CardStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCardsController.class)
@Import(TestSecurityConfig.class)
class AdminCardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminCardService adminCardService;

    @Test
    void adminCardsReturnsUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/admin/cards"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminCardsReturnsForbiddenForUserRole() throws Exception {
        mockMvc.perform(get("/admin/cards"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCardsReturnsPageForAdminRole() throws Exception {
        AdminCardResponse card = new AdminCardResponse(
                UUID.randomUUID(),
                "**** **** **** 9999",
                "Admin Owner",
                LocalDate.now().plusYears(3),
                CardStatusEnum.ACTIVE,
                5000,
                UUID.randomUUID(),
                Instant.now(),
                Instant.now()
        );
        PaginationResponse<AdminCardResponse> response =
                new PaginationResponse<>(List.of(card), new PaginationRequest(1, 10), 1);
        when(adminCardService.list(any(PaginationRequest.class))).thenReturn(response);

        mockMvc.perform(get("/admin/cards").param("page", "1").param("limit", "10"))
                .andExpect(status().isOk());
    }
}
