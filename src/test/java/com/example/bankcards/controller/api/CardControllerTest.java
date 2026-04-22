package com.example.bankcards.controller.api;

import com.example.bankcards.controller.TestSecurityConfig;
import com.example.bankcards.dto.api.card.CardResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.api.card.CardService;
import com.example.bankcards.util.dto.PaginationRequest;
import com.example.bankcards.util.dto.PaginationResponse;
import com.example.bankcards.util.enums.CardStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CardController.class)
@Import(TestSecurityConfig.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Test
    void getUserCardsReturnsUnauthorizedWhenNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserCardsReturnsPageForAuthenticatedUser() throws Exception {
        CardResponse card = CardResponse.builder()
                .id(UUID.randomUUID())
                .number("**** **** **** 1234")
                .balance(1000)
                .owner("Ivan Ivanov")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(CardStatusEnum.ACTIVE)
                .build();
        PaginationResponse<CardResponse> response =
                new PaginationResponse<>(List.of(card), new PaginationRequest(1, 10), 1);
        when(cardService.getUserCards(any(User.class), any())).thenReturn(response);

        mockMvc.perform(get("/api/cards")
                        .with(authentication(authenticatedUserToken("user@mail.com")))
                        .param("page", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void blockCardReturnsBadRequestWhenCardIdMissing() throws Exception {
        mockMvc.perform(post("/api/cards/block")
                        .with(authentication(authenticatedUserToken("user@mail.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private UsernamePasswordAuthenticationToken authenticatedUserToken(String email) {
        User principal = new User();
        principal.setEmail(email);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
