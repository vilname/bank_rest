package com.example.bankcards.controller.api;

import com.example.bankcards.controller.TestSecurityConfig;
import com.example.bankcards.dto.api.transfer.TransferResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.api.transfer.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransferController.class)
@Import(TestSecurityConfig.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @Test
    void makeTransferReturnsUnauthorizedWhenNoAuthentication() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromCardId":"00000000-0000-0000-0000-000000000001",
                                 "toCardId":"00000000-0000-0000-0000-000000000002",
                                 "amount":100}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void makeTransferReturnsBadRequestWhenAmountInvalid() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .with(authentication(authenticatedUserToken("user@mail.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromCardId":"00000000-0000-0000-0000-000000000001",
                                 "toCardId":"00000000-0000-0000-0000-000000000002"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void makeTransferReturnsResponseForValidRequest() throws Exception {
        TransferResponse response = TransferResponse.builder()
                .id(UUID.randomUUID())
                .balance(300)
                .created(Instant.now())
                .fromCardNumber("**** **** **** 1111")
                .toCardNumber("**** **** **** 2222")
                .build();
        when(transferService.makeTransfer(any(User.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/transfers")
                        .with(authentication(authenticatedUserToken("user@mail.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromCardId":"00000000-0000-0000-0000-000000000001",
                                 "toCardId":"00000000-0000-0000-0000-000000000002",
                                 "amount":300}
                                """))
                .andExpect(status().isOk());
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
